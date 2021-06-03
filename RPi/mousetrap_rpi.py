import os
from picamera import PiCamera
from pyrebase import pyrebase

import serial
import threading

import time
from datetime import datetime

TRAPID = '-Madg32Ss6ZrU3MQI34l'
#TRAPID = '-MbDEvGfJY_Wjv8Ovyij'

#take a picture using the connected rpi's serial camera
#the picture will be saved locally with a timestamp as the filename in .jpeg format
#returns filename
def take_picture():
  camera = PiCamera()
  camera.resolution = (480,480)
  camera.rotation = 180
  camera.image_effect='none'
  camera.exposure_mode = 'auto'
  camera.led = True

  title = str(time.time()).split('.')[0]
  camera.capture(str(os.getcwd()) + '/{0}.jpeg'.format(title), format='jpeg')
  camera.close()

  return title

#stream handler callback to deal with changes made in the firebase's realtime database
#this will take new pictures at the request of the android user and communicate with the arduino to control the trap's door
def stream_handler(message):
  print(message["path"])
  print(message["data"])

  if(message["path"] == '/statusImage' and message["data"] == 11111):                         #user requested a new picture of the trap
    print("Take Pic")

    title = take_picture()
    storage.child('{}.jpeg'.format(title)).put('{}.jpeg'.format(title))
    data = {'date':str(datetime.today().strftime('%Y-%m-%d')), 'statusImage':int(title)}
    db.child("cliente1").child("ratoeiras").child(TRAPID).update(data)

  if(message["path"] == '/status'):                                                           #user changed the trap status, making it open or close
    print('message for arduino: {}'.format(message["data"]))
    message_arduino(message["data"])

#arduino listener that will await a for messages from the arduino -> rpi when something has been captured inside the trap
#this function will then update the trap's status on the firebase for the user to see through he's android device
def arduino_listener():
  print("waiting for message from arduino")
  while True:
    if ser.in_waiting > 0:
      line = str(ser.readline().decode("utf-8")).strip()
      ser.flush()
      print("arduino>> {}".format(line))

      if line == "detected":
        print("detected event")
        #take picture, send event to firebase
        title = take_picture()
        data = {'date':str(datetime.today().strftime('%Y-%m-%d')),'status':'detected', 'statusImage':int(title)}
        db.child("cliente1").child("ratoeiras").child(TRAPID).update(data)
        storage.child('{}.jpeg'.format(title)).put('{}.jpeg'.format(title))
      elif line == "close":
        print("close event")
        data = {'date':str(datetime.today().strftime('%Y-%m-%d')),'status':'close'}
        db.child("cliente1").child("ratoeiras").child(TRAPID).update(data)
      
    time.sleep(1)
  return

#send message to the arduino through the Serial port, telling it to close (0) or open (1) the trap
def message_arduino(state):
  print("send to arduino >> {}".format(state))
  ser.write("{}".format("0" if state == "close" else "1").encode('utf-8'))
  return


#firebase connection configs
config = {
  "apiKey": "AIzaSyBctXpkUgrcneMTjGCFZW1ixmxlZiiB5_U",
  "authDomain": "mouse-trap-cb0a7.firebaseapp.com",
  "databaseURL": "https://mouse-trap-cb0a7-default-rtdb.europe-west1.firebasedatabase.app/",
  "storageBucket": "mouse-trap-cb0a7.appspot.com"
}

firebase = pyrebase.initialize_app(config)
db = firebase.database()
storage = firebase.storage()
my_stream = db.child("cliente1").child("ratoeiras").child(TRAPID).stream(stream_handler)

#serial connection to the arduino
try:
  ser = serial.Serial('/dev/ttyACM0', 9600, timeout=1)
  ser.flush()
except:
  print("no serial connection, closing program\n")
  my_stream.close()
  exit()

t = threading.Thread(target=arduino_listener)
t.start()
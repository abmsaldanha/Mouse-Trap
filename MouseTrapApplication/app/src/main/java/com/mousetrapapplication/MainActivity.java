package com.mousetrapapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MyMainActivity";

    // Dataset with mouse traps per client
    ListView trapList;

    // Notifications Handler
    NotificationManager notificationManager;

    // Connect to database
    DatabaseReference myRef = FirebaseDatabase.getInstance("https://mouse-trap-cb0a7-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("cliente1/ratoeiras/");

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        notificationManager = getSystemService(NotificationManager.class);
        createNotificationChannel();
        NotificationManagerCompat notificationManager2 = NotificationManagerCompat.from(this);

        // extension of array adapter to handle "myArrayTrapList"
        ArrayList<TrapStatus> myArrayTrapList = new ArrayList<>();
        TrapAdapter myTrapAdapter = new TrapAdapter(MainActivity.this, R.layout.list_row, myArrayTrapList);

        // display activity to trap list item click
        trapList = (ListView) findViewById(R.id.traps_list);
        trapList.setOnItemClickListener( new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sendTrap(parent,view,position,id);
            }
        });


        /**
         * TODO: DO autenthicator component to login google firebase console account
         **/

        // event Listener to catch data of user account traps from Firebase followed to list parser
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String trap_id = snapshot.getKey();
                TrapStatus trap = snapshot.getValue(TrapStatus.class);
                trap.setDatabaseReference(trap_id);

                if( trap.getStatus().equals("close") )
                    trap.setCageImage(R.drawable.closed_cage);
                else
                    trap.setCageImage(R.drawable.opened_cage);

                myArrayTrapList.add(trap);
                myTrapAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String trap_id = snapshot.getKey();
                TrapStatus updated_trap = snapshot.getValue(TrapStatus.class);
                updated_trap.setDatabaseReference(trap_id);

                int cont = 0;
                for (Iterator<TrapStatus> it = myArrayTrapList.iterator(); it.hasNext(); ) {

                    TrapStatus item = it.next();

                    if ( item.getDatabaseReference().equals(trap_id)  )
                        break;

                    cont++;
                }


                if( updated_trap.getStatus().equals("close") )
                    updated_trap.setCageImage(R.drawable.closed_cage);
                else
                    updated_trap.setCageImage(R.drawable.opened_cage);

                myArrayTrapList.set(cont, updated_trap);
                myTrapAdapter.notifyDataSetChanged();

                addNotification(updated_trap.getName(), trap_id, notificationManager, cont);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String trap_id = snapshot.getKey();
                TrapStatus removed_trap = snapshot.getValue(TrapStatus.class);
                removed_trap.setDatabaseReference(trap_id);

                int cont = 0;
                for (Iterator<TrapStatus> it = myArrayTrapList.iterator(); it.hasNext(); ) {

                    TrapStatus item = it.next();

                    if ( item.getDatabaseReference().equals(trap_id) )
                        break;

                    cont++;
                }

                myArrayTrapList.remove(cont);
                myTrapAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.w(TAG, "WARNING! The admin is drunk and the Firebase have been changed. ");
                myTrapAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read Firebase reference. Maybe you don't have access permissions to ref=" + myRef.toString() + " :(\nMessage is: " + error.getMessage(), error.toException());
                myTrapAdapter.notifyDataSetInvalidated();
            }
        });

        trapList.setAdapter(myTrapAdapter);


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onStart() {
        super.onStart();
    }


    /*
    * Called when the user taps in trap list item
    */
    private void sendTrap(AdapterView<?> parent, View view, int position, long id) {
        // new intent to cast new activity
        Intent intent = new Intent(this, DisplayTrapActivity.class);

        // get and send trap to intent and start new activity
        TrapStatus selected_trap = (TrapStatus) trapList.getItemAtPosition(position);
        intent.putExtra("trap_id", selected_trap.getDatabaseReference());
        intent.putExtra("trap_name", selected_trap.getName());
        startActivity(intent);
    }

    private void addNotification(String trap_name, String trap_id, NotificationManager notificationManager, int notification_id) {
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, DisplayTrapActivity.class);
        intent.putExtra("trap_id", trap_id);
        intent.putExtra("trap_name", trap_name);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Builds your notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "trap_id")
                .setSmallIcon(R.drawable.ic_noun_mouse_trap_153423)
                .setContentTitle(trap_name)
                .setContentText("A new status has just arrived! \nCheck if " + trap_name + " trap have mouses inside :)")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                //.setGroup(GROUP_KEY_WORK_EMAIL)
                // set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(notification_id, builder.build());

    }

    private void createNotificationChannel() {
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ){
            CharSequence name = "MouseOnTrapChannel";
            String description = "Channel to warning user that your trap have success";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            //NotificationChannel channel = new NotificationChannel(trap_id, name, importance);
            NotificationChannel channel = new NotificationChannel("trap_id", name, importance);
            channel.setDescription(description);


            notificationManager.createNotificationChannel(channel);

        }
    }
}
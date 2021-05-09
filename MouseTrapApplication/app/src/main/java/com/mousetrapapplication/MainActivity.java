package com.mousetrapapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    // Dataset with mouse traps per client
    ListView trapList;
    ArrayList<TrapStatus> myArrayTrapList = new ArrayList<>();

    // Connect to database
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://mouse-trap-cb0a7-default-rtdb.europe-west1.firebasedatabase.app/");

    private static final String TAG = "MyMainActivity";
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TrapAdapter myTrapAdapter = new TrapAdapter(MainActivity.this, R.layout.list_row, myArrayTrapList);

        trapList = (ListView) findViewById(R.id.traps_list);

        // dados de teste
        LocalDate date = LocalDate.now();
        myArrayTrapList.add(new TrapStatus(R.drawable.red_light, date, "fechado"));
        myArrayTrapList.add(new TrapStatus(R.drawable.red_light, date, "fechado"));
        myArrayTrapList.add(new TrapStatus(R.drawable.green_light, date, "aberto"));
        myArrayTrapList.add(new TrapStatus(R.drawable.green_light, date, "aberto"));

        /**
         * TODO: Permission denied. FIREBASE NOT WORKING
         * TODO: DO autenthicator component to login google firebase console account
         * TODO: get myRef of the authenticator client with getReference("clienteXPTO")
         **/

        DatabaseReference myRef = database.getReference();

        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                TrapStatus trap = snapshot.getValue(TrapStatus.class);
                if( trap.status == "fechado")
                    trap.image = R.drawable.red_light;
                else
                    trap.image = R.drawable.green_light;

                myArrayTrapList.add(trap);
                myTrapAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                myTrapAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                myTrapAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        trapList.setAdapter(myTrapAdapter);

        /**
         * TODO: ON ITEM CLICK GO TO NEW ACTIVITY
         * */
        //trapList.setOnItemClickListener();

        // Write a message to the database
        //sendBtn.setOnClickListener(new View.OnClickListener() {
            //@Override
            //public void onClick(View view) {
                //ChatMessage chat = new ChatMessage("puf", messageText.getText().toString());
                //myRef.setValue(chat);
                //messageText.setText("");
            //}
        //});

        /*
        List<ChatMessage> messages = new LinkedList<>();
        ArrayAdapter<ChatMessage> adapter = new ArrayAdapter<ChatMessage>(
                this, android.R.layout.two_line_list_item, messages
        ){
            @Override
            public View getView(int position, View view, ViewGroup parent) {
                if ( view == null ) {
                    view = getLayoutInflater().inflate(android.R.layout.two_line_list_item, parent, false);
                }
                ChatMessage chat = messages.get(position);
                ((TextView) view.findViewById(android.R.id.text1)).setText(chat.getName());
                ((TextView) view.findViewById(android.R.id.text2)).setText(chat.getMessage());
                return view;
            }
        };*/

        //messageList.set;

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                TrapStatus trap = dataSnapshot.getValue(TrapStatus.class);
                Log.d(TAG, "Date is: " + trap.getDate()+ ".\n"
                        +"Status is "+trap.getStatus());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read chat value.", error.toException());
            }
        });

    }

    /*
    * Called when the user taps the Send button
    */
    public void sendMessage(View view) {
        // new intent to cast new activity
        Intent intent = new Intent(this, DisplayMessageActivity.class);

        // get inserted text
        //EditText editText = (EditText) findViewById(R.id.editText);
        //String message = editText.getText().toString();

        // send messages to intent and start new activity
        //intent.putExtra(EXTRA_MESSAGE, message);
        //startActivity(intent);
    }
}
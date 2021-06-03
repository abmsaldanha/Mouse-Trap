package com.mousetrapapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.getExternalStoragePublicDirectory;

public class DisplayTrapActivity extends AppCompatActivity {
    private static final String TAG = "MyDisplayTrapActivity";

    StorageReference storageReference;

    DatabaseReference myRef = FirebaseDatabase.getInstance("https://mouse-trap-cb0a7-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("cliente1/ratoeiras/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trap_mouse);


        // Get the Intent that started this activity and extract the string
        Intent parent_intent = getIntent();
        String trap_name = parent_intent.getStringExtra("trap_name");
        String trap_id = parent_intent.getStringExtra("trap_id");

        setTitle(trap_name.toUpperCase());

        // Get the reference of trap item clicked
        DatabaseReference trapRef = myRef.child(trap_id);

        // Capture the layout's Elements
        Button button_open_close = findViewById(R.id.buttonOpenClose);
        Button button_show_mouse_trap = findViewById(R.id.buttonTakePicture);
        TextView text_status = findViewById(R.id.textStatus);
        TextView text_date = findViewById(R.id.textDateLastMove);
        ImageView status_image = findViewById(R.id.picture);

        // Set the layout's Elements
        status_image.setImageResource(R.drawable.loading);

        storageReference = FirebaseStorage.getInstance("gs://mouse-trap-cb0a7.appspot.com").getReference();

        // Attach a listener to read the data at our trap reference
        trapRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator(); it.hasNext(); ) {

                    DataSnapshot item = it.next();

                    if ( item.getKey().equals("status") ){
                        String status = item.getValue(String.class);
                        if ( status.equals("close") ){
                            button_open_close.setText("OPEN");
                            text_status.setText("CLOSE");
                            text_status.setTextColor(Color.parseColor("#FF5440"));
                        }
                        else if (status.equals("detected")) {
                            button_open_close.setText("CLOSE");
                            text_status.setText("DETECTED");
                            text_status.setTextColor(Color.parseColor("#FFBB33"));
                        }
                        else {
                            button_open_close.setText("CLOSE");
                            text_status.setText("OPEN");
                            text_status.setTextColor(Color.parseColor("#80FF33"));
                        }
                    }
                    else if ( item.getKey().equals("date") ){
                        String date = item.getValue(String.class);
                        text_date.setText(date);
                    }
                    else if ( item.getKey().equals("statusImage") ){

                        int statusImage = item.getValue(int.class);
                        String image_name = String.valueOf(statusImage);
                        storageReference =  storageReference.child(image_name + ".jpeg");

                        try {
                            File localFile = File.createTempFile(image_name,"jpeg");
                            storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    //local temp file has been created
                                    Bitmap myBitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                    status_image.setImageBitmap(myBitmap);
                                }
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The trap read failed: " + databaseError.getCode() + "\nMessage is: " + databaseError.getMessage());
            }
        });


        button_show_mouse_trap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trapRef.child("statusImage").setValue(11111);
                status_image.setImageResource(R.drawable.loading);
            }
        });

        // update firebase and close/open trap
        button_open_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (text_status.getText().toString().equals("CLOSE")) {
                    trapRef.child("status").setValue("open");
                    button_open_close.setText("CLOSE");
                    text_status.setText("OPEN");
                } else {
                    trapRef.child("status").setValue("close");
                    button_open_close.setText("OPEN");
                    text_status.setText("CLOSE");
                }
            }
        });

    }
    /*
    // download from Firebase Storage url
    private void download(String image_name) {
        StorageReference image_ref = FirebaseStorage.getInstance("gs://mouse-trap-cb0a7.appspot.com").getReference();
        StorageReference image_ref1 = image_ref.child(image_name + ".jpeg");

        image_ref1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String url = uri.toString();

                downloadFile(DisplayTrapActivity.this, image_name, ".jpeg", DIRECTORY_DOWNLOADS, url);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    //Download Manager to handler the images downloaded from Firebase Storage
    private void downloadFile(Context context, String fileName, String fileExtension, String destinationDirectory, String url) {
        DownloadManager downloadManager = (DownloadManager) context.
                getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, destinationDirectory, fileName + fileExtension);

        downloadManager.enqueue(request);
    }
    */
}
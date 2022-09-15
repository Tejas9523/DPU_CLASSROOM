package com.example.dpuclassroom;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dpuclassroom.databinding.ActivityUploadbeeBinding;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class uploadbee extends AppCompatActivity {
    ActivityUploadbeeBinding binding;

    Uri filepath;

    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityUploadbeeBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel=new NotificationChannel("notificate","notificate", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager=getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        storageReference = FirebaseStorage.getInstance().getReference();

        binding.canclebee.setVisibility(View.INVISIBLE);
        binding.pdfbee.setVisibility(View.INVISIBLE);

        binding.canclebee.setOnClickListener(view -> {
            binding.canclebee.setVisibility(View.INVISIBLE);
            binding.pdfbee.setVisibility(View.INVISIBLE);
            binding.addbee.setVisibility(View.VISIBLE);
        });

        binding.addbee.setOnClickListener(view ->
                Dexter.withContext(getApplicationContext())
                        .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                Intent intent = new Intent();
                                intent.setType("application/pdf");
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(Intent.createChooser(intent, "Select PDF  files"), 101);


                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                                Toast.makeText(uploadbee.this, "Please allow permission", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();
                            }
                        }).check());

        binding.button29bee.setOnClickListener(view -> {
            binding.canclebee.setVisibility(View.INVISIBLE);
            binding.pdfbee.setVisibility(View.INVISIBLE);
            binding.addbee.setVisibility(View.VISIBLE);


            processedupload(filepath);
        });
    }


    FirebaseDatabase database = FirebaseDatabase.getInstance();


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==101 && resultCode==RESULT_OK){
            assert data != null;
            filepath=data.getData();


            binding.canclebee.setVisibility(View.VISIBLE);
            binding.pdfbee.setVisibility(View.VISIBLE);
            binding.addbee.setVisibility(View.INVISIBLE);
        }
    }

    public void processedupload(Uri filepath){

        final ProgressDialog pd=new ProgressDialog(this);
        pd.setTitle("File Uploading....");
        pd.show();


        final StorageReference reference=storageReference.child("Files/"+binding.filetitlebee.getText().toString()+".pdf");
        reference.putFile(filepath)
                .addOnSuccessListener(taskSnapshot -> {
                    pd.dismiss();
                    Toast.makeText(uploadbee.this, "Uploded Successfully", Toast.LENGTH_SHORT).show();


                    reference.getDownloadUrl().addOnSuccessListener((Uri uri) -> {

                        fileinfomodel obj = new fileinfomodel(binding.filetitlebee.getText().toString(), uri.toString());
                        database.getReference().child("Mydocuments").child("bee").child(database.getReference().push().getKey()).setValue(obj);



                        FirebaseMessaging.getInstance().subscribeToTopic("uploaded")
                                .addOnCompleteListener(task -> {
                                    String mssg="Successful";
                                    if (!task.isSuccessful()){
                                        mssg="Failed";
                                    }
                                    Toast.makeText(uploadbee.this, mssg, Toast.LENGTH_SHORT).show();
                                });



                    });
                })
                .addOnProgressListener(snapshot -> {

                    float percent = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    pd.setMessage("Uploaded  " + (int) percent + "%");

                })
                .addOnFailureListener(e -> Toast.makeText(uploadbee.this, "Fill all the data", Toast.LENGTH_SHORT).show());
    }
}

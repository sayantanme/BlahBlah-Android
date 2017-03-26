package com.example.sayantanchakraborty.blahblah;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;

/**
 * Created by sayantanchakraborty on 15/02/17.
 */

public class SettingsTabActivity extends Fragment {
    public static int GALLERY_INTENT_PROFILE =3;
    public ImageView profileImage;
    public TextView userName;
    private DatabaseReference mDataBaseRef;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        profileImage = (ImageView)rootView.findViewById(R.id.image_add);
        userName = (TextView)rootView.findViewById(R.id.textView);

        mDataBaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        Button imgBtn = (Button)rootView.findViewById(R.id.editDisplay);
        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestPermissions();
            }
        });
        setupImageAndName();
        return rootView;

    }

    private void setupImageAndName() {
        mDataBaseRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Object> users = (HashMap<String, Object>) dataSnapshot.getValue();
                userName.setText(users.get("displayName").toString());
                String imgUrl = users.get("profileUrl").toString();
                if(!TextUtils.isEmpty(imgUrl)){
                    Glide.with(getContext()).load(imgUrl).asBitmap().centerCrop().into(new BitmapImageViewTarget(profileImage){
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            profileImage.setImageDrawable(circularBitmapDrawable);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





//        mDataBaseRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                HashMap<String, Object> users = (HashMap<String, Object>) dataSnapshot.getValue();
//                userName.setText(users.get("displayName").toString());
//                String imgUrl = users.get("profileUrl").toString();
//                if(!TextUtils.isEmpty(imgUrl)){
//                    Glide.with(getContext()).load(imgUrl).asBitmap().centerCrop().into(new BitmapImageViewTarget(profileImage){
//                        @Override
//                        protected void setResource(Bitmap resource) {
//                            RoundedBitmapDrawable circularBitmapDrawable =
//                                    RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
//                            circularBitmapDrawable.setCircular(true);
//                            profileImage.setImageDrawable(circularBitmapDrawable);
//                        }
//                    });
//                }
//
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(getContext(),android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent,GALLERY_INTENT_PROFILE);
        }
        else {
            if (shouldShowRequestPermissionRationale(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(getContext(),"Access to photos to send them",Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    GALLERY_INTENT_PROFILE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_INTENT_PROFILE && data != null){
            try {
                Uri uri = data.getData();
                final Bitmap image = BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(uri), null, null);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG,100,baos);
                byte[] dataToUpload = baos.toByteArray();
                //image

                StorageMetadata metadata = new StorageMetadata.Builder()
                        .setContentType("image/jpg")
                        .build();
                StorageReference stoRef = FirebaseStorage.getInstance().getReference()
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()+"/"+System.currentTimeMillis());


                UploadTask uploadTask = stoRef.putBytes(dataToUpload,metadata);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("ChatMessagingActivity","Upload failed");
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        handlSendForProfilePic(downloadUrl);
                    }
                });
            }catch (FileNotFoundException e){
                Log.d("ChatMessagingActivity",e.getLocalizedMessage());
            }
        }
    }

    private void handlSendForProfilePic(Uri downloadUrl) {
        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference().child("Users");
        HashMap<String,Object> map = new HashMap<>();
        //Log.d("profileImage",downloadUrl.toString());
        map.put("profileUrl",downloadUrl.toString());
        messageRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.d("profileImage","update successful");
            }
        });
    }

    //    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == GALLERY_INTENT_PROFILE){
//            try {
//                Uri uri = data.getData();
//                final Bitmap image = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, null);
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                image.compress(Bitmap.CompressFormat.JPEG,100,baos);
//                byte[] dataToUpload = baos.toByteArray();
//                //image
//
//                StorageMetadata metadata = new StorageMetadata.Builder()
//                        .setContentType("image/jpg")
//                        .build();
//                StorageReference stoRef = FirebaseStorage.getInstance().getReference().child("message-Images")
//                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()+"/"+System.currentTimeMillis());
//
//
//                UploadTask uploadTask = stoRef.putBytes(dataToUpload,metadata);
//                uploadTask.addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.d("ChatMessagingActivity","Upload failed");
//                    }
//                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
//                        handlSendForImage(image.getHeight(),image.getWidth(),downloadUrl);
//                    }
//                });
//            }catch (FileNotFoundException e){
//                Log.d("ChatMessagingActivity",e.getLocalizedMessage());
//            }
//        }
//    }
}

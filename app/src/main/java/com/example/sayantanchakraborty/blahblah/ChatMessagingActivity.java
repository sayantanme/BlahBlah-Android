package com.example.sayantanchakraborty.blahblah;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.example.sayantanchakraborty.blahblah.Adapter.ChatMessagesAdapter;
import com.example.sayantanchakraborty.blahblah.Model.Message;
import com.example.sayantanchakraborty.blahblah.Model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.util.ArrayList;
import java.util.HashMap;

public class ChatMessagingActivity extends AppCompatActivity {

    private User user;
    private DatabaseReference refernce;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearlayout;
    private RecyclerView.Adapter adapter;
    private DatabaseReference mDataBaseRef;
    private Context context;
    ArrayList<Message> messagesList;
    ImageButton imgButtonSend;
    ImageButton imgButtonBack;
    TextView imageTxt;

    public static int GALLERY_INTENT =2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_chat_messaging);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.RED));
        imageTxt = (TextView)findViewById(R.id.txtMessage);
//        actionBar.setHomeButtonEnabled(true);
//        actionBar.setDisplayHomeAsUpEnabled(true);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.chat_message_actionbar_layout, null);
        actionBar.setCustomView(mCustomView);
        actionBar.setDisplayShowCustomEnabled(true);
        TextView mTitleTextView = (TextView) mCustomView.findViewById(R.id.chatPersonName);
        final ImageView contactImage1 = (ImageView) mCustomView.findViewById(R.id.chatIcon);
        Bundle b = this.getIntent().getExtras();
        if (b != null)
            user = (User) b.getSerializable("User");
        Log.d("ChatMessagingActivity",user.getDisplayName());
        mTitleTextView.setText(user.getDisplayName());
        if (!TextUtils.isEmpty(user.getProfileUrl()))
            Glide.with(getApplicationContext()).load(user.getProfileUrl()).asBitmap().centerCrop().into(new BitmapImageViewTarget(contactImage1){
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    contactImage1.setImageDrawable(circularBitmapDrawable);
                }
            });

        messagesList = new ArrayList<>();
        recyclerView = (RecyclerView)findViewById(R.id.chat_recycler);


        mDataBaseRef = FirebaseDatabase.getInstance().getReference().child("user-messages");
        ImageButton imgButtonAdd = (ImageButton)findViewById(R.id.btnaddImage);
        imgButtonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestPermissions();

            }
        });
        imgButtonSend = (ImageButton)findViewById(R.id.sendBtn);
        imgButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlSend();
            }
        });
        imgButtonBack = (ImageButton)findViewById(R.id.chatBack);
        imgButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        observeMessages();
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent,GALLERY_INTENT);
        }
        else {
            if (shouldShowRequestPermissionRationale(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this,"Access to photos to send them",Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    GALLERY_INTENT);
        }
    }

    private void handlSend() {
        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference().child("messages");
        //messageRef.push().
        HashMap<String,Object> map = new HashMap<>();
        map.put("SenderFrom",FirebaseAuth.getInstance().getCurrentUser().getUid());
        map.put("SenderTo",user.getId());
        map.put("Text",imageTxt.getText().toString());
        map.put("MessageType","TEXT");
        map.put("TimeStamp",System.currentTimeMillis());
        map.put("ImageUrl","");
        map.put("ImageWidth",0);
        map.put("ImageHeight",0);

        messageRef.push().updateChildren(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                DatabaseReference userMessagesRef = FirebaseDatabase.getInstance().getReference().child("user-messages")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(user.getId());
                String messageId = databaseReference.getKey();

                HashMap<String,Object> messageHash = new HashMap<String, Object>();
                messageHash.put(messageId,1);
                userMessagesRef.updateChildren(messageHash);

                DatabaseReference receipientMessageRef = FirebaseDatabase.getInstance().getReference().child("user-messages")
                        .child(user.getId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                receipientMessageRef.updateChildren(messageHash);
                imageTxt.setText("");
                //Log.d("something",recyclerView.getAdapter().getItemCount());
                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
            }
        });
    }

    private void handlSendForImage(int height, int width,Uri imageUrl) {
        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference().child("messages");
        //messageRef.push().
        HashMap<String,Object> map = new HashMap<>();
        map.put("SenderFrom",FirebaseAuth.getInstance().getCurrentUser().getUid());
        map.put("SenderTo",user.getId());
        map.put("Text",imageTxt.getText().toString());
        map.put("MessageType","PHOTO");
        map.put("TimeStamp",System.currentTimeMillis());
        map.put("ImageUrl",imageUrl.toString());
        map.put("ImageWidth",width);
        map.put("ImageHeight",height);

        messageRef.push().updateChildren(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                DatabaseReference userMessagesRef = FirebaseDatabase.getInstance().getReference().child("user-messages")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(user.getId());
                String messageId = databaseReference.getKey();

                HashMap<String,Object> messageHash = new HashMap<String, Object>();
                messageHash.put(messageId,1);
                userMessagesRef.updateChildren(messageHash);

                DatabaseReference receipientMessageRef = FirebaseDatabase.getInstance().getReference().child("user-messages")
                        .child(user.getId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                receipientMessageRef.updateChildren(messageHash);
                imageTxt.setText("");
                //Log.d("something",recyclerView.getAdapter().getItemCount());
                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
            }
        });
    }


    private void observeMessages() {
        linearlayout = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearlayout);
        String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();


        mDataBaseRef.child(currentUser).child(user.getId()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("ChatMessagingActivity",dataSnapshot.toString());
                String message = dataSnapshot.getKey();
                //if (message.size() > 0) {
                    //for (String key : message.keySet()){
                        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("messages").
                                child(message);
                        dRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                HashMap<String, Object> messages = (HashMap<String, Object>) dataSnapshot.getValue();
                                Message message = new Message();
                                message.setImageHeight((long) messages.get("ImageHeight"));
                                message.setImageWidth((long) messages.get("ImageWidth"));
                                message.setTimestamp((long) messages.get("TimeStamp"));
                                message.setSenderFrom(messages.get("SenderFrom").toString());
                                message.setSenderTo(messages.get("SenderTo").toString());
                                message.setMessageType(messages.get("MessageType").toString());
                                message.setImageUrl(messages.get("ImageUrl").toString());
                                message.setText(messages.get("Text").toString());

                                if (message.chatPartnerId().contentEquals(user.getId()))
                                    messagesList.add(message);
                                Log.d("ChatMessagingActivity", message.getText());


                                //Log.d("ChatMessagingActivity",message.getText());
                                adapter.notifyDataSetChanged();
                                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    //}
                //}
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        adapter = new ChatMessagesAdapter(messagesList,getApplicationContext(),user);
        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_INTENT){
            try {
                Uri uri = data.getData();
                final Bitmap image = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, null);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG,100,baos);
                byte[] dataToUpload = baos.toByteArray();
                //image

                StorageMetadata metadata = new StorageMetadata.Builder()
                        .setContentType("image/jpg")
                        .build();
                StorageReference stoRef = FirebaseStorage.getInstance().getReference().child("message-Images")
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
                        handlSendForImage(image.getHeight(),image.getWidth(),downloadUrl);
                    }
                });
            }catch (FileNotFoundException e){
                Log.d("ChatMessagingActivity",e.getLocalizedMessage());
            }
        }
    }
}

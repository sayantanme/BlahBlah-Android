package com.example.sayantanchakraborty.blahblah.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.example.sayantanchakraborty.blahblah.ChatMessagingActivity;
import com.example.sayantanchakraborty.blahblah.Model.Message;
import com.example.sayantanchakraborty.blahblah.Model.User;
import com.example.sayantanchakraborty.blahblah.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sayantanchakraborty on 17/03/17.
 */

public class MessagesViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private ArrayList<Message> msgList;
    private Context context;
    private HashMap<String,Message> msgHash;
    //private User user;

    public MessagesViewAdapter(ArrayList<Message> msgList, Context context) {
        this.msgList = msgList;
        this.context = context;
        //this.user = user;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.chats_list_item,parent,false);
        return new MessagesViewAdapter.ChatsViewHolder(view1);
    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final MessagesViewAdapter.ChatsViewHolder vW1 = (MessagesViewAdapter.ChatsViewHolder)holder;
        //Message msg = msgHash.
        Message msg1 = msgList.get(position);
        String id = msg1.chatPartnerId();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(id);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String key = dataSnapshot.getKey();
                HashMap<String, Object> messages = (HashMap<String, Object>) dataSnapshot.getValue();
                vW1.contactName.setText(messages.get("displayName").toString());
                String profileUrl = messages.get("profileUrl").toString();
                if (!TextUtils.isEmpty(profileUrl))
                    Glide.with(context).load(profileUrl).asBitmap().centerCrop().into(new BitmapImageViewTarget(vW1.contactImage){
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            vW1.contactImage.setImageDrawable(circularBitmapDrawable);
                        }
                    });
                vW1.continerView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseDatabase.getInstance().getReference().child("Users").child(key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                User user = dataSnapshot.getValue(User.class);
                                Log.d("ContactsTabActivity",dataSnapshot.toString());
                                Intent intent = new Intent(context, ChatMessagingActivity.class);
                                Bundle b = new Bundle();
                                b.putSerializable("User", user);
                                intent.putExtras(b);
                                context.startActivity(intent);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        Toast.makeText(context,key,Toast.LENGTH_LONG).show();

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        vW1.lastMessage.setText(msg1.getText());
        vW1.chatMessageDate.setText(getDate(msg1.getTimestamp(),"hh:mm a"));
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {
        public TextView contactName;
        public TextView lastMessage;
        public TextView chatMessageDate;
        public ImageView contactImage;
        public View continerView;
        public ChatsViewHolder(View itemView) {
            super(itemView);
            contactName = (TextView)itemView.findViewById(R.id.contact_list_tv_chats);
            lastMessage = (TextView)itemView.findViewById(R.id.contact_list_tv_detail_chats);
            chatMessageDate = (TextView)itemView.findViewById(R.id.contact_list_tv_date_chats);
            contactImage = (ImageView)itemView.findViewById(R.id.im_contact_icon_chats);
            continerView = itemView.findViewById(R.id.chat_list_container);
        }
    }

    public static String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}

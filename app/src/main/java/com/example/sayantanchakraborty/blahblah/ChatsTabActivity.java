package com.example.sayantanchakraborty.blahblah;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sayantanchakraborty.blahblah.Adapter.MessagesViewAdapter;
import com.example.sayantanchakraborty.blahblah.Model.Message;
import com.example.sayantanchakraborty.blahblah.Model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.cryptonode.jncryptor.AES256JNCryptor;
import org.cryptonode.jncryptor.CryptorException;
import org.cryptonode.jncryptor.JNCryptor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by sayantanchakraborty on 15/02/17.
 */

public class ChatsTabActivity extends Fragment {

    private DatabaseReference refernce;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearlayout;
    //private FirebaseRecyclerAdapter<Message,ChatsTabActivity.ChatsViewHolder> mFirebaseAdapter;
    private DatabaseReference mDataBaseRef;
    private RecyclerView.Adapter adapter;
    ArrayList<Message> messagesList;
    HashMap<String,Message> messagesDict = new HashMap<>();
    final Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            handleReloadTable();
        }
    };
    private Context context;
    private  String password = "ABCD1234EFGH5678IJKL9012MNOP3456";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_chats, container, false);
        context = getContext();
        messagesList = new ArrayList<>();
        refernce = FirebaseDatabase.getInstance().getReference();
        recyclerView = (RecyclerView)rootView.findViewById(R.id.rec_chats_list);
        mDataBaseRef = FirebaseDatabase.getInstance().getReference().child("user-messages");
        observeUserMessages();
        return rootView;

    }

    private void observeUserMessages() {
        linearlayout = new LinearLayoutManager(this.getActivity());
        recyclerView.setLayoutManager(linearlayout);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDataBaseRef.child(currentUser).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String userId = dataSnapshot.getKey();

                mDataBaseRef.child(currentUser).child(userId).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String messageId = dataSnapshot.getKey();
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("messages")
                                .child(messageId);
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
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
                                if (!TextUtils.isEmpty(messages.get("Text").toString())) {
                                    String decryptedText = decryptText(messages.get("Text").toString());
                                    message.setText(decryptedText);
                                }else{

                                    message.setText(messages.get("Text").toString());
                                }

                                String chatPartnerId = message.chatPartnerId();
                                messagesDict.put(chatPartnerId,message);

                                attemptReloadTable();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

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

        adapter = new MessagesViewAdapter(messagesList,context);
        recyclerView.setAdapter(adapter);
    }

    private void handleReloadTable() {
//        ArrayList<Message> m1 = new ArrayList<Message>(messagesDict.values());
//        messagesList = m1;
        messagesList.clear();
        for(String val: messagesDict.keySet()){


            messagesList.add(messagesDict.get(val));
        }
        handler.removeCallbacks(runnable);
        Collections.sort(messagesList, new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
//                if (o1.getTimestamp() > o2.getTimestamp()){
//                    return o1.getTimestamp();
//                }
                return Long.compare(o2.getTimestamp(),o1.getTimestamp());

            }
        });
        adapter.notifyDataSetChanged();
    }

    private final int FIVE_SECONDS = 1000;

    public void attemptReloadTable() {

        handler.postDelayed(runnable, FIVE_SECONDS);
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

    private String decryptText(String inputText){
        JNCryptor cryptor = new AES256JNCryptor();
        byte[] plaintext = Base64.decode(inputText.getBytes(),Base64.DEFAULT);

        byte[] ciphertext = plaintext;
        try {
            ciphertext = cryptor.decryptData(plaintext,password.toCharArray());
        }catch (CryptorException ex){
            ex.printStackTrace();
        }
        return new String(ciphertext);
        //Base64.encodeToString(ciphertext,Base64.DEFAULT);
    }

}


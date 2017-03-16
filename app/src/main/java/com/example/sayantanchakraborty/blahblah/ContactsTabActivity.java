package com.example.sayantanchakraborty.blahblah;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.example.sayantanchakraborty.blahblah.Model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.bumptech.glide.Glide;

import java.io.Serializable;

import static android.support.v7.recyclerview.R.attr.layoutManager;

/**
 * Created by sayantanchakraborty on 15/02/17.
 */

public class ContactsTabActivity extends Fragment {

    private  DatabaseReference refernce;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearlayout;
    private FirebaseRecyclerAdapter<User,ContactsViewHolder> mFirebaseAdapter;
    private DatabaseReference mDataBaseRef;
    private Context context;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        context = getContext();
        refernce = FirebaseDatabase.getInstance().getReference();
        recyclerView = (RecyclerView)rootView.findViewById(R.id.rec_contact_list);
        mDataBaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        fetchUsers();
        return rootView;

    }

    private void fetchUsers() {

        linearlayout = new LinearLayoutManager(this.getActivity());
        recyclerView.setLayoutManager(linearlayout);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        mFirebaseAdapter = new FirebaseRecyclerAdapter<User, ContactsViewHolder>(
                User.class,R.layout.contacts_list_item,ContactsViewHolder.class,refernce.child("Users")) {
            @Override
            protected void populateViewHolder(final ContactsViewHolder viewHolder, User model, int position) {
                viewHolder.contactName.setText(model.getDisplayName());
                viewHolder.contactDetail.setText(model.getEmail());
                final String key = getRef(position).getKey();

                if (!TextUtils.isEmpty(model.getProfileUrl()))
                    Glide.with(context).load(model.getProfileUrl()).asBitmap().centerCrop().into(new BitmapImageViewTarget(viewHolder.contactImage){
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            viewHolder.contactImage.setImageDrawable(circularBitmapDrawable);
                        }
                    });

                viewHolder.continerView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDataBaseRef.child(key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                User user = dataSnapshot.getValue(User.class);
                                Log.d("ContactsTabActivity",dataSnapshot.toString());
                                Intent intent = new Intent(getActivity(), ChatMessagingActivity.class);
                                Bundle b = new Bundle();
                                b.putSerializable("User", user);
                                intent.putExtras(b);
                                startActivity(intent);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        Toast.makeText(getActivity(),key,Toast.LENGTH_LONG).show();

                    }
                });

            }


        };

        recyclerView.setAdapter(mFirebaseAdapter);
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
        public TextView contactName;
        public TextView contactDetail;
        public ImageView contactImage;
        public View continerView;
        public ContactsViewHolder(View itemView) {
            super(itemView);
            contactName = (TextView)itemView.findViewById(R.id.contact_list_tv);
            contactDetail = (TextView)itemView.findViewById(R.id.contact_list_tv_detail);
            contactImage = (ImageView)itemView.findViewById(R.id.im_contact_icon);
            continerView = itemView.findViewById(R.id.contact_list_container);
        }
    }
}

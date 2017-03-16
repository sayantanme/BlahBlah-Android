package com.example.sayantanchakraborty.blahblah;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by sayantanchakraborty on 01/02/17.
 */

public class SignUpTab extends Fragment {

    EditText emailText;
    EditText passText;
    EditText nametext;
    Button signUp;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference refernce;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_signup, container, false);
        emailText = (EditText)rootView.findViewById(R.id.editText);
        passText = (EditText)rootView.findViewById(R.id.signUpPass);
        nametext = (EditText)rootView.findViewById(R.id.signUpName);

        signUp = (Button)rootView.findViewById(R.id.signUp);
        mAuth = FirebaseAuth.getInstance();
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpWithUserNameAndPassword(emailText.getText().toString(),passText.getText().toString(),nametext.getText().toString());
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("SignUpTab", "onAuthStateChanged:signed_in:" + user.getUid());
                    Intent intent = new Intent(getActivity(), WorkFlowTabbedActivity.class);

                    startActivity(intent);
                } else {
                    // User is signed out
                    Log.d("SignUpTab", "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
        refernce = FirebaseDatabase.getInstance().getReference();

        return rootView;
    }

    private void signUpWithUserNameAndPassword(final String emailtext, final String password, final String userName){
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailtext, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    String uid = mAuth.getCurrentUser().getUid();
                    DatabaseReference databaseReference = refernce.child("Users").child(uid);

                    databaseReference.child("displayName").setValue(userName);
                    databaseReference.child("id").setValue(uid);
                    databaseReference.child("profileUrl").setValue("");
                    databaseReference.child("email").setValue(emailtext);
                    databaseReference.child("password").setValue(password);
                }else {
                    Log.w("SignUpTab", "signUpWithEmail:failed", task.getException());
                    Toast.makeText(getActivity(), "Sign in Failed",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}

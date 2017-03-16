package com.example.sayantanchakraborty.blahblah;

/**
 * Created by sayantanchakraborty on 01/02/17.
 */
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.net.URL;
import java.util.HashMap;

public class LoginTab extends Fragment implements GoogleApiClient.OnConnectionFailedListener {

    EditText emailText;
    EditText passText;
    Button signIn;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private DatabaseReference refernce;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        emailText = (EditText)rootView.findViewById(R.id.email);
        passText = (EditText)rootView.findViewById(R.id.password);
        mAuth = FirebaseAuth.getInstance();
        signIn = (Button)rootView.findViewById(R.id.button2);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("LoginTab","signInTapped" + emailText.getText());
                loginWithUserNameAndPassword(emailText.getText().toString(),passText.getText().toString());
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("LoginTab", "onAuthStateChanged:signed_in:" + user.getUid());
                    Intent intent = new Intent(getActivity(), WorkFlowTabbedActivity.class);

                    startActivity(intent);
                } else {
                    // User is signed out
                    Log.d("LoginTab", "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        //Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity() /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        SignInButton signInButton = (SignInButton) rootView.findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch(v.getId()) {
                    case R.id.sign_in_button:
                        googleSignIn();
                        break;
                }
            }

        });
        refernce = FirebaseDatabase.getInstance().getReference();
        return rootView;
    }

    private void loginWithUserNameAndPassword(String userName,String password){
        //FirebaseAuth.getInstance().signInWithEmailAndPassword()
        mAuth = FirebaseAuth.getInstance();

        mAuth.signInWithEmailAndPassword(userName,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Log.w("LoginTab", "signInWithEmail:failed", task.getException());
                    Toast.makeText(getActivity(), "Auth Failed",
                            Toast.LENGTH_SHORT).show();
                }else {
                    Log.d("LoginTab", "successfully logged in");
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

    private void googleSignIn(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            GoogleSignInResult rsult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(rsult);
        }
    }

    private void handleSignInResult(GoogleSignInResult result){
        if (result.isSuccess()){
            GoogleSignInAccount account = result.getSignInAccount();
            firebaseAuthWithGoogle(account);
        }else{
            Toast.makeText(getActivity(), "Auth Failed",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount account){
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()){
                    Toast.makeText(getActivity(), "Auth Failed",
                            Toast.LENGTH_SHORT).show();
                }else{
                    String uid = mAuth.getCurrentUser().getUid();
                    DatabaseReference databaseReference = refernce.child("Users").child(uid);

                    databaseReference.child("displayName").setValue(account.getDisplayName());
                    databaseReference.child("id").setValue(uid);
                    if(account.getPhotoUrl() != null)
                        databaseReference.child("profileUrl").setValue(account.getPhotoUrl().toString());
                    else
                        databaseReference.child("profileUrl").setValue("");
                    databaseReference.child("email").setValue(account.getEmail());
                }
            }
        });
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

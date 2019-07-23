package com.example.sharkpool_orbital_2019;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class LoginActivity extends AppCompatActivity {
    private int RC_SIGN_IN = 123;

    List<String> approvedAccounts = new ArrayList<>();
    private String uid;
    private String displayName;
    private String emailID;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.AppTheme)
                        .setAvailableProviders(Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build()))
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RC_SIGN_IN && resultCode != RESULT_OK) {
            returnToMain();
        }

        else if (requestCode == RC_SIGN_IN) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            displayName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            emailID = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            approvedAccounts.add("2008fbe@gmail.com");
            approvedAccounts.add("2009fbe@gmail.com");
            approvedAccounts.add("2010fbe@gmail.com");
            approvedAccounts.add("2011fbe@gmail.com");

            if (!emailID.endsWith("@u.nus.edu") && !approvedAccounts.contains(emailID)) {
                deleteAccount();
            }

            else {
                //Initial setup of SendBird and Firestore - if fail, sign out user
                connectToSendBird();
            }
        }
    }


    private void returnToMain() {
        Intent return_intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(return_intent);
        finish();
    }

        private void deleteAccount() {
            Toast.makeText(getApplicationContext(), "Please use a valid NUS email ID.", Toast.LENGTH_LONG).show();
            AuthUI.getInstance().delete(getApplicationContext()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    returnToMain();
                }
            });
        }

        private void signOutAccount() {
            Toast.makeText(getApplicationContext(), "Error occurred during initialization. Please sign in.", Toast.LENGTH_LONG).show();
            AuthUI.getInstance().signOut(getApplicationContext()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    returnToMain();
                }
            });
        }

        private void addToDB() {
            db.collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        if (!task.getResult().exists()){
                            AppUser newUser = new AppUser();
                            newUser.createUser(displayName, emailID);
                            DocumentReference mDocRef = FirebaseFirestore.getInstance().collection("users").document(uid);

                            mDocRef.set(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(getApplicationContext(), "Successfully signed up.", Toast.LENGTH_LONG).show();
                                        returnToMain();

                                    }
                                    else {
                                        signOutAccount();
                                    }
                                }
                            });
                        }
                        returnToMain();
                    }
                    else{
                        signOutAccount();
                    }
                }
            });
        }

    private void connectToSendBird(){
        SendBird.connect(uid, new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                if (e != null){
                    signOutAccount();
                }

                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        SendBird.registerPushTokenForCurrentUser(instanceIdResult.getToken(), new SendBird.RegisterPushTokenWithStatusHandler() {
                            @Override
                            public void onRegistered(SendBird.PushTokenRegistrationStatus status, SendBirdException e) {
                                if (e != null) {
                                    signOutAccount();
                                }
                                else {
                                    addToDB();
                                }
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        signOutAccount();
                    }
                });

                SendBird.disconnect(new SendBird.DisconnectHandler() { //disconnects to ensure chat notifications appear
                    @Override
                    public void onDisconnected() {
                        return;
                    }
                });
            }
        });

    }
}

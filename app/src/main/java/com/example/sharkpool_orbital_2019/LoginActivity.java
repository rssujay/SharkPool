package com.example.sharkpool_orbital_2019;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
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

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase login process
        String uid = FirebaseAuth.getInstance().getUid();
        FirebaseAuth.getInstance().signOut();

        if (uid == null){
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setTheme(R.style.AppTheme)
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Respond to Authentication Callback
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final String displayName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            final String emailID = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            connectToSendBird(uid); //creates new SendBird user

            //CHANGE TO "u.nus.edu" in live version - MAKE SURE THE "u" IS THERE OTHERWISE ALL THE GROUPS@NUS.EDU.SG EMAILS BECOME SPAMMABLE
            if (!emailID.contains("gmail.com")){
                FirebaseAuth.getInstance().getCurrentUser().delete();
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, "Please use a valid NUS email ID", Toast.LENGTH_LONG).show();
                //TODO: deletion of SendBird user
            }

            else{ // Add user details to database if new
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
                                            Log.d("DB","Successfully added new user");
                                            // Adding of nickname to SendBird database
                                            SendBird.updateCurrentUserInfo(displayName, null, new SendBird.UserInfoUpdateHandler() {
                                                @Override
                                                public void onUpdated(SendBirdException e) {
                                                    if (e != null) {    // Error.
                                                        return; //TODO: error handling
                                                    }
                                                }
                                            });
                                        }
                                        else {
                                            Log.d("DB","Cannot add new user",task.getException());
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
            }
            /*
            TODO:

            SendBird integration (automatic authentication with Firebase UID)
             */

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        if (resultCode == RESULT_CANCELED){
            Intent return_intent = new Intent(this, MainActivity.class);
            startActivity(return_intent);
        }
    }
    private void connectToSendBird(String UID){
        SendBird.connect(UID, new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                if (e != null){ //error
                    Log.d("SendBirdPush", "Connection failure.");
                }
                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(LoginActivity.this, new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        SendBird.registerPushTokenForCurrentUser(instanceIdResult.getToken(), new SendBird.RegisterPushTokenWithStatusHandler() {
                            @Override
                            public void onRegistered(SendBird.PushTokenRegistrationStatus status, SendBirdException e) {
                                if (e != null) {        // Error.
                                    Log.d("SendBirdPush", "Token creation failure");
                                    return;
                                }
                                Log.d("SendBirdPush", "Token creation success");
                            }
                        });
                    }
                });
            }
        });

    }
}

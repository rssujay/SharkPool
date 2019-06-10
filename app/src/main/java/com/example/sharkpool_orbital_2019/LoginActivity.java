package com.example.sharkpool_orbital_2019;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
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
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Respond to Authentication Callback
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String displayName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            String emailID = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            //CHANGE TO "u.nus.edu" in live version - MAKE SURE THE "u" IS THERE OTHERWISE ALL THE GROUPS@NUS.EDU.SG EMAILS BECOME SPAMMABLE
            if (!emailID.contains("gmail.com")){
                FirebaseAuth.getInstance().getCurrentUser().delete();
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, "Please use a valid NUS email ID", Toast.LENGTH_LONG).show();
            }

            else{ // Add user details to database
                AppUser newUser = new AppUser();
                newUser.initialize(displayName, emailID, 100);
                DocumentReference mDocRef = FirebaseFirestore.getInstance().collection("users").document(uid);

                mDocRef.set(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d("DB","Successfully added new user");
                        }
                        else {
                            Log.d("DB","Cannot add new user",task.getException());
                        }
                    }
                });
            }

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
}

package com.example.sharkpool_orbital_2019;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

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
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Respond to Authentication Callback
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            String emailID = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            //CHANGE TO "u.nus.edu" in live version - MAKE SURE THE "u" IS THERE OTHERWISE ALL THE GROUPS@NUS.EDU.SG EMAILS BECOME SPAMMABLE
            if (!emailID.contains("gmail.com")){
                FirebaseAuth.getInstance().getCurrentUser().delete();
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, "Please use a valid NUS email ID", Toast.LENGTH_LONG).show();
            }

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
}

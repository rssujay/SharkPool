package com.example.sharkpool_orbital_2019;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String uid = FirebaseAuth.getInstance().getUid();
                if (uid == null){
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Respond to Authentication Callback
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            String uid = FirebaseAuth.getInstance().getUid();
            String emailID = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            if (!emailID.contains("testemaildomain.edu")){
                FirebaseAuth.getInstance().signOut();
                FirebaseAuth.getInstance().getCurrentUser().delete();
                try{
                    Toast.makeText(this, "Please use a valid NUS email ID", Toast.LENGTH_LONG).show();
                    Thread.sleep(5000);
                }
                catch(InterruptedException ex){}
                finish();
            }

            else{
                Toast.makeText(this, "Successful", Toast.LENGTH_LONG).show();
            }

        }
    }
}

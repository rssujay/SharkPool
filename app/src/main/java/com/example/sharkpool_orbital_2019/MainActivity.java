package com.example.sharkpool_orbital_2019;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private int clickCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null && currentUser.isEmailVerified()){
            proceedToMainMenu();
        }
    }

    private void proceedToMainMenu(){
        //Connect user to SendBird
        SendBird.connect(currentUser.getUid(), new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                // Handle exceptions
                if (e != null){
                    Toast.makeText(
                            getApplicationContext(),
                            "Failed to connect to messaging service. Check your connection and try again.", Toast.LENGTH_LONG).show();
                }

                else{
                    // add displayName as Nickname in SendBird database
                    SendBird.updateCurrentUserInfo(currentUser.getDisplayName(), null, new SendBird.UserInfoUpdateHandler() {
                        @Override
                        public void onUpdated(SendBirdException e) {
                            if (e != null) {
                                Toast.makeText(getApplicationContext(),
                                        "Failed to update chat nickname to "
                                                + currentUser.getDisplayName(), Toast.LENGTH_LONG).show();
                            }

                            else{
                                Intent intent = new Intent(getApplicationContext(), MainMenu.class);
                                startActivity(intent);
                            }
                        }
                    });
                }
            }
        });
    }

    public void redirectUser(View v){
        clickCount++;

        // If not registered/signed in
        if (currentUser == null){
            Intent intent = new Intent(v.getContext(), LoginActivity.class);
            startActivity(intent);
        }

        // Else if signed in but email is not verified
        else if (currentUser != null && !currentUser.isEmailVerified()){
            if (clickCount == 1){
                currentUser.sendEmailVerification();
                Toast.makeText(v.getContext(), "Verification email sent - please verify and retry", Toast.LENGTH_LONG).show();
            }

            else{
                Toast.makeText(v.getContext(), "Ensure that you have verified and retry in a minute", Toast.LENGTH_LONG).show();
            }
        }

        //Else for some reason the user has not been redirected to mainMenu
        else{
            proceedToMainMenu();
        }
    }
}
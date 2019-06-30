package com.example.sharkpool_orbital_2019;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        final Button signIn = findViewById(R.id.signinbtn);

        // Proceed to main menu immediately if email verified and logged in
         if (FirebaseAuth.getInstance().getUid() != null && FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
             SendBird.connect(FirebaseAuth.getInstance().getUid(), new SendBird.ConnectHandler() {
                 @Override
                 public void onConnected(User user, SendBirdException e) {
                     if (e != null){ //error
                         Toast.makeText(getApplicationContext(), "Failed to connect to messaging service. Check your connection and try again.", Toast.LENGTH_LONG).show();
                     }
                 }
             });
             // adding displayName as Nickname in SendBird database
             SendBird.updateCurrentUserInfo(FirebaseAuth.getInstance().getCurrentUser().getDisplayName().toString(), null, new SendBird.UserInfoUpdateHandler() {
                 @Override
                 public void onUpdated(SendBirdException e) {
                     if (e != null) { //error
                         Toast.makeText(getApplicationContext(), "Failed to update chat nickname to "+FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), Toast.LENGTH_LONG).show();
                     }
                 }
             });
             Intent intent = new Intent(this, MainMenu.class);
             startActivity(intent);
         }

         signIn.setOnClickListener(
             new View.OnClickListener() {
                 int count = 0;
                 @Override
                 public void onClick(View v) {
                     if (FirebaseAuth.getInstance().getCurrentUser() == null){
                         Intent intent = new Intent(v.getContext(), LoginActivity.class);
                            startActivity(intent);
                     }

                     FirebaseAuth.getInstance().getCurrentUser().reload();
                     if (!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
                         if (count == 0) {
                             FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();
                             Toast.makeText(v.getContext(), "Verification email sent - please verify and retry", Toast.LENGTH_LONG).show();
                             count++;
                         }
                         else{
                             Toast.makeText(v.getContext(), "Ensure that you have verified and retry in a minute", Toast.LENGTH_LONG).show();
                         }
                     }

                     else{
                         Intent intent = new Intent(v.getContext(), MainMenu.class);
                         startActivity(intent);
                     }
                 }
             });
    }
}

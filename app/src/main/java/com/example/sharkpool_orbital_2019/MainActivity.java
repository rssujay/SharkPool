package com.example.sharkpool_orbital_2019;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private int clickCount = 0;

    private Button signIn;
    private ProgressBar delay;
    private String notifRedirect = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        signIn = findViewById(R.id.signinbtn);
        delay = findViewById(R.id.loginProgress);
        mAuth = FirebaseAuth.getInstance();

        if (getIntent().getExtras() != null){
            notifRedirect = getIntent().getExtras().getString("requestID");
            getIntent().removeExtra("requestID");
        }


        final ImageView backgroundOne = (ImageView) findViewById(R.id.background_one);
        final ImageView backgroundTwo = (ImageView) findViewById(R.id.background_two);

        final ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(10000L);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float progress = (float) animation.getAnimatedValue();
                final float width = backgroundOne.getWidth();
                final float translationX = width * progress;
                backgroundOne.setTranslationX(translationX);
                backgroundTwo.setTranslationX(-width + translationX);
            }
        });
        animator.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null && currentUser.isEmailVerified()){
            proceedToMainMenu();
        }
        else{
            enableButton();
        }
    }

    private void enableButton(){
        delay.setVisibility(View.INVISIBLE);
        signIn.setEnabled(true);
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
                    enableButton();
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
                                enableButton();
                            }

                            else{
                                if (notifRedirect == null || notifRedirect.isEmpty()) {
                                    Intent intent = new Intent(getApplicationContext(), MainMenu.class);
                                    startActivity(intent);
                                }

                                else {
                                    Intent intent = new Intent(getApplicationContext(), BRview.class);
                                    intent.putExtra("initiator", notifRedirect);
                                    notifRedirect = "";
                                    startActivity(intent);
                                }
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
            finish();
        }

        // Else if signed in but email is not verified
        else if (currentUser != null){
            currentUser.reload();
            if (!currentUser.isEmailVerified()) {
                if (clickCount == 1) {
                    currentUser.sendEmailVerification();
                    Toast.makeText(v.getContext(), "Verification email sent - please verify and retry", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(v.getContext(), "Ensure that you have verified and retry", Toast.LENGTH_LONG).show();
                }
            }
            else {
                proceedToMainMenu();
            }
        }

        //Else for some reason the user has not been redirected to mainMenu
        else{
            proceedToMainMenu();
        }
    }
}
package com.example.sharkpool_orbital_2019;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        final Button signIn = findViewById(R.id.signinbtn);
        //Uncomment the below line to signout until a log out button is created
        //FirebaseAuth.getInstance().signOut();

        // Proceed to main menu immediately if email verified and logged in
         if (FirebaseAuth.getInstance().getUid() != null && FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
            Intent intent = new Intent(this, MainMenu.class);
            startActivity(intent);
         }

         signIn.setOnClickListener(
                 new View.OnClickListener() {
                     int count = 0;
                    @Override
                    public void onClick(View v) {
                        if (FirebaseAuth.getInstance().getUid() == null){
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
                                Toast.makeText(v.getContext(), "Ensure you have verified and retry in a minute", Toast.LENGTH_LONG).show();
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

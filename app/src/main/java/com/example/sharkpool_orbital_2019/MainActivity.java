package com.example.sharkpool_orbital_2019;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CheckBox;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = (FirebaseAuth.getInstance().getUid() == null)?
                new Intent(this, LoginActivity.class) :
                new Intent(this, MainMenu.class);
        startActivity(intent);
    }
}

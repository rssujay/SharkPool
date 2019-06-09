package com.example.sharkpool_orbital_2019;

import com.google.firebase.auth.FirebaseAuth;

import io.realm.RealmObject;

public class AppUser extends RealmObject {
    private String displayName;
    private String emailAddress;
    private String uid; //Unique ID to be used as primary key

    public AppUser(){
        displayName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        emailAddress = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}

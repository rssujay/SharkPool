package com.example.sharkpool_orbital_2019;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;

public class MainMenu extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    AppUser currUser = new AppUser();
    private String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private ArrayList<String> list = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        setTitle("Dashboard");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        //Drawer config
        View hView = navigationView.getHeaderView(0);
        final TextView nav_credit = hView.findViewById(R.id.creditsNum);
        final TextView nav_name = hView.findViewById(R.id.nav_name);
        final TextView nav_email = hView.findViewById(R.id.nav_email);

        final TextView notification_count = navigationView.getMenu().findItem(R.id.foregroundNotifications)
                .getActionView().findViewById(R.id.notificationCounter);
        Log.d("Notif","hi");

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //Bottom Nav Bar config
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnNavigationItemSelectedListener(navListener);



        //get userdata from DB
        currUser.initialize("Error fetching name", "Error fetching email", -1);


        DocumentReference mDocRef = db.collection("users").document(uid);
        Task<DocumentSnapshot> document = mDocRef.get();

        document.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                String displayName = documentSnapshot.get("displayName").toString();
                String emailAddress = documentSnapshot.get("emailAddress").toString();
                int credits = ((Long) documentSnapshot.get("credits")).intValue();
                Boolean tocAgreed = documentSnapshot.getBoolean("tocAgreed");
                int notifCount = ((Long) documentSnapshot.get("foregroundNotifications")).intValue();
                currUser.initialize(displayName,emailAddress, credits, tocAgreed, notifCount);
                nav_name.setText(currUser.getDisplayName());
                nav_email.setText(currUser.getEmailAddress());
                nav_credit.append(Integer.toString(currUser.getCredits()));
                notification_count.setText(Integer.toString(currUser.getForegroundNotifications()));

                if(!currUser.isTocAgreed()){
                    Intent intent = new Intent(getBaseContext(), tocPage.class);
                    startActivity(intent);
                }

                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        String token = instanceIdResult.getToken();

                        //Update in user information
                        currUser.setNotificationToken(token);
                        db.collection("users").document(uid).update("notificationToken",currUser.getNotificationToken());
                    }
                });
            }
        });

        Fragment selectedFragment;
        selectedFragment = new HistoryFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container,
                selectedFragment).commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_sign_out){
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    finishAffinity();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFragment;

                    switch (menuItem.getItemId()){
                        case R.id.ongoing:
                            selectedFragment = new OpenRequestsFragment();
                            break;
                        case R.id.history:
                            selectedFragment = new HistoryFragment();
                            break;
                        case R.id.requests:
                            selectedFragment = new OngoingRequestsFragment();
                            break;
                        case R.id.profile:
                            selectedFragment = new LendlistFragment();
                            break;
                        default:
                            selectedFragment = new OngoingRequestsFragment();
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container,
                            selectedFragment).commit();

                    return true;
                }
            };


}
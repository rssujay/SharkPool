package com.example.sharkpool_orbital_2019;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainMenu extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    AppUser currUser = new AppUser();

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

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //Bottom Nav Bar config
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //get userdata from DB
        currUser.initialize("Error fetching name", "Error fetching email", -1);


        DocumentReference mDocRef = FirebaseFirestore.getInstance().collection("users").document(uid);
        Task<DocumentSnapshot> document = mDocRef.get();

        document.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String displayName = documentSnapshot.get("displayName").toString();
                String emailAddress = documentSnapshot.get("emailAddress").toString();
                int credits = ((Long) documentSnapshot.get("credits")).intValue();
                Boolean tocAgreed = documentSnapshot.getBoolean("tocAgreed");
                currUser.initialize(displayName,emailAddress, credits, tocAgreed);
                nav_name.setText(currUser.getDisplayName());
                nav_email.setText(currUser.getEmailAddress());

                Integer tempCredits = currUser.getCredits();
                nav_credit.append(tempCredits.toString());

                if(!currUser.isTocAgreed()){
                    Intent intent = new Intent(getBaseContext(), tocPage.class);
                    startActivity(intent);
                }
            }
        });
        /*
        // Ongoing requests fragment configuration
        ArrayList<BorrowRequest> borrowRequests = new ArrayList<>();
        RequestArrayAdaptor requestArrayAdaptor = new RequestArrayAdaptor(this, borrowRequests);
        ListView listView = (ListView) findViewById(R.id.ongoingListView);
        listView.setAdapter(requestArrayAdaptor);

        //TEST FOR ONGOING REQUESTS DATA DISPLAY
        BorrowRequest newRequest = new BorrowRequest();
        newRequest.initialize("test_id","Pen");
        requestArrayAdaptor.add(newRequest);
        */
        Fragment selectedFragment = null;
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_sign_out){
            FirebaseAuth.getInstance().signOut();
            finishAffinity();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFragment = null;

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
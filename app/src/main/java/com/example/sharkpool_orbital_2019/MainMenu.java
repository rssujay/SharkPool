package com.example.sharkpool_orbital_2019;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.View;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;

public class MainMenu extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    AppUser currUser = new AppUser();
    private String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private String token;
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
                currUser.initialize(displayName,emailAddress, credits, tocAgreed);
                nav_name.setText(currUser.getDisplayName());
                nav_email.setText(currUser.getEmailAddress());

                Integer tempCredits = currUser.getCredits();
                nav_credit.append(tempCredits.toString());

                if(!currUser.isTocAgreed()){
                    Intent intent = new Intent(getBaseContext(), tocPage.class);
                    startActivity(intent);
                }

                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        token = instanceIdResult.getToken();
                        getLL();
                    }
                });
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

    public void getLL() {
        db.collection("users").document(uid).collection("lendList").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                            list.add(document.getId());
                        }
                        updateTokens(list);
                    }
                });
    }

    public void updateTokens(ArrayList<String> list){
        WriteBatch batch = db.batch();

        for (int k = 0; k < list.size(); k++){
            DocumentReference ref = db.collection("users").document(uid).collection("lendList").document(list.get(k));
            batch.update(ref, "token", token);
        }

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("Firebase","Tokens updated");
            }
        });
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
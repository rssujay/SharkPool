package com.example.sharkpool_orbital_2019;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class BorrowRequestCreation extends AppCompatActivity {
    private static final String TAG = "BorrowRequestCreation";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Set up variables
    private EditText itemNameEntry;
    private Spinner itemTypeEntry;
    private EditText itemTypeCustomEntry;
    private NumberPicker creditValueEntry;
    private EditText commentsEntry;
    private CheckBox recommendationsEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow_request_creation);

        //Associate variables with XML
        itemNameEntry = findViewById(R.id.itemNameEntry);
        itemTypeEntry = findViewById(R.id.itemTypeEntry);
        itemTypeCustomEntry = findViewById(R.id.itemTypeCustomEntry);
        creditValueEntry = findViewById(R.id.creditValueEntry);
        commentsEntry = findViewById(R.id.commentsEntry);
        recommendationsEntry = findViewById(R.id.recommendationsEntry);

        creditValueEntry.setMinValue(0);
        creditValueEntry.setMaxValue(1000);
        populateOptions();

        itemTypeEntry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = parent.getItemAtPosition(position).toString();
                if (text.equals("Custom")){
                    itemTypeCustomEntry.setVisibility(View.VISIBLE);
                    recommendationsEntry.setChecked(false);
                    recommendationsEntry.setEnabled(false);
                }
                else{
                    itemTypeCustomEntry.setVisibility(View.INVISIBLE);
                    recommendationsEntry.setEnabled(true);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void populateOptions(){
        final ArrayList<String> fromDB = new ArrayList<>();


        CollectionReference collRef = db.collection("itemTypes");

        collRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<String> types = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots){
                    types.add(document.getId());
                }
                types.add("Custom");
                ArrayAdapter options = new ArrayAdapter<String>
                        (BorrowRequestCreation.this,R.layout.support_simple_spinner_dropdown_item, types);
                itemTypeEntry.setAdapter(options);
            }})
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(BorrowRequestCreation.this, "Error, please reload", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void addToDB(View v){
        BorrowRequest newReq = new BorrowRequest();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String itemName = itemNameEntry.getText().toString();

        String defaultType = itemTypeEntry.getSelectedItem().toString();
        String customType = itemTypeCustomEntry.getText().toString();
        String itemType = (defaultType.equals("Custom"))? customType : defaultType;

        int creditValue = creditValueEntry.getValue();
        String comments = commentsEntry.getText().toString();
        boolean recommendations = recommendationsEntry.isChecked();

        if (uid.isEmpty() || itemName.isEmpty() || itemType.isEmpty() || creditValue < 0){
            Toast.makeText(BorrowRequestCreation.this,"Please fill in the required fields",Toast.LENGTH_SHORT).show();
        }

        else{
            newReq.startBorrowRequest(uid, itemName, itemType, creditValue, comments, recommendations);

            //Attempt to store in DB
            db.collection("requests").document().set(newReq)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Intent intent = new Intent(BorrowRequestCreation.this, MainMenu.class);
                    startActivity(intent);
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(BorrowRequestCreation.this,
                                    "Error, please check your connection", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}

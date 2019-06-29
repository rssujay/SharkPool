package com.example.sharkpool_orbital_2019;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Struct;
import java.util.HashMap;
import java.util.Map;

public class logDispute extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private TextView requestID;
    private Spinner disputeType;
    private EditText addEntry;
    private TextView addNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_dispute);
        getSupportActionBar().hide();
        String BRid = getIntent().getStringExtra("BRid");

        requestID = findViewById(R.id.BRid);
        disputeType = findViewById(R.id.typeSelector);
        addEntry = findViewById(R.id.additionalEntry);
        addNotes = findViewById(R.id.additionalNotes);

        requestID.setText(BRid);

        disputeType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String chosen = parent.getItemAtPosition(position).toString();
                if (chosen.equals("Scammed/Item stolen")){
                    addNotes.setText("Sharkpool will look into the issue and get back to you via email within 2 working days. Please inform campus security/police as well.");
                }
                else{
                    addNotes.setText("Sharkpool will look into the issue and get back to you via email within 2 working days. We thank you for your patience.");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void fileDispute(View v){
        final String uniqueBrid = requestID.getText().toString();
        String type = disputeType.getSelectedItem().toString();
        String extra = addEntry.getText().toString();

        Map<String, String> disp = new HashMap<>();
        disp.put("Submitter", FirebaseAuth.getInstance().getCurrentUser().getUid());
        disp.put("uniqueId", uniqueBrid);
        disp.put("type", type);
        disp.put("extra", extra);

        db.collection("disputes").document(uniqueBrid).set(disp).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                db.collection("requests").document(uniqueBrid).update("dispute",true).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent intent = new Intent(getBaseContext(),MainMenu.class);
                        startActivity(intent);
                    }
                });
            }
        });
    }


}

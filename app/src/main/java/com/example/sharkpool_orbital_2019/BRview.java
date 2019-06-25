package com.example.sharkpool_orbital_2019;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.TimeZone;

public class BRview extends AppCompatActivity {
    //Meta
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private boolean userIsBorrower;

    private BorrowRequest request = new BorrowRequest();

    //Elements
    private TextView requestUID;
    private TextView status;
    private TextView itemName;
    private TextView itemType;
    private TextView borrowerName;
    private TextView lenderName;
    private TextView creditValue;
    private TextView comments;
    private TextView codeOne;
    private TextView codeTwo;
    private TextView startDate;
    private TextView returnDate;
    private EditText codeEntry;
    private Button becomeLend;
    private Button deleteReq;
    private Button cancelLend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brview);
        getSupportActionBar().hide();

        requestUID = findViewById(R.id.reqID);
        status = findViewById(R.id.brStatus);
        itemName = findViewById(R.id.itmName);
        itemType = findViewById(R.id.itmType);
        borrowerName = findViewById(R.id.borrowName);
        lenderName = findViewById(R.id.lendName);
        creditValue = findViewById(R.id.itmCredits);
        comments = findViewById(R.id.brComments);
        codeOne = findViewById(R.id.userCode1);
        codeTwo = findViewById(R.id.userCode2);
        codeEntry = findViewById(R.id.codeEntry);
        startDate = findViewById(R.id.startDate);
        returnDate = findViewById(R.id.returnDate);
        becomeLend = findViewById(R.id.becomeLender);
        deleteReq = findViewById(R.id.borrowCancel);
        cancelLend = findViewById(R.id.lendCancel);

        Bundle bundle = getIntent().getExtras();
        final String requestID = bundle.getString("initiator");
        db.collection("requests").document(requestID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                request.populate(documentSnapshot.getString("requestID"), documentSnapshot.getString("borrowerUID"),
                        documentSnapshot.getString("borrowerName"), documentSnapshot.getString("lenderUID"),
                        documentSnapshot.getString("lenderName"), documentSnapshot.getString("status"),
                        documentSnapshot.getString("comments"), documentSnapshot.getDate("createdDate"),
                        documentSnapshot.getDate("startDate"), documentSnapshot.getDate("returnDate"),
                        documentSnapshot.getLong("borrowCodeOne").intValue(), documentSnapshot.getLong("borrowCodeTwo").intValue(),
                        documentSnapshot.getLong("lendCodeOne").intValue(), documentSnapshot.getLong("lendCodeTwo").intValue(),
                        documentSnapshot.getString("itemName"), documentSnapshot.getString("itemType"),
                        documentSnapshot.getBoolean("recommendations"),documentSnapshot.getLong("creditValue").intValue(),
                        documentSnapshot.getBoolean("dispute")
                        );

                userIsBorrower = userUID.equals(request.getBorrowerUID());

                requestUID.append(request.getRequestID());
                status.append(request.getStatus());
                itemName.setText(request.getItemName());
                itemType.setText(request.getItemType());
                borrowerName.setText(request.getBorrowerName());

                if (!request.getLenderName().isEmpty()){
                    lenderName.setText(request.getLenderName());
                }

                creditValue.append(Integer.toString(request.getCreditValue()));
                comments.append(request.getComments());

                if(userIsBorrower) {
                    codeOne.append(Integer.toString(request.getBorrowCodeOne()));
                    codeTwo.append(Integer.toString(request.getBorrowCodeTwo()));
                }

                else{
                    codeOne.append(Integer.toString(request.getLendCodeOne()));
                    codeTwo.append(Integer.toString(request.getLendCodeTwo()));
                }

                if (request.getStatus().equals("Borrowed")){
                    status.setTextColor(Color.YELLOW);
                    TimeZone asiaSingapore = TimeZone.getTimeZone("Asia/Singapore");
                    Calendar nowAsiaSingapore = Calendar.getInstance(asiaSingapore);
                    nowAsiaSingapore.setTime(request.getStartDate());
                    startDate.append(nowAsiaSingapore.toString());
                }

                if (request.getStatus().equals("Completed")){
                    status.setTextColor(Color.GREEN);
                    TimeZone asiaSingapore = TimeZone.getTimeZone("Asia/Singapore");
                    Calendar nowAsiaSingapore = Calendar.getInstance(asiaSingapore);
                    nowAsiaSingapore.setTime(request.getReturnDate());
                    startDate.append(nowAsiaSingapore.toString());
                }

                // Allow borrower to delete request
                String currStatus = request.getStatus();
                if ((currStatus.equals("Open")) && userIsBorrower){
                    deleteReq.setEnabled(true);
                }

                //Enable become lender for potential lender
                if (currStatus.equals("Open") && !userIsBorrower){
                    becomeLend.setVisibility(View.VISIBLE);
                }

                //Allow lender to cancel lending
                if (currStatus.equals("Closed") && !userIsBorrower){
                    cancelLend.setEnabled(true);
                }
            }
        });
    }

    public void borrowDelete(View v){
        db.collection("requests").document(request.getRequestID()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                db.collection("users").document(request.getBorrowerUID()).update("credits",FieldValue.increment(request.getCreditValue()));
                db.collection("users").document(request.getBorrowerUID()).update("requests", FieldValue.arrayRemove(request.getRequestID()));
                Intent intent = new Intent(getBaseContext(), MainMenu.class);
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getBaseContext(),"Error, please check your connection",Toast.LENGTH_SHORT);
            }
        });
    }

    public void becomeLender(View v){
        db.collection("users").document(userUID).update("requests",FieldValue.arrayUnion(request.getRequestID())).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                db.collection("requests").document(request.getRequestID()).update(
                        "lenderName", FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                        "lenderUID", userUID, "status", "Closed");
                Intent intent = new Intent(getBaseContext(), MainMenu.class);
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getBaseContext(),"Error, please check your connection",Toast.LENGTH_SHORT);
            }
        });
    }
}
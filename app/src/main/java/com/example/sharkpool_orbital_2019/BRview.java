package com.example.sharkpool_orbital_2019;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class BRview extends AppCompatActivity {
    //Meta
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private boolean userIsBorrower;
    private boolean userIsLender;

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
    private ProgressBar updateBar;

    private Button submitCode;
    private Button becomeLend;
    private Button deleteReq;
    private Button cancelLend;
    private Button dispute;

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
        dispute = findViewById(R.id.dispute);
        submitCode = findViewById(R.id.submitCode);
        updateBar = findViewById(R.id.updateBar);

        Bundle bundle = getIntent().getExtras();
        final String requestID = bundle.getString("initiator");

        db.collection("requests").document(requestID).addSnapshotListener(BRview.this ,new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (!documentSnapshot.exists()) {
                    Toast.makeText(getApplicationContext(), "Transaction closed", Toast.LENGTH_SHORT).show();
                    finishAffinity();
                    Intent intent = new Intent(getApplicationContext(), MainMenu.class);
                    startActivity(intent);
                    finish();
                } else {
                    WindowManager.LayoutParams lparams = getWindow().getAttributes();
                    lparams.dimAmount = 0.7f;
                    getWindow().setAttributes(lparams);
                    updateBar.setVisibility(View.VISIBLE);

                    request.populate(documentSnapshot.getString("requestID"), documentSnapshot.getString("borrowerUID"),
                            documentSnapshot.getString("borrowerName"), documentSnapshot.getString("lenderUID"),
                            documentSnapshot.getString("lenderName"), documentSnapshot.getString("status"),
                            documentSnapshot.getString("comments"), documentSnapshot.getDate("createdDate"),
                            documentSnapshot.getDate("startDate"), documentSnapshot.getDate("returnDate"),
                            documentSnapshot.getLong("borrowCodeOne").intValue(), documentSnapshot.getLong("borrowCodeTwo").intValue(),
                            documentSnapshot.getLong("lendCodeOne").intValue(), documentSnapshot.getLong("lendCodeTwo").intValue(),
                            documentSnapshot.getString("itemName"), documentSnapshot.getString("itemType"),
                            documentSnapshot.getBoolean("recommendations"), documentSnapshot.getLong("creditValue").intValue(),
                            documentSnapshot.getBoolean("dispute")
                    );

                    userIsBorrower = userUID.equals(request.getBorrowerUID());
                    userIsLender = (request.getLenderUID().isEmpty() || request.getLenderUID().equals(userUID));

                    if (!(userIsLender || userIsBorrower)) {
                        Toast.makeText(getApplicationContext(), "Transaction updated, please retry", Toast.LENGTH_SHORT).show();
                        finishAffinity();
                        Intent intent = new Intent(getApplicationContext(), MainMenu.class);
                        startActivity(intent);
                        finish();
                    }

                    requestUID.setText("Request ID: ".concat(request.getRequestID()));
                    status.setText(request.getStatus());
                    itemName.setText(request.getItemName());
                    itemType.setText(request.getItemType());
                    borrowerName.setText(request.getBorrowerName());

                    if (!request.getLenderName().isEmpty()) {
                        lenderName.setText(request.getLenderName());
                    } else {
                        lenderName.setText("No active lender");
                    }

                    creditValue.setText("Credits: ".concat(Integer.toString(request.getCreditValue())));
                    comments.setText("Borrower comments: ".concat(request.getComments()));

                    if (userIsBorrower) {
                        codeOne.setText("Initial code: ".concat(Integer.toString(request.getBorrowCodeOne())));
                        codeTwo.setText("Return code: ".concat(Integer.toString(request.getBorrowCodeTwo())));
                    } else {
                        codeOne.setText("Initial code: ".concat(Integer.toString(request.getLendCodeOne())));
                        codeTwo.setText("Return code: ".concat(Integer.toString(request.getLendCodeTwo())));
                    }

                    deleteReq.setEnabled(false);
                    becomeLend.setVisibility(View.INVISIBLE);
                    cancelLend.setEnabled(false);
                    submitCode.setEnabled(false);
                    dispute.setEnabled(false);

                    // Status specific code
                    switch (request.getStatus()) {
                        case "Open":
                            // Allow borrower to delete request
                            if (userIsBorrower) {
                                deleteReq.setEnabled(true);
                            }
                            //Enable become lender for potential lender
                            else {
                                becomeLend.setVisibility(View.VISIBLE);
                            }
                            break;

                        case "Closed":
                            //Allow lender to cancel lending
                            if (!userIsBorrower) {
                                cancelLend.setEnabled(true);
                            }
                            submitCode.setEnabled(true);
                            status.setTextColor(Color.MAGENTA);
                            break;

                        case "Lent/Borrowed":
                            startDate.setText("Borrowed at: ".concat(request.getStartDate().toString()));
                            submitCode.setText("Confirm Return");
                            submitCode.setEnabled(true);
                            dispute.setEnabled(true);
                            status.setTextColor(Color.BLUE);
                            break;

                        default: // Completed
                            startDate.setText("Borrowed at: ".concat(request.getStartDate().toString()));
                            returnDate.setText("Returned at: ".concat(request.getReturnDate().toString()));
                            dispute.setEnabled(true);
                            status.setTextColor(Color.GREEN);
                    }

                    //If under dispute, disable all possible actions
                    if (request.isDispute()) {
                        status.setText(status.getText().toString().concat(" (Under Dispute)"));
                        status.setTextColor(Color.RED);
                        submitCode.setEnabled(false);
                        dispute.setEnabled(false);
                    }

                    lparams.dimAmount = 0.0f;
                    getWindow().setAttributes(lparams);
                    updateBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public void borrowDelete(View v){
        db.collection("requests").document(request.getRequestID()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                db.collection("users").document(request.getBorrowerUID()).update("credits",FieldValue.increment(request.getCreditValue())).addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Intent intent = new Intent(getBaseContext(), MainMenu.class);
                                startActivity(intent);
                            }
                        }
                );
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getBaseContext(),"Error, please check your connection",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void becomeLender(View v){
        db.collection("requests").document(request.getRequestID()).update(
                "lenderName", FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                "lenderUID", userUID, "status", "Closed")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(),"Transaction updated",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getBaseContext(),"Error, please check your connection",Toast.LENGTH_SHORT).show();
                    }
        });
    }

    public void cancelLender(View v){
        db.collection("requests").document(request.getRequestID()).update(
                "lenderName", "",
                "lenderUID", "", "status", "Open")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(),"Transaction updated",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getBaseContext(),"Error, please check your connection",Toast.LENGTH_SHORT).show();
                    }
        });
    }

    public void checkCode(View v){
        String enteredCode = codeEntry.getText().toString();
        // Initial codes
        if (request.getStatus().equals("Closed")){
            String correctCode = Integer.toString(request.getLendCodeOne()).concat(Integer.toString(request.getBorrowCodeOne()));

            if (enteredCode.equals(correctCode)){
                db.collection("requests").document(request.getRequestID()).update(
                        "status","Lent/Borrowed","startDate", Timestamp.now())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(),"Transaction updated",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            else{
                Toast.makeText(getBaseContext(),"Incorrect code entered, please try again.",Toast.LENGTH_LONG).show();
            }
        }

        // Return codes
        else if (request.getStatus().equals("Lent/Borrowed")){
            String correctCode = Integer.toString(request.getLendCodeTwo()).concat(Integer.toString(request.getBorrowCodeTwo()));

            if (enteredCode.equals(correctCode)){
                db.collection("requests").document(request.getRequestID()).update(
                        "status","Completed","returnDate",Timestamp.now())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        db.collection("users").document(request.getLenderUID())
                                .update("credits",FieldValue.increment(request.getCreditValue())).addOnSuccessListener(
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getApplicationContext(),"Transaction updated",Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
                    }
                });
            }
            else{
                Toast.makeText(getBaseContext(),"Incorrect code entered, please try again.",Toast.LENGTH_LONG).show();
            }
        }
    }

    public void startDispute(View v){
        Intent intent = new Intent(getBaseContext(),logDispute.class);
        intent.putExtra("BRid",request.getRequestID());
        startActivity(intent);
    }

    public void startChat(View v){
        if (request.getStatus().equals("Open")){
            Toast.makeText(getBaseContext(), "No chat to be opened.", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(getBaseContext(),ChatActivity.class);
        String otherID;
        if (userIsBorrower){
            otherID = request.getLenderUID();
        } else {
            otherID = request.getBorrowerUID();
        }
        List<String> ChatUIDs = new ArrayList<>();
        ChatUIDs.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
        intent.putExtra("otherID", otherID);
        startActivity(intent);
    }
}

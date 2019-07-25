package com.example.sharkpool_orbital_2019;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import static android.text.Layout.JUSTIFICATION_MODE_INTER_WORD;

public class tocPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Terms & Conditions");
        setContentView(R.layout.activity_toc_page);
        TextView toc = findViewById(R.id.tocText);
        toc.setText(Html.fromHtml(getString(R.string.toc)));
        if (Build.VERSION.SDK_INT >= 26) {
            toc.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
        }
    }

    public void agreePress(View v){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid).update("tocAgreed",true).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Intent intent = new Intent(getBaseContext(), MainMenu.class);
                startActivity(intent);
            }
        });
    }
}

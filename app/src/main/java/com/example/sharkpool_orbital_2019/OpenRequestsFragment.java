package com.example.sharkpool_orbital_2019;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

public class OpenRequestsFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private RecyclerView recyclerView;
    private TextView numCheck;
    private ProgressBar progress;
    private Vector<BorrowRequest> openRequests = new Vector<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.openrequests_fragment, container, false);
        progress = rootView.findViewById(R.id.progress_open);
        numCheck = rootView.findViewById(R.id.numCheckOpen);
        recyclerView = rootView.findViewById(R.id.openRequestsList);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));

        //Populate
        CollectionReference collRef = db.collection("requests");

        //Query 1a
        collRef.orderBy("borrowerUID", Query.Direction.ASCENDING)
                .whereEqualTo("status","Open")
                .whereGreaterThan("borrowerUID", uid)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                    openRequests.add(document.toObject(BorrowRequest.class));
                }
                updateRecyclerView();
            }
        });

        //Query 1b
        collRef.orderBy("borrowerUID", Query.Direction.ASCENDING)
                .whereEqualTo("status","Open")
                .whereLessThan("borrowerUID", uid)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                    openRequests.add(document.toObject(BorrowRequest.class));
                }
                updateRecyclerView();
            }
        });

        Button btn = rootView.findViewById(R.id.addRequest_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(),BorrowRequestCreation.class);
                startActivity(intent);
            }
        });
        return rootView;
    }

    private void updateRecyclerView(){
        Collections.sort(openRequests, new Comparator<BorrowRequest>() {
            @Override
            public int compare(BorrowRequest o1, BorrowRequest o2) {
                return o1.getCreatedDate().compareTo(o2.getCreatedDate());
            }
        });

        if (openRequests.size() > 0){
            numCheck.setText("");
        }
        RequestArrayAdaptor mData = new RequestArrayAdaptor(openRequests);
        recyclerView.setAdapter(mData);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        progress.setVisibility(View.INVISIBLE);
    }
}

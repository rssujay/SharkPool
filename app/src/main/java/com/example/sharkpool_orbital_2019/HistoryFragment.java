package com.example.sharkpool_orbital_2019;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Vector;

public class HistoryFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private RecyclerView recyclerView;
    private Vector<BorrowRequest> completedRequests = new Vector<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.history_fragment, container, false);
        recyclerView = rootView.findViewById(R.id.completedRequestsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //Populate
        CollectionReference collRef = db.collection("requests");

        //Query 1
        collRef.orderBy("createdDate", Query.Direction.ASCENDING)
                .whereEqualTo("borrowerUID",userUID)
                .whereEqualTo("status", "Completed")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    completedRequests.add(document.toObject(BorrowRequest.class));
                }
            }
        });

        //Query 2
        collRef.orderBy("createdDate", Query.Direction.ASCENDING)
                .whereEqualTo("lenderUID",userUID)
                .whereEqualTo("status", "Completed")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            completedRequests.add(document.toObject(BorrowRequest.class));
                        }

                        OngoingRequestArrayAdaptor mData = new OngoingRequestArrayAdaptor(completedRequests);
                        recyclerView.setAdapter(mData);
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                    }
                });

        return rootView;
    }
}

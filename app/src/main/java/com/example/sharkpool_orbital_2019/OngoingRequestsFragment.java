package com.example.sharkpool_orbital_2019;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Vector;

public class OngoingRequestsFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private RecyclerView recyclerView;
    private Vector<BorrowRequest> ongoingRequests = new Vector<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.requests_fragment, container, false);
        recyclerView = rootView.findViewById(R.id.OngoingRequestsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //Populate
        CollectionReference collRef = db.collection("requests");

        collRef.orderBy("createdDate", Query.Direction.ASCENDING).limit(10).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    // If BR is not completed and user is either borrower/lender
                    if (
                            !(document.get("status").equals("Completed")) &&
                            (document.get("borrowerUID").equals(userUID)||(document.get("lenderUID").equals(userUID)))) {
                        ongoingRequests.add(document.toObject(BorrowRequest.class));
                    }
                }
                OngoingRequestArrayAdaptor mData = new OngoingRequestArrayAdaptor(ongoingRequests);
                recyclerView.setAdapter(mData);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
            }
        });

        return rootView;
    }
}

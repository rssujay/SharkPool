package com.example.sharkpool_orbital_2019;

import android.content.Intent;
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
import android.widget.Button;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Vector;

public class OpenRequestsFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private RecyclerView recyclerView;
    private Vector<BorrowRequest> openRequests = new Vector<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.openrequests_fragment, container, false);
        recyclerView = rootView.findViewById(R.id.openRequestsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //Populate
        CollectionReference collRef = db.collection("requests");

        collRef.orderBy("createdDate", Query.Direction.ASCENDING).limit(5).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                    if (document.get("status").equals("Open") &&
                            !(document.get("borrowerUID").equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))) {
                        // Comment out the above and uncomment the below version for debugging until "Ongoing" works
                        // (document.get("borrowerUID").equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))) {
                        openRequests.add(document.toObject(BorrowRequest.class));
                    }
                }
                RequestArrayAdaptor mData = new RequestArrayAdaptor(openRequests);
                recyclerView.setAdapter(mData);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
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
}

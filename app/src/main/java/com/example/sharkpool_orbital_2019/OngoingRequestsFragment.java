package com.example.sharkpool_orbital_2019;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private String errorMsg = "";

    private RecyclerView recyclerView;
    private Vector<BorrowRequest> ongoingRequests = new Vector<>();
    private ProgressBar progress;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.requests_fragment, container, false);
        recyclerView = rootView.findViewById(R.id.OngoingRequestsList);
        progress = rootView.findViewById(R.id.progress_horizontal);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //Populate
        CollectionReference collRef = db.collection("requests");

        //Query 1a
        collRef.orderBy("status", Query.Direction.ASCENDING)
                .whereEqualTo("borrowerUID",userUID)
                .whereLessThan("status", "Completed")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            ongoingRequests.add(document.toObject(BorrowRequest.class));
                        }
                        updateRecyclerView();
                    }
                });

        //Query 1b
        collRef.orderBy("status", Query.Direction.ASCENDING)
                .whereEqualTo("borrowerUID",userUID)
                .whereGreaterThan("status", "Completed")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            ongoingRequests.add(document.toObject(BorrowRequest.class));
                        }
                        updateRecyclerView();
                    }
                });

        //Query 2a
        collRef.orderBy("status", Query.Direction.ASCENDING)
                .whereEqualTo("lenderUID",userUID)
                .whereLessThan("status", "Completed")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            ongoingRequests.add(document.toObject(BorrowRequest.class));
                        }
                        updateRecyclerView();
                    }
                });

        //Query 2b
        collRef.orderBy("status", Query.Direction.ASCENDING)
                .whereEqualTo("lenderUID",userUID)
                .whereGreaterThan("status", "Completed")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            ongoingRequests.add(document.toObject(BorrowRequest.class));
                        }
                        updateRecyclerView();

                        if (!errorMsg.isEmpty()){
                            Log.d("Query",errorMsg);
                        }
                    }
                });
        return rootView;
    }

    private void updateRecyclerView(){
        OngoingRequestArrayAdaptor mData = new OngoingRequestArrayAdaptor(ongoingRequests);
        recyclerView.setAdapter(mData);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        progress.setVisibility(View.INVISIBLE);
    }
}

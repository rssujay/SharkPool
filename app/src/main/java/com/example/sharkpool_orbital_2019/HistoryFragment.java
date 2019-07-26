package com.example.sharkpool_orbital_2019;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

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

public class HistoryFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private TextView numChecker;
    private RecyclerView recyclerView;
    private Vector<BorrowRequest> completedRequests = new Vector<>();
    private ProgressBar progress;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.history_fragment, container, false);
        numChecker = rootView.findViewById(R.id.numCheckCompleted);
        recyclerView = rootView.findViewById(R.id.completedRequestsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        progress = rootView.findViewById(R.id.progress_completed);

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
                updateRecyclerView();
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
                    updateRecyclerView();
                    }
                });

        return rootView;
    }

    private void updateRecyclerView(){
        OngoingRequestArrayAdaptor mData = new OngoingRequestArrayAdaptor(completedRequests);
        if (completedRequests.size() > 0){
            numChecker.setText("");
        }
        recyclerView.setAdapter(mData);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        progress.setVisibility(View.INVISIBLE);
    }
}

package com.example.sharkpool_orbital_2019;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class OngoingFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.ongoing_fragment, container, false);
        // Ongoing requests fragment configuration
        ArrayList<BorrowRequest> borrowRequests = new ArrayList<>();
        RequestArrayAdaptor requestArrayAdaptor = new RequestArrayAdaptor(this.getContext(), borrowRequests);
        ListView listView = view.findViewById(R.id.ongoingListView);
        listView.setAdapter(requestArrayAdaptor);

        Button btn = view.findViewById(R.id.addRequest_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(),BorrowRequestCreation.class);
                startActivity(intent);
            }
        });
        return view;
    }
}

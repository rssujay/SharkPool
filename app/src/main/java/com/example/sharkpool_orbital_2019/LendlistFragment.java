package com.example.sharkpool_orbital_2019;

import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class LendlistFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private CollectionReference listRef = db.collection("users").document(uid).collection("lendList");
    private TextView itemCountText;

    //Setup popup variables for adding new items
    private EditText myNewItemNameEntry;
    private AutoCompleteTextView myNewItemTypeEntry;
    private Button myNewItemSubmit;
    private ProgressBar submitBar;
    private Vector<MyItem> lendList = new Vector<>();

    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.lendlist_fragment, container, false);
        recyclerView = rootView.findViewById(R.id.lendItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        itemCountText = rootView.findViewById(R.id.itemCount);
        Button addNewItem = rootView.findViewById(R.id.addItem);
        addNewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemPopup(v);
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                lendList.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    lendList.add(document.toObject(MyItem.class));
                }
                MyAdapter mData = new MyAdapter(lendList);
                Integer itemCount = mData.getItemCount();
                String updateCount = "Number of items in your lending list: ".concat(itemCount.toString());
                itemCountText.setText(updateCount);

                recyclerView.setAdapter(mData);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
            }
        });
    }

    public void ItemPopup(View temp) {
        LayoutInflater inflater = getLayoutInflater();
        View popupView = inflater.inflate(R.layout.addmyitempopup_layout, null);

        int width = RelativeLayout.LayoutParams.WRAP_CONTENT;
        int height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(20);
        }

        popupWindow.showAtLocation(temp, Gravity.CENTER, 0, 0);

        myNewItemNameEntry = popupView.findViewById(R.id.myItemNameEntry);
        myNewItemTypeEntry = popupView.findViewById(R.id.myItemCustomTypeEntry);
        myNewItemSubmit = popupView.findViewById(R.id.myItemCreateBtn);
        submitBar = popupView.findViewById(R.id.indeterminateBar);
        populateOptions();

        myNewItemSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitBar.setVisibility(View.VISIBLE);
                final MyItem newItem = new MyItem();
                final String itemName = myNewItemNameEntry.getText().toString();
                final String itemType = myNewItemTypeEntry.getText().toString();

                if (itemName.isEmpty() || itemType.isEmpty()) {
                    popupWindow.dismiss();
                    submitBar.setVisibility(View.INVISIBLE);
                    return;
                }

                newItem.initialize(itemName, itemType);
                db.collection("users").document(uid).collection("lendList").document(newItem.getLenditemID()).set(newItem)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Map<String, Boolean> temp = new HashMap<>();
                                temp.put("exists", true);

                                db.collection("itemTypes").document(itemType).set(temp).addOnSuccessListener(
                                        new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                submitBar.setVisibility(View.INVISIBLE);
                                                popupWindow.dismiss();
                                            }
                                        });
                            }
                        });
            }
        }
        );
    }

    private void populateOptions(){
        CollectionReference collRef = db.collection("itemTypes");

        collRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<String> types = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots){
                    types.add(document.getId());
                }

                ArrayAdapter options = new ArrayAdapter<>
                        (getContext(), R.layout.support_simple_spinner_dropdown_item, types);
                myNewItemTypeEntry.setAdapter(options);
            }})
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error, please reload", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

package com.example.sharkpool_orbital_2019;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Vector;

public class NotifAdapter extends RecyclerView.Adapter<NotifAdapter.MyViewHolder> {
    private  Vector<NotificationObject> mDataset;

    protected static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView notificationImage;
        public TextView notificationTitle;
        public TextView notificationBody;

        public MyViewHolder(View v) {
            super(v);

            notificationImage = v.findViewById(R.id.notificationImage);
            notificationTitle = v.findViewById(R.id.notificationTitle);
            notificationBody = v.findViewById(R.id.notificationBody);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public NotifAdapter(Vector<NotificationObject> myDataset) {
        mDataset = myDataset;
    }


    // Create new views (invoked by the layout manager)
    @Override
    public NotifAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notifobject_layout, parent, false);

        return new MyViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.notificationTitle.setText(mDataset.elementAt(position).getNotifTitle());
        holder.notificationBody.setText(mDataset.elementAt(position).getNotifBody());

        if (mDataset.elementAt(position).getBrID().isEmpty()){
            holder.notificationImage.setImageResource(R.drawable.shape_bg_incoming_bubble);
        }

        /*
        holder.itemView.setOnTouchListener(new OnSwipeTouchListener(holder.itemView.getContext()){
            @Override
            public void onSwipeLeft() {
                holder.notificationBody.setText("Swipe Left");
                DBupdate(mDataset.elementAt(position).getNotificationUUID());

            }

            @Override
            public void onSwipeRight() {
                holder.notificationBody.setText("Swipe Right");
                DBupdate(mDataset.elementAt(position).getNotificationUUID());
            }
        });
        */

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                holder.notificationBody.setText("Long click");
                DBupdate(mDataset.elementAt(position).getNotificationUUID());
                return false;
            }
        });
    }

    public void DBupdate(String docID){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference countRef = db.collection("users").document(uid);

        countRef.update("foregroundNotifications", FieldValue.increment(-1));
        countRef.collection("notificationsList").document(docID).delete();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

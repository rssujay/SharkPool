package com.example.sharkpool_orbital_2019;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Vector;

public class NotifAdapter extends RecyclerView.Adapter<NotifAdapter.MyViewHolder> {
    public Vector<NotificationObject> mDataset;
    private Context context;

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
    @NonNull
    @Override
    public NotifAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notifobject_layout, parent, false);

        return new MyViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder( @NonNull final MyViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.notificationTitle.setText(mDataset.elementAt(position).getNotifTitle());
        holder.notificationBody.setText(mDataset.elementAt(position).getNotifBody());

        final boolean isSendBird = (mDataset.elementAt(position).getBrID().isEmpty());

        if (isSendBird){
            holder.notificationImage.setImageResource(R.drawable.ic_menu_send);
        }
        else{
            holder.notificationImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            holder.notificationImage.setImageResource(R.drawable.credits);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBupdate(mDataset.elementAt(position).getNotificationUUID());
                if (isSendBird){
                    Intent intent = new Intent(context, ChatActivity.class);
                    String otherID = mDataset.elementAt(position).getOtherID();
                    intent.putExtra("otherID", otherID);
                    context.startActivity(intent);
                }
                else{
                    Intent intent = new Intent(context, BRview.class);
                    intent.putExtra("initiator", mDataset.elementAt(position).getBrID());
                    context.startActivity(intent);
                }
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

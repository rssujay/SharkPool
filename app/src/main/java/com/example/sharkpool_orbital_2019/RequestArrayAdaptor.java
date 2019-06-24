package com.example.sharkpool_orbital_2019;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Vector;

public class RequestArrayAdaptor extends RecyclerView.Adapter<RequestArrayAdaptor.MyViewHolder>{
    private Vector<BorrowRequest> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    protected static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView borrowerName;
        public TextView creditVal;
        public TextView itemName;
        public TextView itemType;
        public TextView creationDate;

        public MyViewHolder(View v) {
            super(v);

            itemName = v.findViewById(R.id.ItemName);
            itemType = v.findViewById(R.id.ItemType);
            borrowerName = v.findViewById(R.id.borrowerName);
            creditVal = v.findViewById(R.id.creditVal);
            creationDate = v.findViewById(R.id.creationDate);

        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RequestArrayAdaptor(Vector<BorrowRequest> myDataset) {
        mDataset = myDataset;
    }


    // Create new views (invoked by the layout manager)
    @Override
    public RequestArrayAdaptor.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.openrequest_layout, null);


        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RequestArrayAdaptor.MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.itemName.setText(mDataset.elementAt(position).getItemName());
        holder.itemType.setText(mDataset.elementAt(position).getItemType());
        holder.creationDate.append(mDataset.elementAt(position).getCreatedDate().toString());

        Integer borrowerCredits =  mDataset.elementAt(position).getBorrowerCredits();
        holder.creditVal.setText(borrowerCredits.toString());

        String borrowerName = mDataset.elementAt(position).getBorrowerName();
        holder.borrowerName.setText(borrowerName + holder.borrowerName.getText());

        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.setBackgroundResource(R.color.fui_buttonShadow);
                return false;
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
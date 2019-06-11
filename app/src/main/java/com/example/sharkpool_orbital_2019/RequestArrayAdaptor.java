package com.example.sharkpool_orbital_2019;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class RequestArrayAdaptor extends ArrayAdapter {

    public RequestArrayAdaptor(Context context, ArrayList<BorrowRequest> requests){
        super(context, 0 , requests);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        BorrowRequest borrowRequest = (BorrowRequest) getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_request, parent, false);
        }
        TextView itemType = (TextView) convertView.findViewById(R.id.itemType);
        itemType.setText(borrowRequest.getItemType());

        return convertView;
    }
}

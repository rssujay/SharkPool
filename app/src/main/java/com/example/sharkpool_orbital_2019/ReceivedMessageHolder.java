package com.example.sharkpool_orbital_2019;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.sendbird.android.UserMessage;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ReceivedMessageHolder extends RecyclerView.ViewHolder {
    private TextView messageText, timeText;

    ReceivedMessageHolder(View itemView) {
        super(itemView);
        messageText = (TextView) itemView.findViewById(R.id.text_message_body);
        timeText = (TextView) itemView.findViewById(R.id.text_message_time);
    }

    void bind(UserMessage message) {
        messageText.setText(message.getMessage());

        // Format the stored timestamp into a readable String using method.
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        timeText.setText(dateFormat.format(message.getCreatedAt()));
    }
}
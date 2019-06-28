package com.example.sharkpool_orbital_2019;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    final String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        final String otherUID = bundle.getString("otherID");
        List<String> ChatUIDs = new ArrayList<>();
        ChatUIDs.add(userUID);
        ChatUIDs.add(otherUID);
        //Double-checks whether user is connected to SendBird

        if (SendBird.getConnectionState() != SendBird.ConnectionState.OPEN){
            SendBird.connect(FirebaseAuth.getInstance().getUid(), new SendBird.ConnectHandler() {
                @Override
                public void onConnected(User user, SendBirdException e) {
                    if (e != null){ //error
                        return; //Toast.makeText(getApplicationContext(), "Failed to connect to messaging service. Check your connection and try again.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        createGroupChannel(ChatUIDs); //creates 1-on-1 channel; goes to previously created one if already created
        //Temporary test message to test for channel creation
        Toast.makeText(getApplicationContext(), "Channel successfully created!", Toast.LENGTH_LONG).show();

        //TODO: retrieving list of messages

        setContentView(R.layout.activity_chat);
        mMessageRecycler = findViewById(R.id.reyclerview_message_list);
       // mMessageAdapter = new MessageListAdapter(this, messageList); //TODO
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));

    }

    private void createGroupChannel(List<String> ChatUIDs){
        GroupChannel.createChannelWithUserIds(ChatUIDs, true, new GroupChannel.GroupChannelCreateHandler() {
            @Override
            public void onResult(GroupChannel groupChannel, SendBirdException e) {
                if (e != null) {
                    // Error!
                    return;
                }

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                //finish();
            }
        });
    }
}

package com.example.sharkpool_orbital_2019;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    final String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public String mChannelURL = "default";

    private RecyclerView mMessageRecycler;
    private ChatAdapter mMessageAdapter;
    private LinearLayoutManager mLayoutManager;

    private Button sendButton;
    private EditText chatbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        final String otherUID = bundle.getString("otherID");
        List<String> ChatUIDs = new ArrayList<>();
        ChatUIDs.add(userUID);
        ChatUIDs.add(otherUID);

        setContentView(R.layout.activity_chat);
        mMessageRecycler = findViewById(R.id.recycler_chat);
        sendButton = findViewById(R.id.button_chatbox_send);
        chatbox = findViewById(R.id.edittext_chatbox);

        //Double-checks whether user is connected to SendBird

        if (SendBird.getConnectionState() != SendBird.ConnectionState.OPEN){
            SendBird.connect(FirebaseAuth.getInstance().getUid(), new SendBird.ConnectHandler() {
                @Override
                public void onConnected(User user, SendBirdException e) {
                    if (e != null){ //error
                        Toast.makeText(getApplicationContext(), "Failed to connect to messaging service. Check your connection and try again.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            });
        }

        createGroupChannel(ChatUIDs); //creates 1-on-1 channel; goes to previously created one if already created

        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mMessageRecycler.setLayoutManager(mLayoutManager);

        //mChannelURL = "sendbird_group_channel_124131094_8135440ea76a101be52fd7d3a7e4c8018e788081";
        /*
        oof = (mmChannelURL.equals(mChannelURL)) ? "Same" : "Not Same: " + mChannelURL;
        Log.d("URL_error", oof);
        */
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                GroupChannel.getChannel(mChannelURL, new GroupChannel.GroupChannelGetHandler() {
                    @Override
                    public void onResult(GroupChannel groupChannel, SendBirdException e) {
                        if (e != null) { //error
                            Toast.makeText(getApplicationContext(), "Channel getting error!", Toast.LENGTH_LONG).show();
                            Log.d("URL_error", "Error: " + mChannelURL);
                            return;
                        }
                        mMessageAdapter = new ChatAdapter(groupChannel);
                        mMessageRecycler.setAdapter(mMessageAdapter);
                /*
                groupChannel.sendUserMessage("hello there", new BaseChannel.SendUserMessageHandler() {
                    @Override
                    public void onSent(UserMessage userMessage, SendBirdException e) {
                        if (e != null) { //error
                            Toast.makeText(getApplicationContext(), "Message sending error!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        Toast.makeText(getApplicationContext(), "Message sending success!", Toast.LENGTH_SHORT).show();
                    }
                });
                */
                    }
                });
            }
        }, 1000);

        mMessageRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            //@Nullable
            @Override
            public void onScrollStateChanged(@Nullable RecyclerView recyclerView, int newState) {
                if (mLayoutManager.findLastVisibleItemPosition() == (mMessageAdapter.getItemCount() - 1)) {
                    mMessageAdapter.loadPreviousMessages();
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = chatbox.getText().toString();
                chatbox.setText("");
                mMessageAdapter.sendMessage(message);
                mMessageAdapter.refresh();
            }
        });



    }

    private void createGroupChannel(List<String> ChatUIDs){

        GroupChannel.createChannelWithUserIds(ChatUIDs, true, new GroupChannel.GroupChannelCreateHandler() {
            @Override
            public void onResult(final GroupChannel groupChannel, SendBirdException e){
                if (e != null) {
                    // Error!
                    Log.d("URL_error", "An unknown error occurred.");
                    return;
                }
                mChannelURL = groupChannel.getUrl().trim();
                Log.d("URL_error", "URL saved as " + mChannelURL);
                //mmChannelURL = "sendbird_group_channel_124131094_8135440ea76a101be52fd7d3a7e4c8018e788081";
                //oof = (mmChannelURL.equals(mChannelURL)) ? "Same" :"Not Same";
                //Log.d("URL_error", oof);
            }
        });
    }

    private class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int VIEW_TYPE_MESSAGE_SENT = 1;
        private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

        private ArrayList<BaseMessage> mMessageList;
        private GroupChannel mChannel;

        ChatAdapter(GroupChannel channel) {
            mMessageList = new ArrayList<>();
            mChannel = channel;

            refresh();
        }

        // Retrieves 30 most recent messages.
        void refresh() {
            mChannel.getPreviousMessagesByTimestamp(Long.MAX_VALUE, true, 30, true,
                    BaseChannel.MessageTypeFilter.USER, null, new BaseChannel.GetMessagesHandler() {
                        @Override
                        public void onResult(List<BaseMessage> list, SendBirdException e) {
                            if (e != null) {
                                e.printStackTrace();
                                return;
                            }
                            mMessageList = (ArrayList<BaseMessage>) list;

                            notifyDataSetChanged();

                        }
                    });

        }

        void loadPreviousMessages() {
            final long lastTimestamp = mMessageList.get(mMessageList.size() - 1).getCreatedAt();
            mChannel.getPreviousMessagesByTimestamp(lastTimestamp, false, 30, true,
                    BaseChannel.MessageTypeFilter.USER, null, new BaseChannel.GetMessagesHandler() {
                        @Override
                        public void onResult(List<BaseMessage> list, SendBirdException e) {
                            if (e != null) {
                                e.printStackTrace();
                                return;
                            }
                            mMessageList.addAll(list);

                            notifyDataSetChanged();
                        }
                    });
        }

        // Appends a new message to the beginning of the message list.
        void appendMessage(UserMessage message) {
            mMessageList.add(0, message);
            notifyDataSetChanged();
        }

        // Sends a new message, and appends the sent message to the beginning of the message list.
        void sendMessage(final String message) {
            mChannel.sendUserMessage(message, new BaseChannel.SendUserMessageHandler() {
                @Override
                public void onSent(UserMessage userMessage, SendBirdException e) {
                    if (e != null) {
                        e.printStackTrace();
                        return;
                    }

                    mMessageList.add(0, userMessage);
                    notifyDataSetChanged();
                }
            });
        }


        // Determines the appropriate ViewType according to the sender of the message.
        @Override
        public int getItemViewType(int position) {
            UserMessage message = (UserMessage) mMessageList.get(position);

            if (message.getSender().getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                // If the current user is the sender of the message
                return VIEW_TYPE_MESSAGE_SENT;
            } else {
                // If some other user sent the message
                return VIEW_TYPE_MESSAGE_RECEIVED;
            }
        }

        // Inflates the appropriate layout according to the ViewType.
        @Nullable
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;

            if (viewType == VIEW_TYPE_MESSAGE_SENT) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_sent, parent, false);
                return new SentMessageHolder(view);
            } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_received, parent, false);
                return new ReceivedMessageHolder(view);
            }

            return null;
        }

        // Passes the message object to a ViewHolder so that the contents can be bound to UI.
        @Nullable
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            UserMessage message = (UserMessage) mMessageList.get(position);

            switch (holder.getItemViewType()) {
                case VIEW_TYPE_MESSAGE_SENT:
                    ((SentMessageHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_MESSAGE_RECEIVED:
                    ((ReceivedMessageHolder) holder).bind(message);
            }
        }

        @Override
        public int getItemCount() {
            return mMessageList.size();
        }

        // Messages sent by me do not display a profile image or nickname.
        private class SentMessageHolder extends RecyclerView.ViewHolder {
            TextView messageText, timeText;

            SentMessageHolder(View itemView) {
                super(itemView);

                messageText = (TextView) itemView.findViewById(R.id.text_message_body_sent);
                timeText = (TextView) itemView.findViewById(R.id.text_message_time_sent);
            }

            void bind(UserMessage message) {
                messageText.setText(message.getMessage());

                // Format the stored timestamp into a readable String using method.
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                timeText.setText(dateFormat.format(message.getCreatedAt()));
            }
        }

        // Messages sent by others display a profile image and nickname.
        private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
            TextView messageText, timeText;

            ReceivedMessageHolder(View itemView) {
                super(itemView);

                messageText = (TextView) itemView.findViewById(R.id.text_message_body);
                timeText = (TextView) itemView.findViewById(R.id.text_message_time);
            }

            void bind(UserMessage message) {
                messageText.setText(message.getMessage());
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                timeText.setText(dateFormat.format(message.getCreatedAt()));
            }
        }


    }
}

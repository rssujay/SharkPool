package com.example.sharkpool_orbital_2019;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.BaseMessageParams;
import com.sendbird.android.FileMessage;
import com.sendbird.android.FileMessageParams;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ChatActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    final String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public String mChannelURL = "default";
    static final int REQUEST_TAKE_PHOTO = 1;
    String currentPhotoPath;

    private RecyclerView mMessageRecycler;
    private ChatAdapter mMessageAdapter;
    private LinearLayoutManager mLayoutManager;

    private Button sendButton;
    private Button cameraButton;
    private EditText chatbox;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap mImageBitmap;

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
        cameraButton = findViewById(R.id.button_chatbox_cam);
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
                    FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(ChatActivity.this, new OnSuccessListener<InstanceIdResult>() {
                        @Override
                        public void onSuccess(InstanceIdResult instanceIdResult) {
                            SendBird.registerPushTokenForCurrentUser(instanceIdResult.getToken(), new SendBird.RegisterPushTokenWithStatusHandler() {
                                @Override
                                public void onRegistered(SendBird.PushTokenRegistrationStatus status, SendBirdException e) {
                                    if (e != null) {        // Error.
                                        Log.d("SendBirdPush", "Token creation failure");
                                        return;
                                    }
                                    Log.d("SendBirdPush", "Token creation success");
                                }
                            });
                        }
                    });
                }
            });
        }

        createGroupChannel(ChatUIDs); //creates 1-on-1 channel; goes to previously created one if already created

        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mMessageRecycler.setLayoutManager(mLayoutManager);

        //Delay for URL to be updated before channel is accessed

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
                    }
                });
            }
        }, 1500);

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

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error in file creation
                    }
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(getBaseContext(),
                                "com.example.android.fileprovider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    }
                }
            }

        });

    }

    @Override
    public void onBackPressed(){
        SendBird.disconnect(new SendBird.DisconnectHandler() {
            @Override
            public void onDisconnected() {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { // image retrieval
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                mImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(currentPhotoPath));
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                mImageBitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
                byte[] bitmapData = bos.toByteArray();
                final File imageFile = new File(getBaseContext().getCacheDir(), "Image");
                FileOutputStream fos = new FileOutputStream(imageFile);
                fos.write(bitmapData);
                fos.flush();
                try { if (fos != null) fos.close(); Log.d("SendBirdImage", "fos closed"); } catch(IOException e){ e.printStackTrace(); }
                if (SendBird.getConnectionState() != SendBird.ConnectionState.OPEN){
                    SendBird.connect(FirebaseAuth.getInstance().getUid(), new SendBird.ConnectHandler() {
                        @Override
                        public void onConnected(User user, SendBirdException e) {
                                mMessageAdapter.sendImage(imageFile);
                        }
                    });
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(currentPhotoPath);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            getBaseContext().sendBroadcast(mediaScanIntent);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = "file:"+image.getAbsolutePath();
        return image;
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
            }
        });
    }

    private class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int VIEW_TYPE_MESSAGE_SENT = 1;
        private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
        private static final int VIEW_TYPE_IMAGE_SENT = 3;
        private static final int VIEW_TYPE_IMAGE_RECEIVED = 4;

        private ArrayList<BaseMessage> mMessageList;
        private GroupChannel mChannel;

        ChatAdapter(GroupChannel channel) {
            mMessageList = new ArrayList<>();
            mChannel = channel;

            refresh();
            Timer mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    refresh();
                }
            }, 1000, 500);
        }

        // Retrieves 30 most recent messages.
        void refresh() {
            mChannel.getPreviousMessagesByTimestamp(Long.MAX_VALUE, true, 30, true,
                    BaseChannel.MessageTypeFilter.ALL, null, new BaseChannel.GetMessagesHandler() {
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
                    BaseChannel.MessageTypeFilter.ALL, null, new BaseChannel.GetMessagesHandler() {
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

        void sendImage(final File image) {

            List<FileMessage.ThumbnailSize> thumbnailSizes = new ArrayList<>();
            thumbnailSizes.add(new FileMessage.ThumbnailSize(240, 240));

            FileMessageParams params = new FileMessageParams()
                    .setFile(image)
                    .setFileName("Image")
                    .setThumbnailSizes(thumbnailSizes)
                    .setFileSize((int) image.length()/1024)
                    .setPushNotificationDeliveryOption(BaseMessageParams.PushNotificationDeliveryOption.DEFAULT);

            mChannel.sendFileMessage(params, new BaseChannel.SendFileMessageHandler() {
                @Override
                public void onSent(FileMessage fileMessage, SendBirdException e) {
                    if (e != null) {
                        e.printStackTrace();
                        return;
                    }

                    mMessageList.add(0, fileMessage);
                    notifyDataSetChanged();
                    refresh();
                }
            });
        }


        // Determines the appropriate ViewType according to the sender of the message.
        @Override
        public int getItemViewType(int position) {
            BaseMessage message = (BaseMessage) mMessageList.get(position);

            if (message instanceof UserMessage){

                UserMessage userMessage = (UserMessage) message;

                if (userMessage.getSender().getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                    // If the current user is the sender of the message
                    return VIEW_TYPE_MESSAGE_SENT;
                } else {
                    // If some other user sent the message
                    return VIEW_TYPE_MESSAGE_RECEIVED;
                }

            } else if (message instanceof FileMessage){

                FileMessage fileMessage = (FileMessage) message;

                if (fileMessage.getType().toLowerCase().startsWith("image")) {
                    if (fileMessage.getSender().getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                        return VIEW_TYPE_IMAGE_SENT;
                    } else {
                        return VIEW_TYPE_IMAGE_RECEIVED;
                    }
                }
            }
            return -1;
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
            } else if (viewType == VIEW_TYPE_IMAGE_SENT) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_image_sent, parent, false);
                return new SentImageHolder(view);
            } else if (viewType == VIEW_TYPE_IMAGE_RECEIVED) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_image_received, parent, false);
                return new ReceivedImageHolder(view);
            }
            return null;
        }

        // Passes the message object to a ViewHolder so that the contents can be bound to UI.
        @Nullable
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            BaseMessage message = (BaseMessage) mMessageList.get(position);

            switch (holder.getItemViewType()) {
                case VIEW_TYPE_MESSAGE_SENT:
                    ((SentMessageHolder) holder).bind((UserMessage) message);
                    break;
                case VIEW_TYPE_MESSAGE_RECEIVED:
                    ((ReceivedMessageHolder) holder).bind((UserMessage) message);
                    break;
                case VIEW_TYPE_IMAGE_SENT:
                    ((SentImageHolder) holder).bind((FileMessage) message);
                    break;
                case VIEW_TYPE_IMAGE_RECEIVED:
                    ((ReceivedImageHolder) holder).bind((FileMessage) message);
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

        private class SentImageHolder extends RecyclerView.ViewHolder {
            TextView timeText;
            final ImageView image;

            SentImageHolder(View itemView) {
                super(itemView);

                image = (ImageView) itemView.findViewById(R.id.image_body_sent);
                timeText = (TextView) itemView.findViewById(R.id.image_time_sent);

            }

            void bind(FileMessage message) {

                Glide.with(getBaseContext())
                        .load(message.getUrl())
                        .asBitmap()
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(null)
                        .placeholder(R.drawable.sharkpool_trans)
                        .into(image);


                if (Build.VERSION.SDK_INT >=21 ) {
                    image.setClipToOutline(true);
                }


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

        private class ReceivedImageHolder extends RecyclerView.ViewHolder {
            TextView timeText;
            ImageView image;

            ReceivedImageHolder(View itemView) {
                super(itemView);

                image = (ImageView) itemView.findViewById(R.id.image_body_received);
                timeText = (TextView) itemView.findViewById(R.id.image_time_received);

            }

            void bind(FileMessage message) {

                Glide.with(getBaseContext())
                        .load(message.getUrl())
                        .asBitmap()
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(null)
                        .placeholder(R.drawable.sharkpool_trans)
                        .into(image);


                if (Build.VERSION.SDK_INT >=21 ) {
                    image.setClipToOutline(true);
                }


                // Format the stored timestamp into a readable String using method.
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                timeText.setText(dateFormat.format(message.getCreatedAt()));
            }
        }
    }
}

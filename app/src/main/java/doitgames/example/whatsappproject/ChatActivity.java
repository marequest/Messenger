package doitgames.example.whatsappproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import doitgames.example.whatsappproject.Chat.MediaAdapter;
import doitgames.example.whatsappproject.Chat.MessageAdapter;
import doitgames.example.whatsappproject.Chat.ChatObject;
import doitgames.example.whatsappproject.Chat.MessageAdapterTest;
import doitgames.example.whatsappproject.Chat.MessageObject;
import doitgames.example.whatsappproject.User.UserObject;
import doitgames.example.whatsappproject.Utils.SendNotification;

import static java.text.DateFormat.getDateTimeInstance;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";

    RecyclerView mChatList, mMedia;
    RecyclerView.Adapter mChatListAdapter, mMediaAdapter;
    LinearLayoutManager mChatListLayoutManager, mMediaLayoutManager;

    ArrayList<MessageObject> messageList;

    ChatObject mChatObject;

    DatabaseReference mChatMessagesDb;

    EditText mMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_chat_test);

        mChatObject = (ChatObject) getIntent().getSerializableExtra("chatObject");

        Toolbar myToolbar = findViewById(R.id.toolbarChat);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mChatObject.getName());

        mChatMessagesDb = FirebaseDatabase.getInstance().getReference().child("chat").child(mChatObject.getChatId()).child("messages");

        ImageButton mSend = findViewById(R.id.verifyCode);
        ImageButton mAddMedia = findViewById(R.id.addMedia);
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        mAddMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        initializeMessageRecyclerView();
        initializeMediaRecyclerView();
        getChatMessages();
    }

    private void getChatMessages() {
        mChatMessagesDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    String text = "", creatorID = "", timestamp = "";

                    ArrayList<String> mediaUrlList = new ArrayList<>();

                    if(dataSnapshot.child("text").getValue() != null){
                        text = dataSnapshot.child("text").getValue().toString();
                    }
                    if(dataSnapshot.child("creator").getValue() != null){
                        creatorID = dataSnapshot.child("creator").getValue().toString();
                    }
                    if(dataSnapshot.child("timestamp").getValue() != null){
                        timestamp = dataSnapshot.child("timestamp").getValue().toString();
                    }

                    if(dataSnapshot.child("media").getChildrenCount() > 0){
                        for(DataSnapshot mediaSnapshot : dataSnapshot.child("media").getChildren()){
                            mediaUrlList.add(mediaSnapshot.getValue().toString());
                        }
                    }

                    final MessageObject mMessage = new MessageObject(dataSnapshot.getKey(), creatorID, text, mediaUrlList, timestamp);
                    messageList.add(mMessage);
                    mChatListAdapter.notifyDataSetChanged();
                    mChatListLayoutManager.scrollToPosition(messageList.size() - 1);
                }   
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    int totalMediaUploaded = 0;
    ArrayList<String> mediaIdList = new ArrayList<>();
    private void sendMessage(){
        mMessage = findViewById(R.id.messageEt);
        String messageId = mChatMessagesDb.push().getKey();
        final DatabaseReference newMessageDb = mChatMessagesDb.child(messageId);

        final Map newMessageMap = new HashMap<>();

        newMessageMap.put("creator", FirebaseAuth.getInstance().getUid());
        newMessageMap.put("timestamp", ServerValue.TIMESTAMP);

        if(!mMessage.getText().toString().isEmpty()) {
            newMessageMap.put("text", mMessage.getText().toString());
        }

        if(!mediaUriList.isEmpty()){
            for(String mediaUri : mediaUriList){
                final String mediaId = newMessageDb.child("media").push().getKey();
                mediaIdList.add(mediaId);
                final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("chat").child(mChatObject.getChatId()).child(messageId).child(mediaId);

                UploadTask uploadTask = filePath.putFile(Uri.parse(mediaUri));

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                newMessageMap.put("/media/" + mediaIdList.get(totalMediaUploaded) + "/", uri.toString());
                                totalMediaUploaded++;
                                if(totalMediaUploaded == mediaUriList.size()){//we successfully uploaded all images
                                    updateDatabaseWithNewMessage(newMessageDb, newMessageMap);
                                }
                            }
                        });
                    }
                });
            }
        } else if(!mMessage.getText().toString().isEmpty()){
            updateDatabaseWithNewMessage(newMessageDb, newMessageMap);
        }

        mMessage.setText(null);
        mMedia.setAdapter(null);
    }

    private void updateDatabaseWithNewMessage(DatabaseReference newMessageDb, Map newMessageMap){
        newMessageDb.updateChildren(newMessageMap);
        mediaUriList.clear();
        mediaIdList.clear();
        mMedia.setAdapter(mMediaAdapter);
        mMediaAdapter.notifyDataSetChanged();

        //Update database notification with +1 for every user - START
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("chat").child(mChatObject.getChatId()).child("info").child("users");
        usersRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(!dataSnapshot.getKey().equals(FirebaseAuth.getInstance().getUid())){
                    int value = 0;
                    if(dataSnapshot.child("numOfNotifications").getValue() != null ){
                        value = Integer.valueOf(dataSnapshot.child("numOfNotifications").getValue().toString());
                    }
                    value++;
                    dataSnapshot.child("numOfNotifications").getRef().setValue(String.valueOf(value));
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //Update database notification with +1 for every user - END

        //Send Notification - START
        String message;
        if(newMessageMap.get("text") != null){
            message = newMessageMap.get("text").toString();
        }
        else{
            message = "Sent Media";
        }
        for(UserObject userObject : mChatObject.getUserObjectArrayList()){
            if(!userObject.getUid().equals(FirebaseAuth.getInstance().getUid())){
                new SendNotification(message, "New Message", userObject.getNotificationKey(), mChatObject.getName());
            }
        }
        //Send Notification - END
    }

    private void initializeMessageRecyclerView() {
        messageList = new ArrayList<>();
        mChatList = findViewById(R.id.messageList);
        //mChatList.addItemDecoration(new DividerItemDecoration(mChatList.getContext(), DividerItemDecoration.VERTICAL));
        mChatList.setHasFixedSize(false);
        mChatList.setNestedScrollingEnabled(false);
        mChatListLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mChatList.setLayoutManager(mChatListLayoutManager);
        mChatListAdapter = new MessageAdapterTest(messageList, mChatObject.getChatId());
        mChatList.clearAnimation();
        mChatList.setAdapter(mChatListAdapter);
    }

    static int PICK_IMAGE_INTENT = 1;
    ArrayList<String> mediaUriList;
    private void initializeMediaRecyclerView() {
        mediaUriList = new ArrayList<>();
        mMedia = findViewById(R.id.mediaList);
        mMedia.addItemDecoration(new DividerItemDecoration(mMedia.getContext(), DividerItemDecoration.HORIZONTAL));
        mMedia.setNestedScrollingEnabled(false);
        mMedia.setHasFixedSize(false);
        mMediaLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        mMedia.setLayoutManager(mMediaLayoutManager);
        mMediaAdapter = new MediaAdapter(getApplicationContext(), mediaUriList);
        mMedia.setAdapter(mMediaAdapter);
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Picture(s)"), PICK_IMAGE_INTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == PICK_IMAGE_INTENT){
                if(data.getClipData() == null){ //User picked only one image
                    mediaUriList.add(data.getData().toString());
                }
                else {
                    for(int i = 0; i < data.getClipData().getItemCount(); i++){
                        mediaUriList.add(data.getClipData().getItemAt(i).getUri().toString());
                    }
                }
                mMediaAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.view_users_menu_item) {
            Intent intent = new Intent(getApplicationContext(), ViewUsersActivity.class);
            intent.putExtra("chatObject2", mChatObject);
            startActivity(intent);
            return true;
        } else if(id == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        removeNotifications();
    }

    @Override
    protected void onStop() {
        removeNotifications();
        super.onStop();
    }

    private void removeNotifications() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("chat").child(mChatObject.getChatId()).child("info").child("users").child(FirebaseAuth.getInstance().getUid());
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataSnapshot.child("numOfNotifications").getRef().setValue("0");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

package doitgames.example.whatsappproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import doitgames.example.whatsappproject.User.UserListAdapter;
import doitgames.example.whatsappproject.User.UserObject;
import doitgames.example.whatsappproject.Utils.CountryToPhonePrefix;

public class FindUserActivity extends AppCompatActivity {
    private static final String TAG = "FindUserActivity";

    RecyclerView mUserList;
    UserListAdapter mUserListAdapter;
    RecyclerView.LayoutManager mUserListLayoutManager;

    ArrayList<UserObject> userList, contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_user);

        Toolbar myToolbar = findViewById(R.id.toolbarFindUser);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Create chat room");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userList = new ArrayList<>();
        contactList = new ArrayList<>();

        Button mCreateRoom = findViewById(R.id.createChatRoom);
        mCreateRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createChat();
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted
            initializeRecyclerView();
            getContactList();
        } else {
            requestPermissions();
        }


    }


    private void requestPermissions() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission_group.CONTACTS)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed to add your contacts to your friend list.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(FindUserActivity.this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, 1);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, 1);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
                initializeRecyclerView();
                getContactList();
            } else {
                //Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createChat(){
        EditText chatRoomName = findViewById(R.id.chatRoomName);
        if(chatRoomName.getText().toString().equals("")){
            Toast.makeText(this, "You must enter chat room name", Toast.LENGTH_LONG).show();
            return;
        }
        String key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();
        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("user");
        DatabaseReference chatInfoDb = FirebaseDatabase.getInstance().getReference().child("chat").child(key).child("info");

        HashMap newChatMap = new HashMap();
        newChatMap.put("id", key);
        newChatMap.put("name", chatRoomName.getText().toString());
        newChatMap.put("users/" + FirebaseAuth.getInstance().getUid(), true);
        updateDatabase(userDb, FirebaseAuth.getInstance().getUid(), key);

        Boolean validChat = false;
        for(UserObject userObject : userList){
            if(userObject.getSelected() && !userObject.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                validChat = true;
                newChatMap.put("users/" + userObject.getUid(), true); //4:51:35
                userDb.child(userObject.getUid()).child("chat").child(key).setValue("true");
                updateDatabase(userDb, userObject.getUid(), key);
            }
        }
        if(validChat) {
            chatInfoDb.updateChildren(newChatMap);
            userDb.child(FirebaseAuth.getInstance().getUid()).child("chat").child(key).setValue("true");
            //Upali taj chat
            finish();
        }
    }

    void updateDatabase(DatabaseReference userDb, final String uId, final String key){
        userDb.child(uId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    final String name = dataSnapshot.child("name").getValue().toString();
                    final String phone = dataSnapshot.child("phone").getValue().toString();
                    DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("chat").child(key).child("info").child("users");
                    mUserDB.child(uId).child("name").setValue(name);
                    mUserDB.child(uId).child("phone").setValue(phone);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getContactList(){
        String ISOPrefix = getCountryISO();
        Cursor phonesCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null ,null);
        while(phonesCursor.moveToNext()){
            String currName = phonesCursor.getString(phonesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String currPhone = phonesCursor.getString(phonesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            currPhone = currPhone.replace(" ", "");
            currPhone = currPhone.replace("-", "");
            currPhone = currPhone.replace("(", "");
            currPhone = currPhone.replace(")", "");
            if(!String.valueOf(currPhone.charAt(0)).equals("+")){
                if(String.valueOf(currPhone.charAt(0)).equals("0")){
                    currPhone = currPhone.substring(1, currPhone.length());
                }
                currPhone = ISOPrefix + currPhone;
            }
            /** Уређивање броја у читљив облик **/
            currPhone = ISOPrefix + currPhone;

            UserObject contact = new UserObject(currName, currPhone);
            contactList.add(contact);
            Log.d(TAG, "currName = " + currName + ", currPhone = " + currPhone);

            getUserDetails(contact);
        }
    }

    private void getUserDetails(UserObject contact) {
        final DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("user");
        Query query = mUserDB.orderByChild("phone").equalTo(contact.getPhone());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String phone = "", name = "";
                    for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                        if(childSnapshot.child("phone").getValue() != null){
                            phone = childSnapshot.child("phone").getValue().toString();
                        }/*
                        if(childSnapshot.child("name").getValue() != null){
                            name = childSnapshot.child("name").getValue().toString();
                        }*/
                        name = getContactName(phone, getApplicationContext());

                        Log.d(TAG, "AAAAAAAAAAAAcurrName = " + name + ", currPhone = " + phone);

                        UserObject contact = new UserObject(childSnapshot.getKey(), name, phone);
                        if(name.equals(phone)){
                            for(UserObject userObject : contactList){
                                if(userObject.equals(contact.getPhone())){
                                    contact.setName(userObject.getName());
                                }
                            }
                        }

                        userList.add(contact);
                        mUserListAdapter.notifyDataSetChanged();
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public String getContactName(final String phoneNumber, Context context)
    {
        Uri uri=Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName="";
        Cursor cursor=context.getContentResolver().query(uri,projection,null,null,null);

        if (cursor != null) {
            if(cursor.moveToFirst()) {
                contactName=cursor.getString(0);
            }
            cursor.close();
        }

        return contactName;
    }

    private String getCountryISO(){
        String iso = null;

        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        if(telephonyManager.getNetworkCountryIso() != null && !telephonyManager.getNetworkCountryIso().equals("")){
            iso = telephonyManager.getNetworkCountryIso();
        }

        return CountryToPhonePrefix.getPhone(iso);
    }

    private void initializeRecyclerView() {
        mUserList = findViewById(R.id.userList);
        mUserList.addItemDecoration(new DividerItemDecoration(mUserList.getContext(), DividerItemDecoration.VERTICAL));
        mUserList.setNestedScrollingEnabled(false);
        mUserList.setHasFixedSize(false);
        mUserListLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mUserList.setLayoutManager(mUserListLayoutManager);
        mUserListAdapter = new UserListAdapter(userList, this);
        mUserList.setAdapter(mUserListAdapter);
        /*
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                //Izbrisi na primer
            }
        }).attachToRecyclerView(mUserList);
        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.info_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }
        else if (id == R.id.info_item) {
            pokaziDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void pokaziDialog() {
        //String s = "*Ako vam smetaju notifikacije, možete ih isključiti u podešavanjima.";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Uputstvo");
        builder.setMessage("-Brojevi koje vidite dole su vaši kontakti koji koriste ovu aplikaciju.\n\n" +
                "-Pravljenje grupe: \n" +
                "1.Štiklirajte kontakte koje želite da dodate u grupu\n" +
                "2.Smislite naziv grupe\n" +
                "3.Kreirajte grupu klikom na dugme\n\n" +
                "-U grupi možete kliknuti na tri tačke u gornjem desnom uglu, i držanjem na imena članova grupe dobijate nove opcije.");


        builder.setCancelable(true);
        builder.setNegativeButton("I got it!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.create().show();
    }
}

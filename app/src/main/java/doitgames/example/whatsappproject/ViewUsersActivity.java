package doitgames.example.whatsappproject;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import doitgames.example.whatsappproject.Chat.ChatObject;
import doitgames.example.whatsappproject.User.UserObject;
import doitgames.example.whatsappproject.User.ViewUsersListAdapter;
import doitgames.example.whatsappproject.Utils.RecyclerItemClickListener;

public class ViewUsersActivity extends AppCompatActivity implements RecyclerItemClickListener.OnRecyclerClickListener{
    private static final String TAG = "ViewUserActivity";

    RecyclerView mUserList;
    ViewUsersListAdapter mUserListAdapter;
    RecyclerView.LayoutManager mUserListLayoutManager;

    ArrayList<UserObject> userList, contactList;
    ChatObject mChatObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_view_user);

        mChatObject = (ChatObject) getIntent().getSerializableExtra("chatObject2");

        Toolbar myToolbar = findViewById(R.id.toolbarFindUser);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userList = new ArrayList<>();

        initializeRecyclerView();
        getUserList();
    }

    String mName = "";
    String mPhone = "";
    Boolean hasNameAndPhone = false;
    private void getUserList() {
        try {
            final DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("chat").child(mChatObject.getChatId()).child("info").child("users");
            mUserDB.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            if (data.getKey() != null) {
                                String userId = data.getKey().toString();
                                if(data.child("name").exists() && data.child("phone").exists()){
//                                    Log.d(TAG, "onDataChange: data.getKey() has name = " + data.getKey() + " has name = " + data.child("name").getValue().toString());
                                    mName = data.child("name").getValue().toString();
                                    mPhone = data.child("phone").getValue().toString();
                                    hasNameAndPhone = true;
                                    UserObject user = new UserObject(userId, mName, mPhone);
                                    userList.add(user);
                                    mUserListAdapter.notifyDataSetChanged();
                                }
                            } else {
                                Log.e(TAG, "onDataChange: !!!!!!!");
                            }
                            hasNameAndPhone = false;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    databaseError.toException().printStackTrace();
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initializeRecyclerView() {
        mUserList = findViewById(R.id.userList);
        mUserList.addItemDecoration(new DividerItemDecoration(mUserList.getContext(), DividerItemDecoration.VERTICAL));
        mUserList.setNestedScrollingEnabled(false);
        mUserList.setHasFixedSize(false);
        mUserListLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mUserList.setLayoutManager(mUserListLayoutManager);
        mUserList.addOnItemTouchListener(new RecyclerItemClickListener(this, mUserList, this));
        mUserListAdapter = new ViewUsersListAdapter(userList, this, mChatObject.getChatId());
        mUserList.setAdapter(mUserListAdapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        if(mUserListAdapter != null){
            mUserListAdapter.onItemClick(view, position);
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {
        if(mUserListAdapter != null){
            mUserListAdapter.onLongItemClick(view, position);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}

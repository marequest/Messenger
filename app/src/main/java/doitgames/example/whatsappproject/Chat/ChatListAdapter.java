package doitgames.example.whatsappproject.Chat;

import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import doitgames.example.whatsappproject.ChatActivity;
import doitgames.example.whatsappproject.R;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    ArrayList<ChatObject> chatList;

    public ChatListAdapter(ArrayList<ChatObject> list) {
        this.chatList = list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView mTitle;
        public TextView mNotifications;
        public ConstraintLayout layoutHolder;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.title);
            layoutHolder = itemView.findViewById(R.id.layoutHolder);
            mNotifications = itemView.findViewById(R.id.notifications);
        }
    }

    @NonNull
    @Override
    public ChatListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, null, false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(layoutParams);

        return new ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatListAdapter.ViewHolder holder, final int position) {
        if(chatList.get(position).getName() == null || chatList.get(position).getName().equals("")){
        //    holder.mTitle.setText(chatList.get(position).getChatId());
        //} else {
            DatabaseReference DB = FirebaseDatabase.getInstance().getReference().child("chat").child(chatList.get(position).getChatId()).child("info").child("name");
            DB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        String name = dataSnapshot.getValue().toString();
                        chatList.get(position).setName(name);
                        holder.mTitle.setText(name);
                    } else {
                        holder.mTitle.setText(chatList.get(position).getChatId());
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            holder.mTitle.setText(chatList.get(position).getName());
        }
        getNumOfNotifications(holder, position);

        holder.layoutHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ChatActivity.class);
                intent.putExtra("chatObject", chatList.get(holder.getAdapterPosition()));
                view.getContext().startActivity(intent);
            }
        });
    }

    private void getNumOfNotifications(final ChatListAdapter.ViewHolder holder, int position) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("chat").child(chatList.get(position).getChatId()).child("info").child("users").child(FirebaseAuth.getInstance().getUid());
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("numOfNotifications").getValue() == null){
                    holder.mNotifications.setVisibility(View.GONE);
                } else if(dataSnapshot.child("numOfNotifications").getValue().toString().equals("0")){
                    holder.mNotifications.setVisibility(View.GONE);
                } else {
                    holder.mNotifications.setVisibility(View.VISIBLE);
                    holder.mNotifications.setText(dataSnapshot.child("numOfNotifications").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }
}

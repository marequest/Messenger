package doitgames.example.whatsappproject.Chat;

import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import doitgames.example.whatsappproject.R;

import static java.text.DateFormat.getDateTimeInstance;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private static final String TAG = "MessageAdapter";
    /*
    @Override
    public long getItemId(int position) {
        Log.d(TAG, "getItemId: Long.valueOf(messageList.get(position).getMessageId()) = " + Long.valueOf(messageList.get(position).getMessageId()));
        return Long.valueOf(messageList.get(position).getMessageId());
    }
    */

    ArrayList<MessageObject> messageList;
    String mChatId;

    public MessageAdapter(ArrayList<MessageObject> list, String chatId) {
        this.messageList = list;
        this.mChatId = chatId;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        LinearLayout container;
        TextView mMessage, mSender, mTimestamp;
        ImageButton mViewMedia;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container_message);
            mMessage = itemView.findViewById(R.id.message);
            mSender = itemView.findViewById(R.id.sender);
            mViewMedia = itemView.findViewById(R.id.viewMedia);
            mTimestamp = itemView.findViewById(R.id.timestamp);
        }
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, null, false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(layoutParams);

        return new ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageAdapter.ViewHolder holder, final int position) {
        if(messageList.get(position).getMessage().equals("") || messageList.get(position).getMessage() == null){
            holder.mMessage.setText("Media Sent.");
        } else {
            holder.mMessage.setText(messageList.get(position).getMessage());
        }

        if(FirebaseAuth.getInstance().getUid().equals(messageList.get(position).getSenderId())){//Current user salje poruku
            holder.mSender.setText("My message");
        } else {
            final DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("chat").child(mChatId).child("info").child("users").child(messageList.get(position).getSenderId());
            mUserDB.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        holder.mSender.setText(dataSnapshot.child("name").getValue().toString());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    databaseError.toException().printStackTrace();
                }
            });
        }

        holder.mTimestamp.setText(messageList.get(position).getTimestamp());


        if(FirebaseAuth.getInstance().getUid().equals(messageList.get(position).getSenderId())){//Current user salje poruku
            //holder.container.setBackgroundColor(Color.parseColor("#87BFFF"));
            holder.container.setBackgroundResource(R.drawable.blue_shape);
        } else {
            //holder.container.setBackgroundColor(Color.parseColor("#FFFFFF"));
            holder.container.setBackgroundResource(R.drawable.white_shape);
        }

        if(messageList.get(holder.getAdapterPosition()).getMediaUrlList().isEmpty()){
            holder.mViewMedia.setVisibility(View.GONE);
        } else {
            holder.mViewMedia.setVisibility(View.VISIBLE);
        }
        holder.mViewMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ImageViewer.Builder(view.getContext(), messageList.get(holder.getAdapterPosition()).getMediaUrlList())
                        .setStartPosition(0)
                        .setImageMargin(view.getContext(), R.dimen.image_margin)
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}

package doitgames.example.whatsappproject.Chat;

import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import doitgames.example.whatsappproject.R;

import static java.text.DateFormat.getDateTimeInstance;

public class MessageAdapterTest extends RecyclerView.Adapter<MessageAdapterTest.ViewHolder> {
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

    public MessageAdapterTest(ArrayList<MessageObject> list, String chatId) {
        this.messageList = list;
        this.mChatId = chatId;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        LinearLayout containerRight;
        TextView mMessageRight, mTimestampRight;
        ImageButton mViewMediaRight;

        LinearLayout containerLeft;
        TextView mMessageLeft, mSenderLeft, mTimestampLeft;
        ImageButton mViewMediaLeft;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            containerRight = itemView.findViewById(R.id.container_right);
            mMessageRight = itemView.findViewById(R.id.message_right);
            mTimestampRight = itemView.findViewById(R.id.timestamp_right);
            mViewMediaRight = itemView.findViewById(R.id.viewMedia_right);

            containerLeft = itemView.findViewById(R.id.container_left);
            mMessageLeft = itemView.findViewById(R.id.message_left);
            mSenderLeft = itemView.findViewById(R.id.sender_left);
            mTimestampLeft = itemView.findViewById(R.id.timestamp_left);
            mViewMediaLeft = itemView.findViewById(R.id.viewMedia_left);
        }
    }

    @NonNull
    @Override
    public MessageAdapterTest.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_test, null, false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(layoutParams);

        return new ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageAdapterTest.ViewHolder holder, final int position) {
        if(FirebaseAuth.getInstance().getUid().equals(messageList.get(position).getSenderId())) {//Current user salje poruku
            //if(position != 0 && messageList.get(position - 1) != null && messageList.get(position - 1).getSenderId().equals(messageList.get(position).getSenderId())) {
                //Ovde spoji textove
                //Ako nema position - 1 nek bude krug, Ako ima prethodni onda normalno, ako dobije sledeci onda sa obe strane
            //}

            holder.containerLeft.setVisibility(View.GONE);
            holder.containerRight.setVisibility(View.VISIBLE);
            if (messageList.get(position).getMessage().equals("") || messageList.get(position).getMessage() == null) {
                holder.mMessageRight.setText("Media Sent.");
            } else {
                holder.mMessageRight.setText(messageList.get(position).getMessage());
            }

            holder.mTimestampRight.setVisibility(View.GONE);
            holder.containerRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(holder.mTimestampRight.getVisibility() == View.GONE) {
                        holder.mTimestampRight.setVisibility(View.VISIBLE);
                        try {
                            holder.mTimestampRight.setText(getTimeDate(Long.valueOf(messageList.get(position).getTimestamp())));
                        } catch (Exception e){
                            Log.w(TAG, "onClick: Not getting the timestamp right");
                            holder.mTimestampRight.setText(messageList.get(position).getTimestamp());
                        }
                    } else {
                        holder.mTimestampRight.setVisibility(View.GONE);
                    }
                }
            });

            holder.mMessageRight.setBackgroundResource(R.drawable.blue_shape_oval);

            if (messageList.get(holder.getAdapterPosition()).getMediaUrlList().isEmpty()) {
                holder.mViewMediaRight.setVisibility(View.GONE);
            } else {
                holder.mViewMediaRight.setVisibility(View.VISIBLE);
            }
            holder.mViewMediaRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new ImageViewer.Builder(view.getContext(), messageList.get(holder.getAdapterPosition()).getMediaUrlList())
                            .setStartPosition(0)
                            .setImageMargin(view.getContext(), R.dimen.image_margin)
                            .show();
                }
            });
        } else {
            if(position != 0 && messageList.get(position - 1) != null && messageList.get(position - 1).getSenderId().equals(messageList.get(position).getSenderId())){
                //Spoji textove
                String earlierMessage = messageList.get(position - 1).getTimestamp();
                String laterMessage = messageList.get(position).getTimestamp();
                if (Math.abs(Long.valueOf(earlierMessage) - Long.valueOf(laterMessage)) < 300000) {
                    holder.mSenderLeft.setVisibility(View.GONE);
                    holder.mMessageLeft.setBackgroundResource(R.drawable.white_shape_oval);
                } else {
                    holder.mSenderLeft.setVisibility(View.VISIBLE);
                    holder.mMessageLeft.setBackgroundResource(R.drawable.white_shape_oval);
                }
            } else {
                holder.mSenderLeft.setVisibility(View.VISIBLE);
                holder.mMessageLeft.setBackgroundResource(R.drawable.white_shape_oval);
            }
            holder.containerRight.setVisibility(View.GONE);
            holder.containerLeft.setVisibility(View.VISIBLE);
            if (messageList.get(position).getMessage().equals("") || messageList.get(position).getMessage() == null) {
                holder.mMessageLeft.setText("Media Sent.");
            } else {
                holder.mMessageLeft.setText(messageList.get(position).getMessage());
            }

            holder.mTimestampLeft.setVisibility(View.GONE);
            holder.containerLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(holder.mTimestampLeft.getVisibility() == View.GONE) {
                        holder.mTimestampLeft.setVisibility(View.VISIBLE);
                        try {
                            holder.mTimestampLeft.setText(getTimeDate(Long.valueOf(messageList.get(position).getTimestamp())));
                            Log.w(TAG, "onClick: Not getting the timestamp right");
                        } catch (Exception e){
                            holder.mTimestampLeft.setText(messageList.get(position).getTimestamp());
                        }
                    } else {
                        holder.mTimestampLeft.setVisibility(View.GONE);
                    }
                }
            });

            final DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("chat").child(mChatId).child("info").child("users").child(messageList.get(position).getSenderId());
            mUserDB.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        holder.mSenderLeft.setText(dataSnapshot.child("name").getValue().toString());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    databaseError.toException().printStackTrace();
                }
            });


            if (messageList.get(holder.getAdapterPosition()).getMediaUrlList().isEmpty()) {
                holder.mViewMediaLeft.setVisibility(View.GONE);
            } else {
                holder.mViewMediaLeft.setVisibility(View.VISIBLE);
            }
            holder.mViewMediaLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new ImageViewer.Builder(view.getContext(), messageList.get(holder.getAdapterPosition()).getMediaUrlList())
                            .setStartPosition(0)
                            .setImageMargin(view.getContext(), R.dimen.image_margin)
                            .show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public String getTimeDate(long timestamp){
        try{
            if(DateUtils.isToday(timestamp)){
                Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                cal.setTimeInMillis(timestamp);
                return DateFormat.format("HH:mm", cal).toString();
            } else {
                java.text.DateFormat dateFormat = getDateTimeInstance();
                Date netDate = (new Date(timestamp));
                return dateFormat.format(netDate);
            }
        } catch(Exception e) {
            return "date";
        }
    }
}

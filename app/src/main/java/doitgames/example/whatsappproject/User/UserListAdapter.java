package doitgames.example.whatsappproject.User;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import doitgames.example.whatsappproject.R;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    ArrayList<UserObject> userList;

    Boolean isUserChecked = false;
    Context mContext;

    public UserListAdapter(ArrayList<UserObject> userList, Context context) {
        this.userList = userList;
        this.mContext = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView mName, mPhone;
        public ConstraintLayout layoutHolder;
        public CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mName = itemView.findViewById(R.id.name);
            mPhone = itemView.findViewById(R.id.phone);
            layoutHolder = itemView.findViewById(R.id.layoutHolder);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }

    @NonNull
    @Override
    public UserListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, null, false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(layoutParams);

        return new ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserListAdapter.ViewHolder holder, final int position) {
        holder.mName.setText(userList.get(position).getName());
        holder.mPhone.setText(userList.get(position).getPhone());

        holder.layoutHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isUserChecked = !isUserChecked;
                holder.checkBox.setChecked(isUserChecked);
            }
        });

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isSelected) {
                userList.get(holder.getAdapterPosition()).setSelected(isSelected);
            }
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}

package doitgames.example.whatsappproject.User;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import doitgames.example.whatsappproject.R;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class ViewUsersListAdapter extends RecyclerView.Adapter<ViewUsersListAdapter.ViewHolder> {

    ArrayList<UserObject> userList;

    Boolean isUserChecked = false;
    Context mContext;
    String mChatId;

    public ViewUsersListAdapter(ArrayList<UserObject> userList, Context context, String chatId) {
        this.userList = userList;
        this.mContext = context;
        this.mChatId = chatId;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView mName, mPhone;
        public ConstraintLayout layoutHolder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mName = itemView.findViewById(R.id.name);
            mPhone = itemView.findViewById(R.id.phone);
            layoutHolder = itemView.findViewById(R.id.layoutHolder);
        }
    }

    @NonNull
    @Override
    public ViewUsersListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_user, null, false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(layoutParams);

        return new ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewUsersListAdapter.ViewHolder holder, final int position) {
        holder.mName.setText(userList.get(position).getName());
        holder.mPhone.setText(userList.get(position).getPhone());

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void onLongItemClick(View view, final int position) {
        List<String> entrys = new ArrayList<>();
        entrys.add("Rename");

        final CharSequence[] items = entrys.toArray(new CharSequence[entrys.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//        builder.setTitle();
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int item) {
                if(item == 0){
                    renameUser(position);
                }
            }
        });
        builder.setCancelable(true);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.create().show();
    }

    private void renameUser(final int position) {
        Log.d(TAG, "renameUser: Renaming user");
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View renameView = layoutInflater.inflate(R.layout.rename_file_dialog, null);

        final EditText editText = renameView.findViewById(R.id.newName);
        editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(30)});
        editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        builder.setTitle(userList.get(position).getName())
                .setView(renameView)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean cancelDialog = true;
                        String newName = editText.getText().toString().trim();
                        if(newName.length() > 0){
                            cancelDialog = rename(position, newName);
                        }
                        if(cancelDialog){
                            dialogInterface.cancel();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        builder.create().show();
    }

    boolean rename(int position, String newName) {
        final DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("chat").child(mChatId).child("info").child("users").child(userList.get(position).getUid());
        mUserDB.child("name").setValue(newName);
        Log.d(TAG, "rename: Changed name in database");
        userList.clear();
        return true;
    }

    public void onItemClick(View view, int position) {

    }
}

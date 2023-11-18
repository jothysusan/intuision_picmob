package com.picmob.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.picmob.android.R;
import com.picmob.android.listeners.UserFromVicinityListeners;
import com.picmob.android.mvvm.events.UserVicinityPojo;
import com.picmob.android.utils.UtilsFunctions;
import com.wafflecopter.multicontactpicker.ContactResult;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SelectedRegisteredUsersAdapter extends RecyclerView.Adapter<SelectedRegisteredUsersAdapter.ViewHolder> {
    private static final String TAG = "SelectedRegisteredUsersAdapter";
    List<UserVicinityPojo> userList = new ArrayList<>();
    List<ContactResult> contact = new ArrayList<>();
    private Context context;
    private UserFromVicinityListeners mListener;

    public SelectedRegisteredUsersAdapter(List<UserVicinityPojo> userList, Context context,
                                          UserFromVicinityListeners mListener) {
        this.userList = userList;
        this.context = context;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_vicinity_user, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserVicinityPojo pojo = userList.get(position);
        holder.tvUserName.setText(pojo.getUsername());
        holder.tbRemoveSelected.setVisibility(View.VISIBLE);
        holder.selectUser.setVisibility(View.GONE);
        holder.tbRemoveSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAt(holder.getAbsoluteAdapterPosition());
            }
        });
    }

    public void removeAt(int position) {
        userList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, userList.size());
    }

    @Override
    public int getItemCount() {
        return userList == null ? 0 : userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvUserName)
        TextView tvUserName;
        @BindView(R.id.tbRemoveSelect)
        TextView tbRemoveSelected;
        @BindView(R.id.tbUserSelect)
        CheckBox selectUser;
        @BindView(R.id.user_vicinity_layout)
        LinearLayout userVicinityLayout;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}

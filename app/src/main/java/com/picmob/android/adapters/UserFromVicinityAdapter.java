package com.picmob.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.picmob.android.R;
import com.picmob.android.listeners.UserFromVicinityListeners;
import com.picmob.android.mvvm.events.UserVicinityPojo;
import com.picmob.android.utils.UtilsFunctions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserFromVicinityAdapter extends RecyclerView.Adapter<UserFromVicinityAdapter.ViewHolder> {
    private static final String TAG = "UserFromVicinityAdapter";
    List<UserVicinityPojo> userFromVicinityList = new ArrayList<>();
    private Context context;
    private UserFromVicinityListeners mListener;
    boolean isRegistered;
    int row_index = -1;

    public UserFromVicinityAdapter(Context context, UserFromVicinityListeners mListener,
                                   List<UserVicinityPojo> userFromVicinityList, boolean isRegistered) {
        this.userFromVicinityList = userFromVicinityList;
        this.context = context;
        this.mListener = mListener;
        this.isRegistered = isRegistered;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_vicinity_user, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserVicinityPojo pojo = userFromVicinityList.get(position);
        holder.tvUserName.setText(pojo.getUsername());
        if (isRegistered) {
            holder.tbRemoveSelected.setVisibility(View.GONE);
            holder.selectUser.setVisibility(View.GONE);
        } else {
            holder.tbRemoveSelected.setVisibility(View.GONE);
            holder.selectUser.setVisibility(View.VISIBLE);
        }

        holder.selectUser.setChecked(pojo.isSelected());
        holder.selectUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCheckboxStatus(pojo, !pojo.isSelected());
            }
        });

        holder.userVicinityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UtilsFunctions.isNetworkAvail(context)) {
                    row_index = holder.getAdapterPosition();
                    mListener.onItemSelected(pojo);
                    notifyDataSetChanged();
                }
            }
        });
        if (row_index == position) {
            holder.userVicinityLayout.setBackgroundColor(context.getResources().getColor(R.color.selection_bg_color));
        } else {
            holder.userVicinityLayout.setBackgroundColor(context.getResources().getColor(R.color.white));
        }
    }

    @Override
    public int getItemCount() {
        return userFromVicinityList == null ? 0 : userFromVicinityList.size();
    }

    public void changeCheckboxStatus(UserVicinityPojo pojo, boolean value) {
        pojo.setSelected(value);
        notifyDataSetChanged();
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

    public void filterList(ArrayList<UserVicinityPojo> filteredList) {
        userFromVicinityList=filteredList;
        notifyDataSetChanged();
    }

}

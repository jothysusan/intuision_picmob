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
import com.picmob.android.mvvm.events.ContactsPojo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    private static final String TAG = "ContactAdapter";
    List<ContactsPojo> contactList = new ArrayList<>();
    private Context context;
    private UserFromVicinityListeners mListener;

    public ContactAdapter(List<ContactsPojo> contactList, Context context,
                          UserFromVicinityListeners mListener) {
        this.contactList = contactList;
        this.context = context;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ContactAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_vicinity_user, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContactsPojo pojo = contactList.get(position);
        holder.tvUserName.setText(!pojo.getmDisplayName().equals("") ? pojo.getmDisplayName() : pojo.getPhoneNumber());
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
        contactList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, contactList.size());
    }


    @Override
    public int getItemCount() {
        return contactList == null ? 0 : contactList.size();
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

package com.picmob.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.picmob.android.R;
import com.picmob.android.listeners.FriendsListener;
import com.picmob.android.mvvm.friends.FriendsPojo;
import com.picmob.android.utils.UtilsFunctions;
import com.picmob.android.utils.widgets.ShapeImageView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private Context context;
    private FriendsListener mListener;
    private List<FriendsPojo> friendsList;
    private static final String TAG = "FriendsAdapter";

    public FriendsAdapter(Context context, FriendsListener mListener, List<FriendsPojo> friendsList) {
        this.context = context;
        this.mListener = mListener;
        this.friendsList = friendsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitems_friends, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendsPojo pojo = friendsList.get(position);

        if (pojo.getAvatarURL() != null && !pojo.getAvatarURL().isEmpty()) {
            Glide.with(holder.itemView)
                    .load(pojo.getAvatarURL())
                    .placeholder(UtilsFunctions.getCircularProgressDrawable(context))
                    .error(R.drawable.ic_user)
                    .into(holder.imgProfile);

            holder.layProfile.setVisibility(View.GONE);
            holder.imgProfile.setVisibility(View.VISIBLE);
        } else {

            holder.layProfile.setBackground(UtilsFunctions.setRandomColor(context));
            holder.tvFirstLatter.setText(String.valueOf(pojo.getUsername().charAt(0)).toUpperCase());
            holder.layProfile.setVisibility(View.VISIBLE);
            holder.imgProfile.setVisibility(View.GONE);

        }

        holder.tvUserName.setText(pojo.getUsername());
        holder.tvName.setText(pojo.getFirstName() + " " + pojo.getLastName());

        holder.layFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UtilsFunctions.isNetworkAvail(context))
                    mListener.onClickFriend(pojo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendsList == null ? 0 : friendsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        @BindView(R.id.layFriends)
        LinearLayout layFriends;
        @BindView(R.id.imgProfile)
        ShapeImageView imgProfile;
        @BindView(R.id.layProfile)
        RelativeLayout layProfile;
        @BindView(R.id.tvFirstLatter)
        TextView tvFirstLatter;
        @BindView(R.id.tvUserName)
        TextView tvUserName;
        @BindView(R.id.tvName)
        TextView tvName;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}

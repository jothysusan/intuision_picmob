package com.picmob.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.picmob.android.R;
import com.picmob.android.activity.ImagePreviewActivity;
import com.picmob.android.listeners.VideoClickListener;
import com.picmob.android.models.DmModel;
import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.UtilsFunctions;
import com.picmob.android.utils.widgets.ShapeImageView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DmAdapter extends RecyclerView.Adapter<DmAdapter.ViewHolder> {

    private Context context;
    private List<DmModel> dmList;
    private static final String TAG = "DmAdapter";
    private VideoClickListener mListener;

    public DmAdapter(Context context, List<DmModel> dmList, VideoClickListener videoClickListener) {
        this.context = context;
        this.dmList = dmList;
        mListener = videoClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitems_chat, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        DmModel model = dmList.get(position);

        if (model.getSend()) {
            holder.send.setVisibility(View.VISIBLE);
            holder.receive.setVisibility(View.GONE);
            holder.tvMsgSnd.setText(model.getMessage());
            holder.tvSDate.setText(model.getDateTime());
            if (model.getMediaURL() != null && !model.getMediaURL().isEmpty()) {
                if (UtilsFunctions.getFileType(model.getMediaURL()) != null) {
                    holder.laySMedia.setVisibility(View.VISIBLE);
                    if (UtilsFunctions.getFileType(model.getMediaURL()).equalsIgnoreCase(AppConstants.VIDEOS)) {
                        holder.Svideo_view.setVisibility(View.VISIBLE);
                        holder.Svideo_view.setOnClickListener(v -> mListener.onVideoClick(model.getMediaURL()));
                        Glide.with(holder.itemView)
                                .load(model.getMediaURL())
                                .placeholder(UtilsFunctions.getCircularProgressDrawable(context))
                                .error(R.drawable.ic_movie_)
                                .into(holder.imgSProfile);
                    }

                    if (UtilsFunctions.getFileType(model.getMediaURL()).equalsIgnoreCase(AppConstants.PICTURES)) {
                        holder.Svideo_view.setVisibility(View.GONE);
                        holder.imgSProfile.setVisibility(View.VISIBLE);
                        holder.imgSProfile.setOnClickListener(v -> {
                            Intent intent = new Intent(context, ImagePreviewActivity.class);
                            intent.putExtra(AppConstants.MEDIA, model.getMediaURL());
                            context.startActivity(intent);
                        });
                        Glide.with(holder.itemView)
                                .load(model.getMediaURL().toString())
                                .placeholder(UtilsFunctions.getCircularProgressDrawable(context))
                                .error(R.drawable.ic_landscape)
                                .into(holder.imgSProfile);
                    }
                }
            } else {
                holder.laySMedia.setVisibility(View.GONE);
            }

        } else {
            holder.receive.setVisibility(View.VISIBLE);
            holder.send.setVisibility(View.GONE);
            holder.tvMsgRecv.setText(model.getMessage());
            holder.tvRDate.setText(model.getDateTime());
            Log.e(TAG, "onBindViewHolder: " + new Gson().toJson(model));
            if (model.getMediaURL() != null && !model.getMediaURL().isEmpty()) {
                holder.layRMedia.setVisibility(View.VISIBLE);
                if (UtilsFunctions.getFileType(model.getMediaURL()) != null) {
                    if (UtilsFunctions.getFileType(model.getMediaURL()).equalsIgnoreCase(AppConstants.VIDEOS)) {
                        holder.Rvideo_view.setVisibility(View.VISIBLE);
                        holder.Rvideo_view.setOnClickListener(v -> mListener.onVideoClick(model.getMediaURL()));
                        Glide.with(holder.itemView)
                                .load(model.getMediaURL())
                                .placeholder(UtilsFunctions.getCircularProgressDrawable(context))
                                .error(R.drawable.ic_movie_)
                                .into(holder.imgRProfile);
                    }
                    if (UtilsFunctions.getFileType(model.getMediaURL()).equalsIgnoreCase(AppConstants.PICTURES)) {
                        holder.Rvideo_view.setVisibility(View.GONE);
                        holder.imgRProfile.setVisibility(View.VISIBLE);
                        holder.imgRProfile.setOnClickListener(v -> {
                            Intent intent = new Intent(context, ImagePreviewActivity.class);
                            intent.putExtra(AppConstants.MEDIA, model.getMediaURL());
                            context.startActivity(intent);
                        });
                        Glide.with(holder.itemView)
                                .load(model.getMediaURL())
                                .placeholder(UtilsFunctions.getCircularProgressDrawable(context))
                                .error(R.drawable.ic_landscape)
                                .into(holder.imgRProfile);
                    }
                }
            } else {
                holder.layRMedia.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return dmList == null ? 0 : dmList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.send)
        ConstraintLayout send;
        @BindView(R.id.receive)
        ConstraintLayout receive;
        @BindView(R.id.laySMedia)
        FrameLayout laySMedia;
        @BindView(R.id.layRMedia)
        FrameLayout layRMedia;
        @BindView(R.id.imgSProfile)
        ShapeImageView imgSProfile;
        @BindView(R.id.imgRProfile)
        ShapeImageView imgRProfile;
        @BindView(R.id.Svideo_view)
        ImageView Svideo_view;
        @BindView(R.id.Rvideo_view)
        ImageView Rvideo_view;
        @BindView(R.id.tvMsgSnd)
        TextView tvMsgSnd;
        @BindView(R.id.tvMsgRecv)
        TextView tvMsgRecv;
        @BindView(R.id.tvSDate)
        TextView tvSDate;
        @BindView(R.id.tvRDate)
        TextView tvRDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}

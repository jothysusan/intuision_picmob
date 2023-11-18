package com.picmob.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.picmob.android.R;
import com.picmob.android.listeners.EventClickListeners;
import com.picmob.android.mvvm.events.EventListPojo;
import com.picmob.android.utils.UtilsFunctions;

import java.sql.SQLOutput;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {
    private static final String TAG = "EventsAdapter";
    private List<EventListPojo> modelList;
    private Context context;
    private EventClickListeners mListener;

    public EventsAdapter(Context context, EventClickListeners listener,
                         List<EventListPojo> modelList) {
        this.context = context;
        this.modelList = modelList;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EventsAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_items_events, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull EventsAdapter.ViewHolder holder, int position) {

        EventListPojo pojo = modelList.get(position);
        Glide.with(holder.itemView)
                .load(pojo.getDisplayImageURL() != null ? pojo.getDisplayImageURL() : "")
                .placeholder(UtilsFunctions.getCircularProgressDrawable(context))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_landscape)
                .into(holder.img);
        holder.tvMsg.setText(pojo.getName());
        holder.tvDescription.setText(pojo.getDescription());
        holder.cvContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UtilsFunctions.isNetworkAvail(context))
                    mListener.onClickEventItem(pojo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return modelList == null ? 0 : modelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.event_img)
        ImageView img;
        @BindView(R.id.tvMsg)
        TextView tvMsg;
        @BindView(R.id.tvDescription)
        TextView tvDescription;
        @BindView(R.id.cvContent)
        CardView cvContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}

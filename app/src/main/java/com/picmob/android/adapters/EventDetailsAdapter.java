package com.picmob.android.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.picmob.android.R;
import com.picmob.android.listeners.EventItemSelectionListener;
import com.picmob.android.mvvm.events.EventImage;
import com.picmob.android.mvvm.events.EventImageListener;
import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.UtilsFunctions;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventDetailsAdapter extends RecyclerView.Adapter<EventDetailsAdapter.ViewHolder> {

    private Context context;
    private EventImageListener mListener;
    private EventItemSelectionListener selectionListener;
    private ArrayList<EventImage> galleryList;
    private ArrayList<EventImage> selectedItems = new ArrayList<>();
    private static final String TAG = "GalleryAdapter";

    public EventDetailsAdapter(Context context, EventImageListener clickListener, ArrayList<EventImage> galleryList,
                               EventItemSelectionListener selectionListener) {
        this.context = context;
        this.mListener = clickListener;
        this.galleryList = galleryList;
        this.selectionListener = selectionListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_event_details, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        EventImage pojo = galleryList.get(position);

        if (pojo.getImageUrl() != null && !pojo.getImageUrl().isEmpty())
            if (UtilsFunctions.getFileType(pojo.getImageUrl()) != null) {
                if (UtilsFunctions.getFileType(pojo.getImageUrl()).equalsIgnoreCase(AppConstants.VIDEOS)) {
                    Log.e(TAG, "onBindViewHolder: " + pojo.getImageUrl());
                    holder.videoPlaceholder.setVisibility(View.VISIBLE);
                    Glide.with(holder.itemView)
                            .load(pojo.getImageUrl())
                            .placeholder(UtilsFunctions.getCircularProgressDrawable(context))
                            .error(R.drawable.ic_movie_)
                            .into(holder.img);
                } else if (UtilsFunctions.getFileType(pojo.getImageUrl()).equalsIgnoreCase(AppConstants.PICTURES))
                    holder.videoPlaceholder.setVisibility(View.GONE);
                Glide.with(holder.itemView)
                        .load(pojo.getImageUrl())
                        .placeholder(UtilsFunctions.getCircularProgressDrawable(context))
                        .error(R.drawable.ic_landscape)
                        .into(holder.img);
            }

        holder.checkBox.setChecked(pojo.isSelected());
        holder.checkBox.setTag(position);
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Integer clickedPosition = (Integer) buttonView.getTag();
            if (isChecked) {
                selectedItems.add(galleryList.get(clickedPosition));
            } else {
                selectedItems.remove(galleryList.get(clickedPosition));
            }
            selectionListener.onCheckboxClick(selectedItems);
        });

        holder.tvUsrName.setText(pojo.getUserName());

        holder.cvContent.setOnClickListener(v -> mListener.onImageClick(pojo));
    }

    @Override
    public int getItemCount() {
        return galleryList == null ? 0 : galleryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imgView)
        ImageView img;
        @BindView(R.id.videoPlaceholder)
        ImageView videoPlaceholder;
        @BindView(R.id.tvUsername)
        TextView tvUsrName;
        @BindView(R.id.myCheckBox)
        CheckBox checkBox;
        @BindView(R.id.cvContent)
        CardView cvContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}

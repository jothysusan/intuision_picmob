package com.picmob.android.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.picmob.android.R;
import com.picmob.android.listeners.GalleryListener;
import com.picmob.android.mvvm.gallery.GalleryPojo;
import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.UtilsFunctions;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private Context context;
    private GalleryListener mListener;
    private List<GalleryPojo> galleryList;
    private static final String TAG = "GalleryAdapter";

    public GalleryAdapter(Context context, GalleryListener mListener, List<GalleryPojo> galleryList) {
        this.context = context;
        this.mListener = mListener;
        this.galleryList = galleryList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitems_gallery, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        GalleryPojo pojo = galleryList.get(position);


        if (pojo.getMediaURL() != null && !pojo.getMediaURL().isEmpty())
            if (UtilsFunctions.getFileType(pojo.getMediaURL()) != null) {

               /* Bitmap bitmap = null;
                try {
                    bitmap = UtilsFunctions.retriveVideoFrameFromVideo(context, pojo.getMediaURL());
                    if (bitmap != null)
                        holder.img.setImageBitmap(bitmap);
                    else {

                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }*/

                if (UtilsFunctions.getFileType(pojo.getMediaURL()).equalsIgnoreCase(AppConstants.VIDEOS)) {
                    Log.e(TAG, "onBindViewHolder: " + pojo.getMediaURL());
                    holder.videoPlaceholder.setVisibility(View.VISIBLE);
                    Glide.with(holder.itemView)
                            .load(pojo.getMediaURL())
                            .placeholder(UtilsFunctions.getCircularProgressDrawable(context))
                            .error(R.drawable.ic_movie_)
                            .into(holder.img);

                }

                if (UtilsFunctions.getFileType(pojo.getMediaURL()).equalsIgnoreCase(AppConstants.PICTURES))
                    Glide.with(holder.itemView)
                            .load(pojo.getMediaURL())
                            .placeholder(UtilsFunctions.getCircularProgressDrawable(context))
                            .error(R.drawable.ic_landscape)
                            .into(holder.img);
            }


        holder.tvMsg.setText(pojo.getMessage());
        holder.tvUsrName.setText("Shared by " + pojo.getUserName());

        holder.cvContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UtilsFunctions.isNetworkAvail(context))
                    mListener.onGalleryClick(pojo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return galleryList == null ? 0 : galleryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.img)
        ImageView img;
        @BindView(R.id.videoPlaceholder)
        ImageView videoPlaceholder;
        @BindView(R.id.tvUsrName)
        TextView tvUsrName;
        @BindView(R.id.tvMsg)
        TextView tvMsg;
        @BindView(R.id.cvContent)
        CardView cvContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}

package com.picmob.android.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.picmob.android.R;
import com.picmob.android.listeners.RequestResponseClickListener;
import com.picmob.android.mvvm.requests_response.RequestResponseModel;
import com.picmob.android.mvvm.requests_response.RequestResponsePoJo;
import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.UtilsFunctions;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RequestResponseAdapter extends RecyclerView.Adapter<RequestResponseAdapter.ViewHolder> {

    private List<RequestResponseModel> modelList;
    private Context context;
    private RequestResponseClickListener mListener;

    public RequestResponseAdapter(Context context, RequestResponseClickListener listener,
                                  List<RequestResponseModel> modelList) {
        this.context = context;
        this.modelList = modelList;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RequestResponseAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitems_request_response, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RequestResponseModel dashboardItem = modelList.get(position);
        if (dashboardItem.getType() == AppConstants.REQUEST_TYPE) {
            holder.tvMsg.setText(context.getString(R.string.request_from) + " " + dashboardItem.getUsers().get(0).getUserName());
            holder.tvTxt.setText(context.getString(R.string.request));
        } else if (dashboardItem.getType() == AppConstants.RESPONSE_TYPE) {
            holder.tvMsg.setText(context.getString(R.string.response_from) + " " + dashboardItem.getUsers().get(0).getUserName());
            holder.tvTxt.setText(context.getString(R.string.response));
        } else {
            holder.tvMsg.setText(context.getString(R.string.requested_to));
            holder.tvTxt.setText(context.getString(R.string.request));
        }
        holder.tvDate.setText(dashboardItem.getDateTime());

        holder.cvCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UtilsFunctions.isNetworkAvail(context))
                    mListener.onClickReqItem(dashboardItem);
            }
        });

    }

    @Override
    public int getItemCount() {
        return modelList == null ? 0 : modelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.cvCont)
        CardView cvCont;
        @BindView(R.id.tvMesg)
        TextView tvMsg;
        @BindView(R.id.tvTxt)
        TextView tvTxt;
        @BindView(R.id.tvDate)
        TextView tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}

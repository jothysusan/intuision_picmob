package com.picmob.android.adapters;

import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.picmob.android.R;
import com.picmob.android.models.MessageModel;
import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.UtilsFunctions;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {

    private ArrayList<MessageModel> msgList;

    public MessageListAdapter(ArrayList<MessageModel> msgList) {
        this.msgList = msgList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitems_chat, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        MessageModel msgModel = msgList.get(position);
        if (msgModel.getType() == AppConstants.sendMsg) {
            holder.send.setVisibility(View.VISIBLE);
            holder.receive.setVisibility(View.GONE);
            if (UtilsFunctions.validateUrl(msgModel.getMsg())) {
                holder.tvSnd.setClickable(true);
                SpannableString content = new SpannableString(msgModel.getMsg());
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                holder.tvSnd.setText(content);
            } else
                holder.tvSnd.setText(msgModel.getMsg());
        } else {
            holder.send.setVisibility(View.GONE);
            holder.receive.setVisibility(View.VISIBLE);
            if (UtilsFunctions.validateUrl(msgModel.getMsg())) {
                holder.tvRcv.setClickable(true);
                SpannableString content = new SpannableString(msgModel.getMsg());
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                holder.tvRcv.setText(content);
            } else
                holder.tvRcv.setText(msgModel.getMsg());
        }
    }

    public void addItem(ArrayList<MessageModel> msgModel) {
        msgList = msgModel;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return msgList == null ? 0 : msgList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvMsgSnd)
        TextView tvSnd;
        @BindView(R.id.tvMsgRecv)
        TextView tvRcv;
        @BindView(R.id.send)
        LinearLayout send;
        @BindView(R.id.receive)
        LinearLayout receive;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}

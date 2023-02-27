package com.example.navpe.Adapters;

import static com.example.navpe.Models.GetMessage.RECEIVE_MESSAGE;
import static com.example.navpe.Models.GetMessage.SENT_MESSAGE;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.navpe.Models.GetMessage;
import com.example.navpe.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Objects;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<GetMessage> arrayList;

    public MessageAdapter(ArrayList<GetMessage> arrayList) { this.arrayList = arrayList; }
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENT_MESSAGE) {
            View layoutSent = LayoutInflater.from(parent.getContext()).inflate(R.layout.send_chat, parent, false);
            return new LayoutSentViewHolder(layoutSent);
        } else if (viewType == RECEIVE_MESSAGE) {
            View layoutReceived = LayoutInflater.from(parent.getContext()).inflate(R.layout.receive_chat, parent, false);
            return new LayoutReceivedViewHolder(layoutReceived);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        GetMessage model = arrayList.get(position);
        if(holder instanceof LayoutSentViewHolder){
            String text = model.getSendMessage();
            ((LayoutSentViewHolder)holder).sentMsg.setText(text);
            ((LayoutSentViewHolder)holder).phNo.setText(Objects.requireNonNull(user.getPhoneNumber()).substring(3));
        }else if(holder instanceof LayoutReceivedViewHolder){
            String receivedMsg = model.getReceiveMessage();
            ((LayoutReceivedViewHolder)holder).receivedMsg.setText(receivedMsg);
            ((LayoutReceivedViewHolder)holder).phNo1.setText(model.getPhoneNo());
        }
    }
    @Override
    public int getItemViewType(int position)
    {
        switch (arrayList.get(position).getViewType()) {
            case 0:
                return SENT_MESSAGE;
            case 1:
                return RECEIVE_MESSAGE;
            default:
                return -1;
        }
    }
    @Override
    public int getItemCount() {
        return arrayList.size();
    }
    public static class LayoutSentViewHolder extends RecyclerView.ViewHolder {
        TextView sentMsg;
        TextView phNo;
        public LayoutSentViewHolder(@NonNull View itemView) {
            super(itemView);
            sentMsg = this.itemView.findViewById(R.id.send1);
            phNo = itemView.findViewById(R.id.userChat);
        }
    }
    public static class LayoutReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView receivedMsg, phNo1;
        public LayoutReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            receivedMsg = itemView.findViewById(R.id.receive_1);
            phNo1 = itemView.findViewById(R.id.userChat1);
        }
    }
}
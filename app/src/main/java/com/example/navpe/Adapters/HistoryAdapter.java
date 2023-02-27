package com.example.navpe.Adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.navpe.Activities.Chat;
import com.example.navpe.Models.GetHistory;
import com.example.navpe.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    ArrayList<GetHistory> arrayList;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    public HistoryAdapter(ArrayList<GetHistory> arrayList) { this.arrayList = arrayList; }
    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View HistoryView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_history,parent,false);
        return new HistoryViewHolder(HistoryView);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        GetHistory model = arrayList.get(position);
        holder.user_name.setText(model.getName());
        if(!model.getImage().equals("")) {
            Picasso.get().load(model.getImage()).into(holder.imageView);
        }else {
            Picasso.get().load(R.drawable.logo).into(holder.imageView);
        }
        holder.txn.setText(model.getTransaction());
        holder.typeOfTxn.setText(model.getTypeOfTxn());
        holder.amount.setText(model.getAmount());
        holder.time.setText(model.getTime());
        holder.imageView.setOnClickListener(v -> {
            Intent in = new Intent(v.getContext(), Chat.class);
            in.putExtra("Name",model.getName());
            in.putExtra("receiverPhone", model.getReceiverPhone());
            if(model.getReceiverPhone().length() > 0){
                if( !Objects.requireNonNull(user.getPhoneNumber()).contains(model.getReceiverPhone())){
                    v.getContext().startActivity(in);
                }
            }else{
                Toast.makeText(v.getContext(), "User not registered on Navpe", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView time, user_name, txn, typeOfTxn, amount;
        ImageView imageView;
        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.time);
            user_name = itemView.findViewById(R.id.usr_name);
            txn = itemView.findViewById(R.id.txn);
            typeOfTxn = itemView.findViewById(R.id.type_txn);
            amount = itemView.findViewById(R.id.amountHist);
            imageView = itemView.findViewById(R.id.userImage);
        }
    }
}
package com.example.navpe.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.navpe.Models.GetAccount;
import com.example.navpe.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {
    ArrayList<GetAccount> arrayList;

    public AccountAdapter(ArrayList<GetAccount> arrayList) { this.arrayList = arrayList; }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View AccountView = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_details,parent,false);
        return new AccountViewHolder(AccountView);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        GetAccount model = arrayList.get(position);
        holder.Bank_name.setText(model.getBankName());
        String acc = model.getAccountNo().substring(0,3) + "-xxx-xxx";
        holder.Account_No.setText(acc);
        Picasso.get().load(model.getBankImage()).into(holder.imageView);
//        holder.cardView.setOnClickListener(v -> {
//            Intent in = new Intent(v.getContext(), CardDetails.class);
//            v.getContext().startActivity(in);
//        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
    public static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView Bank_name, Account_No;
        ImageView imageView;
        CardView cardView;
        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            Bank_name = itemView.findViewById(R.id.bankName);
            Account_No = itemView.findViewById(R.id.AccNo);
            imageView = itemView.findViewById(R.id.bankImage);
            cardView = itemView.findViewById(R.id.card1);
        }
    }
}
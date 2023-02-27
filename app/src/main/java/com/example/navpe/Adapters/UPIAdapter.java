package com.example.navpe.Adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.navpe.Activities.UPI_Pin;
import com.example.navpe.Models.GetAccount;
import com.example.navpe.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UPIAdapter extends RecyclerView.Adapter<UPIAdapter.UPIViewHolder> {
    ArrayList<GetAccount> arrayList;

    public UPIAdapter(ArrayList<GetAccount> arrayList) { this.arrayList = arrayList; }

    @NonNull
    @Override
    public UPIViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View AccountView = LayoutInflater.from(parent.getContext()).inflate(R.layout.accounts_balance,parent,false);
        return new UPIViewHolder(AccountView);
    }

    @Override
    public void onBindViewHolder(@NonNull UPIViewHolder holder, int position) {
        GetAccount model = arrayList.get(position);
        String value = model.getBankName() + "-" + model.getAccountNo().substring(0,3) + "-xxx-xxx";
        holder.Bank_name.setText(value);
        Picasso.get().load(model.getBankImage()).into(holder.imageView);
        holder.linearLayout.setOnClickListener(v -> {
            Intent in = new Intent(v.getContext(), UPI_Pin.class);
            in.putExtra("Function","Balance");
            v.getContext().startActivity(in);
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
    public static class UPIViewHolder extends RecyclerView.ViewHolder {
        TextView Bank_name;
        ImageView imageView;
        LinearLayout linearLayout;
        public UPIViewHolder(@NonNull View itemView) {
            super(itemView);
            Bank_name = itemView.findViewById(R.id.bankAccName);
            imageView = itemView.findViewById(R.id.bankAccIcon);
            linearLayout = itemView.findViewById(R.id.linearLayout13);
        }
    }
}
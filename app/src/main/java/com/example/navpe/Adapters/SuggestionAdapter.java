package com.example.navpe.Adapters;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.navpe.Activities.PayActivity;
import com.example.navpe.Models.GetSuggestion;
import com.example.navpe.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder> {
    ArrayList<GetSuggestion> arrayList;

    public SuggestionAdapter(ArrayList<GetSuggestion> arrayList) { this.arrayList = arrayList; }
    String data;
    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View SuggestionView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_suggestion,parent,false);
        return new SuggestionViewHolder(SuggestionView);
    }
    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        GetSuggestion model = arrayList.get(position);
        holder.userName.setText(model.getUser_name());
        if(model.getImage().length() == 0){Picasso.get().load(R.drawable.img).into(holder.imageView);}
        else {
            Picasso.get().load(model.getImage()).into(holder.imageView);
        }
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PayActivity.class);
            FirebaseDatabase.getInstance().getReference().child("QrCode").child(Objects.requireNonNull(FirebaseAuth.getInstance().
                    getCurrentUser()).getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    data = Objects.requireNonNull(snapshot.getValue()).toString();
                    if(data.length() > 0){
                        String name =  data.split("&")[1];
                        name = name.substring(3).replace("%20", " ");
                        if(name.equals(model.getUser_name())){
                            intent.putExtra("Payment", data);
                            v.getContext().startActivity(intent);
                        }
                    }else{
                        Toast.makeText(v.getContext(), "The user is Invalid", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Error", error.toString());
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
    public static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        ImageView imageView;
        CardView cardView;
        public SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user1);
            imageView = itemView.findViewById(R.id.userImage1);
            cardView = itemView.findViewById(R.id.view0);
        }
    }
}
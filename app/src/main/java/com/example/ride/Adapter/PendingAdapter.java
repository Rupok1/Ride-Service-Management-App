package com.example.ride.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ride.Model.PendingItem;
import com.example.ride.R;

import java.util.List;

public class PendingAdapter extends RecyclerView.Adapter<PendingAdapter.MyViewHolder> {

    Context context;
    List<PendingItem>pendingItemList;
    private ItemClickListener mItemListener;

    public PendingAdapter(Context context, List<PendingItem> pendingItemList, ItemClickListener mItemListener) {
        this.context = context;
        this.pendingItemList = pendingItemList;
        this.mItemListener = mItemListener;
    }

    @NonNull
    @Override
    public PendingAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.pending_item,parent,false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PendingAdapter.MyViewHolder holder, int position) {

        PendingItem item = pendingItemList.get(position);

        holder.name.setText(item.getName());
        holder.email.setText(item.getEmail());
        holder.phone.setText(item.getPhone());
        holder.userId.setText(item.getUserId());

        holder.itemView.setOnClickListener(view -> {

            mItemListener.onItemCLick(pendingItemList.get(position));

        });


    }

    @Override
    public int getItemCount() {
        return pendingItemList.size();
    }

    public interface ItemClickListener{
        void onItemCLick(PendingItem item);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView name,email,phone,userId;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.pName);
            email = itemView.findViewById(R.id.pEmail);
            phone = itemView.findViewById(R.id.pPhone);
            userId = itemView.findViewById(R.id.userId);

        }
    }

}

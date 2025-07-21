package com.example.tradeupproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tradeupproject.R;
import com.example.tradeupproject.models.Listing;

import java.util.List;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.VH> {

    public interface OnItemClick { void onClick(Listing item); }

    private final List<Listing> data;
    private final OnItemClick listener;

    public ListingAdapter(List<Listing> data, OnItemClick l) {
        this.data = data;
        this.listener = l;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_listing, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int pos) {
        Listing it = data.get(pos);
        holder.title.setText(it.getTitle());
        holder.price.setText("₫" + it.getPrice());
        holder.location.setText(it.getLocation());
        // load ảnh đầu tiên nếu có
        if (it.getImages()!=null && !it.getImages().isEmpty()) {
            Glide.with(holder.itemView)
                    .load(it.getImages().get(0))
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.placeholder_image);
        }
        holder.itemView.setOnClickListener(v -> listener.onClick(it));
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, price, location;
        VH(View item) {
            super(item);
            image    = item.findViewById(R.id.imgListing);
            title    = item.findViewById(R.id.tvTitle);
            price    = item.findViewById(R.id.tvPrice);
            location = item.findViewById(R.id.tvLocation);
        }
    }
}

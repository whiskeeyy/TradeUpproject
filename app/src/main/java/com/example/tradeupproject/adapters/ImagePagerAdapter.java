package com.example.tradeupproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tradeupproject.R;

import java.util.List;

public class ImagePagerAdapter
        extends RecyclerView.Adapter<ImagePagerAdapter.VH> {

    private final Context ctx;
    private final List<String> images;

    public ImagePagerAdapter(Context c, List<String> imgs) {
        ctx = c;
        images = imgs;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx)
                .inflate(R.layout.item_image_page, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int pos) {
        Glide.with(ctx)
                .load(images.get(pos))
                .placeholder(R.drawable.placeholder_image)
                .into(holder.imageView);
    }

    @Override public int getItemCount() { return images.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imageView;
        VH(View item) {
            super(item);
            imageView = item.findViewById(R.id.imagePage);
        }
    }
}


package com.example.tradeupproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tradeupproject.R;
import com.example.tradeupproject.models.Notification;
import com.google.firebase.Timestamp;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter
        extends RecyclerView.Adapter<NotificationAdapter.VH> {

    private final List<Notification> items;

    public NotificationAdapter(List<Notification> items) {
        this.items = items;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int pos) {
        Notification n = items.get(pos);
        holder.tvTitle.setText(n.getTitle());
        holder.tvMessage.setText(n.getMessage());

        // Format timestamp
        Timestamp ts = n.getTimestamp();
        if (ts != null) {
            DateFormat df = DateFormat.getDateTimeInstance(
                    DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
            holder.tvTime.setText(df.format(ts.toDate()));
        } else {
            holder.tvTime.setText("");
        }
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;
        VH(View item) {
            super(item);
            tvTitle   = item.findViewById(R.id.tvNotifTitle);
            tvMessage = item.findViewById(R.id.tvNotifMessage);
            tvTime    = item.findViewById(R.id.tvNotifTime);
        }
    }
}

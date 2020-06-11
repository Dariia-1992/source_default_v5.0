package com.hi.appskin_v40.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hi.appskin_v40.R;
import com.hi.appskin_v40.model.Skin;
import com.hi.appskin_v40.utils.DownloadHelper;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SkinsAdapter extends RecyclerView.Adapter<SkinsAdapter.ViewHolder> {
    public interface OnClickItem {
        void onClicked(String id);
    }

    private final OnClickItem listener;
    private List<Skin> items;

    public SkinsAdapter(List<Skin> mods, OnClickItem listener) {
        items = mods;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mods_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Skin skin = items.get(position);
        Picasso.get()
                .load(DownloadHelper.getThumbnailUrl(skin.getThumbnail()))
                .into(holder.postImageView);
        holder.titleView.setText(skin.getTitle());
        holder.isUpdate.setVisibility(skin.isUpdatedToday() ? View.VISIBLE : View.VISIBLE);
        setRating(holder, skin);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null){
                listener.onClicked(items.get(position).getId());
            }
        });
    }

    @Override
    public int getItemCount() { return items != null ? items.size() : 0; }

    private void setRating(@NonNull ViewHolder holder, Skin skin) {
        int rating = skin.getRating();

        holder.ratingContainer.removeAllViews();
        for (int i = 0; i < rating; i++) {
            View view = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.item_rating_star, holder.ratingContainer, false);
            holder.ratingContainer.addView(view);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleView;
        ImageView postImageView;
        ViewGroup ratingContainer;
        View isUpdate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            postImageView = itemView.findViewById(R.id.postImageView);
            titleView = itemView.findViewById(R.id.titleView);
            ratingContainer = itemView.findViewById(R.id.ratingContainer);
            isUpdate = itemView.findViewById(R.id.isUpdate);
        }
    }
}

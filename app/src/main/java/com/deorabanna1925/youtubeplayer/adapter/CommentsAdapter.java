package com.deorabanna1925.youtubeplayer.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.deorabanna1925.youtubeplayer.R;
import com.deorabanna1925.youtubeplayer.model.Comments;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Comments> arrayList = new ArrayList<>();

    public CommentsAdapter(Context context, ArrayList<Comments> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public CommentsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_player_comments, parent, false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull CommentsAdapter.ViewHolder holder, int position) {
        Comments comment = arrayList.get(position);
        Glide.with(context).load(comment.getAuthorImage()).into(holder.player_comment_author_image);
        holder.player_comment_author_name.setText(comment.getAuthorName());
        holder.player_comment_text_original.setText(comment.getTextOriginal());
        String likes = "👍 " + comment.getLikeCount();
        holder.player_comment_likeCount.setText(likes);
        String date = comment.getPublishedAt();
        ZonedDateTime dateTime = ZonedDateTime.parse(date);
        String res = dateTime.withZoneSameInstant(ZoneId.of("IST")).format(DateTimeFormatter.ofPattern("⌚ dd MM yyyy | hh:mm:ss a"));
        holder.player_comment_publishedAt.setText(res);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public CircularImageView player_comment_author_image;
        public TextView player_comment_author_name;
        public TextView player_comment_text_original;
        public TextView player_comment_likeCount;
        public TextView player_comment_publishedAt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            player_comment_author_image = itemView.findViewById(R.id.player_comment_author_image);
            player_comment_author_name = itemView.findViewById(R.id.player_comment_author_name);
            player_comment_text_original = itemView.findViewById(R.id.player_comment_text_original);
            player_comment_likeCount = itemView.findViewById(R.id.player_comment_likeCount);
            player_comment_publishedAt = itemView.findViewById(R.id.player_comment_publishedAt);
        }
    }
}
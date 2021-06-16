package com.deorabanna1925.youtubeplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deorabanna1925.youtubeplayer.R;
import com.deorabanna1925.youtubeplayer.model.Notes;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {

    private SimpleExoPlayer player;
    private BottomSheetBehavior bottomSheetBehavior;
    private Context context;
    private ArrayList<Notes> arrayList = new ArrayList<>();

    public NotesAdapter(Context context, SimpleExoPlayer player, BottomSheetBehavior bottomSheetBehavior4, ArrayList<Notes> arrayList) {
        this.context = context;
        this.player = player;
        this.bottomSheetBehavior = bottomSheetBehavior4;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_player_notes, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notes notes = arrayList.get(position);
        holder.player_notes_time.setText(notes.getTime());
        holder.player_notes_text.setText(notes.getText());
        holder.itemView.setOnClickListener(v -> {
            player.seekTo(Long.parseLong(notes.getStamp()));
            player.play();
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView player_notes_time;
        public TextView player_notes_text;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            player_notes_time = itemView.findViewById(R.id.player_notes_time);
            player_notes_text = itemView.findViewById(R.id.player_notes_text);
        }
    }
}
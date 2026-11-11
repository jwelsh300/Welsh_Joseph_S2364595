package com.example.welsh_joseph_s2364595.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.welsh_joseph_s2364595.R;
import com.example.welsh_joseph_s2364595.model.Situation;

import java.util.ArrayList;
import java.util.List;

public class SituationAdapter extends RecyclerView.Adapter<SituationAdapter.ViewHolder> {

    private List<Situation> situations;

    public SituationAdapter() {
        situations = new ArrayList<>();
    }

    public void setSituations(List<Situation> newList) {
        situations = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_situation, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Situation s = situations.get(position);

        String title = s.getCategory();
        if (!s.getRoadName().isEmpty()) {
            title = title + " · " + s.getRoadName();
        }

        holder.rowTitle.setText(title);
        holder.rowComment.setText(s.getComment());
    }

    @Override
    public int getItemCount() {
        return situations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView rowTitle;
        private final TextView rowComment;

        ViewHolder(View v) {
            super(v);
            rowTitle = v.findViewById(R.id.rowTitle);
            rowComment = v.findViewById(R.id.rowComment);
        }
    }
}

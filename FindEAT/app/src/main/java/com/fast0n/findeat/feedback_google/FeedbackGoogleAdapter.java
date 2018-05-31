package com.fast0n.findeat.feedback_google;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fast0n.findeat.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FeedbackGoogleAdapter extends RecyclerView.Adapter<FeedbackGoogleAdapter.MyViewHolder> {

    private List<Feedback> feedbackList;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView nome, ora, feedback;
        public ImageView iconfeedback, iconrating;

        public MyViewHolder(View view) {
            super(view);
            nome = view.findViewById(R.id.nome);
            ora = view.findViewById(R.id.ora);
            feedback = view.findViewById(R.id.feedback);
            iconfeedback = view.findViewById(R.id.iconfeedback);
            iconrating = view.findViewById(R.id.iconrating);
        }
    }

    public FeedbackGoogleAdapter(List<Feedback> feedbackList) {
        this.feedbackList = feedbackList;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Feedback c = feedbackList.get(position);
        holder.nome.setText(c.nome);
        holder.ora.setText(c.ora);
        holder.feedback.setText(c.feedback);

        if (c.rating.equals("1"))
            Picasso.get().load(R.drawable.rating_1).into(holder.iconrating);
        else if (c.rating.equals("2"))
            Picasso.get().load(R.drawable.rating_2).into(holder.iconrating);
        else if (c.rating.equals("3"))
            Picasso.get().load(R.drawable.rating_3).into(holder.iconrating);
        else if (c.rating.equals("4"))
            Picasso.get().load(R.drawable.rating_4).into(holder.iconrating);
        else if (c.rating.equals("5"))
            Picasso.get().load(R.drawable.rating_5).into(holder.iconrating);

        Picasso.get().load(c.iconfeedback).into(holder.iconfeedback);
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_feedback, parent, false);
        return new MyViewHolder(v);
    }
}

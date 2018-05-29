package com.fast0n.findeat.feedback_findeat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fast0n.findeat.R;
import com.fast0n.findeat.feedback_google.Feedback;

import java.util.List;

public class FeedbackFindEATAdapter extends RecyclerView.Adapter<FeedbackFindEATAdapter.MyViewHolder> {

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

    public FeedbackFindEATAdapter(List<Feedback> feedbackList) {
        this.feedbackList = feedbackList;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Feedback c = feedbackList.get(position);
        holder.nome.setText(c.nome);
        holder.ora.setText(c.ora);
        holder.feedback.setText(c.feedback);

        holder.iconrating.setVisibility(View.GONE);
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

package com.fast0n.findeat.db_recents;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fast0n.findeat.R;

import java.util.List;

public class RecentsAdapter extends RecyclerView.Adapter<RecentsAdapter.MyViewHolder> {

    private Context context;
    private List<Recent> recordsList;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView record;

        MyViewHolder(View view) {
            super(view);
            record = view.findViewById(R.id.recent);
        }
    }

    public RecentsAdapter(Context context, List<Recent> recordsList) {
        this.context = context;
        this.recordsList = recordsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_record, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Recent recent = recordsList.get(position);

        String recordText = recent.getRecord().toUpperCase().charAt(0)
                + recent.getRecord().substring(1, recent.getRecord().length());
        holder.record.setText(recordText);

    }

    @Override
    public int getItemCount() {
        return recordsList.size();
    }

}
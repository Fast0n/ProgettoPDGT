package com.fast0n.findeat.database;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.fast0n.findeat.R;

import java.util.List;

public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.MyViewHolder> {

    private Context context;
    private List<Record> recordsList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView record;

        public MyViewHolder(View view) {
            super(view);
            record = view.findViewById(R.id.record);
        }
    }

    public RecordsAdapter(Context context, List<Record> recordsList) {
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
        Record record = recordsList.get(position);

        String recordText = record.getRecord().toUpperCase().charAt(0)+record.getRecord().substring(1,record.getRecord().length());
        holder.record.setText(recordText);

    }

    @Override
    public int getItemCount() {
        return recordsList.size();
    }

}
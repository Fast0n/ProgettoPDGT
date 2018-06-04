package com.fast0n.findeat.list_restaurants;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fast0n.findeat.R;

import java.util.List;

public class CustomAdapterRestaurants extends RecyclerView.Adapter<CustomAdapterRestaurants.MyViewHolder> {

    private List<DataRestaurants> countryList;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView nome, apertura, valutazione, indirizzo;

        MyViewHolder(View view) {
            super(view);
            // java addresses
            nome = view.findViewById(R.id.nome);
            apertura = view.findViewById(R.id.apertura);
            valutazione = view.findViewById(R.id.valutazione);
            indirizzo = view.findViewById(R.id.indirizzo);
        }
    }

    CustomAdapterRestaurants(List<DataRestaurants> countryList) {
        this.countryList = countryList;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        DataRestaurants c = countryList.get(position);
        holder.nome.setText(c.nome);
        holder.apertura.setText(c.apertura);
        holder.valutazione.setText(c.valutazione);
        holder.indirizzo.setText(c.indirizzo);
    }

    @Override
    public int getItemCount() {
        return countryList.size();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_restaurants, parent, false);
        return new MyViewHolder(v);
    }
}

package com.fast0n.findeat.db_favorites;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fast0n.findeat.R;
import com.fast0n.findeat.TabsActivity;
import com.fast0n.findeat.java.RecyclerItemListener;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class FavoritesActivity extends AppCompatActivity {

    private List<Favorite> recordsList = new ArrayList<>();
    private FavoritesAdapter mAdapter;

    RecyclerView recyclerView;
    private DatabaseFavorites db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView mTitle = toolbar.findViewById(R.id.toolbar_title);
        mTitle.setText(toolbar.getTitle());
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        db = new DatabaseFavorites(this);
        recordsList.addAll(db.getAllRecords());
        mAdapter = new FavoritesAdapter(this, recordsList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerItemListener(getApplicationContext(), recyclerView,
                new RecyclerItemListener.RecyclerTouchListener() {
                    public void onClickItem(View arg1, int position) {

                        if (isOnline()) {
                            TextView luogo = arg1.findViewById(R.id.recent);
                            String luogo1 = luogo.getText().toString();

                            Intent intent = new Intent(FavoritesActivity.this, TabsActivity.class);
                            intent.putExtra("nome", luogo1.split(" - ")[0]);
                            intent.putExtra("luogo", luogo1.split(" - ")[1].toLowerCase());
                            startActivity(intent);

                        } else
                            Toasty.error(FavoritesActivity.this, getString(R.string.noConnection), Toast.LENGTH_LONG)
                                    .show();

                    }

                    @Override
                    public void onLongClickItem(View v, int position) {
                        showActionsDialog(position);
                    }

                }));
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[] { getString(R.string.delete) };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sure);
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    deleteRecord(position);
                    if (db.getRecordsCount() > 0) {

                    } else {
                        recyclerView.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
        builder.show();
    }

    private void deleteRecord(int position) {
        db.deleteRecord(recordsList.get(position));
        recordsList.remove(position);
        mAdapter.notifyItemRemoved(position);

    }

}

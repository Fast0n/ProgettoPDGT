package com.fast0n.findeat.list_restaurants;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fast0n.findeat.MainActivity;
import com.fast0n.findeat.R;
import com.fast0n.findeat.RestorantsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RestaurantsActivityList extends AppCompatActivity {

    String luogo;
    ActionBar actionBar;
    ProgressBar loading;

    private List<DataRestaurants> countryList = new ArrayList<>();
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurants_list);

        // set row icon in the toolbar
        actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        recyclerView = findViewById(R.id.recycler_view);
        loading = findViewById(R.id.progressBar);
        loading.setVisibility(View.VISIBLE);
        final Bundle extras = getIntent().getExtras();
        assert extras != null;
        luogo = extras.getString("search");

        String title = luogo.toUpperCase().charAt(0) + luogo.substring(1, luogo.length());

        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.app_name) + " " + title);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        String cerca = "?tipo=luogo&lista=";
        String site_url = "https://progetto-pdgt.glitch.me/";
        String url = site_url + cerca + luogo;

        createList(url);

        recyclerView.addOnItemTouchListener(new RecyclerItemListener(getApplicationContext(), recyclerView,
                new RecyclerItemListener.RecyclerTouchListener() {
                    public void onClickItem(View arg1, int position) {
                        TextView getNome = arg1.findViewById(R.id.nome);
                        String nome = getNome.getText().toString();

                        Intent myIntent = new Intent(RestaurantsActivityList.this, RestorantsActivity.class);
                        myIntent.putExtra("nome", nome);
                        myIntent.putExtra("luogo", luogo);
                        RestaurantsActivityList.this.startActivity(myIntent);

                    }

                    public void onLongClickItem(View v, int position) {
                    }
                }));

    }

    private void createList(String url) {

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            JSONObject json_raw = new JSONObject(response.toString());
                            String lista = json_raw.getString("lista");
                            JSONArray arraylista = new JSONArray(lista);

                            int nElementi = Integer.parseInt(String.valueOf(arraylista.length())) - 1;

                            for (int i = 0; i < nElementi; i++) {
                                String ristoranti = arraylista.getString(i);

                                JSONObject scorroRistoranti = new JSONObject(ristoranti);

                                String id = scorroRistoranti.getString("id");
                                String nome = scorroRistoranti.getString("nome");
                                String apertura = scorroRistoranti.getString("apertura").replace("null",
                                        "Apertura non disponibile");
                                String valutazione = scorroRistoranti.getString("valutazione");
                                String indirizzo = scorroRistoranti.getString("indirizzo");

                                countryList.add(new DataRestaurants(nome, apertura, valutazione, indirizzo));
                            }
                            CustomAdapterRestaurants ca = new CustomAdapterRestaurants(countryList);
                            recyclerView.setAdapter(ca);
                            loading.setVisibility(View.INVISIBLE);

                        } catch (JSONException ignored) {
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        int error_code = error.networkResponse.statusCode;

                        if (error_code == 404) {
                            countryList.add(new DataRestaurants("Errore API... riprova", "Apertura non disponibile", "Valutazione non disponibile", "Indirizzo non disponibile"));
                            CustomAdapterRestaurants ca = new CustomAdapterRestaurants(countryList);
                            recyclerView.setAdapter(ca);
                            loading.setVisibility(View.INVISIBLE);
                        }


                    }
                });

        // add it to the RequestQueue
        queue.add(getRequest);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case android.R.id.home:

            finish();

            Intent mainActivity = new Intent(RestaurantsActivityList.this, MainActivity.class);

            startActivity(mainActivity);

            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onBackPressed() {
        finish();
        Intent mainActivity = new Intent(RestaurantsActivityList.this, MainActivity.class);
        startActivity(mainActivity);
    }
}

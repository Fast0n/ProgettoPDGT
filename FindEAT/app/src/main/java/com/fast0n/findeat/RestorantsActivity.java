package com.fast0n.findeat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fast0n.findeat.list_restaurants.CustomAdapterRestaurants;
import com.fast0n.findeat.list_restaurants.DataRestaurants;
import com.robertlevonyan.views.customfloatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class RestorantsActivity extends AppCompatActivity {

    String getNome, getLuogo;
    TextView apertura, numtell, valutazione, sitoweb, indirizzo, orari;
    FloatingActionButton fab;
    RelativeLayout click1, click2, click3;
    RelativeLayout times;
    ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restorants);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = findViewById(R.id.fab);
        numtell = findViewById(R.id.numtell);
        sitoweb = findViewById(R.id.sitoweb);
        indirizzo = findViewById(R.id.indirizzo);
        valutazione = findViewById(R.id.valutazione);
        apertura = findViewById(R.id.apertura);
        orari = findViewById(R.id.orari);
        click1 = findViewById(R.id.click1);
        click2 = findViewById(R.id.click2);
        click3 = findViewById(R.id.click3);
        times = findViewById(R.id.times);
        loading = findViewById(R.id.progressBar);
        loading.setVisibility(View.VISIBLE);


        final Bundle extras = getIntent().getExtras();
        assert extras != null;
        getNome = extras.getString("nome");
        getLuogo = extras.getString("luogo");

        Objects.requireNonNull(getSupportActionBar()).setTitle(getNome);

        String cerca = "?tipo=diretto&lista=";
        String site_url = "https://progetto-pdgt.glitch.me/";
        String url = site_url + cerca + getNome + " " + getLuogo;

        RequestQueue queue = Volley.newRequestQueue(this);
        url = url.replaceAll(" ", "%20");

        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            JSONObject json_raw = new JSONObject(response.toString());
                            String lista = json_raw.getString("lista");
                            String listaorari = json_raw.getString("orari").replace("\\[|]", "Non disponibile");

                            JSONArray arraylista = new JSONArray(lista);

                            String listaLista = arraylista.getString(0);

                            if (!listaorari.equals("Non disponibile")) {
                                JSONArray arraylistaorari = new JSONArray(listaorari);

                                String listaListaOrari = arraylistaorari.getString(0);

                                orari.setText(listaListaOrari.replaceAll(",", "\n").replaceAll("\"", "")
                                        .replaceAll("\\[|]", ""));
                            } else {
                                orari.setText("Non disponibile");

                            }

                            JSONObject scorroLista = new JSONObject(listaLista);

                            String getIndirizzo = scorroLista.getString("indirizzo");
                            String getValutazione = scorroLista.getString("valutazione");
                            String getNumtell = scorroLista.getString("numtell").replace("null", "Non disponibile");
                            String getApertura = scorroLista.getString("apertura").replace("null", "Non disponibile");
                            String getSitoweb = scorroLista.getString("sitoweb").replace("null", "Non disponibile");

                            if (getApertura.equals("Aperto")) {
                                fab.setFabText("Aperto");
                                fab.setFabColor(getResources().getColor(R.color.aperto));
                                fab.getLayoutParams().width = 200;
                            } else {
                                fab.setFabText("Chiuso");
                                times.setVisibility(View.VISIBLE);
                                fab.setFabColor(getResources().getColor(R.color.chiuso));
                                fab.getLayoutParams().width = 200;

                            }
                            indirizzo.setText(getIndirizzo);
                            valutazione.setText(getValutazione);
                            numtell.setText(getNumtell);
                            sitoweb.setText(getSitoweb);
                            loading.setVisibility(View.INVISIBLE);
                        } catch (JSONException ignored) {
                        }

                    }

                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        int error_code = error.networkResponse.statusCode;

                    }
                });

        // add it to the RequestQueue
        queue.add(getRequest);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (fab.getFabText().equals("Aperto")) {
                    Toasty.success(RestorantsActivity.this, fab.getFabText(), Toast.LENGTH_LONG).show();
                } else {
                    Toasty.error(RestorantsActivity.this, fab.getFabText(), Toast.LENGTH_LONG).show();

                }


            }
        });

        click1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!numtell.getText().equals("Non disponibile")) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + numtell.getText()));
                    startActivity(intent);
                }
            }
        });

        click2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sitoweb.getText().equals("Non disponibile")) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) sitoweb.getText()));
                    startActivity(browserIntent);
                }
            }
        });

        click3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!indirizzo.getText().equals("Non disponibile")) {
                    String url = "http://maps.google.com/maps?daddr=" + indirizzo.getText();
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            }
        });

    }

}

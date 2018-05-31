package com.fast0n.findeat.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fast0n.findeat.MainActivity;
import com.fast0n.findeat.R;
import com.fast0n.findeat.db_favorites.DatabaseFavorites;
import com.fast0n.findeat.db_favorites.Favorite;
import com.robertlevonyan.views.customfloatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class OneFragment extends Fragment {

    List<Favorite> recordsList = new ArrayList<>();
    DatabaseFavorites db;

    public OneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.activity_fragment_one, container, false);

        return view;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = new DatabaseFavorites(Objects.requireNonNull(getActivity()).getApplicationContext());

        final String getNome, getLuogo;
        final TextView tvName, tvNumtell, tvValutazione, tvSitoweb, tvIndirizzo, tvOrari;
        final FloatingActionButton fab, fab1, fab2;
        final CardView click1, click2, click3, click4, click5, click6;
        final ProgressBar loading;

        fab = view.findViewById(R.id.fab);
        fab1 = view.findViewById(R.id.fab1);
        fab2 = view.findViewById(R.id.fab2);

        tvName = view.findViewById(R.id.nome);
        tvNumtell = view.findViewById(R.id.numtell);
        tvSitoweb = view.findViewById(R.id.sitoweb);
        tvIndirizzo = view.findViewById(R.id.indirizzo);
        tvValutazione = view.findViewById(R.id.valutazione);
        tvOrari = view.findViewById(R.id.orari);
        click1 = view.findViewById(R.id.click1);
        click2 = view.findViewById(R.id.click2);
        click3 = view.findViewById(R.id.click3);
        click4 = view.findViewById(R.id.click4);
        click5 = view.findViewById(R.id.click5);
        click6 = view.findViewById(R.id.click6);
        loading = view.findViewById(R.id.progressBar);
        loading.setVisibility(View.VISIBLE);

        Bundle extras = getActivity().getIntent().getExtras();
        assert extras != null;
        getNome = extras.getString("nome");
        getLuogo = extras.getString("luogo");

        String cerca = "?tipo=diretto&lista=";
        String site_url = "https://progetto-pdgt.glitch.me/";
        String url = site_url + cerca + getNome + " " + getLuogo;

        RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(getActivity()).getApplicationContext());
        url = url.replaceAll(" ", "%20");

        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONObject json_raw = new JSONObject(response.toString());
                            String listaorari = json_raw.getString("orari");
                            JSONArray arraylistaorari = new JSONArray(listaorari);
                            String listaListaOrari = arraylistaorari.getString(0);
                            tvOrari.setText(listaListaOrari.replaceAll("\"", "").replaceAll("\\[|]", ""));
                        } catch (JSONException e) {
                            tvOrari.setText(getString(R.string.unavailable));
                        }

                        try {

                            JSONObject json_raw = new JSONObject(response.toString());
                            String lista = json_raw.getString("lista");
                            String listaorari = json_raw.getString("orari");

                            JSONArray arraylista = new JSONArray(lista);
                            String listaLista = arraylista.getString(0);

                            JSONObject scorroLista = new JSONObject(listaLista);

                            String getNome2 = scorroLista.getString("nome");
                            String getIndirizzo = scorroLista.getString("indirizzo");
                            String getValutazione = scorroLista.getString("valutazione");
                            String getNumtell = scorroLista.getString("numtell").replace("null", getString(R.string.unavailable));
                            String getApertura = scorroLista.getString("apertura").replace("null", getString(R.string.unavailable));
                            String getSitoweb = scorroLista.getString("sitoweb").replace("null", getString(R.string.unavailable));

                            if (getApertura.equals("Aperto")) {
                                click1.setCardBackgroundColor(
                                        ContextCompat.getColor(getActivity().getApplicationContext(), R.color.aperto));
                                click2.setCardBackgroundColor(
                                        ContextCompat.getColor(getActivity().getApplicationContext(), R.color.aperto));
                                click3.setCardBackgroundColor(
                                        ContextCompat.getColor(getActivity().getApplicationContext(), R.color.aperto));
                                click4.setCardBackgroundColor(
                                        ContextCompat.getColor(getActivity().getApplicationContext(), R.color.aperto));
                                click5.setCardBackgroundColor(
                                        ContextCompat.getColor(getActivity().getApplicationContext(), R.color.aperto));
                                click6.setCardBackgroundColor(
                                        ContextCompat.getColor(getActivity().getApplicationContext(), R.color.aperto));

                                fab.setFabText("Aperto");
                                fab.setFabColor(
                                        ContextCompat.getColor(getActivity().getApplicationContext(), R.color.aperto));
                                fab.getLayoutParams().width = 300;
                            } else {
                                click1.setCardBackgroundColor(
                                        ContextCompat.getColor(getActivity().getApplicationContext(), R.color.chiuso));
                                click2.setCardBackgroundColor(
                                        ContextCompat.getColor(getActivity().getApplicationContext(), R.color.chiuso));
                                click3.setCardBackgroundColor(
                                        ContextCompat.getColor(getActivity().getApplicationContext(), R.color.chiuso));
                                click4.setCardBackgroundColor(
                                        ContextCompat.getColor(getActivity().getApplicationContext(), R.color.chiuso));
                                click5.setCardBackgroundColor(
                                        ContextCompat.getColor(getActivity().getApplicationContext(), R.color.chiuso));
                                click6.setCardBackgroundColor(
                                        ContextCompat.getColor(getActivity().getApplicationContext(), R.color.chiuso));

                                click6.setVisibility(View.VISIBLE);
                                fab.setFabText("Chiuso");
                                fab.setFabColor(
                                        ContextCompat.getColor(getActivity().getApplicationContext(), R.color.chiuso));
                                fab.getLayoutParams().width = 300;
                            }

                            tvName.setText(getNome2);
                            tvIndirizzo.setText(getIndirizzo);
                            tvValutazione.setText(getValutazione);
                            tvNumtell.setText(getNumtell);
                            tvSitoweb.setText(getSitoweb);
                            loading.setVisibility(View.INVISIBLE);

                        } catch (JSONException ignored) {
                        }

                    }

                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        int error_code = error.networkResponse.statusCode;

                        if (error_code == 503) {
                            Intent intent = new Intent((Objects.requireNonNull(getActivity()).getApplicationContext()),
                                    MainActivity.class);
                            startActivity(intent);
                            Toasty.error((Objects.requireNonNull(getActivity()).getApplicationContext()),
                                    getString(R.string.error_api), Toast.LENGTH_LONG).show();

                        }

                    }
                });

        // add it to the RequestQueue
        queue.add(getRequest);

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    final String stringLuogo = getLuogo.toUpperCase().charAt(0)
                            + getLuogo.substring(1, getLuogo.length());
                    createRecord(getNome + " - " + stringLuogo);
                } catch (Exception ignored) {
                }
                fab2.setFabIcon(
                        ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.ic_favorite));
                fab.getLayoutParams().width = 300;

            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("smsto:" + tvNumtell.getText());
                Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                it.putExtra("sms_body", "Salve la contatto per avere delle informazioni\n");
                startActivity(it);
            }
        });

        click2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!tvNumtell.getText().equals(getString(R.string.unavailable))) {
                    // fab1.setVisibility(View.VISIBLE);
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + tvNumtell.getText()));
                    startActivity(intent);
                }
            }
        });

        click3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!tvSitoweb.getText().equals(getString(R.string.unavailable))) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) tvSitoweb.getText()));
                    startActivity(browserIntent);
                }
            }
        });

        click4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!tvIndirizzo.getText().equals(getString(R.string.unavailable))) {
                    String url = "http://maps.google.com/maps?daddr=" + tvIndirizzo.getText();
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            }
        });

    }

    private void updateNote(String note, int position) {
        Favorite n = recordsList.get(position);

        System.out.println(n);

    }

    private void createRecord(String favotite) {
        long id = db.insertRecord(favotite);
        Favorite n = db.getRecord(id);

        if (n != null) {
            recordsList.add(0, n);
        }
    }

}

package com.fast0n.findeat.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fast0n.findeat.R;
import com.fast0n.findeat.feedback_google.Feedback;
import com.fast0n.findeat.feedback_google.FeedbackGoogleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ThreeFragment extends Fragment {

    public ThreeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.activity_fragment_three, container, false);
        String getNome, getLuogo;
        final List<Feedback> feedbackList = new ArrayList<>();
        final RecyclerView recycler_view;

        // java addresses
        recycler_view = view.findViewById(R.id.recycler_view);

        recycler_view.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(
                Objects.requireNonNull(getActivity()).getApplicationContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recycler_view.setLayoutManager(llm);

        Bundle extras = getActivity().getIntent().getExtras();
        assert extras != null;
        getNome = extras.getString("nome");
        getLuogo = extras.getString("luogo");

        String cerca = "?tipo=diretto&lista=";
        String site_url = getString(R.string.site_url);
        String url = site_url + cerca + getNome + " " + getLuogo;

        RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(getActivity()).getApplicationContext());
        url = url.replaceAll(" ", "%20");

        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            JSONObject json_raw = new JSONObject(response.toString());
                            String lista = json_raw.getString("feedback");
                            JSONArray arraylista = new JSONArray(lista);

                            int nElementi = Integer.parseInt(String.valueOf(arraylista.length())) - 1;

                            for (int i = 0; i < nElementi; i++) {
                                String ristoranti = arraylista.getString(i);

                                JSONObject scorroRistoranti = new JSONObject(ristoranti);
                                String nome = scorroRistoranti.getString("author_name");
                                String ora = scorroRistoranti.getString("time");
                                String feedback = scorroRistoranti.getString("text");
                                String iconfeedback = scorroRistoranti.getString("profile_photo_url");
                                String rating = scorroRistoranti.getString("rating");

                                Calendar cal = Calendar.getInstance(Locale.ITALIAN);
                                cal.setTimeInMillis(Integer.parseInt(ora) * 1000L);
                                String date = DateFormat.format("dd/MM/yyyy", cal).toString();

                                feedbackList.add(new Feedback(nome, date, feedback, iconfeedback, rating));
                            }
                            FeedbackGoogleAdapter ca = new FeedbackGoogleAdapter(feedbackList);
                            recycler_view.setAdapter(ca);

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

        return view;
    }

}

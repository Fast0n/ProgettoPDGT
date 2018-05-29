package com.fast0n.findeat.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fast0n.findeat.R;
import com.fast0n.findeat.feedback_findeat.FeedbackFindEATAdapter;
import com.fast0n.findeat.feedback_google.Feedback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class TwoFragment extends Fragment{

    public TwoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.activity_fragment_two, container, false);

        final String getNome, getLuogo;
        final TextView tvCounter, tvCartadicredito, tvCosto, tvCeliaci, tvBambino;
        final ProgressBar progressCartadicredito, progressCosto, progressCeliaci, progressBambino;
        final List<Feedback> feedbackList = new ArrayList<>();
        final RecyclerView recycler_view;

        recycler_view = view.findViewById(R.id.recycler_view);

        recycler_view.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(Objects.requireNonNull(getActivity()).getApplicationContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recycler_view.setLayoutManager(llm);


        tvCounter = view.findViewById(R.id.contatore);
        tvCartadicredito = view.findViewById(R.id.cartadicredito);
        tvCosto = view.findViewById(R.id.costo);
        tvCeliaci = view.findViewById(R.id.celiaci);
        tvBambino = view.findViewById(R.id.bambino);


        progressCartadicredito = view.findViewById(R.id.progressCartadicredito);
        progressCosto = view.findViewById(R.id.progressCosto);
        progressCeliaci = view.findViewById(R.id.progressCeliaci);
        progressBambino = view.findViewById(R.id.progressBambino);



        Bundle extras = getActivity().getIntent().getExtras();
        assert extras != null;
        //getNome = extras.getString("nome");
        getLuogo = extras.getString("luogo");

        getNome = "Il Giardino della Galla Ristorante Pizzeria Urbino";

        final FirebaseDatabase database;
        final DatabaseReference databaseRef;

        /**
         * Firebase
         */
        database = FirebaseDatabase.getInstance();
        databaseRef = database.getReference();


        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                showData(dataSnapshot);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

            private void showData(DataSnapshot dataSnapshot) {

                for (final DataSnapshot ds : dataSnapshot.getChildren()) {

                    final Query query = databaseRef.child("filters").child(getLuogo).child(getNome);

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {




                            String stringContatore = String.valueOf(ds.child(getLuogo).child(getNome).child("contatore").child("0").getValue());

                            if (!stringContatore.equals("null")) {
                                if (stringContatore.equals("1"))
                                    tvCounter.setText("Cercato " + stringContatore + " volta");
                                else
                                    tvCounter.setText("Cercato " + stringContatore + " volte");}

                            try {
                                String stringCartadicredito = String.valueOf(ds.child(getLuogo).child(getNome).child("cartadicredito").child("votisi").getValue());
                                String stringCartadicredito1 = String.valueOf(ds.child(getLuogo).child(getNome).child("cartadicredito").child("votino").getValue());

                                String stringCosto= String.valueOf(ds.child(getLuogo).child(getNome).child("costo").child("basso").getValue());
                                String stringCosto1 = String.valueOf(ds.child(getLuogo).child(getNome).child("costo").child("alto").getValue());

                                String stringCeliaci1 = String.valueOf(ds.child(getLuogo).child(getNome).child("menu").child("0").child("celiaci").child("votisi").getValue());
                                String stringCeliaci = String.valueOf(ds.child(getLuogo).child(getNome).child("menu").child("0").child("celiaci").child("votino").getValue());


                                String stringBambino1 = String.valueOf(ds.child(getLuogo).child(getNome).child("menu").child("1").child("bambino").child("votisi").getValue());
                                String stringBambino = String.valueOf(ds.child(getLuogo).child(getNome).child("menu").child("1").child("bambino").child("votino").getValue());





                                tvCartadicredito.setText(stringCartadicredito + " voti per 'Si' su " + stringCartadicredito1 + " voti su 'No'");
                                tvCosto.setText(stringCosto + " voti per 'Basso' su " + stringCosto1 + " voti su 'Alto'");
                                tvCeliaci.setText(stringCeliaci + " voti per 'Si' su " + stringCeliaci1 + " voti su 'No'");
                                tvBambino.setText(stringBambino + " voti per 'Si' su " + stringBambino1 + " voti su 'No'");



                                double a =Double.parseDouble(stringCartadicredito);
                                double b = Double.parseDouble(stringCartadicredito1);

                                double c =Double.parseDouble(stringCosto);
                                double d = Double.parseDouble(stringCosto1);

                                double e =Double.parseDouble(stringCeliaci);
                                double f = Double.parseDouble(stringCeliaci1);

                                double g =Double.parseDouble(stringBambino);
                                double h = Double.parseDouble(stringBambino1);


                                Double ab = (a/(a+b))*100;
                                Double cd = (c/(c+d))*100;

                                Double ef = (e/(e+f))*100;
                                Double gh = (g/(g+h))*100;

                                int result = ab.intValue();
                                int result1 = cd.intValue();
                                int result2 = ef.intValue();
                                int result3 = gh.intValue();

                                progressCartadicredito.setProgress( result );
                                progressCosto.setProgress( result1 );
                                progressCeliaci.setProgress( result2 );
                                progressBambino.setProgress( result3 );


                            }

                            catch (Exception e){

                                tvCartadicredito.setText("0 voti per 'Si' su 0 voti su 'No'");
                                tvCosto.setText("0 voti per 'Basso' su 0 voti su 'Alto'");
                                tvCeliaci.setText("0 voti per 'Si' su 0 voti su 'No'");
                                tvBambino.setText("0 voti per 'Si' su 0 voti su 'No'");

                                progressCartadicredito.setProgress( 0 );
                                progressCosto.setProgress( 0 );
                                progressCeliaci.setProgress( 0 );
                                progressBambino.setProgress( 0 );
                            }


                        }



                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }


                    });


                    final Query query1 = databaseRef.child("restaurants").child(getLuogo).child(getNome);

                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String stringFeedback = String.valueOf(ds.child(getLuogo).child(getNome).getChildrenCount());

                            int len = Integer.parseInt(stringFeedback);

                            for (int i = 0; i<len; i++){

                                String author_name = String.valueOf(ds.child(getLuogo).child(getNome).child(String.valueOf(i)).child("author_name").getValue());
                                String text = String.valueOf(ds.child(getLuogo).child(getNome).child(String.valueOf(i)).child("text").getValue());
                                String time = String.valueOf(ds.child(getLuogo).child(getNome).child(String.valueOf(i)).child("time").getValue());

                                if (!author_name.equals("null")){

                                    Calendar cal = Calendar.getInstance(Locale.ITALIAN);
                                    cal.setTimeInMillis(Integer.parseInt(time) * 1000L);
                                    String date = DateFormat.format("dd/MM/yyyy", cal).toString();

                                    feedbackList.add(new Feedback(author_name, date, text, "", ""));
                                    FeedbackFindEATAdapter ca = new FeedbackFindEATAdapter(feedbackList);
                                    recycler_view.setAdapter(ca);

                                }

                            }





                        }



                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }


                    });





                }
            }
        });




            return view;
    }

}

package com.fast0n.findeat.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fast0n.findeat.R;
import com.fast0n.findeat.feedback_findeat.FeedbackFindEATAdapter;
import com.fast0n.findeat.feedback_google.Feedback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.robertlevonyan.views.customfloatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;


public class TwoFragment extends Fragment {

    public TwoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.activity_fragment_two, container, false);

        final String getNome, getLuogo;
        final TextView tvCounter, tvCartadicredito, tvCosto, tvCeliaci, tvBambino;
        final ProgressBar progressCartadicredito, progressCosto, progressCeliaci, progressBambino;
        final List<Feedback> feedbackList = new ArrayList<>();
        final RecyclerView recycler_view;
        final FloatingActionButton fab;

        recycler_view = view.findViewById(R.id.recycler_view);

        recycler_view.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(
                Objects.requireNonNull(getActivity()).getApplicationContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recycler_view.setLayoutManager(llm);

        // java addresses
        tvCounter = view.findViewById(R.id.contatore);
        tvCartadicredito = view.findViewById(R.id.cartadicredito);
        tvCosto = view.findViewById(R.id.costo);
        tvCeliaci = view.findViewById(R.id.celiaci);
        tvBambino = view.findViewById(R.id.bambino);
        fab = view.findViewById(R.id.fab);

        progressCartadicredito = view.findViewById(R.id.progressCartadicredito);
        progressCosto = view.findViewById(R.id.progressCosto);
        progressCeliaci = view.findViewById(R.id.progressCeliaci);
        progressBambino = view.findViewById(R.id.progressBambino);

        Bundle extras = getActivity().getIntent().getExtras();
        assert extras != null;
        getNome = extras.getString("nome");
        getLuogo = extras.getString("luogo");

        final FirebaseDatabase database;
        final DatabaseReference databaseRef;

        /**
         * Firebase
         */
        database = FirebaseDatabase.getInstance();
        databaseRef = database.getReference();

        final boolean[] counter = { true };
        final boolean[] read_feedback = { true };
        final boolean[] add_feedback = new boolean[1];

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (counter[0]) {
                    String stringContatore = String.valueOf(dataSnapshot.child("filters").child(getLuogo).child(getNome)
                            .child("contatore").child("0").getValue());

                    if (!stringContatore.equals("null")) {
                        int piu = Integer.parseInt(stringContatore);
                        databaseRef.child("filters").child(getLuogo).child(getNome).child("contatore").child("0")
                                .setValue(String.valueOf(++piu));
                        counter[0] = false;
                        tvCounter.setText(getString(R.string.tvCounter_one) +" " + String.valueOf(piu)
                                + " " +getString(R.string.tvCounter_two));

                    } else {
                        databaseRef.child("filters").child(getLuogo).child(getNome).child("cartadicredito")
                                .child("votisi").setValue(String.valueOf(0));
                        databaseRef.child("filters").child(getLuogo).child(getNome).child("cartadicredito")
                                .child("votino").setValue(String.valueOf(0));
                        databaseRef.child("filters").child(getLuogo).child(getNome).child("costo").child("alto")
                                .setValue(String.valueOf(0));
                        databaseRef.child("filters").child(getLuogo).child(getNome).child("costo").child("basso")
                                .setValue(String.valueOf(0));
                        databaseRef.child("filters").child(getLuogo).child(getNome).child("menu").child("0")
                                .child("celiaci").child("votisi").setValue(String.valueOf(0));
                        databaseRef.child("filters").child(getLuogo).child(getNome).child("menu").child("0")
                                .child("celiaci").child("votino").setValue(String.valueOf(0));
                        databaseRef.child("filters").child(getLuogo).child(getNome).child("menu").child("1")
                                .child("bambino").child("votisi").setValue(String.valueOf(0));
                        databaseRef.child("filters").child(getLuogo).child(getNome).child("menu").child("1")
                                .child("bambino").child("votino").setValue(String.valueOf(0));
                        databaseRef.child("filters").child(getLuogo).child(getNome).child("contatore").child("0")
                                .setValue(1);
                        counter[0] = false;
                        tvCounter.setText(R.string.tvCounter);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String stringContatore = String
                        .valueOf(dataSnapshot.child("restaurants").child(getLuogo).child(getNome).getChildrenCount());
                int len = Integer.parseInt(stringContatore);

                if (read_feedback[0]) {
                    for (int i = 0; i < len; i++) {

                        String author_name = String.valueOf(dataSnapshot.child("restaurants").child(getLuogo)
                                .child(getNome).child(String.valueOf(i)).child("author_name").getValue());
                        String text = String.valueOf(dataSnapshot.child("restaurants").child(getLuogo).child(getNome)
                                .child(String.valueOf(i)).child("text").getValue());
                        String time = String.valueOf(dataSnapshot.child("restaurants").child(getLuogo).child(getNome)
                                .child(String.valueOf(i)).child("time").getValue());

                        if (!time.equals("null")) {
                            Calendar cal = Calendar.getInstance(Locale.ITALIAN);
                            cal.setTimeInMillis(Integer.parseInt(time) * 1000L);
                            String date = DateFormat.format("dd/MM/yyyy", cal).toString();
                            feedbackList.add(new Feedback(author_name, date, text, "", ""));
                            FeedbackFindEATAdapter ca = new FeedbackFindEATAdapter(getActivity(),feedbackList);
                            recycler_view.setAdapter(ca);
                            read_feedback[0] = false;
                        }
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

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

                            String stringCartadicredito = String.valueOf(ds.child(getLuogo).child(getNome)
                                    .child("cartadicredito").child("votisi").getValue());


                                String stringCartadicredito1 = String.valueOf(ds.child(getLuogo).child(getNome)
                                        .child("cartadicredito").child("votino").getValue());

                                String stringCosto = String.valueOf(
                                        ds.child(getLuogo).child(getNome).child("costo").child("basso").getValue());
                                String stringCosto1 = String.valueOf(
                                        ds.child(getLuogo).child(getNome).child("costo").child("alto").getValue());

                                String stringCeliaci = String.valueOf(ds.child(getLuogo).child(getNome).child("menu")
                                        .child("0").child("celiaci").child("votisi").getValue());
                                String stringCeliaci1 = String.valueOf(ds.child(getLuogo).child(getNome).child("menu")
                                        .child("0").child("celiaci").child("votino").getValue());

                                String stringBambino = String.valueOf(ds.child(getLuogo).child(getNome).child("menu")
                                        .child("1").child("bambino").child("votisi").getValue());
                                String stringBambino1 = String.valueOf(ds.child(getLuogo).child(getNome).child("menu")
                                        .child("1").child("bambino").child("votino").getValue());

                                try {
                                    tvCartadicredito.setText(stringCartadicredito + " "+getString(R.string.si)+", "
                                            + stringCartadicredito1 + " " + getString(R.string.no));
                                    tvCosto.setText(stringCosto + " "+getString(R.string.si)+", "
                                            + stringCosto1 + " " + getString(R.string.no));
                                    tvCeliaci.setText(stringCeliaci + " "+getString(R.string.si)+", "
                                            + stringCeliaci1 + " " + getString(R.string.no));
                                    tvBambino.setText(stringBambino + " "+getString(R.string.si)+", "
                                            + stringBambino1 + " " + getString(R.string.no));

                                    double a = Double.parseDouble(stringCartadicredito);
                                    double b = Double.parseDouble(stringCartadicredito1);

                                    double c = Double.parseDouble(stringCosto);
                                    double d = Double.parseDouble(stringCosto1);

                                    double e = Double.parseDouble(stringCeliaci);
                                    double f = Double.parseDouble(stringCeliaci1);

                                    double g = Double.parseDouble(stringBambino);
                                    double h = Double.parseDouble(stringBambino1);

                                    Double ab = (a / (a + b)) * 100;
                                    Double cd = (c / (c + d)) * 100;

                                    Double ef = (e / (e + f)) * 100;
                                    Double gh = (g / (g + h)) * 100;

                                    int result = ab.intValue();
                                    int result1 = cd.intValue();
                                    int result2 = ef.intValue();
                                    int result3 = gh.intValue();

                                    progressCartadicredito.setProgress(result);
                                    progressCosto.setProgress(result1);
                                    progressCeliaci.setProgress(result2);
                                    progressBambino.setProgress(result3);
                                } catch (Exception ignored) {

                                }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }

                    });

                }
            }

        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
                View mView = getLayoutInflater().inflate(R.layout.activity_dialog, null);
                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();

                final Button button;
                final EditText editText, editText1;
                final CheckBox checkBox, checkBox2, checkBox3, checkBox4, checkBox5, checkBox6, checkBox7, checkBox8;

                // java addresses
                button = mView.findViewById(R.id.button);
                checkBox = mView.findViewById(R.id.checkBox);
                checkBox2 = mView.findViewById(R.id.checkBox2);
                checkBox3 = mView.findViewById(R.id.checkBox3);
                checkBox4 = mView.findViewById(R.id.checkBox4);
                checkBox5 = mView.findViewById(R.id.checkBox5);
                checkBox6 = mView.findViewById(R.id.checkBox6);
                checkBox7 = mView.findViewById(R.id.checkBox7);
                checkBox8 = mView.findViewById(R.id.checkBox8);

                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            checkBox.setChecked(true);
                            checkBox2.setChecked(false);
                        }
                    }
                });

                checkBox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            checkBox.setChecked(false);
                            checkBox2.setChecked(true);
                        }
                    }
                });

                checkBox3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            checkBox3.setChecked(true);
                            checkBox4.setChecked(false);
                        }
                    }
                });

                checkBox4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            checkBox3.setChecked(false);
                            checkBox4.setChecked(true);
                        }
                    }
                });

                checkBox5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            checkBox5.setChecked(true);
                            checkBox6.setChecked(false);
                        }
                    }
                });

                checkBox6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            checkBox5.setChecked(false);
                            checkBox6.setChecked(true);
                        }
                    }
                });

                checkBox7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            checkBox7.setChecked(true);
                            checkBox8.setChecked(false);
                        }
                    }
                });

                checkBox8.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            checkBox7.setChecked(false);
                            checkBox8.setChecked(true);
                        }
                    }
                });

                editText = mView.findViewById(R.id.editText);
                editText1 = mView.findViewById(R.id.editText1);

                final String timeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + "";

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (editText1.getText().length() == 0) {
                            Toasty.error(Objects.requireNonNull(getActivity()).getApplicationContext(), getString(R.string.toasty_null),
                                    Toast.LENGTH_LONG).show();
                        }

                        if (editText1.getText().length() > 5) {

                            add_feedback[0] = true;

                            databaseRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (add_feedback[0]) {

                                        final String stringContatore = String.valueOf(dataSnapshot.child("restaurants")
                                                .child(getLuogo).child(getNome).getChildrenCount());

                                        final String stringCartadicredito = String
                                                .valueOf(dataSnapshot.child("filters").child(getLuogo).child(getNome)
                                                        .child("cartadicredito").child("votisi").getValue());
                                        final String stringCartadicredito1 = String
                                                .valueOf(dataSnapshot.child("filters").child(getLuogo).child(getNome)
                                                        .child("cartadicredito").child("votino").getValue());

                                        final String stringCosto = String
                                                .valueOf(dataSnapshot.child("filters").child(getLuogo).child(getNome)
                                                        .child("costo").child("alto").getValue());
                                        final String stringCosto1 = String
                                                .valueOf(dataSnapshot.child("filters").child(getLuogo).child(getNome)
                                                        .child("costo").child("basso").getValue());

                                        final String stringceliaci = String.valueOf(dataSnapshot.child("filters")
                                                .child(getLuogo).child(getNome).child("menu").child("0")
                                                .child("celiaci").child("votisi").getValue());
                                        final String stringceliaci1 = String.valueOf(dataSnapshot.child("filters")
                                                .child(getLuogo).child(getNome).child("menu").child("0")
                                                .child("celiaci").child("votino").getValue());

                                        final String stringbambino = String.valueOf(dataSnapshot.child("filters")
                                                .child(getLuogo).child(getNome).child("menu").child("1")
                                                .child("bambino").child("votisi").getValue());
                                        final String stringbambino1 = String.valueOf(dataSnapshot.child("filters")
                                                .child(getLuogo).child(getNome).child("menu").child("1")
                                                .child("bambino").child("votino").getValue());

                                        if (!stringContatore.equals("null")) {

                                            int intCartadicredito = Integer.parseInt(stringCartadicredito);
                                            int intCartadicredito1 = Integer.parseInt(stringCartadicredito1);

                                            int intCosto = Integer.parseInt(stringCosto);
                                            int intCosto1 = Integer.parseInt(stringCosto1);

                                            int intceliaci = Integer.parseInt(stringceliaci);
                                            int intceliaci1 = Integer.parseInt(stringceliaci1);

                                            int intbambino = Integer.parseInt(stringbambino);
                                            int intbambino1 = Integer.parseInt(stringbambino1);

                                            if (checkBox.isChecked())
                                                databaseRef.child("filters").child(getLuogo).child(getNome)
                                                        .child("cartadicredito").child("votisi")
                                                        .setValue(String.valueOf(++intCartadicredito));
                                            if (checkBox2.isChecked())
                                                databaseRef.child("filters").child(getLuogo).child(getNome)
                                                        .child("cartadicredito").child("votino")
                                                        .setValue(String.valueOf(++intCartadicredito1));
                                            if (checkBox3.isChecked())
                                                databaseRef.child("filters").child(getLuogo).child(getNome)
                                                        .child("costo").child("alto")
                                                        .setValue(String.valueOf(++intCosto));
                                            if (checkBox4.isChecked())
                                                databaseRef.child("filters").child(getLuogo).child(getNome)
                                                        .child("costo").child("basso")
                                                        .setValue(String.valueOf(++intCosto1));
                                            if (checkBox5.isChecked())
                                                databaseRef.child("filters").child(getLuogo).child(getNome)
                                                        .child("menu").child("0").child("celiaci").child("votisi")
                                                        .setValue(String.valueOf(++intceliaci));
                                            if (checkBox6.isChecked())
                                                databaseRef.child("filters").child(getLuogo).child(getNome)
                                                        .child("menu").child("0").child("celiaci").child("votino")
                                                        .setValue(String.valueOf(++intceliaci1));
                                            if (checkBox7.isChecked())
                                                databaseRef.child("filters").child(getLuogo).child(getNome)
                                                        .child("menu").child("1").child("bambino").child("votisi")
                                                        .setValue(String.valueOf(++intbambino));
                                            if (checkBox8.isChecked())
                                                databaseRef.child("filters").child(getLuogo).child(getNome)
                                                        .child("menu").child("1").child("bambino").child("votino")
                                                        .setValue(String.valueOf(++intbambino1));

                                            if (editText.getText().length() == 0) {
                                                databaseRef.child("restaurants").child(getLuogo).child(getNome)
                                                        .child(stringContatore).child("author_name")
                                                        .setValue("Anonimo");
                                                databaseRef.child("restaurants").child(getLuogo).child(getNome)
                                                        .child(stringContatore).child("text")
                                                        .setValue(editText1.getText().toString());
                                                databaseRef.child("restaurants").child(getLuogo).child(getNome)
                                                        .child(stringContatore).child("time").setValue(timeStamp);
                                                add_feedback[0] = false;
                                                dialog.dismiss();
                                            } else {
                                                databaseRef.child("restaurants").child(getLuogo).child(getNome)
                                                        .child(stringContatore).child("author_name")
                                                        .setValue(editText.getText().toString());
                                                databaseRef.child("restaurants").child(getLuogo).child(getNome)
                                                        .child(stringContatore).child("text")
                                                        .setValue(editText1.getText().toString());
                                                databaseRef.child("restaurants").child(getLuogo).child(getNome)
                                                        .child(stringContatore).child("time").setValue(timeStamp);
                                                add_feedback[0] = false;
                                                dialog.dismiss();

                                            }
                                        }

                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }

                            });

                        }
                    }
                });

            }

        });

        return view;
    }

}

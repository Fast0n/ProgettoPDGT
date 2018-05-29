package com.fast0n.findeat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

public class InfoActivity extends AppCompatActivity {

    TextView version, author;
    CardView click1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        version = findViewById(R.id.version);
        author = findViewById(R.id.author);
        click1 = findViewById(R.id.click1);

        version.setText(         Html.fromHtml(getString(R.string.version) + "<br><small>" + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ") (" + BuildConfig.APPLICATION_ID + ")</small>"));
        author.setText(         Html.fromHtml("Autori" + "<br><small>Giorgia Giuseppetti (gg97g)</small><br><small>Massimiliano Montaleone (Fast0n)</small>"));

        click1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) "https://github.com/Fast0n/ProgettoPDGT"));
                startActivity(browserIntent);
            }
        });


    }

}

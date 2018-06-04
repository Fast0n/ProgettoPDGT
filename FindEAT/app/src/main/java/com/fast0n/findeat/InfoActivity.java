package com.fast0n.findeat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.util.Objects;

public class InfoActivity extends AppCompatActivity {

    TextView tvVersion, tvAuthor;
    CardView click;
    AdView mAdView;
    InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView mTitle = toolbar.findViewById(R.id.toolbar_title);
        mTitle.setText(toolbar.getTitle());
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        // java addresses
        tvVersion = findViewById(R.id.version);
        tvAuthor = findViewById(R.id.author);
        click = findViewById(R.id.click1);
        mAdView = findViewById(R.id.adView);

        // banner && interstitialAd
        MobileAds.initialize(this, "ca-app-pub-9646303341923759~9003031985");
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-9646303341923759/8129894435");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });

        tvVersion.setText(Html.fromHtml(getString(R.string.version) + "<br><small>" + BuildConfig.VERSION_NAME + " ("
                + BuildConfig.VERSION_CODE + ") (" + BuildConfig.APPLICATION_ID + ")</small>"));
        tvAuthor.setText(Html.fromHtml("Autori"
                + "<br><small>Giorgia Giuseppetti (gg97g)</small><br><small>Massimiliano Montaleone (Fast0n)</small>"));

        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/Fast0n/ProgettoPDGT"));
                startActivity(browserIntent);
            }
        });

    }

}

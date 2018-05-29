package com.fast0n.findeat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.Objects;

public class DirectActivity extends AppCompatActivity {

    MaterialSearchBar searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direct);


        // java addresses
        searchBar = findViewById(R.id.searchBar);

        final Bundle extras = getIntent().getExtras();
        assert extras != null;
        final String luogo = extras.getString("search");
        final String title = luogo.toUpperCase().charAt(0) + luogo.substring(1, luogo.length());
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.app_name) + " " + title);



        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {


                if (searchBar.getText().trim().length() > 0) {

                    Intent myIntent = new Intent(DirectActivity.this, TabsActivity.class);
                    myIntent.putExtra("nome", searchBar.getText());
                    myIntent.putExtra("luogo", luogo);
                    DirectActivity.this.startActivity(myIntent);

                }
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
        Intent mainActivity = new Intent(DirectActivity.this, MainActivity.class);
        startActivity(mainActivity);
    }
}

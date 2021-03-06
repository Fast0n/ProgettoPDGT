package com.fast0n.findeat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fast0n.findeat.db_favorites.FavoritesActivity;
import com.fast0n.findeat.db_recents.Recent;
import com.fast0n.findeat.java.GPSTracker;
import com.fast0n.findeat.java.RecyclerItemListener;
import com.fast0n.findeat.list_restaurants.RestaurantsActivityList;
import com.fast0n.findeat.db_recents.DatabaseRecents;
import com.fast0n.findeat.db_recents.RecentsAdapter;
import com.mancj.materialsearchbar.MaterialSearchBar;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;
import net.yslibrary.android.keyboardvisibilityevent.Unregistrar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    MaterialSearchBar searchBar;

    private RecentsAdapter mAdapter;
    private List<Recent> recordsList = new ArrayList<>();
    private RecyclerView recyclerView;
    TextView tvRecents, tvLocation;
    InputMethodManager keyboard;
    Unregistrar mUnregistrar;
    private DatabaseRecents db;
    LinearLayout suggestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView mTitle = toolbar.findViewById(R.id.toolbar_title);
        mTitle.setText(toolbar.getTitle());
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // java addresses
        searchBar = findViewById(R.id.searchBar);
        tvRecents = findViewById(R.id.recents);
        tvLocation = findViewById(R.id.location);
        recyclerView = findViewById(R.id.recycler_view);
        keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        suggestion = findViewById(R.id.suggestion);

        db = new DatabaseRecents(this);
        recordsList.addAll(db.getAllRecords());
        mAdapter = new RecentsAdapter(this, recordsList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        suggestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBar.setText(tvLocation.getText().toString());

                if (!tvLocation.getText().equals("") && isOnline()) {

                    try {
                        createRecord(searchBar.getText().toLowerCase());
                    } catch (Exception ignored) {
                    }

                    Intent intent = new Intent(MainActivity.this, RestaurantsActivityList.class);
                    intent.putExtra("search", tvLocation.getText().toString().toLowerCase());
                    startActivity(intent);
                    suggestion.setVisibility(View.GONE);
                } else
                    Toasty.error(MainActivity.this, getString(R.string.noConnection), Toast.LENGTH_LONG).show();
            }
        });

        show_gps();

        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

                if (isOnline()) {
                    searchBar.clearSuggestions();
                    show_gps();
                } else {
                    Toasty.error(MainActivity.this, getString(R.string.noConnection), Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {

                Intent intent = new Intent(MainActivity.this, RestaurantsActivityList.class);
                if (searchBar.getText().trim().length() > 0 && isOnline()) {

                    try {
                        createRecord(searchBar.getText().toLowerCase());
                    } catch (Exception ignored) {
                    }

                    intent.putExtra("search", searchBar.getText().toLowerCase());
                    startActivity(intent);
                    tvRecents.setVisibility(View.INVISIBLE);
                    recyclerView.setVisibility(View.INVISIBLE);

                } else
                    Toasty.error(MainActivity.this, getString(R.string.noConnection), Toast.LENGTH_LONG).show();

            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

        mUnregistrar = KeyboardVisibilityEvent.registerEventListener(MainActivity.this,
                new KeyboardVisibilityEventListener() {
                    @Override
                    public void onVisibilityChanged(boolean isOpen) {
                        updateKeyboardStatusText(isOpen);
                    }
                });
        updateKeyboardStatusText(KeyboardVisibilityEvent.isKeyboardVisible(MainActivity.this));

        recyclerView.addOnItemTouchListener(new RecyclerItemListener(getApplicationContext(), recyclerView,
                new RecyclerItemListener.RecyclerTouchListener() {
                    public void onClickItem(View arg1, int position) {

                        if (isOnline()) {
                            TextView luogo = arg1.findViewById(R.id.recent);
                            String luogo1 = luogo.getText().toString();

                            Intent intent = new Intent(MainActivity.this, RestaurantsActivityList.class);
                            intent.putExtra("search", luogo1.toLowerCase());
                            startActivity(intent);
                        } else
                            Toasty.error(MainActivity.this, getString(R.string.noConnection), Toast.LENGTH_LONG).show();

                    }

                    @Override
                    public void onLongClickItem(View v, int position) {
                        showActionsDialog(position);
                    }

                }));
    }

    public void createRecord(String record) {
        long id = db.insertRecord(record);
        Recent n = db.getRecord(id);

        if (n != null) {
            recordsList.add(0, n);
            mAdapter.notifyDataSetChanged();

        }
    }

    private void deleteRecord(int position) {
        db.deleteRecord(recordsList.get(position));
        recordsList.remove(position);
        mAdapter.notifyItemRemoved(position);

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
                        tvRecents.setVisibility(View.INVISIBLE);
                        recyclerView.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
        builder.show();
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void updateKeyboardStatusText(boolean isOpen) {

        if (isOpen) {
            suggestion.setVisibility(View.VISIBLE);
            tvRecents.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        } else {
            suggestion.setVisibility(View.GONE);
            if (db.getRecordsCount() > 0) {
                tvRecents.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
            } else {
                tvRecents.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);

            }

        }

    }

    public void show_gps() {
        GPSTracker gps = new GPSTracker(this);
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 1);
        } else {

            gps = new GPSTracker(MainActivity.this);

            if (gps.canGetLocation()) {

                double latitude = gps.getLatitude();
                double longitude = gps.getLongitude();

                geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                List<Address> addresses = null;

                try {
                    addresses = geocoder.getFromLocation(latitude, longitude, 1);

                    if (addresses != null && !addresses.isEmpty()) {

                        String location = addresses.get(0).getLocality();

                        Log.e("Errr", location);
                        tvLocation.setText(location);

                    }

                } catch (IOException ignored) {
                }

            } else {

                gps.showSettingsAlert();
            }

        }

    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            this.finish();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            int pid = android.os.Process.myPid();
            android.os.Process.killProcess(pid);
            super.onBackPressed();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_favorites) {

            Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
            startActivity(intent);

        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_search) {
            // Handle the camera action
        } else if (id == R.id.nav_info) {

            Intent i = new Intent(MainActivity.this, InfoActivity.class);
            startActivity(i);

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

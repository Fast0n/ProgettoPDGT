package com.fast0n.findeat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.fast0n.findeat.database.DatabaseHelper;
import com.fast0n.findeat.database.Record;
import com.fast0n.findeat.database.RecordsAdapter;
import com.fast0n.findeat.database.RecyclerItemListener;
import com.fast0n.findeat.list_restaurants.RestaurantsActivityList;
import com.mancj.materialsearchbar.MaterialSearchBar;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;
import net.yslibrary.android.keyboardvisibilityevent.Unregistrar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    MaterialSearchBar searchBar;

    private RecordsAdapter mAdapter;
    private List<Record> recordsList = new ArrayList<>();
    private RecyclerView recyclerView;
    TextView textView;
    InputMethodManager keyboard;
    Unregistrar mUnregistrar;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 1);
            return;
        } else {
            // Write you code here if permission already given.
        }

        // java addresses
        searchBar = findViewById(R.id.searchBar);
        textView = findViewById(R.id.textView);
        recyclerView = findViewById(R.id.recycler_view);
        keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        db = new DatabaseHelper(this);
        recordsList.addAll(db.getAllRecords());
        mAdapter = new RecordsAdapter(this, recordsList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        toggleEmptyRecords();





        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                textView.setVisibility(View.INVISIBLE);
                recyclerView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {

                Intent start = new Intent(MainActivity.this, RestaurantsActivityList.class);
                if (searchBar.getText().trim().length() > 0) {

                    try {
                        createRecord(searchBar.getText().toLowerCase());
                    } catch (Exception ignored) {
                    }

                    start.putExtra("search", searchBar.getText().toLowerCase());
                    startActivity(start);
                    textView.setVisibility(View.INVISIBLE);
                    recyclerView.setVisibility(View.INVISIBLE);

                }
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                mUnregistrar = KeyboardVisibilityEvent.registerEventListener(MainActivity.this, new KeyboardVisibilityEventListener() {
                    @Override
                    public void onVisibilityChanged(boolean isOpen) {
                        updateKeyboardStatusText(isOpen);
                    }
                });
                updateKeyboardStatusText(KeyboardVisibilityEvent.isKeyboardVisible(MainActivity.this));

            }
        });

        try {
            get_position();

        } catch (Exception ignored) {
        }

        recyclerView.addOnItemTouchListener(
                new RecyclerItemListener(this, recyclerView, new RecyclerItemListener.ClickListener() {
                    @Override
                    public void onClick(View arg1, final int position) {

                        TextView luogo = arg1.findViewById(R.id.record);
                        String luogo1 = luogo.getText().toString();

                        Intent start = new Intent(MainActivity.this, RestaurantsActivityList.class);
                        start.putExtra("search", luogo1.toLowerCase());
                        startActivity(start);

                    }

                    @Override
                    public void onLongClick(View view, int position) {
                        showActionsDialog(position);

                    }
                }));

    }

    public void createRecord(String record) {
        long id = db.insertRecord(record);
        Record n = db.getRecord(id);

        if (n != null) {
            recordsList.add(0, n);
            mAdapter.notifyDataSetChanged();

            toggleEmptyRecords();
        }
    }

    private void deleteRecord(int position) {
        db.deleteRecord(recordsList.get(position));
        recordsList.remove(position);
        mAdapter.notifyItemRemoved(position);

        toggleEmptyRecords();
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
                }
            }
        });
        builder.show();
    }

    private void toggleEmptyRecords() {
        if (db.getRecordsCount() > 0) {
            textView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
        }
    }

    private void updateKeyboardStatusText(boolean isOpen) {

        if (isOpen) {
            textView.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
        } else {
            textView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);

        }

    }

    @SuppressLint("MissingPermission")
    public void get_position() {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new MyLocationListener();

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);

    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            searchBar.setText("");

            String longitude = "Longitude: " + loc.getLongitude();
            String latitude = "Latitude: " + loc.getLatitude();

            String cityName = null;
            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                if (addresses.size() > 0) {
                    cityName = addresses.get(0).getLocality();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            String s = cityName;
            searchBar.setText(s);
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    @Override
    public void onBackPressed() {
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

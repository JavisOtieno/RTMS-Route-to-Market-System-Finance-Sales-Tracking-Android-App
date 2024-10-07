package com.scg.sf;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.scg.sf.util.Toasts;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SetLocationActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap googleMap;
    private String mLatitude;
    private String mLongitude;
    private String mName;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Marker userMarker;
    private Boolean setZoomFirstTime = false;
    private Button setAsCurrentLocation;
    private boolean isTaskRunning = false;
    private Location mLastLocation;
    private String mOutletId;
    private String mOutletName;
    private String mVisitId;
    private ProgressDialog pd;
    private SharedPreferences mPrefs;
    private String mAuthToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set_location);
        setAsCurrentLocation = (Button) findViewById(R.id.setAsCurrentLocationButton);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mPrefs = getSharedPreferences("label", 0);
        mAuthToken = mPrefs.getString("authToken", "");

        initToolbar();
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateLocationOnMap(location);
                    mLastLocation = location;

                    if (!setZoomFirstTime) {
                        LatLng locationNew = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationNew, 12));
                        setZoomFirstTime = true;
                    }
                }
            }
        };

        setAsCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isTaskRunning) {

                    isTaskRunning = true;


                    //Double distance=MyUtils.calculateDistance(Double.parseDouble(lat1),Double.parseDouble(lon1),mLastLocation.getLatitude(),mLastLocation.getLongitude());

                    //toastIconError(distance*1000+lat1+" - "+lon1+" - "+mLastLocation.getLatitude()+" - "+mLastLocation.getLongitude());

                    if (mLastLocation != null) {

                        String locationText = mLastLocation.getLatitude() + " - " + mLastLocation.getLongitude() + "";
                        //   Toast.makeText(getBaseContext(),locationText,Toast.LENGTH_SHORT).show();
                        String lat = mLastLocation.getLatitude() + "";
                        String lon = mLastLocation.getLongitude() + "";
//                        lat = "22.22";
//                        lon = "22.22";
                        //   long unixTime = System.currentTimeMillis() / 1000L;

//                            String lat1 = "40.714268"; // New York
//                            String lon1 = "-74.005974";
//                            String lat2 = "34.0522"; // Los Angeles
//                            String lon2 = "-118.2437";
                        System.out.println("   lat" + lat + " - " + lon);
//                        long unixTime = System.currentTimeMillis() / 1000L;
//                        Toasts.toastIconError(SetLocationActivity.this,
//                                "Location"+lat+" "+lon);


                        new JsonTask().execute(Constants.BASE_URL + "api/setlocation/" +
                                        mOutletId
                                , "PUT", lat + "", lon + "");



                    } else {
                        Toasts.toastIconError(SetLocationActivity.this,
                                "Detecting Location...");
                        isTaskRunning = false;
                    }
                }

            }
        });


    }



    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(10000); // 10 seconds
            locationRequest.setFastestInterval(5000); // 5 seconds
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }



    private void updateLocationOnMap(Location location) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if (userMarker != null) {
            //  userMarker.setPosition(userLocation);
        } else {
            //  userMarker = googleMap.addMarker(new MarkerOptions().position(userLocation).title("You are here"));
        }
        // googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
    }
    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(SetLocationActivity.this);

            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();

        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {

                URL url = new URL(params[0]);
                String requestMethod = params[1];


                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod(requestMethod);
                connection.setRequestProperty("Accept","application/json");
                connection.setRequestProperty("Authorization", "Bearer " + mAuthToken);
                connection.setRequestProperty("Content-Type", "application/json");

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("lat", params[2]);
                jsonParam.put("long", params[3]);

//                Uri.Builder builder= new Uri.Builder()
//                        .appendQueryParameter("checkout_time", params[2])
//                        .appendQueryParameter("checkout_latitude", params[3])
//                        .appendQueryParameter("checkout_longitude", params[4]);
//
//                System.out.println("Params " + params[2] + " " + params[3] + " " + params[4] );
//
//                String query = builder.build().getEncodedQuery();

                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
//                writer.write(query);
                writer.write(jsonParam.toString());
                writer.flush();
                writer.close();
                os.close();

                System.out.println("Params " + params[2] + " " + params[3] + " ");

                connection.connect();

                InputStream stream;

                if (connection.getResponseCode() == 200) {
                    stream = connection.getInputStream();

                } else {
                    /* error from server */
                    //_is = httpConn.getErrorStream();
                    stream = connection.getErrorStream();

                }

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {

                    buffer.append(line + "\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.e(TAG, "Malf Message");
            } catch (IOException e) {
                Log.e("Message here: ", e.getMessage());
                e.printStackTrace();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Dismiss the dialog on the UI thread
                runOnUiThread(() -> {
                    if (pd != null && pd.isShowing() && !isFinishing()) {
                        pd.dismiss();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            isTaskRunning = false;

            try {

                System.out.println("Result: " + result);
                if (result != null) {
                    JSONObject jsonObject = new JSONObject(result);

                    //Checkout Failed means server returns nothing
                    //Error checking out means server responds with something other than success

                    if(result.contains("errors")){
                        String message = jsonObject.getString("message");
                        Toasts.toastIconError(SetLocationActivity.this,message);
                        //Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                    }
                    else if (result.contains("success") ){


                        //txtJson.setText(jsonObject.toString());
                        //String visitId = jsonObject.getString("visitId");
                        //String outletId = jsonObject.getString("outletId");
                        //long unixTimestamp = Instant.now().getEpochSecond();
                        Toasts.toastIconSuccess(SetLocationActivity.this,
                                jsonObject.getString("message"));



                        Intent intent = new Intent(SetLocationActivity.this, SelectActionActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        //Toast.makeText(SelectExistingCustomerActivity.this,"visitId"+visitId+" - outletId"+outletId+" - outletName"+mOutletName,Toast.LENGTH_SHORT).show();
                        intent.putExtra("visitId", mVisitId);
                        intent.putExtra("outletName",mOutletName);
                        intent.putExtra("outletId", mOutletId);
                        //intent.putExtra("lat","");
                        //intent.putExtra("long","");
                        startActivity(intent);


                    }else{
                        Toasts.toastIconError(SetLocationActivity.this,
                                "Error Checking Out. Please Try Again");
                        FirebaseCrashlytics.getInstance().recordException(new Exception("Checkout Error: "+result));

                    }



                }else
                {
                    Toasts.toastIconError(SetLocationActivity.this,
                            "Checkout Failed. Please Try Again");
                    FirebaseCrashlytics.getInstance().recordException(new Exception("Checkout Error: "+result));

                }


            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
    }







    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Set Location");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Tools.setSystemBarColor(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        // You can customize the map here if needed
        // For example, enable zoom controls:
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        Intent intent = getIntent();
        mOutletId = intent.getStringExtra("outletId");
        mOutletName = intent.getStringExtra("outletName");
        mVisitId = intent.getStringExtra("visitId");
//        mLongitude = intent.getStringExtra("long");
//        mName = intent.getStringExtra("name");
//
//        // Add a marker at a specific location and move the camera
//        // Example: San Francisco
//        LatLng sanFrancisco = new LatLng(Double.parseDouble(mLatitude), Double.parseDouble(mLongitude));
//        googleMap.addMarker(new MarkerOptions().position(sanFrancisco).title(mName));
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sanFrancisco, 12)); // Zoom level 12

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            startLocationUpdates();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}


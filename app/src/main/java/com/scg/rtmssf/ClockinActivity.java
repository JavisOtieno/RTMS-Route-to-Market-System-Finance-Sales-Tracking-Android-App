package com.scg.rtmssf;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static androidx.core.location.LocationManagerCompat.requestLocationUpdates;
import static java.lang.Integer.parseInt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
//import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.scg.rtmssf.util.EncryptedPrefsUtil;
import com.scg.rtmssf.util.LocationUtils;
import com.scg.rtmssf.util.Toasts;

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

public class ClockinActivity extends AppCompatActivity {
    private Button confirmClockinButton;
    private Button backButton;

    private ProgressDialog pd;

    private int REQUEST_LOCATION = 99;

    private LocationManager locationManager;
    private String provider;
    //private MyLocationListener mylistener;
    private Criteria criteria;
    private Location location;
    private SharedPreferences mPrefs;
    private String mUserId;
    private String mAuthToken;
    int PERMISSION_ID = 44;
    private FusedLocationProviderClient mFusedLocationClient;

    private Location mLastLocation;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private boolean isTaskRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clockin);

        mPrefs = getSharedPreferences("label", 0);
//        mUserId = mPrefs.getString("userId", "0");
//        mAuthToken = mPrefs.getString("authToken", "");

        mUserId = EncryptedPrefsUtil.getString("userId","0");
        mAuthToken = EncryptedPrefsUtil.getString("authToken", "");

        System.out.println("Retrieved token: " + mAuthToken);
        System.out.println("Retrieved userId: " + mUserId);

        backButton = (Button) findViewById(R.id.backButton);
        confirmClockinButton = (Button) findViewById(R.id.confirmClockinButton);

        initToolbar();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // user defines the criteria
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);   //default
        criteria.setCostAllowed(false);
        // get the best provider depending on the criteria
        //provider = locationManager.getBestProvider(criteria, false);
        provider=LocationManager.NETWORK_PROVIDER;

        location=null;
        if (ActivityCompat.checkSelfPermission(ClockinActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ClockinActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(ClockinActivity.this, new String[]
                    {ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        }else{
            //should select the best provider but bypassing with NETWORK Provider for now
            //location = locationManager.getLastKnownLocation(provider);
            location = locationManager.getLastKnownLocation(provider);
        }

        //mylistener = new MyLocationListener();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClockinActivity.this, MainActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        confirmClockinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check and prompt for location services
                LocationUtils.checkLocationServices(ClockinActivity.this);
                //Intent intent = new Intent(ClockoutActivity.this, MainActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                //startActivity(intent);
//                long unixTime = System.currentTimeMillis() / 1000L;
//
//                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
//                getLastLocation();
                if (!isTaskRunning) {
                    isTaskRunning = true;
                    if (mLastLocation != null) {
                        String locationText = mLastLocation.getLatitude() + " - " + mLastLocation.getLongitude() + "";
                        //       Toast.makeText(getBaseContext(),locationText,Toast.LENGTH_SHORT).show();
                        String lat = mLastLocation.getLatitude() + "";
                        String lon = mLastLocation.getLongitude() + "";
                        long unixTime = System.currentTimeMillis() / 1000L;






//                        new JsonTask().execute(Constants.BASE_URL + "api/clockin", "POST", mUserId, unixTime + "", lat + "", lon + "");

                        new TokenManager(ClockinActivity.this,  (success, result) -> {
                            if (success) {
                                // Handle the JSON result, update UI, etc.
                                System.out.println("API Result: " + result);

                                handleResult(result);

                            } else {
                                isTaskRunning = false;
                                // Handle errors (e.g., token refresh failed)
                                Toasts.toastIconError(ClockinActivity.this, result);
//                                Intent intent = new Intent(ClockinActivity.this, LoginActivity.class);
//                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                                startActivity(intent);
                            }
                        }).executeRequest(Constants.BASE_URL +"api/clockin", "POST",
                                "userId",mUserId,"time",unixTime+"","lat",lat+"","long",lon+"");

                        //new JsonTask().execute(Constants.BASE_URL + "api/clockin", "POST", mUserId, unixTime + "", lat + "", lon + "");


                    } else {
                        Toasts.toastIconError(ClockinActivity.this,"Detecting Location ...");
                        isTaskRunning = false;
                    }
                }


            }
        });

        //START OF LATEST LOCATION CALL UPDATE

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds'

        detectLocation();

        requestLocationUpdates();
        //END OF LATEST LOCATION CALL UPDATE


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
        getSupportActionBar().setTitle("Start Day");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Tools.setSystemBarColor(this);
    }

    //START OF LATEST LOCATION CODE

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates();
            } else {
                Toasts.toastIconError(ClockinActivity.this,"Location Permission Denied");
                //Toast.makeText(this, "Location permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void detectLocation(){
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {

                    mLastLocation = location;


                    if(mLastLocation!=null){
                        //Double distance = MyUtils.calculateDistance(Double.parseDouble(lat1),Double.parseDouble(lon1),location.getLatitude(),location.getLongitude());
                        //toastIconError(distance+"");
                        //+" - "+mLastLocation.getAltitude()
                        //toastIconError(mLastLocation.getLatitude()+" - "+mLastLocation.getLongitude());

                    }else if(mLastLocation!=null){
                        //+" - "+mLastLocation.getAltitude()
                        //toastIconError(mLastLocation.getLatitude()+" - "+mLastLocation.getLongitude());

                    }

                }
            }
        };
    }

    //END OF LATEST LOCATION CODE

    //PICKED FROM OLD CODE BUT MIGHT BE VALID
    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
            detectLocation();
        }
    }
    //PICKED FROM OLD CODE BUT MIGHT BE VALID


    private void handleResult(String result){
        isTaskRunning = false;

        try {

            System.out.println("Result: " + result);
            if (result != null && result.contains("Clockin")){

                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.has("message") &&
                        jsonObject.get("message") instanceof String &&
                        jsonObject.get("message").equals("Clockin Successful")
                ) {
                    //txtJson.setText(jsonObject.toString());
                    String clockinId = jsonObject.getString("clockinId");
                    SharedPreferences.Editor mEditor = mPrefs.edit();
                    mEditor.putString("clockinId", clockinId).commit();
//                        Toasts.toastIconError(ClockinActivity.this,clockinId);

                    Intent intent = new Intent(ClockinActivity.this, CheckinActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                }
                else if(result.contains("errors")){
                    String message = jsonObject.getString("message");
                    Toasts.toastIconError(ClockinActivity.this,message);
                    //Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                }
                else{

                    Toasts.toastIconError(ClockinActivity.this,"Error Clocking in. Please Try Again");
                    FirebaseCrashlytics.getInstance().recordException(new Exception("Clockin Error: "+result));

                }

            }else{

                Toasts.toastIconError(ClockinActivity.this,"Clockin Failed. Please Try Again");
                FirebaseCrashlytics.getInstance().recordException(new Exception("Clockin Error: "+result));

            }





        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(ClockinActivity.this);

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
                if (requestMethod.equals("POST")) {
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("userId", params[2]);
                    jsonParam.put("time", params[3]);
                    jsonParam.put("lat", params[4]);
                    jsonParam.put("long", params[5]);


//                    Uri.Builder builder = new Uri.Builder()
//                            .appendQueryParameter("userId", params[2])
//                            .appendQueryParameter("time", params[3])
//                            .appendQueryParameter("lat", params[4])
//                            .appendQueryParameter("long", params[5]);

//                    String query = builder.build().getEncodedQuery();

                    OutputStream os = connection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
//                    writer.write(query);
                    writer.write(jsonParam.toString());
                    writer.flush();
                    writer.close();
                    os.close();

                    System.out.println("Params " + params[2] + " " + params[3] + " " + params[4] + " " + params[5]);
                }
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
                if (result != null && result.contains("Clockin")){

                        JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.has("message") &&
                            jsonObject.get("message") instanceof String &&
                                jsonObject.get("message").equals("Clockin Successful")
                    ) {
                        //txtJson.setText(jsonObject.toString());
                        String clockinId = jsonObject.getString("clockinId");
                        SharedPreferences.Editor mEditor = mPrefs.edit();
                        mEditor.putString("clockinId", clockinId).commit();
//                        Toasts.toastIconError(ClockinActivity.this,clockinId);

                        Intent intent = new Intent(ClockinActivity.this, CheckinActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                    }
                    else if(result.contains("errors")){
                        String message = jsonObject.getString("message");
                        Toasts.toastIconError(ClockinActivity.this,message);
                        //Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                    }
                    else{

                        Toasts.toastIconError(ClockinActivity.this,"Error Clocking in. Please Try Again");
                        FirebaseCrashlytics.getInstance().recordException(new Exception("Clockin Error: "+result));

                    }

                    }else{

                    Toasts.toastIconError(ClockinActivity.this,"Clockin Failed. Please Try Again");
                    FirebaseCrashlytics.getInstance().recordException(new Exception("Clockin Error: "+result));

                }





            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
    }


    // OLD LOCATION CODE


//    @SuppressLint("MissingPermission")
//    private void getLastLocation() {
//        // check if permissions are given
//        if (checkPermissions()) {
//
//            // check if location is enabled
//            if (isLocationEnabled()) {
//
//                // getting last
//                // location from
//                // FusedLocationClient
//                // object
//                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Location> task) {
//                        location = task.getResult();
//                        //we should check the location of the user as they
//                        //click on clock out that's why we've removed the if statement
//                        //if (location == null) {
//                        requestNewLocationData();
//                        //} else {
//                        String locationText=location.getLatitude() + " - "+location.getLongitude() + "";
//                        //  Toast.makeText(getBaseContext(),locationText,Toast.LENGTH_SHORT).show();
//                        String lat = location.getLatitude()+"";
//                        String lon = location.getLongitude()+"";
//                        long unixTime = System.currentTimeMillis() / 1000L;
//                        //new JsonTask().execute(Constants.BASE_URL + "api/clockin", "POST", mUserId, unixTime + "", lat + "", lon + "");
//                        new JsonTask().execute(Constants.BASE_URL + "api/clockin","POST",mUserId,unixTime+"",lat+"",lon+"");
//
//                        //}
//                    }
//                });
//            } else {
//                toastIconError("Please turn on" + " your location...");
//                //Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
//                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                startActivity(intent);
//            }
//        } else {
//            // if permissions aren't available,
//            // request for permissions
//            requestPermissions();
//        }
//    }

//    @SuppressLint("MissingPermission")
//    private void requestNewLocationData() {
//
//        // Initializing LocationRequest
//        // object with appropriate methods
//        LocationRequest mLocationRequest = new LocationRequest();
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        mLocationRequest.setInterval(5);
//        mLocationRequest.setFastestInterval(0);
//        mLocationRequest.setNumUpdates(1);
//
//        // setting LocationRequest
//        // on FusedLocationClient
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
//    }

//    private LocationCallback mLocationCallback = new LocationCallback() {
//
//        @Override
//        public void onLocationResult(LocationResult locationResult) {
//            mLastLocation = locationResult.getLastLocation();
//            location=mLastLocation;
//            //latitudeTextView.setText("Latitude: " + mLastLocation.getLatitude() + "");
//            //longitTextView.setText("Longitude: " + mLastLocation.getLongitude() + "");
//        }
//    };

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    // method to check
    // if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // OLD LOCATION CODE

    // If everything is alright then
//    @Override
//    public void
//    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == PERMISSION_ID) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
//                getLastLocationWithoutSubmit();
//            }
//        }
//    }

    // OLD LOCATION CODE




    // OLD LOCATION CODE
//    @SuppressLint("MissingPermission")
//    private void getLastLocationWithoutSubmit() {
//        // check if permissions are given
//        if (checkPermissions()) {
//
//            // check if location is enabled
//            if (isLocationEnabled()) {
//
//                // getting last
//                // location from
//                // FusedLocationClient
//                // object
//                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Location> task) {
//                        location = task.getResult();
//                        if (location == null) {
//                            requestNewLocationData();
//                        } else {
//                            String locationText=location.getLatitude() + " - "+location.getLongitude() + "";
//                            // Toast.makeText(getBaseContext(),locationText,Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//
//
//            } else {
//                toastIconError("Please turn on" + " your location...");
//                //Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
//                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                startActivity(intent);
//            }
//        } else {
//            // if permissions aren't available,
//            // request for permissions
//            requestPermissions();
//        }
//    }
    // OLD LOCATION CODE

}
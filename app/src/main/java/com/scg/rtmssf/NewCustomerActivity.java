package com.scg.rtmssf;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

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
import android.widget.EditText;
import android.widget.Toast;

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
import java.time.Instant;

public class NewCustomerActivity extends AppCompatActivity {
    private Button submitButton;
    private EditText outletNameEditText;
    private EditText contactPersonEditText;
    private EditText phoneEditText;
//    private EditText latEditText;
//    private EditText longEditText;
    private String outletName;
    private String contactPersonName;
    private String phone;
    private String latitude;
    private String longitude;
    private ProgressDialog pd;
    private SharedPreferences mPrefs;
    private String followupWithoutCheckin;
    private String mUserId;
    private String mAuthToken;

    private int REQUEST_LOCATION = 99;

    private LocationManager locationManager;
    private String provider;
    //private MyLocationListener mylistener;
    private Criteria criteria;
    private Location location;
    int PERMISSION_ID = 44;
    private FusedLocationProviderClient mFusedLocationClient;

    private Location mLastLocation;

    private String outletId;
    private String lat1;
    private String lon1;

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private String clockinId;
    private boolean isTaskRunning = false;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_outlet);
        initToolbar();

//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        // user defines the criteria
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);   //default
        criteria.setCostAllowed(false);
        // get the best provider depending on the criteria
        //provider = locationManager.getBestProvider(criteria, false);
//        provider=LocationManager.NETWORK_PROVIDER;
//
//        location=null;
//
//        if (ActivityCompat.checkSelfPermission(NewOutletActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(NewOutletActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//
//            ActivityCompat.requestPermissions(NewOutletActivity.this, new String[]
//                    {ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
//
//        }else{
//            //should select the best provider but bypassing with NETWORK Provider for now
//            //location = locationManager.getLastKnownLocation(provider);
//            location = locationManager.getLastKnownLocation(provider);
//        }

        //mylistener = new MyLocationListener();

        mPrefs = getSharedPreferences("label", 0);
//        mUserId = mPrefs.getString("userId", "0");
//        mAuthToken = mPrefs.getString("authToken", "");
        mUserId = EncryptedPrefsUtil.getString("userId","0");
        mAuthToken = EncryptedPrefsUtil.getString("authToken", "");
//        Toasts.toastIconError(NewOutletActivity.this,mUserId);

        clockinId = mPrefs.getString("clockinId", "0");

        submitButton=(Button)findViewById(R.id.bt_submit);
        //longEditText = (EditText)findViewById(R.id.longitudeEditText);
        //latEditText = (EditText) findViewById(R.id.longitudeEditText);
        outletNameEditText = (EditText) findViewById(R.id.outletNameEditText);
        phoneEditText = (EditText) findViewById(R.id.phoneEditText);
        contactPersonEditText = (EditText) findViewById(R.id.contactPersonName);

        //START OF LATEST LOCATION CALL UPDATE

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds'



        detectLocation();
        requestLocationUpdates();

        //END OF LATEST LOCATION CALL UPDATE

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isTaskRunning) {
                    isTaskRunning = true;

                //   longitude = longEditText.getText().toString();
                //   latitude = latEditText.getText().toString();

                phone = phoneEditText.getText().toString();
                outletName = outletNameEditText.getText().toString();
                contactPersonName = contactPersonEditText.getText().toString();

                LocationUtils.checkLocationServices(NewCustomerActivity.this);
//
//                long unixTime = System.currentTimeMillis() / 1000L;
//
//                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
//                //getLastLocation();
//                requestNewLocationData();
                if (mLastLocation != null) {
                    String locationText = mLastLocation.getLatitude() + " - " + mLastLocation.getLongitude() + "";
                    //    Toast.makeText(getBaseContext(),locationText,Toast.LENGTH_SHORT).show();
                    String lat = mLastLocation.getLatitude() + "";
                    String lon = mLastLocation.getLongitude() + "";

                    SharedPreferences.Editor mEditor = mPrefs.edit();
                    mEditor.putString("outletLat", lat).commit();
                    mEditor.putString("outletLong", lon).commit();
                    long unixTime = System.currentTimeMillis() / 1000L;
                    //Toast.makeText(NewOutletActivity.this,mUserId+" This",Toast.LENGTH_SHORT).show();
                    //new JsonTask().execute(Constants.BASE_URL + "api/clockin", "POST", mUserId, unixTime + "", lat + "", lon + "");
//                    new JsonTask().execute(Constants.BASE_URL + "api/addoutlet", "POST",
//                            mUserId, outletName, contactPersonName, phone,
//                            lat + "", lon + "", unixTime + "", clockinId);

                    String[] params = {"test","test2"};


                    new TokenManager(NewCustomerActivity.this,  (success, result) -> {
                        if (success) {
                            // Handle the JSON result, update UI, etc.
                            System.out.println("API Result: " + result);

                            handleResult(result);

                        } else {
                            // Handle errors (e.g., token refresh failed)
                            Toasts.toastIconError(NewCustomerActivity.this, result);
//                            Intent intent = new Intent(NewCustomerActivity.this, LoginActivity.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(intent);
                        }
                    }).executeRequest(Constants.BASE_URL +"api/addoutlet", "POST",
                            "userId",mUserId,"time", unixTime + "", "lat",lat + "",
                            "long", lon + "","outletName",outletName,
                            "phone", phone+"","contactPersonName",
                            contactPersonName,"clockinId", clockinId);










                } else {
                    Toasts.toastIconError(NewCustomerActivity.this,
                            "Detecting Location...");
                    isTaskRunning = false;
                }
            }


            }
        });

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
        getSupportActionBar().setTitle("New Customer");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Tools.setSystemBarColor(this);
    }
//    private class JsonTask extends AsyncTask<String, String, String> {
//
//        protected void onPreExecute() {
//            super.onPreExecute();
//
//            pd = new ProgressDialog(NewCustomerActivity.this);
//
//            pd.setMessage("Please wait");
//            pd.setCancelable(false);
//            pd.show();
//
//        }
//
//        protected String doInBackground(String... params) {
//
//
//            HttpURLConnection connection = null;
//            BufferedReader reader = null;
//
//            try {
//
//                URL url = new URL(params[0]);
//                String requestMethod = params[1];
//
//                connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod(requestMethod);
//                connection.setRequestProperty("Accept","application/json");
//                connection.setRequestProperty("Authorization", "Bearer " + mAuthToken);
//                connection.setRequestProperty("Content-Type", "application/json");
//                if (requestMethod.equals("POST")) {
//                    JSONObject jsonParam = new JSONObject();
//                    jsonParam.put("userId", params[2]);
//                    jsonParam.put("outletName", params[3]);
//                    jsonParam.put("contactPersonName", params[4]);
//                    jsonParam.put("phone", params[5]);
//                    jsonParam.put("lat", params[6]);
//                    jsonParam.put("long", params[7]);
//                    jsonParam.put("time", params[8]);
//                    jsonParam.put("clockinId", params[9]);
////                    Uri.Builder builder = new Uri.Builder()
////                            .appendQueryParameter("userId", params[2])
////                            .appendQueryParameter("outletName", params[3])
////                            .appendQueryParameter("contactPersonName", params[4])
////                            .appendQueryParameter("phone", params[5])
////                            .appendQueryParameter("lat", params[6])
////                            .appendQueryParameter("long", params[7])
////                            .appendQueryParameter("time", params[8])
////                            .appendQueryParameter("clockinId", params[9]);
//
////                    String query = builder.build().getEncodedQuery();
//
//                    OutputStream os = connection.getOutputStream();
//                    BufferedWriter writer = new BufferedWriter(
//                            new OutputStreamWriter(os, "UTF-8"));
////                    writer.write(query);
//                    writer.write(jsonParam.toString());
//                    writer.flush();
//                    writer.close();
//                    os.close();
//
//                    System.out.println("Params " + params[2] + " " + params[3] + " " + params[4] + " " + params[5]);
//                }
//                connection.connect();
//
//                InputStream stream;
//
//                if (connection.getResponseCode() == 200) {
//                    stream = connection.getInputStream();
//
//                } else {
//                    /* error from server */
//                    //_is = httpConn.getErrorStream();
//                    stream = connection.getErrorStream();
//
//                }
//
//                reader = new BufferedReader(new InputStreamReader(stream));
//
//                StringBuffer buffer = new StringBuffer();
//                String line = "";
//
//                while ((line = reader.readLine()) != null) {
//
//                    buffer.append(line + "\n");
//                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)
//
//                }
//
//                return buffer.toString();
//
//
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//                Log.e(TAG, "Malf Message");
//            } catch (IOException e) {
//                Log.e("Message here: ", e.getMessage());
//                e.printStackTrace();
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            } finally {
//                if (connection != null) {
//                    connection.disconnect();
//                }
//                try {
//                    if (reader != null) {
//                        reader.close();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                // Dismiss the dialog on the UI thread
//                runOnUiThread(() -> {
//                    if (pd != null && pd.isShowing() && !isFinishing()) {
//                        pd.dismiss();
//                    }
//                });
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            super.onPostExecute(result);
//            isTaskRunning = false;
//
//                       if(pd != null && pd.isShowing() && !isFinishing()){
//                pd.dismiss();
//            }
//
//            System.out.println("Result: " + result);
//            try {
//
//                if (result != null) {
//
//                    JSONObject jsonObject = new JSONObject(result);
//                    //txtJson.setText(jsonObject.toString());
//                    //Submit Sale Failed means server returns nothing
//                    //Error saving means server responds with something other than success
//                    if(result.contains("errors")){
//                        String message = jsonObject.getString("message");
//                        Toasts.toastIconError(NewCustomerActivity.this, message);
//                        //Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
//                    }
//                    else if(result.contains("success")){
//                        String outletId=jsonObject.getString("outletId");
//                        String visitId=jsonObject.getString("visitId");
//                        long unixTimestamp = Instant.now().getEpochSecond();
//
//                        followupWithoutCheckin = mPrefs.getString("callingActivityFollowup","");
//
//
//                        Intent intent = new Intent(NewCustomerActivity.this, TakePhotoActivity.class);
//
//
//                        Toast.makeText(NewCustomerActivity.this,followupWithoutCheckin,Toast.LENGTH_SHORT).show();
//
//                        if(followupWithoutCheckin.equals("FollowupActivity")){
//                            intent = new Intent(NewCustomerActivity.this,
//                                    AddFollowupActivity.class);
//
//                        }
//
//                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
////                        Toast.makeText(NewOutletActivity.this,"visitid"+visitId+" outletid"+outletId,Toast.LENGTH_SHORT).show();
//                        intent.putExtra("outletId", outletId);
//                        intent.putExtra("visitId", visitId);
//                        intent.putExtra("callingActivity","Outlet");
//                        intent.putExtra("outletName",outletName);
//
//                        startActivity(intent);
//
//                    }else{
//                        Toasts.toastIconError(NewCustomerActivity.this,
//                                "Error Saving. Please Try Again");
//                        FirebaseCrashlytics.getInstance().recordException(new Exception("New Customer Error: "+result));
//
//                    }
//
//
//
//                }else
//                {
//                    Toasts.toastIconError(NewCustomerActivity.this,
//                            "Add Customer Failed. Please Try Again");
//                    FirebaseCrashlytics.getInstance().recordException(new Exception("New Customer Error: "+result));
//
//                }
//
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            }
//
//
//        }
//    }

    private void handleResult(String result) {

            isTaskRunning = false;

            if(pd != null && pd.isShowing() && !isFinishing()){
                pd.dismiss();
            }

            System.out.println("Result: " + result);
            try {

                if (result != null) {

                    JSONObject jsonObject = new JSONObject(result);
                    //txtJson.setText(jsonObject.toString());
                    //Submit Sale Failed means server returns nothing
                    //Error saving means server responds with something other than success
                    if(result.contains("errors")){
                        String message = jsonObject.getString("message");
                        Toasts.toastIconError(NewCustomerActivity.this, message);
                        //Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                    }
                    else if(result.contains("success")){
                        String outletId=jsonObject.getString("outletId");
                        String visitId=jsonObject.getString("visitId");
                        long unixTimestamp = Instant.now().getEpochSecond();

                        Intent intent = new Intent(NewCustomerActivity.this, TakePhotoActivity.class);

                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                        Toast.makeText(NewOutletActivity.this,"visitid"+visitId+" outletid"+outletId,Toast.LENGTH_SHORT).show();
                        intent.putExtra("outletId", outletId);
                        intent.putExtra("visitId", visitId);
                        intent.putExtra("callingActivity","Outlet");
                        intent.putExtra("outletName",outletName);

                        startActivity(intent);
                    }else{
                        Toasts.toastIconError(NewCustomerActivity.this,
                                "Error Saving. Please Try Again");
                        FirebaseCrashlytics.getInstance().recordException(new Exception("New Customer Error: "+result));

                    }



                }else
                {
                    Toasts.toastIconError(NewCustomerActivity.this,
                            "Add Customer Failed. Please Try Again");
                    FirebaseCrashlytics.getInstance().recordException(new Exception("New Customer Error: "+result));

                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }



    }

    //START OF LATEST LOCATION CODE

    private void  detectLocation(){
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {

                    mLastLocation = location;

                    if(mLastLocation!=null){
//                        Toasts.toastIconError(NewOutletActivity.this,
//                                mLastLocation.getLatitude()+" - "+
//                                        mLastLocation.getLongitude());
                    }



                }
            }
        };
    }

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
                Toasts.toastIconError(NewCustomerActivity.this,
                        "Location Permission Denied");
                //Toast.makeText(this, "Location permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    //END OF LATEST LOCATION CODE









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

    // If everything is alright then
//    @Override
//    public void
//    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == PERMISSION_ID) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                getLastLocation();
//            }
//        }
//    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(
                    getApplicationContext());
            detectLocation();
        }
    }



}
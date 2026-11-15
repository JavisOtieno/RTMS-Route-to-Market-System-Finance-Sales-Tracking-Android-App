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
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.scg.rtmssf.util.EncryptedPrefsUtil;
import com.scg.rtmssf.util.LocationUtils;
import com.scg.rtmssf.util.NotificationHelper;
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

public class CheckoutActivity extends AppCompatActivity {
    private Button confirmCheckOutButton;

    private ProgressDialog pd;

    private int REQUEST_LOCATION = 99;

    private LocationManager locationManager;
    private String provider;
    //private MyLocationListener mylistener;
    private Criteria criteria;
    private Location location;
//    private SharedPreferences mPrefs;
    private String mUserId;
    private String mAuthToken;
    int PERMISSION_ID = 44;
    private FusedLocationProviderClient mFusedLocationClient;

    private Location mLastLocation;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private long lastIntentTime = 0;
    private static final long MIN_CLICK_INTERVAL = 1000; // 1000 milliseconds

    private String outletId;
    private String lat1;
    private String lon1;
    private String mOutletName;
    private TextView outletNameTextView;
//    private Button callButton;
//    private Button viewOnMapsButton;
    private String visitId;
//    private ImageView outletImageView;
    private static final int REQUEST_CALL_PHONE = 1;
    private Boolean notificationSent = false;
    private boolean isTaskRunning = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

//        mPrefs = getSharedPreferences("label", 0);
//        mUserId = mPrefs.getString("userId", "0");
//        mAuthToken = mPrefs.getString("authToken", "");
        mUserId = EncryptedPrefsUtil.getString("userId","0");
        mAuthToken = EncryptedPrefsUtil.getString("authToken", "");


        confirmCheckOutButton = (Button) findViewById(R.id.checkOutButton);

        outletNameTextView = (TextView) findViewById(R.id.outletNameTextView);
//        viewOnMapsButton = (Button) findViewById(R.id.viewOnMapsButton);
//        callButton = (Button) findViewById(R.id.callButton);
//        outletImageView = (ImageView) findViewById(R.id.outletImageView);

        Intent i = getIntent();
        outletId=i.getStringExtra("outletId");
        visitId=i.getStringExtra("visitId");
        //toastIconError("Outlet Id: "+outletId);
        //callingActivity=i.getStringExtra("callingActivity");
        mOutletName=i.getStringExtra("outletName");
        lat1 = i.getStringExtra("lat");
        lon1 = i.getStringExtra("long");
 //       Outlet outlet = (Outlet) i.getSerializableExtra("outlet");
        //toastIconError(Constants.STORAGE_URL+outlet.image);
//        Log.d("Image: ",Constants.STORAGE_URL+outlet.image);


        outletNameTextView.setText(mOutletName);

        initToolbar();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // user defines the criteria
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);   //default
        criteria.setCostAllowed(false);
        // get the best provider depending on the criteria
        //provider = locationManager.getBestProvider(criteria, false);
        provider=LocationManager.NETWORK_PROVIDER;

        //START OF LATEST LOCATION CALL UPDATE

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds'

        detectLocation();

        requestLocationUpdates();
        //END OF LATEST LOCATION CALL UPDATE

        location=null;
        if (ActivityCompat.checkSelfPermission(CheckoutActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(CheckoutActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(CheckoutActivity.this, new String[]
                    {ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        }else{
            //should select the best provider but bypassing with NETWORK Provider for now
            //location = locationManager.getLastKnownLocation(provider);
            location = locationManager.getLastKnownLocation(provider);
        }

//        Picasso.get()
//                .load(Constants.STORAGE_URL+outlet.image)
////                .placeholder(R.drawable.placeholder) // Optional placeholder image
//                .error(R.drawable.no_image_available) // Optional error image
//                .into(outletImageView);

//        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
//        sendIntent.setData(Uri.parse("sms:"));
//        sendIntent.putExtra("sms_body", Constants.STORAGE_URL+outlet.image);
//        startActivity(sendIntent);

        //mylistener = new MyLocationListener();

//        backButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(CheckinToExistingOutlet.this, SelectExistingCustomerActivity.class);
//                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//            }
//        });
//        viewOnMapsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String strUri = "http://maps.google.com/maps?q=loc:" + outlet.lat + "," + outlet.lon + " (" + outlet.name + ")";
//                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));
//
//                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
//                startActivity(intent);
//            }
//        });
//
//        callButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (ContextCompat.checkSelfPermission(CheckoutActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                    ActivityCompat.requestPermissions(CheckoutActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE);
//                } else {
//                    //makePhoneCall();
//                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + outlet.phone));
//                    startActivity(intent);
//                }
//
//
//
//            }
//        });

        confirmCheckOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // System.out.println("Item clicked " + obj.name + " clicked - id" + obj.id);


                //Toast.makeText(SelectExistingCustomerActivity.this,mOutletName,Toast.LENGTH_SHORT).show();

                LocationUtils.checkLocationServices(CheckoutActivity.this);
                long unixTime = System.currentTimeMillis() / 1000L;

            if (!isTaskRunning) {

                isTaskRunning = true;

                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

                //Double distance=MyUtils.calculateDistance(Double.parseDouble(lat1),Double.parseDouble(lon1),mLastLocation.getLatitude(),mLastLocation.getLongitude());

                //toastIconError(distance*1000+lat1+" - "+lon1+" - "+mLastLocation.getLatitude()+" - "+mLastLocation.getLongitude());

                if (mLastLocation != null) {

                    String locationText = mLastLocation.getLatitude() + " - " + mLastLocation.getLongitude() + "";
                    //   Toast.makeText(getBaseContext(),locationText,Toast.LENGTH_SHORT).show();
                    String lat2 = mLastLocation.getLatitude() + "";
                    String lon2 = mLastLocation.getLongitude() + "";
                    //   long unixTime = System.currentTimeMillis() / 1000L;

//                            String lat1 = "40.714268"; // New York
//                            String lon1 = "-74.005974";
//                            String lat2 = "34.0522"; // Los Angeles
//                            String lon2 = "-118.2437";
                    System.out.println("lat1: " + lat1 + " - long1: " + lon1 + " : lat2" + lat2 + " - " + lon2);

//                            double distanceAccurate = MyUtils.calculateVincentyDistance(Double.parseDouble(lat1), Double.parseDouble(lon1),
//                                    Double.parseDouble(lat2), Double.parseDouble(lon2));
                    double distanceEstimate = MyUtils.calculateDistance(Double.parseDouble(lat1), Double.parseDouble(lon1),
                            Double.parseDouble(lat2), Double.parseDouble(lon2));
                    System.out.println("Distance: " + distanceEstimate);
                    //toastIconError("Vincenty: "+distanceAccurate);
                    //new JsonTask().execute(Constants.BASE_URL + "api/logcheckin","POST",lat1+"",lon1+"",lat2+"",lon2+"");

                    // toastIconError("Distance: "+distanceEstimate);
//                    if (distanceEstimate < 0.04) {
                        // toastIconError(unixTime+" - "+lat2+" - "+lon2);
//                        new JsonTask().execute(Constants.BASE_URL + "api/checkout/" +
//                                visitId, "PUT", unixTime + "", lat2 + "", lon2 + "");

                    new TokenManager(CheckoutActivity.this,  (success, result) -> {
                        if (success) {
                            // Handle the JSON result, update UI, etc.
                            System.out.println("API Result: " + result);

                            handleResult(result);

                        } else {
                            isTaskRunning = false;
                            // Handle errors (e.g., token refresh failed)
                            Toasts.toastIconError(CheckoutActivity.this, result);
//                            Intent intent = new Intent(CheckoutActivity.this, LoginActivity.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(intent);

                        }
                    }).executeRequest(Constants.BASE_URL +"api/checkout/" +
                                visitId   , "PUT",
                            "checkout_time",unixTime+"","checkout_latitude",lat2,
                            "checkout_longitude",lon2);




                        // new JsonTask().execute(Constants.BASE_URL + "api/checkout", "POST", mUserId, unixTime + "", lat2 + "", lon2 + "", outletId);

                        //toastIconError("Checking In");
//                    } else {
//                        Toasts.toastIconError(CheckoutActivity.this,
//                                "Please move closer to the outlet");
//                        isTaskRunning = false;
//                    }

                } else {
                    Toasts.toastIconError(CheckoutActivity.this,
                            "Detecting Location...");
                    isTaskRunning = false;
                }
            }

            }
        });




    }

    private void handleResult(String result){
        isTaskRunning = false;

        try {

            System.out.println("Result: " + result);
            if (result != null ) {
                JSONObject jsonObject = new JSONObject(result);

                //Checkout Failed means server returns nothing
                //Error checking out means server responds with something other than success

                if(result.contains("errors")){
                    String message = jsonObject.getString("message");
                    Toasts.toastIconError(CheckoutActivity.this,message);
                    //Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                }
                else if (result.contains("success") ){


                    //txtJson.setText(jsonObject.toString());
                    //String visitId = jsonObject.getString("visitId");
                    //String outletId = jsonObject.getString("outletId");
                    //long unixTimestamp = Instant.now().getEpochSecond();



                    Intent intent = new Intent(CheckoutActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    //Toast.makeText(SelectExistingCustomerActivity.this,"visitId"+visitId+" - outletId"+outletId+" - outletName"+mOutletName,Toast.LENGTH_SHORT).show();
                    //intent.putExtra("visitId", visitId);
                    //intent.putExtra("outletName",mOutletName);
                    //intent.putExtra("outletId", outletId);
                    //intent.putExtra("lat","");
                    //intent.putExtra("long","");
                    startActivity(intent);


                }else{
                    Toasts.toastIconError(CheckoutActivity.this,
                            "Error Checking Out. Please Try Again");
                    FirebaseCrashlytics.getInstance().recordException(new Exception("Checkout Error: "+result));

                }



            }else
            {
                Toasts.toastIconError(CheckoutActivity.this,
                        "Checkout Failed. Please Try Again");
                FirebaseCrashlytics.getInstance().recordException(new Exception("Checkout Error: "+result));

            }


        } catch (JSONException e) {
            throw new RuntimeException(e);
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
        getSupportActionBar().setTitle("Check Out");
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
                Toasts.toastIconError(CheckoutActivity.this,
                        "Location Permission Denied");
                //Toast.makeText(this, "Location permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    //END OF LATEST LOCATION CODE
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
                        Double distance = MyUtils.calculateDistance(Double.parseDouble(lat1),Double.parseDouble(lon1),location.getLatitude(),location.getLongitude());
                        if (distance > 0.040 && !notificationSent )  {
                            // Initialize the NotificationHelper
                            NotificationHelper notificationHelper = new NotificationHelper(CheckoutActivity.this);
                            // Send a notification
                            notificationHelper.sendNotification("Check Out Reminder",
                                    "You're 40m outside the outlet range. " +
                                            "Please return to check out.");
                            notificationSent = true;

                        }
                        //toastIconError(distance+"");
                        //+" - "+mLastLocation.getAltitude()
                        //toastIconError(location.getLatitude()+" "+location.getLongitude()+mLastLocation.getLatitude()+" - "+mLastLocation.getLongitude()+" - distance: "+distance*1000);

                    }

                }
            }
        };
    }

    // PICKED FROM OLD LOCATION CODE BUT MIGHT BE VALID
    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
            detectLocation();
        }
    }
    // PICKED FROM OLD LOCATION CODE BUT MIGHT BE VALID


    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(CheckoutActivity.this);

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
                jsonParam.put("checkout_time", params[2]);
                jsonParam.put("checkout_latitude", params[3]);
                jsonParam.put("checkout_longitude", params[4]);

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

                System.out.println("Params " + params[2] + " " + params[3] + " " + params[4] );

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
                        Toasts.toastIconError(CheckoutActivity.this,message);
                        //Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                    }
                    else if (result.contains("success") ){


                        //txtJson.setText(jsonObject.toString());
                        //String visitId = jsonObject.getString("visitId");
                        //String outletId = jsonObject.getString("outletId");
                        //long unixTimestamp = Instant.now().getEpochSecond();



                        Intent intent = new Intent(CheckoutActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        //Toast.makeText(SelectExistingCustomerActivity.this,"visitId"+visitId+" - outletId"+outletId+" - outletName"+mOutletName,Toast.LENGTH_SHORT).show();
                        //intent.putExtra("visitId", visitId);
                        //intent.putExtra("outletName",mOutletName);
                        //intent.putExtra("outletId", outletId);
                        //intent.putExtra("lat","");
                        //intent.putExtra("long","");
                        startActivity(intent);


                    }else{
                        Toasts.toastIconError(CheckoutActivity.this,
                                "Error Checking Out. Please Try Again");
                        FirebaseCrashlytics.getInstance().recordException(new Exception("Checkout Error: "+result));

                    }



                }else
                {
                    Toasts.toastIconError(CheckoutActivity.this,
                            "Checkout Failed. Please Try Again");
                    FirebaseCrashlytics.getInstance().recordException(new Exception("Checkout Error: "+result));

                }


            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
    }




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
//                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
//                getLastLocationWithoutSubmit();
//            }
//        }
//    }





}
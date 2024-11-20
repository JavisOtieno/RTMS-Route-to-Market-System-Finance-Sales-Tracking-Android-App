package com.scg.rtmssf;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
//import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.scg.rtmssf.adapters.AdapterListActivities;
import com.scg.rtmssf.util.EncryptedPrefsUtil;
import com.scg.rtmssf.util.Toasts;

import org.json.JSONArray;

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

public class ActivitiesActivity extends AppCompatActivity {
    private Button checkOutButton;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private ProgressDialog pd;
    private Button makeSaleButton;
    private Button addPOSAuditButton;
    private Button addCompetitiveAnalysisButton;
    private Button addFeedbackButton;
    private String visitId;
    private String outletId;

    private int REQUEST_LOCATION = 99;

    private LocationManager locationManager;
    private String provider;
    //private MyLocationListener mylistener;
    private Criteria criteria;
    private Location location;
    private SharedPreferences mPrefs;
    private String mUserId;
    private String mAuthToken;
    private Location mLastLocation;
    private String callingActivity;
    int PERMISSION_ID = 44;
    private FusedLocationProviderClient mFusedLocationClient;
    private String outletName;
    private TextView outletNameTextView;
    private JSONArray sales = null;

    private RecyclerView recyclerView;
    private AdapterListActivities mAdapter;
    private Button viewSalesButton;
    private Button viewActivitiesButton;

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private String lat1;
    private String lon1;
    private String lastUpdatedActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities_activiy);
        initToolbar();

//        mPrefs = getSharedPreferences("label", 0);
        mAuthToken = EncryptedPrefsUtil.getString("authToken", "");

        lat1= mPrefs.getString("outletLat", "0");
        lon1 = mPrefs.getString("outletLong", "0");

        outletNameTextView = (TextView) findViewById(R.id.outletNameTextView);

        checkOutButton = (Button) findViewById(R.id.checkoutButton);
        makeSaleButton = (Button) findViewById(R.id.makeSaleButton);
        addPOSAuditButton = (Button) findViewById(R.id.posAuditButton);
        addCompetitiveAnalysisButton = (Button) findViewById(R.id.competitiveAnalysisButton);
        addFeedbackButton = (Button) findViewById(R.id.feedbackButton);
        viewSalesButton = (Button) findViewById(R.id.viewSalesButton);
        viewActivitiesButton = (Button) findViewById(R.id.viewActivitiesButton);

        Intent i = getIntent();
        visitId=i.getStringExtra("visitId");
        outletId=i.getStringExtra("outletId");
        //callingActivity=i.getStringExtra("callingActivity");
        outletName=i.getStringExtra("outletName");
        if(i.getStringExtra("lastUpdatedActivity")!=null) {
            lastUpdatedActivity = i.getStringExtra("lastUpdatedActivity");

            if(lastUpdatedActivity.equals("sale")){
                Toasts.toastIconSuccess(ActivitiesActivity.this,
                        "Sale Added Successfully");
            }else if(lastUpdatedActivity.equals("outlet")){
                Toasts.toastIconSuccess(ActivitiesActivity.this,
                        "Outlet Added Successfully");
            }
            else if(lastUpdatedActivity.equals("posaudit")){
                Toasts.toastIconSuccess(ActivitiesActivity.this,
                        "POS Audit Added Successfully");
            }else if(lastUpdatedActivity.equals("competitiveanalysis")){
                Toasts.toastIconSuccess(ActivitiesActivity.this,
                        "Competitive Analysis Added Successfully");
            }else if (lastUpdatedActivity.equals("feedback")){
                Toasts.toastIconSuccess(ActivitiesActivity.this,
                        "Feedback Added Successfully");
            }else{
//                Toasts.toastIconSuccess(ActivitiesActivity.this,
//                        lastUpdatedActivity);
            }
        }




//        outletNameTextView.setText(outletName);

//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // new JsonTask().execute(Constants.BASE_URL + "api/activitieslist","POST",visitId);

        // user defines the criteria
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);   //default
        criteria.setCostAllowed(false);
        // get the best provider depending on the criteria
        //provider = locationManager.getBestProvider(criteria, false);
        //should get the best but bypassing for now;
//        provider=LocationManager.NETWORK_PROVIDER;
//        location=null;
//        if (ActivityCompat.checkSelfPermission(ActivitiesActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ActivitiesActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//
//            ActivityCompat.requestPermissions(ActivitiesActivity.this, new String[]
//                    {ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
//
//        }else{
//            //should select the best provider but bypassing with NETWORK Provider for now
//            //location = locationManager.getLastKnownLocation(provider);
//            location = locationManager.getLastKnownLocation(provider);
//        }

        //mylistener = new MyLocationListener();



        makeSaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivitiesActivity.this, CheckinActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("visitId", visitId);
                intent.putExtra("outletId", outletId);
                intent.putExtra("outletName", outletName);
                intent.putExtra("callingActivity","makeSale");
                startActivity(intent);

            }
        });

        addPOSAuditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivitiesActivity.this, ProductsCheckListActivity.class);
                //Intent intent = new Intent(ActivitiesActivity.this, SelectProductActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("visitId", visitId);
                intent.putExtra("outletId", outletId);
                intent.putExtra("outletName", outletName);
                intent.putExtra("callingActivity","addPOSAudit");
                startActivity(intent);
            }
        });

        addCompetitiveAnalysisButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivitiesActivity.this, AddCompetitiveAnalysisActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("visitId", visitId);
                intent.putExtra("outletName", outletName);
                intent.putExtra("outletId", outletId);
                startActivity(intent);
            }
        });

        addFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivitiesActivity.this, AddFeedbackActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("visitId", visitId);
                intent.putExtra("outletName", outletName);
                intent.putExtra("outletId", outletId);
                startActivity(intent);
            }
        });

        viewSalesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivitiesActivity.this, VisitSalesActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("visitId", visitId);
                intent.putExtra("outletName", outletName);
                intent.putExtra("outletId", outletId);
                startActivity(intent);
            }
        });

        viewActivitiesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivitiesActivity.this, ViewActivitiesActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("visitId", visitId);
                intent.putExtra("outletName", outletName);
                intent.putExtra("outletId", outletId);
                startActivity(intent);
            }
        });

        //START OF LATEST LOCATION CALL UPDATE

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds'

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {

                    mLastLocation = location;


                    if(lat1!=null && mLastLocation!=null){
                        Double distance = MyUtils.calculateDistance(Double.parseDouble(lat1),Double.parseDouble(lon1),location.getLatitude(),location.getLongitude());
                        //toastIconError(distance+"");
                        //+" - "+mLastLocation.getAltitude()
                        //toastIconError(mLastLocation.getLatitude()+" - "+mLastLocation.getLongitude()+" - distance: "+distance*1000);

                    }else if(mLastLocation!=null){
                        //+" - "+mLastLocation.getAltitude()
                        //toastIconError(mLastLocation.getLatitude()+" - "+mLastLocation.getLongitude());

                    }

                }
            }
        };

        requestLocationUpdates();
        //END OF LATEST LOCATION CALL UPDATE

        checkOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ActivitiesActivity.this,CheckoutActivity.class);
                intent.putExtra("visitId",visitId);
                intent.putExtra("outletId",outletId);
                intent.putExtra("outletName",outletName);
                intent.putExtra("lat",lat1);
                intent.putExtra("long",lon1);
                //intent.putExtra("outlet",mOutlet);
                startActivity(intent);
//                // Initialize the NotificationHelper
//                NotificationHelper notificationHelper = new NotificationHelper(ActivitiesActivity.this);
//
//                // Send a notification
//                notificationHelper.sendNotification("Test Title", "Test Message");

//                long unixTime = System.currentTimeMillis() / 1000L;
//                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
//
//
//                if(mLastLocation!=null) {
//                    String locationText = mLastLocation.getLatitude() + " - " + mLastLocation.getLongitude() + "";
//                    // Toast.makeText(getBaseContext(),locationText,Toast.LENGTH_SHORT).show();
//                    String lat2 = mLastLocation.getLatitude() + "";
//                    String lon2 = mLastLocation.getLongitude() + "";
//                    //new JsonTask().execute(Constants.BASE_URL + "api/clockin", "POST", mUserId, unixTime + "", lat + "", lon + "");
//                    //new JsonTask().execute(Constants.BASE_URL + "api/addoutlet","POST",mUserId,outletName,contactPersonName,phone,lat+"",lon+"",unixTime+"");
//
//                    double distance = MyUtils.calculateDistance(Double.parseDouble(lat1), Double.parseDouble(lon1),
//                            Double.parseDouble(lat2), Double.parseDouble(lon2));
//                    System.out.println("Distance: " + distance);
////                            toastIconError(distance+"");
//                    if (distance < 0.04) {
//                        new JsonTask().execute(Constants.BASE_URL + "api/checkout/" + visitId, "PUT", unixTime + "", lat2 + "", lon2 + "");
//                    } else {
//                        toastIconError("Please move closer to the outlet");
//                    }
//                }else{
//                    toastIconError("Detecting Location...");
//                }



            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                Intent intent = new Intent(ActivitiesActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Select Activity");
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
                Toasts.toastIconError(ActivitiesActivity.this,
                        "Location Permission Denied");
                //Toast.makeText(this, "Location permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    //END OF LATEST LOCATION CODE

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(ActivitiesActivity.this);

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

                Uri.Builder builder;

                if (requestMethod.equals("PUT")) {
                    builder= new Uri.Builder()
                            .appendQueryParameter("checkout_time", params[2])
                            .appendQueryParameter("checkout_latitude", params[3])
                            .appendQueryParameter("checkout_longitude", params[4]);

                    System.out.println("Params " + params[2] + " " + params[3] + " " + params[4] );
                }else{
                    //if its a get message instead for the activities
                    builder = new Uri.Builder()
                            .appendQueryParameter("visit_id", params[2]);
                }

                String query = builder.build().getEncodedQuery();
                System.out.println("Query: "+query);
                System.out.println("RequestMethod: "+params[1]);

                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

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

//            try {
//
//                System.out.println("Resultsfs: " + result);
//                if (result != null) {
//
//                    //txtJson.setText(jsonObject.toString());
//
////                    if(!result.contains("Checkout")){
////
////                        JSONArray sales = new JSONArray(result);
////
////                        List<Map<String,String>> activities = new ArrayList<>();
////
////                        Map<String, String> activity = new HashMap<String, String>();
////
////                        System.out.println("Sales length: "+sales.length());
////
////                        for (int i = 0; i < sales.length(); i++) {
////                            //obj.image = drw_arr.getResourceId(i, -1);
////
////                            JSONArray saleItems = sales.getJSONObject(i).getJSONArray("saleitems");
////
////                            String allProductsSalesitemString="";
////
////
////                            for (int j = 0; j < saleItems.length(); j++) {
////                                String saleItemProductName=saleItems.getJSONObject(j).getJSONObject("product").getString("name");
////                                String saleItemPieces=saleItems.getJSONObject(j).getString("pieces");
////                                allProductsSalesitemString=allProductsSalesitemString+saleItemProductName+" - "+saleItemPieces+" \n";
////
////                                System.out.println("SaleItem: "+allProductsSalesitemString);
////
////                            }
////                            System.out.println("SaleItems: "+saleItems.length());
////                            activity.put("description", allProductsSalesitemString);
////
////                            //obj.name = sales.getJSONObject(i).getJSONObject("product").getString("name");
////                            String createdAt = sales.getJSONObject(i).getString("created_at");
////                            ZonedDateTime dateTime = Instant.parse(createdAt).atZone(ZoneId.of("Africa/Nairobi"));
////                            String formatted = dateTime.format(DateTimeFormatter.ofPattern("HH:mm "));
////                            activity.put("time", formatted);
////                            activity.put("type", "Sale");
////
////                            activities.add(activity);
////                        }
////
////                        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
////                        recyclerView.setLayoutManager(new LinearLayoutManager(ActivitiesActivity.this));
////                        recyclerView.setHasFixedSize(true);
////
////
////                        //set data and list adapter
////                        mAdapter = new AdapterListActivities(activities);
////                        recyclerView.setAdapter(mAdapter);
////
////                        // on item list clicked
////                        mAdapter.setOnItemClickListener(new AdapterListActivities.OnItemClickListener() {
////                            @Override
////                            public void onItemClick(View view, Map<String,String> obj, int position) {
////                                //Snackbar.make(parent_view, "Item " + obj.name + " clicked", Snackbar.LENGTH_SHORT).show();
////                                //Intent intent = new Intent(ActivitiesActivity.this, IndividualSaleActivity.class);
////                                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
////                                //intent.putExtra("visitId", visitId);
////                                //intent.putExtra("outletId", outletId);
////                                //intent.putExtra("activity", activities.get(position));
////                                //startActivity(intent);
////                            }
////                        });
////
////                    }
////                    else {
//
//                        Intent intent = new Intent(ActivitiesActivity.this, MainActivity.class);
//                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(intent);
////                    }
//
//                }
//
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            }

        }
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                // getting last
                // location from
                // FusedLocationClient
                // object
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        location = task.getResult();
                        //we should constantly check the location of the user as they try to check out
                        //that's why we've removed the if statement
                        if (location == null) {
                            requestNewLocationData();
                        } else {

                            String locationText=location.getLatitude() + " - "+location.getLongitude() + "";
                            // Toast.makeText(getBaseContext(),locationText,Toast.LENGTH_SHORT).show();
                            String lat2 = location.getLatitude()+"";
                            String lon2 = location.getLongitude()+"";
                            long unixTime = System.currentTimeMillis() / 1000L;
                            //new JsonTask().execute(Constants.BASE_URL + "api/clockin", "POST", mUserId, unixTime + "", lat + "", lon + "");
                            //new JsonTask().execute(Constants.BASE_URL + "api/addoutlet","POST",mUserId,outletName,contactPersonName,phone,lat+"",lon+"",unixTime+"");

                            double distance = MyUtils.calculateDistance(Double.parseDouble(lat1), Double.parseDouble(lon1),
                                    Double.parseDouble(lat2), Double.parseDouble(lon2));
                            System.out.println("Distance: "+distance);
//                            toastIconError(distance+"");
//                            if(distance<0.04){
                            new JsonTask().execute(Constants.BASE_URL + "api/checkout/"+visitId,"PUT",unixTime+"",lat2+"",lon2+"");
//                            }else{
//                                toastIconError("Please move closer to the outlet");
//                            }

                        }
                    }
                });



            } else {
                Toasts.toastIconError(ActivitiesActivity.this,
                        "Please turn on" + " your location...");
                //Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions();
        }
    }


    @SuppressLint("MissingPermission")
    private void getLastLocationWithoutSubmit() {
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                // getting last
                // location from
                // FusedLocationClient
                // object
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            String locationText=location.getLatitude() + " - "+location.getLongitude() + "";
                            //Toast.makeText(getBaseContext(),locationText,Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            } else {
                Toasts.toastIconError(ActivitiesActivity.this,
                        "Please turn on" + " your location...");
                //Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            mLastLocation = locationResult.getLastLocation();
            //toastIconError("mLastLocation:"+mLastLocation.getLatitude()+""+" - "+mLastLocation.getLongitude());
            //latitudeTextView.setText("Latitude: " + mLastLocation.getLatitude() + "");
            //longitTextView.setText("Longitude: " + mLastLocation.getLongitude() + "");
        }
    };

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
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
            getLastLocationWithoutSubmit();
        }
    }



}
package com.scg.sf;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.SearchManager;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.scg.sf.adapters.AdapterListOutlets;
import com.scg.sf.models.Outlet;
import com.scg.sf.util.EncryptedPrefsUtil;
import com.scg.sf.util.Toasts;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SelectExistingCustomerActivity extends AppCompatActivity {

    private Button selectOutletButton;
    private RecyclerView recyclerView;

    private ProgressDialog pd;
    private JSONArray outlets = null;
    private AdapterListOutlets mAdapter;
    private View parent_view;

//    private SharedPreferences mPrefs;
    private String mUserId;
    private String mAuthToken;
    private int REQUEST_LOCATION = 99;

    private LocationManager locationManager;
    private String provider;
    //private MyLocationListener mylistener;
    private Criteria criteria;
    private Location location;
    private FusedLocationProviderClient mFusedLocationClient;
    int PERMISSION_ID = 44;
    private String mOutletName;

    private Location mLastLocation;
    private TextView emptyView;

    private String outletId;
    private String lat1;
    private String lon1;

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private long lastIntentTime = 0;
    private static final long MIN_CLICK_INTERVAL = 1000; // 1000 milliseconds

    private List<Outlet> items;

    private TextView mNoOutletsTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_existing_outlet);
        initToolbar();

//        mPrefs = getSharedPreferences("label", 0);
//        mUserId = mPrefs.getString("userId", "0");
//        mAuthToken = mPrefs.getString("authToken", "");
        mUserId = EncryptedPrefsUtil.getString("userId","0");
        mAuthToken = EncryptedPrefsUtil.getString("authToken", "");
        mNoOutletsTextView = (TextView) findViewById(R.id.noOutletsText);



//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // user defines the criteria
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);   //default
        criteria.setCostAllowed(false);
        // get the best provider depending on the criteria
        //provider = locationManager.getBestProvider(criteria, false);
//        provider = LocationManager.NETWORK_PROVIDER;



        emptyView = (TextView) findViewById(R.id.empty_view);

        //START OF LATEST LOCATION CALL UPDATE

//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//
//        locationRequest = LocationRequest.create();
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        locationRequest.setInterval(10000); // 10 seconds
//        locationRequest.setFastestInterval(5000); // 5 seconds'
//
//        locationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                if (locationResult == null) {
//                    return;
//                }
//                for (Location location : locationResult.getLocations()) {
//
//                    mLastLocation = location;
//
//
//                    if(lat1!=null && mLastLocation!=null){
//                        Double distance = MyUtils.calculateDistance(Double.parseDouble(lat1),Double.parseDouble(lon1),location.getLatitude(),location.getLongitude());
//                        //toastIconError(distance+"");
//                        //+" - "+mLastLocation.getAltitude()
//                        toastIconError(mLastLocation.getLatitude()+" - "+mLastLocation.getLongitude()+" - distance: "+distance*1000);
//
//                    }else if(mLastLocation!=null){
//                        //+" - "+mLastLocation.getAltitude()
//                        toastIconError(mLastLocation.getLatitude()+" - "+mLastLocation.getLongitude());
//
//                    }
//
//                }
//            }
//        };
//
//        requestLocationUpdates();

        //END OF LATEST LOCATION CALL UPDATE







//        location = null;
//
//        if (ActivityCompat.checkSelfPermission(SelectExistingCustomerActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(SelectExistingCustomerActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//
//            ActivityCompat.requestPermissions(SelectExistingCustomerActivity.this, new String[]
//                    {ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
//
//        } else {
//            //should select the best provider but bypassing with NETWORK Provider for now
//            //location = locationManager.getLastKnownLocation(provider);
//            location = locationManager.getLastKnownLocation(provider);
//        }



        //mylistener = new MyLocationListener();

        //selectOutletButton=(Button) findViewById(R.id.selectOutletButton);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

//        new JsonTask().execute(Constants.BASE_URL + "api/outlets", "GET");



        new TokenManager(SelectExistingCustomerActivity.this,  (success, result) -> {
            if (success) {
                // Handle the JSON result, update UI, etc.
                System.out.println("API Result: " + result);

                handleResult(result);

            } else {
                // Handle errors (e.g., token refresh failed)
                Toasts.toastIconError(SelectExistingCustomerActivity.this, result);
                Intent intent = new Intent(SelectExistingCustomerActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }).executeRequest(Constants.BASE_URL +"api/outlets", "GET");


    }

    private void handleResult(String result) {
        if(pd != null && pd.isShowing() && !isFinishing()){
            pd.dismiss();
        }

        try {

            System.out.println("Result: " + result);
            if (result != null && result.contains("outlets")) {
                JSONObject jsonObject = new JSONObject(result);
                //txtJson.setText(jsonObject.toString());
                items = new ArrayList<>();

                if (jsonObject.has("outlets") &&
                        jsonObject.get("outlets") instanceof JSONArray) {

                    outlets = jsonObject.getJSONArray("outlets");
                    System.out.println("Outlets length: " + outlets.length());

                    for (int i = 0; i < outlets.length(); i++) {
                        Outlet obj = new Outlet();
                        //obj.image = drw_arr.getResourceId(i, -1);
                        obj.name = outlets.getJSONObject(i).getString("name");
                        obj.contact_person_name = outlets.getJSONObject(i).getString("contact_person_name");
                        obj.phone = outlets.getJSONObject(i).getString("phone");
                        obj.id = outlets.getJSONObject(i).getString("id");
                        obj.lat = outlets.getJSONObject(i).getString("lat");
                        obj.lon = outlets.getJSONObject(i).getString("long");
                        obj.image = outlets.getJSONObject(i).getString("image");
                        //obj.imageDrw = drw_arr.getDrawable(obj.image);
                        String createdAt = outlets.getJSONObject(i).getString("created_at");
                        ZonedDateTime dateTime = Instant.parse(createdAt).atZone(ZoneId.of("Africa/Nairobi"));
                        String formatted = dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy \nHH:mm"));
                        obj.date = formatted;
                        items.add(obj);
                    }

                    if (items.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);
                    }

                    //set data and list adapter
                    mAdapter = new AdapterListOutlets(items);
                    recyclerView.setAdapter(mAdapter);

                    // on item list clicked
                    mAdapter.setOnItemClickListener(new AdapterListOutlets.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, Outlet obj, int position) {

                            Outlet outlet = items.get(position);
                            showIndividualActivityWithoutDoubleClick(outlet);


                        }
                    });

                }


                else{
                    //this is logging the checkin
                    //toastIconError("logged");
                    FirebaseCrashlytics.getInstance().recordException(new Exception("SelectExistingOutlet Error: "+result));
                    Intent intent = new Intent(SelectExistingCustomerActivity.this, ErrorLoadingActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("callingActivity", "SelectExistingOutlet");

                    //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
                    startActivity(intent);
                    finish();
                }

            }else{
                FirebaseCrashlytics.getInstance().recordException(new Exception("SelectExistingOutlet Error: "+result));
                Intent intent = new Intent(SelectExistingCustomerActivity.this, ErrorLoadingActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("callingActivity", "SelectExistingOutlet");
                startActivity(intent);
                finish();
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
        getSupportActionBar().setTitle("Select Existing Customer");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Tools.setSystemBarColor(this);
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(SelectExistingCustomerActivity.this);

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
                String stringUrl = params[0];

                System.out.println("String Url: " + stringUrl);

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod(requestMethod);
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + mAuthToken);

                if (requestMethod.equals("POST")) {

                    Uri.Builder builder;

                    if (stringUrl.contains("api/logcheckin")) {
                        builder = new Uri.Builder()
                                .appendQueryParameter("outletLat", params[2])
                                .appendQueryParameter("outletLong", params[3])
                                .appendQueryParameter("checkinLat", params[4])
                                .appendQueryParameter("checkinLong", params[5]);

                    } else {

                        builder = new Uri.Builder()
                                .appendQueryParameter("userId", params[2])
                                .appendQueryParameter("time", params[3])
                                .appendQueryParameter("lat", params[4])
                                .appendQueryParameter("long", params[5])
                                .appendQueryParameter("outletId", params[6]);

                    }

                    String query = builder.build().getEncodedQuery();

                    OutputStream os = connection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
                    writer.write(query);
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

                       if(pd != null && pd.isShowing() && !isFinishing()){
                pd.dismiss();
            }

            try {

                System.out.println("Result: " + result);
                if (result != null && result.contains("outlets")) {
                        JSONObject jsonObject = new JSONObject(result);
                        //txtJson.setText(jsonObject.toString());
                        items = new ArrayList<>();

                        if (jsonObject.has("outlets") &&
                                jsonObject.get("outlets") instanceof JSONArray) {

                        outlets = jsonObject.getJSONArray("outlets");
                        System.out.println("Outlets length: " + outlets.length());

                        for (int i = 0; i < outlets.length(); i++) {
                            Outlet obj = new Outlet();
                            //obj.image = drw_arr.getResourceId(i, -1);
                            obj.name = outlets.getJSONObject(i).getString("name");
                            obj.contact_person_name = outlets.getJSONObject(i).getString("contact_person_name");
                            obj.phone = outlets.getJSONObject(i).getString("phone");
                            obj.id = outlets.getJSONObject(i).getString("id");
                            obj.lat = outlets.getJSONObject(i).getString("lat");
                            obj.lon = outlets.getJSONObject(i).getString("long");
                            obj.image = outlets.getJSONObject(i).getString("image");
                            //obj.imageDrw = drw_arr.getDrawable(obj.image);
                            String createdAt = outlets.getJSONObject(i).getString("created_at");
                            ZonedDateTime dateTime = Instant.parse(createdAt).atZone(ZoneId.of("Africa/Nairobi"));
                            String formatted = dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy \nHH:mm"));
                            obj.date = formatted;
                            items.add(obj);
                        }

                        if (items.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyView.setVisibility(View.GONE);
                        }

                        //set data and list adapter
                        mAdapter = new AdapterListOutlets(items);
                        recyclerView.setAdapter(mAdapter);

                        // on item list clicked
                        mAdapter.setOnItemClickListener(new AdapterListOutlets.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, Outlet obj, int position) {

                                Outlet outlet = items.get(position);
                                showIndividualActivityWithoutDoubleClick(outlet);


                            }
                        });

                    }


                    else{
                        //this is logging the checkin
                        //toastIconError("logged");
                        FirebaseCrashlytics.getInstance().recordException(new Exception("SelectExistingOutlet Error: "+result));
                        Intent intent = new Intent(SelectExistingCustomerActivity.this, ErrorLoadingActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("callingActivity", "SelectExistingOutlet");

                        //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
                        startActivity(intent);
                        finish();
                    }

                }else{
                    FirebaseCrashlytics.getInstance().recordException(new Exception("SelectExistingOutlet Error: "+result));
                    Intent intent = new Intent(SelectExistingCustomerActivity.this, ErrorLoadingActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("callingActivity", "SelectExistingOutlet");
                    startActivity(intent);
                    finish();
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

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
            location=mLastLocation;

            //implement checkin action on mlastlocation later
            //toastIconError("mLastLocation:"+mLastLocation.getLatitude()+""+" - "+mLastLocation.getLongitude());

            String locationText=location.getLatitude() + " - "+location.getLongitude() + "";
            //toastIconError(locationText);
            //Toast.makeText(getBaseContext(),locationText,Toast.LENGTH_SHORT).show();
            String lat2 = location.getLatitude()+"";
            String lon2 = location.getLongitude()+"";
            long unixTime = System.currentTimeMillis() / 1000L;

//                            String lat1 = "40.714268"; // New York
//                            String lon1 = "-74.005974";
//                            String lat2 = "34.0522"; // Los Angeles
//                            String lon2 = "-118.2437";
            System.out.println("lat1: "+lat1+" - long1: "+lon1+" : lat2"+lat2+" - "+lon2);

//                            double distanceAccurate = MyUtils.calculateVincentyDistance(Double.parseDouble(lat1), Double.parseDouble(lon1),
//                                    Double.parseDouble(lat2), Double.parseDouble(lon2));
            double distanceEstimate = MyUtils.calculateDistance(Double.parseDouble(lat1), Double.parseDouble(lon1),
                    Double.parseDouble(lat2), Double.parseDouble(lon2));
            System.out.println("Distance: "+distanceEstimate);
            //toastIconError("Vincenty: "+distanceAccurate);
            new JsonTask().execute(Constants.BASE_URL + "api/logcheckin","POST",lat1+"",lon1+"",lat2+"",lon2+"");


            //toastIconError("Haversine Distance: "+distanceEstimate);
            if(distanceEstimate<0.04){
//                                new JsonTask().execute(Constants.BASE_URL + "api/checkin","POST",mUserId,unixTime+"",lat2+"",lon2+"",outletId);
//                                SharedPreferences.Editor mEditor = mPrefs.edit();
//                                mEditor.putString("outletLat", lat1).commit();
//                                mEditor.putString("outletLong", lon1).commit();
//                                toastIconError("Checking In");
            }else{
                Toasts.toastIconError(SelectExistingCustomerActivity.this,
                        "Please move closer to the outlet");
            }
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
//                //mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
//                //getLastLocationWithoutSubmit();
//            }
//        }
//    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            //mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
            //getLastLocationWithoutSubmit();
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Search by outlet name...");
        AutoCompleteTextView searchTextView = (AutoCompleteTextView) searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        try {
            @SuppressLint("SoonBlockedPrivateApi") Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(searchTextView, R.drawable.cursor); //This sets the cursor resource ID to 0 or @null which will make it visible on white background
        } catch (Exception e) {

        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                populateAdapter(s);
                return false;
            }
        });

        return true;
    }

    //searchview
    private void populateAdapter(String query) {
//        if(query.equals("")){
//
//        }
//        else {

            //final ArrayList<Integer> counterGroup = new ArrayList<Integer>();
            List<Outlet> newOutletsList =new ArrayList<>();

            for (int counter = 0; counter < items.size(); counter++) {
                if(query.contains(" ")){
                    String[] querySplit = query.toLowerCase().split("\\s+");
                    String product_name_searched=items.get(counter).name.toLowerCase();


                    if(containsAllWords(product_name_searched,querySplit)){
                       // counterGroup.add(counter);
                        newOutletsList.add(items.get(counter));
                    }

                }else {
                    if (items.get(counter).name.toLowerCase().contains(query.toLowerCase())) {
                        //mNoProductsTextView.setVisibility(View.GONE);
                        //counterGroup.add(counter);
                        newOutletsList.add(items.get(counter));

                    }

                }

            }

            if (newOutletsList.size()>0) {
                mNoOutletsTextView.setVisibility(View.GONE);
            }else
            {
                mNoOutletsTextView.setVisibility(View.VISIBLE);
            }

            if(recyclerView==null) {
                recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            }
            mAdapter = new AdapterListOutlets(newOutletsList);
            recyclerView.setAdapter(mAdapter);
//            recyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View v,
//                                        int position, long id) {
//
//                    // Sending image id to FullScreenActivity
//                    Intent i = new Intent(MainActivity.this, DisplayProductsActivity.class);
//                    // passing array index
//                    i.putExtra("productClicked",productsList.get(counterGroup.get(position)) );
//                    startActivity(i);
//
//                }
//            });
            mAdapter.setOnItemClickListener(new AdapterListOutlets.OnItemClickListener() {
                @Override
                public void onItemClick(View view, Outlet obj, int position) {

                    Outlet outlet = newOutletsList.get(position);

                    showIndividualActivityWithoutDoubleClick(outlet);



                }
            });
//        }
    }
    public static boolean containsAllWords(String word, String ...keywords) {
        for (String k : keywords)
            if (!word.contains(k)) return false;
        return true;
    }
    private void showIndividualActivityWithoutDoubleClick(Outlet outlet){
        long currentTime = System.currentTimeMillis();
        //Toast.makeText(SalesActivity.this, "Current"+currentTime+" - Last"+lastIntentTime+" - Diff "+(currentTime - lastIntentTime) , Toast.LENGTH_SHORT).show();

        if (currentTime - lastIntentTime > MIN_CLICK_INTERVAL) {

            lastIntentTime = currentTime;
            // Handle the item click, e.g., start a new activity
            Intent intent = new Intent(SelectExistingCustomerActivity.this, CheckinToExistingOutlet.class);
            mOutletName = outlet.name;
            outletId = outlet.id;
            //Toast.makeText(SelectExistingCustomerActivity.this,"outletId"+outletId+" - outletName"+mOutletName,Toast.LENGTH_SHORT).show();
            intent.putExtra("outletName",mOutletName);
            intent.putExtra("outletId", outletId);
            intent.putExtra("lat",outlet.lat);
            intent.putExtra("long",outlet.lon);
            intent.putExtra("outlet",outlet);
            intent.putExtra("outlet", outlet);
            startActivity(intent);

        } else {
            // Ignore the click or show a message
            // Toast.makeText(ProductsListActivity.this, "Please wait before clicking again", Toast.LENGTH_SHORT).show();
        }
    }




}
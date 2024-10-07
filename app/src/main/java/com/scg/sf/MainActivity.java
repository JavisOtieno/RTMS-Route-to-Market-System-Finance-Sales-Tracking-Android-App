package com.scg.sf;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import static java.lang.Integer.parseInt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
//import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.firebase.FirebaseApp;
//import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.scg.sf.adapters.AdapterDashboardSalesTarget;
import com.scg.sf.adapters.AdapterListRecentSales;
import com.scg.sf.models.Product;
import com.scg.sf.models.Sale;
import com.scg.sf.models.SaleItem;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private Toolbar toolbar;
    private Button clockInButton;
    private ProgressDialog pd;
    TextView txtJson;
    private LocalDate clockinDate;
    private LocalDate currentDate;

    JSONArray sales = null;
    JSONArray products = null;
    private LinearLayout lyt_no_connection;

    private LinearLayout seeMoreProductRow;
    private LinearLayout seeMoreTargetsRow;

    private TextView outletsNumber;
    private TextView posAuditsNumber;
    private TextView salesNumber;
    private TextView feedbackNumber;
    private TextView visitsNumber;
    private TextView competitiveAnalysisNumber;
    private TextView targetSales;
    private TextView currentSales;
    private TextView dayTextView;
    private Button clockOutButton;
    private SharedPreferences mPrefs;
    private String mUserId;

    private int REQUEST_LOCATION = 99;

    private LocationManager locationManager;
    private String provider;
//    private MyLocationListener mylistener;
    private FusedLocationProviderClient mFusedLocationClient;

    // Initializing other items
    // from layout file
    TextView latitudeTextView, longitTextView;
    int PERMISSION_ID = 44;
    private Criteria criteria;
    private Location mLastLocation;
    private Location location;
    private String mAuthToken;

    private BarChart barChart;

    private RecyclerView recyclerViewRecentSales;
    private RecyclerView recyclerViewTargets;

    private ArrayList<SaleItem> saleItemsList;
    private List<Sale> items;
    private List<Product> itemsTargets;

    private AdapterListRecentSales mAdapter;
    private AdapterDashboardSalesTarget mAdapterDashboardTarget;
    private LinearLayout noSalesRow;
    private LinearLayout noTargetsRow;
    private SwipeRefreshLayout swipeRefreshLayout;

    private RelativeLayout errorLoadingRelativeLayout;
    private Button targetsButton;
    private LinearLayout targetsLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();
        initNavigationMenu();

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

//        FirebaseCrashlytics.getInstance().log("Testing Crashlytics");

//        FirebaseCrashlytics.getInstance().crash(); // Force a crash
//        forceCrash();


        noSalesRow = (LinearLayout) findViewById(R.id.noSalesRow);
        noTargetsRow = (LinearLayout) findViewById(R.id.noTargetsRow);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        errorLoadingRelativeLayout = findViewById(R.id.errorLoading);
        errorLoadingRelativeLayout.setVisibility(View.GONE);

        lyt_no_connection = (LinearLayout) findViewById(R.id.lyt_no_connection);
        //lyt_no_connection.setVisibility(View.VISIBLE);

        lyt_no_connection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
             //   lyt_no_connection.setVisibility(View.GONE);
                fetchData();
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        fetchData();
//
//
//                    }
//                }, 1000);
            }
        });


        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorPrimaryDark,
                R.color.colorAccent
        );
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

//        getLastLocation();
//        String locationText=location.getLatitude() + ""+location.getLongitude() + "";
//        Toast.makeText(getBaseContext(),locationText,Toast.LENGTH_SHORT).show();

        //barChart = findViewById(R.id.barChart_view);

        //showBarChart();

        //check whether user has signed in
        mPrefs = getSharedPreferences("label", 0);
        mUserId = mPrefs.getString("userId", "0");

//        Toasts.toastIconError(MainActivity.this,mUserId);

        mAuthToken = mPrefs.getString("authToken", "");

//        Toasts.toastIconError(MainActivity.this,mAuthToken);


        if (mUserId.equals("0")) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else{

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

//            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
//                provider = LocationManager.NETWORK_PROVIDER;
//                Toast.makeText(this, "Network is Enabled in your devide", Toast.LENGTH_SHORT).show();
//            }else{
//                provider = LocationManager.GPS_PROVIDER;
//                Toast.makeText(this, "GPS is Enabled in your devide", Toast.LENGTH_SHORT).show();
//            }
        // user defines the criteria
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);   //default
        criteria.setCostAllowed(false);
        // get the best provider depending on the criteria
        //provider = locationManager.getBestProvider(criteria, true);
        //should get the best but bypassing for now;

        //location = null;
        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]
                    {ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            // should select the best provider but bypassing with NETWORK Provider for now
            // location = locationManager.getLastKnownLocation(provider);
            // location = locationManager.getLastKnownLocation(provider);

        }

        //mylistener = new MyLocationListener();

            //display recyclerview
            initComponent();


        clockInButton = (Button) findViewById(R.id.clockInButton);
        clockOutButton = (Button) findViewById(R.id.clockoutButton);

//        targetsLayout = (LinearLayout) findViewById(R.id.targetsLayout);
        dayTextView = (TextView) findViewById(R.id.dayText);
        //txtJson = (TextView) findViewById(R.id.textView2);



        seeMoreProductRow = (LinearLayout) findViewById(R.id.seeMoreSection);
        seeMoreTargetsRow = (LinearLayout) findViewById(R.id.seeMoreTargetsSection);

        outletsNumber = (TextView) findViewById(R.id.outletsNumber);
        salesNumber = (TextView) findViewById(R.id.salesNumber);
        feedbackNumber = (TextView) findViewById(R.id.feedbackNumber);
        visitsNumber = (TextView) findViewById(R.id.visitsNumber);

//        currentSales = (TextView) findViewById(R.id.currentSales);
//        targetSales = (TextView) findViewById(R.id.targetSales);

        fetchData();

        clockInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // long unixTime = System.currentTimeMillis() / 1000L;

                Intent intent = new Intent(MainActivity.this, ClockinActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);



            }
        });
        clockOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ClockoutActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
//            targetsButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(MainActivity.this, ProductsListActivity.class);
//                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//                }
//            });

        seeMoreProductRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DealsActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

            seeMoreTargetsRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ProductsListActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });



    }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Perform your data fetching here
//                Intent intent = new Intent(MainActivity.this, MainActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
                fetchData();
                swipeRefreshLayout.setRefreshing(false);


            }
        });



    }

    public void forceCrash() {
        throw new RuntimeException("Test Crash"); // Force a crash
    }


    private void initToolbar() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Dashboard");
        //Tools.setSystemBarColor(this);;

    }

    private void initComponent() {

        recyclerViewRecentSales = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerViewRecentSales.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRecentSales.setHasFixedSize(true);

        recyclerViewTargets = (RecyclerView) findViewById(R.id.recyclerViewTargets);
        recyclerViewTargets.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTargets.setHasFixedSize(true);

        //List<Sale> items = DataGenerator.getPeopleData(this);
        //items.addAll(DataGenerator.getPeopleData(this));
        //items.addAll(DataGenerator.getPeopleData(this));

        //set data and list adapter
        //mAdapter = new AdapterListBasic(this, items);
        //recyclerView.setAdapter(mAdapter);

        // on item list clicked
//        mAdapter.setOnItemClickListener(new AdapterListBasic.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, Sale obj, int position) {
//                Snackbar.make(parent_view, "Item " + obj.name + " clicked", Snackbar.LENGTH_SHORT).show();
//            }
//        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            //finish();
        }else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }



    private void initNavigationMenu() {
        NavigationView nav_view = (NavigationView) findViewById(R.id.nav_view);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(final MenuItem item) {
                //Toast.makeText(getApplicationContext(), item.getTitle() + " Selected", Toast.LENGTH_SHORT).show();
                if(item.getTitle().equals("Dashboard")){
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    //finish();
                    //startActivity(getIntent());
                }else if(item.getTitle().equals("Deals")){
                    Intent intent = new Intent(MainActivity.this, SalesActivity.class);
                    startActivity(intent);
                }else if(item.getTitle().equals("Customers")){
                    Intent intent = new Intent(MainActivity.this, CustomersActivity.class);
                    //Intent intent2 = new Intent(MainActivity.this, LocationTestActivity.class);
                    startActivity(intent);
                }else if(item.getTitle().equals("Route Plans")){
                    Intent intent = new Intent(MainActivity.this, RouteplansActivity.class);
                    //Intent intent2 = new Intent(MainActivity.this, LocationTestActivity.class);
                    startActivity(intent);
                }

                else if(item.getTitle().equals("Products")){
                    Intent intent = new Intent(MainActivity.this, ProductsListActivity.class);
                    //Intent intent2 = new Intent(MainActivity.this, LocationTestActivity.class);
                    startActivity(intent);
                }else if(item.getTitle().equals("Visits")){
                    Intent intent = new Intent(MainActivity.this, VisitsActivity.class);
                    //Intent intent2 = new Intent(MainActivity.this, LocationTestActivity.class);
                    startActivity(intent);
                }
                else if(item.getTitle().equals("Competitive Analysis")){
                    Intent intent = new Intent(MainActivity.this, CompetitiveAnalysisListActivity.class);
                    //Intent intent2 = new Intent(MainActivity.this, LocationTestActivity.class);
                    startActivity(intent);
                }
                else if(item.getTitle().equals("Feedback")){
                    Intent intent = new Intent(MainActivity.this, FeedbackListActivity.class);
                    startActivity(intent);
                }
                else if(item.getTitle().equals("Geofence Test")){
//                    Intent intent = new Intent(MainActivity.this, GeofencingTest.class);
//                    startActivity(intent);
                }
                else if(item.getTitle().equals("Log out")){
                    SharedPreferences.Editor mEditor = mPrefs.edit();
                    mEditor.putString("userId", "0").commit();

                    Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }else if(item.getTitle().equals("Profile")){
                    Intent intent = new Intent(MainActivity.this,ProfileActivity.class);
                    startActivity(intent);
                }else if(item.getTitle().equals("POS Audits")){
                    Intent intent = new Intent(MainActivity.this,POSAuditsListActivity.class);
                    startActivity(intent);
                }

                //actionBar.setTitle(item.getTitle());
                drawer.closeDrawers();
                return true;

            }
        });

        // open drawer at start
        //drawer.openDrawer(GravityCompat.START);
    }

    private void showBarChart(){
        ArrayList<Double> valueList = new ArrayList<Double>();
        ArrayList<BarEntry> entries = new ArrayList<>();
        String title = "Total Sales";

        //input data
        for(int i = 0; i < 6; i++){
            valueList.add(i * 100.1);
        }
        String[] cars = {"Monday", "Tuesday", "Wednesday", "Thursday","Friday"};

        //fit the data into a bar
        for (int i = 0; i < valueList.size(); i++) {
            BarEntry barEntry = new BarEntry(i, valueList.get(i).floatValue());
            entries.add(barEntry);
        }

        BarDataSet barDataSet = new BarDataSet(entries, title);

        BarData data = new BarData(barDataSet);
        barChart.setData(data);
        barChart.invalidate();
        initBarDataSet(barDataSet);
    }

    private void initBarDataSet(BarDataSet barDataSet){
        //Changing the color of the bar
        barDataSet.setColor(Color.parseColor("#dc1814"));
        //Setting the size of the form in the legend
        barDataSet.setFormSize(15f);
        //showing the value of the bar, default true if not set
        barDataSet.setDrawValues(false);
        //setting the text size of the value of the bar
        barDataSet.setValueTextSize(12f);
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);

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
                //connection.setRequestProperty("");
                if(requestMethod.equals("POST")){
                    Uri.Builder builder = new Uri.Builder()
                            .appendQueryParameter("userId", params[2])
                            .appendQueryParameter("time", params[3])
                            .appendQueryParameter("lat", params[4])
                            .appendQueryParameter("long",params[5]);

                    String query = builder.build().getEncodedQuery();

                    OutputStream os = connection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
                    writer.write(query);
                    writer.flush();
                    writer.close();
                    os.close();

                    System.out.println("Params "+params[2]+" "+params[3]+" "+params[4]+" "+params[5]);
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
                Log.e(TAG,"Malf Message");
            } catch (IOException e) {
                Log.e("Message here: ",e.getMessage());
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

//            if (pd.isShowing()) {
//                pd.dismiss();
//            }
//            if(pd != null && pd.isShowing() ){
//                pd.dismiss();
//            }

            try {

                System.out.println("Result: "+result);
                if (result != null && result.contains("sales")) {
                    JSONObject jsonObject = new JSONObject(result);
                    //txtJson.setText(jsonObject.toString());


                        if (jsonObject.has("sales") &&
                                jsonObject.get("sales") instanceof JSONArray) {

                        swipeRefreshLayout.setVisibility(View.VISIBLE);
                        errorLoadingRelativeLayout.setVisibility(View.GONE);

                        sales = jsonObject.getJSONArray("sales");
                        products = jsonObject.getJSONArray("products");
                        //JSONArray saleItems = jsonObject.getJSONArray("saleItems");
                        String salesTotal = jsonObject.getString("salesCount");
                        String posAuditsTotal = jsonObject.getString("posauditsCount");
                        String outletsTotal = jsonObject.getString("outletsCount");
                        String feedbackTotal = jsonObject.getString("feedbackCount");
                        String visitsTotal = jsonObject.getString("visitsCount");
                        String competitiveAnalysisTotal = jsonObject.getString("competitorsCount");

                        String dailySalesTotal = jsonObject.getString("dailySalesTotal");
                        String currentSalesTarget = jsonObject.getString("currentTargets");

                        String clockinTime = jsonObject.getString("clockinTime");
                        String clockoutTime = jsonObject.getString("clockoutTime");
                        String checkoutTime = jsonObject.getString("checkoutTime");
                        String checkinTime = jsonObject.getString("checkinTime");
                        String currentTime = jsonObject.getString("currentTime");
//                        Toasts.toastIconError(MainActivity.this,checkinTime);
                        String visitId = jsonObject.getString("visitId");
                        String clockinId = jsonObject.getString("clockinId");
                        String outletId = jsonObject.getString("outletId");
                        String outletName = jsonObject.getString("outletName");
                        String outletLat = jsonObject.getString("outletLat");
                        String outletLong = jsonObject.getString("outletLong");
                        String userId = jsonObject.getString("userId");

                        String organizationProducts = jsonObject.getString("productsType");

                        long currentTimeMillis = Long.valueOf(currentTime + "000");
                        currentDate = null;
                        currentDate = Instant.ofEpochMilli(currentTimeMillis).
                                atZone(ZoneId.systemDefault()).toLocalDate();



                        //should be within clockin verified activities
                        //such as complete checkin and continue day
                        if(!clockinId.equals("undefined")){
                            SharedPreferences.Editor mEditor = mPrefs.edit();
//                            Toasts.toastIconError(MainActivity.this,clockinId);
                            mEditor.putString("clockinId", clockinId).commit();
                        }

                        if(!clockinTime.equals("undefined")) {

                        // millis value
                        long clockinMillis = Long.valueOf(clockinTime + "000");
                        // get the date in system default timezone
                        clockinDate = null;

                        clockinDate = Instant.ofEpochMilli(clockinMillis).atZone(ZoneId.systemDefault()).toLocalDate();

//                        Toasts.toastIconError(MainActivity.this,"clockinDate: "+clockinDate);
//                        Toasts.toastIconError(MainActivity.this," currentDate: "+currentDate);

                            // check if it's equals to today
                            //System.out.println(dt.equals(LocalDate.now(ZoneId.systemDefault())));
                            //System.out.println("LocalDate"+LocalDate.now(ZoneId.systemDefault()));
                            //System.out.println("UnixDate"+dt);
                            if ( clockinDate.equals(currentDate) ) {


                                //if not implement code below
                                dayTextView.setText("");
                                clockInButton.setText("Continue Day");
                                clockInButton.setVisibility(View.VISIBLE);
                                clockInButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(MainActivity.this, CheckinActivity.class);
                                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }
                                });
                                clockOutButton.setVisibility(View.VISIBLE);


                            }


                    }

                            LocalDate clockoutDate = null;
                            if(!clockoutTime.equals("undefined")) {
//                            Toasts.toastIconError(MainActivity.this,
//                                    "clockoutTime");
                                long clockoutMillis = Long.valueOf(clockoutTime + "000");
                                clockoutDate = Instant.ofEpochMilli(clockoutMillis).atZone(ZoneId.systemDefault()).toLocalDate();

                                if (clockoutDate.equals(currentDate)) {
//                                Toasts.toastIconError(MainActivity.this,
//                                        clockoutDate+" "+currentDate);
                                    dayTextView.setText("Day Complete. Wait till next day");
                                    clockOutButton.setVisibility(View.GONE);
                                    clockInButton.setVisibility(View.GONE);
                                }

                            }

                            if(!checkoutTime.equals("undefined") &&
                                    !clockinTime.equals("undefined")) {
//                                Toasts.toastIconError(MainActivity.this,
//                                        "checkOutTime");

                                LocalDate checkinDate = null;
                                long checkinMillis = Long.valueOf(checkinTime + "000");
                                checkinDate = Instant.ofEpochMilli(checkinMillis).atZone(ZoneId.systemDefault()).toLocalDate();


                                clockinDate = null;
                                long clockinMillis = Long.valueOf(clockinTime + "000");
                                clockinDate = Instant.ofEpochMilli(clockinMillis).atZone(ZoneId.systemDefault()).toLocalDate();

                                if (checkoutTime.equals("")
                                        && clockinDate.equals(currentDate)
                                        && checkinDate.equals(currentDate)
                                ) {

                                    dayTextView.setText("Checkin Incomplete");
                                    clockOutButton.setVisibility(View.GONE);
                                    clockInButton.setText("Complete Checkin");
                                    clockInButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(MainActivity.this, SelectActionActivity.class);
                                            intent.putExtra("callingActivity", "MainActivity");
                                            intent.putExtra("visitId", visitId);
                                            intent.putExtra("outletId", outletId);
                                            intent.putExtra("outletName", outletName);
                                            SharedPreferences.Editor mEditor = mPrefs.edit();
                                            mEditor.putString("outletLat", outletLat).commit();
                                            mEditor.putString("outletLong", outletLong).commit();
                                            startActivity(intent);
                                        }
                                    });
                                    //clockInButton.setVisibility(View.GONE);
                                }
                            }
//                        else{
//                            Toasts.toastIconError(MainActivity.this,
//                                    "Everything is undefined");
//                            //display START DAY
//                        }





                        //displaying recent sales
                        items = new ArrayList<>();
                        Integer arrayLimit = 0;

                        if(sales.length()<4){
                            arrayLimit=sales.length();
                        }else{
                            arrayLimit=4;
                        }

                        System.out.println("ArrayLimit: "+arrayLimit);


                        for (int i = 0; i < arrayLimit; i++) {
                            Sale obj = new Sale();
                            //obj.image = drw_arr.getResourceId(i, -1);

                            JSONArray saleItems = sales.getJSONObject(i).getJSONArray("saleitems");

                            String allProductsSalesitemString="";

                            String firstsaleItemProductName="";

                            if(!saleItems.getJSONObject(0).isNull("product")){
                                firstsaleItemProductName = saleItems.getJSONObject(0).getJSONObject("product")
                                        .getString("name");
                            }else{
                                firstsaleItemProductName = "Product Not Found";
                            }

                            String firstsaleItemPieces=
                                    saleItems.getJSONObject(0).getString("pieces");
                            String singleProductsSalesitemString=firstsaleItemProductName+" - "+firstsaleItemPieces;

                            saleItemsList = new ArrayList<>();

                            for (int j = 0; j < saleItems.length(); j++) {
                                String saleItemProductName="";

                                if(!saleItems.getJSONObject(0).isNull("product")){
                                    saleItemProductName = saleItems.getJSONObject(0).getJSONObject("product")
                                            .getString("name");
                                }else{
                                    saleItemProductName = "Product Not Found";
                                }

                                String saleItemPieces=saleItems.getJSONObject(j).getString("pieces");
                                allProductsSalesitemString=allProductsSalesitemString+saleItemProductName+" - "+saleItemPieces+" ";

                                System.out.println("SaleItem: "+allProductsSalesitemString);

                                SaleItem saleItem=new SaleItem();
                                if(!saleItems.getJSONObject(j).isNull("product")){
                                    saleItem.productName = saleItems.getJSONObject(j).getJSONObject("product")
                                            .getString("name");
                                }else{
                                    saleItem.productName = "Product Not Found";
                                }
                                saleItem.pieces=saleItems.getJSONObject(j).getString("pieces");
                                saleItem.price=saleItems.getJSONObject(j).getString("price");
                                saleItem.value=saleItems.getJSONObject(j).getString("value");
                                saleItemsList.add(saleItem);

                            }
                            if(saleItems.length()>1){
                                singleProductsSalesitemString += "...";
                            }


                            System.out.println("SaleItems: "+saleItems.length());
                            obj.name=singleProductsSalesitemString;
                            obj.allproductsname=allProductsSalesitemString;
                            obj.saleItems = saleItemsList;
                            //saleItemsList.clear();


                            //obj.name = sales.getJSONObject(i).getJSONObject("product").getString("name");
                            String createdAt = sales.getJSONObject(i).getString("created_at");
                            ZonedDateTime dateTime = Instant.parse(createdAt).atZone(ZoneId.of("Africa/Nairobi"));
                            //removed time HH:mm  from formatting to decongest the recent sales layout
                            String formatted = dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
                            obj.time = formatted;

                            Integer pieces = Integer.valueOf(sales.getJSONObject(i).getString("pieces"));
                            String sellerName = sales.getJSONObject(i).getJSONObject("user").getString("name");
                            String outletNameFromSales = sales.getJSONObject(i).getJSONObject("outlet").getString("name");
                            String allProductsName = allProductsSalesitemString;
                            obj.pieces=pieces+"";
                            obj.sellerName=sellerName;
                            obj.outletName=outletNameFromSales;
                            obj.allproductsname=allProductsName;
                            obj.status = sales.getJSONObject(i).getString("status");
                            //Integer price = Integer.valueOf(sales.getJSONObject(i).getString("price"));
                            Integer value = Integer.valueOf(sales.getJSONObject(i).getString("value"));
                            obj.price = "KSh. "+NumberFormat.getNumberInstance(Locale.US).format(value) ;
                            //obj.imageDrw = drw_arr.getDrawable(obj.image);
                            items.add(obj);
                        }

                            //displaying recent sales
                            itemsTargets = new ArrayList<>();
                            Integer arrayLimitTargets = 0;

                            if(products.length()<4){
                                arrayLimitTargets=products.length();
                            }else{
                                arrayLimitTargets=4;
                            }

                        for (int i = 0; i < arrayLimitTargets; i++) {
                                Product obj = new Product();
                                //obj.image = drw_arr.getResourceId(i, -1);

                                JSONObject product = products.getJSONObject(i);

                                obj.name=product.getString("name");
                                obj.targetType = product.getString("targettype");

                                if(obj.targetType.equals("amount")){
                                    System.out.println("Total Sales: "+product.get("total_sales"));
                                    if(product.get("total_sales") instanceof Integer) {
                                        System.out.println( "Total Sales: "+product.get("total_sales") );
                                        Integer pendingTarget = Integer.valueOf(product.getString("target"))
                                                - Integer.valueOf(product.getString("total_sales"));
                                        if(pendingTarget<0){
                                            pendingTarget = 0;
                                        }
                                        obj.target = "KSh. " + NumberFormat.getNumberInstance(Locale.US).format(
                                                pendingTarget);
                                    }else{
                                        System.out.println( "Total Sales: "+"does not exist" );
                                        obj.target = "KSh. " + NumberFormat.getNumberInstance(Locale.US).format(
                                                Integer.valueOf(product.getString("target")));
                                    }

//                                        if(product.get("total_sales") instanceof String){
//                                    obj.currentSalesTotal =  "Current : KSh. "+NumberFormat.getNumberInstance(Locale.US).format(
//                                            Integer.valueOf(product.getString("total_sales")));
//                                    }else{
//                                        obj.currentSalesTotal = "Current : KSh. 0";
//                                    }

                                }else{

                                    if(product.get("total_quantity") instanceof Integer) {
                                        System.out.println( "Total Sales: "+product.get("total_sales") );
                                        Integer pendingTarget = Integer.valueOf(product.getString("target")) -
                                                Integer.valueOf(product.getString("total_quantity"));
                                        if(pendingTarget<0){
                                            pendingTarget = 0;
                                        }
                                        obj.target = NumberFormat.getNumberInstance(Locale.US).format(
                                                pendingTarget);

                                    }else{
                                        obj.target = NumberFormat.getNumberInstance(Locale.US).format(
                                            Integer.valueOf(product.getString("target")));

                                    }

//                                    obj.target = NumberFormat.getNumberInstance(Locale.US).format(
//                                            Integer.valueOf(product.getString("target")));
//                                    if(product.getString("total_quantity") instanceof String){
//                                        obj.currentSalesQuantity = "Current : "+NumberFormat.getNumberInstance(Locale.US).format(
//                                                Integer.valueOf(product.getString("total_quantity")));
//                                    }else{
//                                        obj.currentSalesQuantity = "Current : 0";
//                                    }

                                }

                                //obj.imageDrw = drw_arr.getDrawable(obj.image);
                                itemsTargets.add(obj);
                            System.out.println("Nametarget"+obj.name+" "+obj.target);

                            }

                            if (itemsTargets.isEmpty()) {
                                recyclerViewTargets.setVisibility(View.GONE);
                                noTargetsRow.setVisibility(View.VISIBLE);
                                System.out.println("Items: is empty");
                                seeMoreTargetsRow.setVisibility(View.GONE);
                            }
                            else {
                                recyclerViewTargets.setVisibility(View.VISIBLE);
                                noTargetsRow.setVisibility(View.GONE);
                                seeMoreTargetsRow.setVisibility(View.VISIBLE);
                                System.out.println("Items: is not empty");
                            }

                            //set data and list adapter
                            mAdapterDashboardTarget = new AdapterDashboardSalesTarget(itemsTargets);
                            recyclerViewTargets.setAdapter(mAdapterDashboardTarget);

                            if (items.isEmpty()) {
                                recyclerViewRecentSales.setVisibility(View.GONE);
                                noSalesRow.setVisibility(View.VISIBLE);
                                seeMoreProductRow.setVisibility(View.GONE);
                                System.out.println("Items sales: is empty");
                            }
                            else {
                                recyclerViewRecentSales.setVisibility(View.VISIBLE);
                                noSalesRow.setVisibility(View.GONE);
                                seeMoreProductRow.setVisibility(View.VISIBLE);
                                System.out.println("Items sales: is not empty");
                            }







                        //set data and list adapter
                        mAdapter = new AdapterListRecentSales(items);
                        recyclerViewRecentSales.setAdapter(mAdapter);





                        // on item list clicked
                        mAdapter.setOnItemClickListener(new AdapterListRecentSales.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, Sale obj, int position) {
                                //Snackbar.make(parent_view, "Item " + obj.name + " clicked", Snackbar.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this, IndividualSaleActivity.class);
                                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                //intent.putExtra("visitId", visitId);
                                //intent.putExtra2("outletId", outletId);
                                intent.putExtra("sale", items.get(position));
                                startActivity(intent);
                            }
                        });



                    outletsNumber.setText(outletsTotal);
                    salesNumber.setText(salesTotal);
                    feedbackNumber.setText(feedbackTotal);
                    visitsNumber.setText(visitsTotal);

//                    currentSales.setText("KSh " + NumberFormat.getNumberInstance(Locale.US).format(parseInt(dailySalesTotal)));
//                    targetSales.setText("KSh " + NumberFormat.getNumberInstance(Locale.US).format(parseInt(currentSalesTarget)));
//                    if(parseInt(dailySalesTotal)>=parseInt(currentSalesTarget)){
//                        currentSales.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.green_400));
//                    }

                    //firstProductName.setText(sales.getJSONObject(0).getJSONObject("product").getString("name"));
                    //secondProductName.setText(sales.getJSONObject(1).getJSONObject("product").getString("name"));
                    //thirdProductName.setText(sales.getJSONObject(2).getJSONObject("product").getString("name"));
                    //fourthProductName.setText(sales.getJSONObject(3).getJSONObject("product").getString("name"));

                    //secondProductName.setText(sales.getJSONObject(1).getString("name"));
                    //thirdProductName.setText(sales.getJSONObject(2).getString("name"));
                    //fourthProductName.setText(sales.getJSONObject(3).getString("name"));

               /* for (int i = 0; i < sales.length(); i++) {
                    JSONObject c = sales.getJSONObject(i);

                    // Storing each json item in variable
                    String id = c.getString("id");
                    String user_id = c.getString("user_id");
                    String name = c.getString("name");
                    String sku = c.getString("sku");
                    String pieces = c.getString("pieces");

                    txtJson.setText("id: "+id);


                    // creating new HashMap
                    //HashMap<String, String> map = new HashMap<String, String>();

                    // adding each child node to HashMap key => value
                    //map.put(TAG_PID, id);
                    //map.put(TAG_NAME, name);

                    //productName = name;
                    //productImageUrl=c.getString("image");

                    //productsList.add(new ProductOnline(name, price,specs,image,profit,id));

                }

                */
                }
//                    else if(result.contains("Clockin")){
//                        Intent intent = new Intent(MainActivity.this, CheckinActivity.class);
//                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(intent);
//                    }
                    else{
                    //Intent intent = new Intent(MainActivity.this, CheckinActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    //startActivity(intent);
                    swipeRefreshLayout.setVisibility(View.GONE);
                    FirebaseCrashlytics.getInstance().recordException(new Exception("Dashboard Error: "+result));
                    errorLoadingRelativeLayout.setVisibility(View.VISIBLE);
//                    Toasts.toastIconError(MainActivity.this,
//                            "Does not Contain Sales");

                }

            }else{

                    swipeRefreshLayout.setVisibility(View.GONE);
                    FirebaseCrashlytics.getInstance().recordException(new Exception("Dashboard Error: "+result));
                    errorLoadingRelativeLayout.setVisibility(View.VISIBLE);
//                    Toasts.toastIconError(MainActivity.this,"Is Null");

                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }

        public String convertMysqlTimestampToDate(String dateAsString) {

            ZonedDateTime dateTime = Instant.parse(dateAsString).atZone(ZoneId.of("Africa/Nairobi"));
            String formatted = dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm"));

            return formatted;
        }

    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    //make sure mainactivity is restarted on back button pressed
    @Override
    public void onRestart()
    {
        super.onRestart();
        fetchData();
//        Intent intent = new Intent(MainActivity.this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);



    }
    public void fetchData(){

        if (isNetworkConnected() != true) {
            Intent intent = new Intent(MainActivity.this, NoInternetActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {

            new JsonTask().execute(Constants.BASE_URL + "api/dashboard", "GET");

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
                        //we should check the location of the user as they
                        //click on clock in that's why we've removed the if statement

                        location = task.getResult();
//                        if (location == null) {
                            requestNewLocationData();
//                        } else {
                            String locationText=location.getLatitude() + " - "+location.getLongitude() + "";
                     //       Toast.makeText(getBaseContext(),locationText,Toast.LENGTH_SHORT).show();
                            String lat = location.getLatitude()+"";
                            String lon = location.getLongitude()+"";
                            long unixTime = System.currentTimeMillis() / 1000L;
                            new JsonTask().execute(Constants.BASE_URL + "api/clockin", "POST", mUserId, unixTime + "", lat + "", lon + "");

//                        }
                    }
                });
            } else {
                Toasts.toastIconError(MainActivity.this,
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

    //introduced to prevent logging of last location onresume
    //this is to ensure the app is not reloaded with the previous location to interfere
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
                        //    Toast.makeText(getBaseContext(),locationText,Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            } else {
                Toasts.toastIconError(MainActivity.this,
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
            location=mLastLocation;
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
    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
               // getLastLocation();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(
                    getApplicationContext());
           // getLastLocationWithoutSubmit();
        }
    }




}


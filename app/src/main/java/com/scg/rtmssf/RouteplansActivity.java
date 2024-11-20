package com.scg.rtmssf;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import com.scg.rtmssf.adapters.AdapterListRoutePlans;
import com.scg.rtmssf.models.Routeplan;
import com.scg.rtmssf.models.RouteplanItem;
import com.scg.rtmssf.util.EncryptedPrefsUtil;
import com.scg.rtmssf.util.PreventDoubleClickOnLists;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

public class RouteplansActivity extends AppCompatActivity {
    private RecyclerView recyclerView;

    private ProgressDialog pd;
    private JSONArray routeplans = null;
    private AdapterListRoutePlans mAdapter;
    private String mAuthToken;
//    private SharedPreferences mPrefs;
    private ArrayList<RouteplanItem> routeplanItemsList;
    private View parent_view;
    private TextView emptyView;

    private long lastIntentTime = 0;
    private static final long MIN_CLICK_INTERVAL = 1000; // 1000 milliseconds

    private List<Routeplan> items;
    private TextView mNoRouteplansTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routeplans);

        initToolbar();

//        mPrefs = getSharedPreferences("label", 0);
//        mAuthToken = mPrefs.getString("authToken", "");
//        mUserId = EncryptedPrefsUtil.getString("userId","0");
        mAuthToken = EncryptedPrefsUtil.getString("authToken", "");
        mNoRouteplansTextView= (TextView)findViewById(R.id.noRouteplansText);


        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        emptyView = (TextView) findViewById(R.id.empty_view);

        new JsonTask().execute(Constants.BASE_URL + "api/routeplans","GET");


    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Route Plans");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Tools.setSystemBarColor(this);
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

        // final ArrayList<Integer> counterGroup = new ArrayList<Integer>();
        List<Routeplan> newRouteplansList =new ArrayList<>();

        for (int counter = 0; counter < items.size(); counter++) {
            if(query.contains(" ")){
                String[] querySplit = query.toLowerCase().split("\\s+");
                String product_name_searched=items.get(counter).name.toLowerCase();


                if(containsAllWords(product_name_searched,querySplit)){
                    //counterGroup.add(counter);
                    newRouteplansList.add(items.get(counter));
                }

            }else {
                if (items.get(counter).name.toLowerCase().contains(query.toLowerCase())) {
                    //mNoProductsTextView.setVisibility(View.GONE);
                    //counterGroup.add(counter);
                    newRouteplansList.add(items.get(counter));

                }

            }

        }

        if (newRouteplansList.size()>0) {
            mNoRouteplansTextView.setVisibility(View.GONE);
        }else
        {
            mNoRouteplansTextView.setVisibility(View.VISIBLE);
        }

        if(recyclerView==null) {
            recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        }
        mAdapter = new AdapterListRoutePlans(newRouteplansList);
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
        mAdapter.setOnItemClickListener(new AdapterListRoutePlans.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Routeplan obj, int position) {

                Routeplan routeplan= newRouteplansList.get(position);


                PreventDoubleClickOnLists.
                        showActivityWithoutDoubleClick(
                                "routeplan",RouteplansActivity.this,
                                routeplan, IndividualRouteplanActivity.class);



            }
        });
//        }
    }
    public static boolean containsAllWords(String word, String ...keywords) {
        for (String k : keywords)
            if (!word.contains(k)) return false;
        return true;
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(RouteplansActivity.this);

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

                       if(pd != null && pd.isShowing() && !isFinishing()){
                pd.dismiss();
            }

            try {

                System.out.println("Result: "+result);
                if (result != null  && result.contains("routeplans")) {
                    JSONObject jsonObject = new JSONObject(result);
                    //txtJson.setText(jsonObject.toString());
                    if (jsonObject.has("routeplans") &&
                            jsonObject.get("routeplans") instanceof JSONArray) {
                        items = new ArrayList<>();

                        routeplans = jsonObject.getJSONArray("routeplans");
                        System.out.println("Route plans length: " + routeplans.length());

                        for (int i = 0; i < routeplans.length(); i++) {
                            Routeplan obj = new Routeplan();
                            //obj.image = drw_arr.getResourceId(i, -1);
                            obj.name=routeplans.getJSONObject(i).getString("name");

                            //obj.name = posAudits.getJSONObject(i).getJSONObject("outlet").getString("name");
                            String createdAt = routeplans.getJSONObject(i).getString("created_at");
                            ZonedDateTime dateTime = Instant.parse(createdAt).atZone(ZoneId.of("Africa/Nairobi"));
                            String formatted = dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy \nHH:mm "));
                            obj.date = formatted;

                            String scheduledDate = routeplans.getJSONObject(i).getString("date");
                            String formattedScheduledDate = dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
                            obj.scheduledDate = formattedScheduledDate;

                            JSONArray routeplanItems = routeplans.getJSONObject(i).
                                    getJSONArray("routeplanitems");
                            String routeplanItemString = "";

                            routeplanItemsList = new ArrayList<>();
                            System.out.println("Routeplanitemslength: "+routeplanItems.length());



                            for (int j = 0; j < routeplanItems.length(); j++) {

                                String routeplanItemOutletName="";
                                RouteplanItem routeplanItem = new RouteplanItem();

                                if(!routeplanItems.getJSONObject(j).isNull("outlet")){
                                    routeplanItemOutletName = routeplanItems.getJSONObject(j).
                                            getJSONObject("outlet").getString("name");
                                    routeplanItem.outletName = routeplanItems.getJSONObject(j).
                                            getJSONObject("outlet").getString("name");
                                    routeplanItem.outletLat=routeplanItems.getJSONObject(j).
                                            getJSONObject("outlet").getString("lat");
                                    routeplanItem.outletLong=routeplanItems.getJSONObject(j).
                                            getJSONObject("outlet").getString("long");

                                }else{
                                    routeplanItemOutletName = "Outlet Not Found";
                                    routeplanItem.outletName = "Outlet Not Found";
                                    routeplanItem.outletLat="-1.3";
                                    routeplanItem.outletLong="36.8";
                                }



//                                String posAuditItemPieces = routeplanItems.
//                                        getJSONObject(j).getJSONObject("outlet").getString("name");


//                                routeplanItem.pieces = routeplanItems.getJSONObject(j)
//                                        .getString("pieces");
                                routeplanItemsList.add(routeplanItem);

                                if (j == 2) {
                                    routeplanItemString = routeplanItemString + "...";
                                } else if (j <= 1) {
                                    routeplanItemString = routeplanItemString + routeplanItemOutletName + "\n";
                                }






                            }

//                            LatLng waypoint1 = new LatLng(40.758896, -73.985130); // Example waypoint 1
//                            LatLng waypoint2 = new LatLng(40.761581, -73.977691); // Example waypoint 2
//
//                            waypoints = waypoint1.latitude + "," + waypoint1.longitude + "|" +
//                                    waypoint2.latitude + "," + waypoint2.longitude;





                            obj.routeplanItemsPreview = routeplanItemString;
                            obj.routeplanItems = routeplanItemsList;



                            //obj.imageDrw = drw_arr.getDrawable(obj.image);
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
                        mAdapter = new AdapterListRoutePlans(items);
                        recyclerView.setAdapter(mAdapter);

                        // on item list clicked
                        mAdapter.setOnItemClickListener(new AdapterListRoutePlans.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, Routeplan obj, int position) {

                                Routeplan routeplan = items.get(position);


                                PreventDoubleClickOnLists.
                                        showActivityWithoutDoubleClick(
                                                "routeplan",RouteplansActivity.this,
                                                routeplan, IndividualRouteplanActivity.class);


                            }
                        });


//                    outletsNumber.setText(outletsTotal);
//                    posAuditsNumber.setText(posAuditsTotal);
//                    salesNumber.setText(salesTotal);
//                    feedbackNumber.setText(feedbackTotal);
//                    targetSales.setText("KSh " + NumberFormat.getNumberInstance(Locale.US).format(parseInt(dailySalesTotal)));
//                    currentSales.setText("KSh " + NumberFormat.getNumberInstance(Locale.US).format(parseInt(currentSalesTarget)));
                    }else{
                        FirebaseCrashlytics.getInstance().recordException(new Exception("Routeplans Error: "+result));

                        Intent intent = new Intent(RouteplansActivity.this, ErrorLoadingActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("callingActivity", "routeplans");

                        //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
                        startActivity(intent);
                        finish();
                    }

                }else{
                    FirebaseCrashlytics.getInstance().recordException(new Exception("Routeplans" +
                            " Error: "+result));

                    Intent intent = new Intent(RouteplansActivity.this, ErrorLoadingActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("callingActivity", "routeplans");

                    //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
                    startActivity(intent);
                    finish();
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }





    }


}
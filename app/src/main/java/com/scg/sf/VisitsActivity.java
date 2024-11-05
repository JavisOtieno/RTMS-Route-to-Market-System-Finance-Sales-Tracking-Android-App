package com.scg.sf;

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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.scg.sf.adapters.AdapterListVisits;
import com.scg.sf.models.Visit;
import com.scg.sf.util.EncryptedPrefsUtil;
import com.scg.sf.util.PreventDoubleClickOnLists;
import com.scg.sf.util.Toasts;

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
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class VisitsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private ProgressDialog pd;
    private JSONArray visits = null;
    private AdapterListVisits mAdapter;
    private String mAuthToken;
//    private SharedPreferences mPrefs;
    private TextView emptyView;

    private long lastIntentTime = 0;
    private static final long MIN_CLICK_INTERVAL = 1000; // 1000 milliseconds
    private List<Visit> items;
    private TextView mNoVisitsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visits);

        initToolbar();

//        mPrefs = getSharedPreferences("label", 0);
//        mAuthToken = mPrefs.getString("authToken", "");
        mAuthToken = EncryptedPrefsUtil.getString("authToken", "");
        mNoVisitsTextView = (TextView) findViewById(R.id.noVisitsText);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        emptyView = (TextView) findViewById(R.id.empty_view);

//        new JsonTask().execute(Constants.BASE_URL + "api/visits","GET");

        new TokenManager(this,  (success, result) -> {
            if (success) {
                // Handle the JSON result, update UI, etc.
                System.out.println("API Result: " + result);

                handleResult(result);

            } else {
                // Handle errors (e.g., token refresh failed)
                Toasts.toastIconError(VisitsActivity.this, result);
                Intent intent = new Intent(VisitsActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }).executeRequest(Constants.BASE_URL +"api/visits", "GET");

    }

    private void handleResult(String result){

        if(pd != null && pd.isShowing() && !isFinishing()){
            pd.dismiss();
        }

        try {

            System.out.println("Result: " + result);
            if (result != null && result.contains("visits")){
                JSONObject jsonObject = new JSONObject(result);
                //txtJson.setText(jsonObject.toString());
                if (jsonObject.has("visits") &&
                        jsonObject.get("visits") instanceof JSONArray) {
                    items = new ArrayList<>();

                    visits = jsonObject.getJSONArray("visits");
                    System.out.println("Visits length: "+visits.length());

                    for (int i = 0; i < visits.length(); i++) {
                        Visit obj = new Visit();
                        //obj.image = drw_arr.getResourceId(i, -1);
                        if(!visits.getJSONObject(i).isNull("outlet")){
                            obj.outletName = visits.getJSONObject(i).getJSONObject("outlet").getString("name");
                            obj.outletLat = visits.getJSONObject(i).getJSONObject("outlet").getString("lat");
                            obj.outletLong = visits.getJSONObject(i).getJSONObject("outlet").getString("long");
                        }else{
                            obj.outletName="Outlet Not Found";
                            obj.outletLat="-1.3";
                            obj.outletLong="36.8";
                        }

                        String createdAt = visits.getJSONObject(i).getString("created_at");
                        ZonedDateTime dateTime = Instant.parse(createdAt).atZone(ZoneId.of("Africa/Nairobi"));
                        String formatted = dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy \nHH:mm"));
                        obj.date = formatted;

                        String checkoutTime = visits.getJSONObject(i).getString("checkout_time");
                        String checkinTime = visits.getJSONObject(i).getString("checkin_time");

                        if(!checkoutTime.equals("")) {
                            ZonedDateTime checkoutDateTime = Instant.ofEpochSecond(Long.parseLong(checkoutTime)).atZone(ZoneId.of("Africa/Nairobi"));
                            String formattedCheckout = checkoutDateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm"));
                            obj.checkoutTime = formattedCheckout;
                        }else {
                            obj.checkoutTime = "Not Checked Out";
                        }

                        Integer timeSpent=0;
                        if(!checkoutTime.equals("")){
                            timeSpent=Integer.parseInt( checkoutTime)-Integer.parseInt(checkinTime);
                        }
                        LocalTime time = LocalTime.MIN.plusSeconds(timeSpent);
                        String formatted2 = time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                        obj.duration= formatted2;
                        obj.id = visits.getJSONObject(i).getString("id");
                        //obj.imageDrw = drw_arr.getDrawable(obj.image);
                        items.add(obj);
                    }

                    if (items.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    }
                    else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);
                    }


                    //set data and list adapter
                    mAdapter = new AdapterListVisits(items);
                    recyclerView.setAdapter(mAdapter);

                    // on item list clicked
                    mAdapter.setOnItemClickListener(new AdapterListVisits.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, Visit obj, int position) {

                            Visit visit = items.get(position);

                            PreventDoubleClickOnLists.
                                    showActivityWithoutDoubleClick(
                                            "visit",VisitsActivity.this,
                                            visit, IndividualVisitActivity.class);



                        }
                    });

                }else {
                    FirebaseCrashlytics.getInstance().recordException(new Exception("Visits JSONOBJECT Error: "+result));

                    Intent intent = new Intent(VisitsActivity.this, ErrorLoadingActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("callingActivity", "Visits");

                    //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
                    startActivity(intent);
                    finish();

                    //Error
                    //Toast.makeText(ProductsListActivity.this,result,Toast.LENGTH_SHORT).show();

                }

            }else{
                FirebaseCrashlytics.getInstance().recordException(new Exception("Visits Null Contains Error: "+result));

                Intent intent = new Intent(VisitsActivity.this, ErrorLoadingActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("callingActivity", "Visits");

                //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
                startActivity(intent);
                finish();
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Visits");
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

            //final ArrayList<Integer> counterGroup = new ArrayList<Integer>();
            List<Visit> newVisitsList =new ArrayList<>();

            for (int counter = 0; counter < items.size(); counter++) {
                if(query.contains(" ")){
                    String[] querySplit = query.toLowerCase().split("\\s+");
                    String product_name_searched=items.get(counter).outletName.toLowerCase();


                    if(containsAllWords(product_name_searched,querySplit)){
                        //counterGroup.add(counter);
                        newVisitsList.add(items.get(counter));
                    }

                }else {
                    if (items.get(counter).outletName.toLowerCase().contains(query.toLowerCase())) {
                        //mNoProductsTextView.setVisibility(View.GONE);
                        //counterGroup.add(counter);
                        newVisitsList.add(items.get(counter));

                    }

                }

            }

            if (newVisitsList.size()>0) {
                mNoVisitsTextView.setVisibility(View.GONE);
            }else
            {
                mNoVisitsTextView.setVisibility(View.VISIBLE);
            }

            if(recyclerView==null) {
                recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            }
            mAdapter = new AdapterListVisits(newVisitsList);
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
            mAdapter.setOnItemClickListener(new AdapterListVisits.OnItemClickListener() {
                @Override
                public void onItemClick(View view, Visit obj, int position) {

                    Visit visit = newVisitsList.get(position);

                    PreventDoubleClickOnLists.
                            showActivityWithoutDoubleClick(
                                    "visit",VisitsActivity.this,
                                    visit, IndividualVisitActivity.class);



                }
            });
    //    }
    }
    public static boolean containsAllWords(String word, String ...keywords) {
        for (String k : keywords)
            if (!word.contains(k)) return false;
        return true;
    }


    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(VisitsActivity.this);

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
                if (result != null && result.contains("visits")){
                        JSONObject jsonObject = new JSONObject(result);
                        //txtJson.setText(jsonObject.toString());
                    if (jsonObject.has("visits") &&
                            jsonObject.get("visits") instanceof JSONArray) {
                        items = new ArrayList<>();

                        visits = jsonObject.getJSONArray("visits");
                        System.out.println("Visits length: "+visits.length());

                        for (int i = 0; i < visits.length(); i++) {
                            Visit obj = new Visit();
                            //obj.image = drw_arr.getResourceId(i, -1);
                            if(!visits.getJSONObject(i).isNull("outlet")){
                                obj.outletName = visits.getJSONObject(i).getJSONObject("outlet").getString("name");
                                obj.outletLat = visits.getJSONObject(i).getJSONObject("outlet").getString("lat");
                                obj.outletLong = visits.getJSONObject(i).getJSONObject("outlet").getString("long");
                            }else{
                                obj.outletName="Outlet Not Found";
                                obj.outletLat="-1.3";
                                obj.outletLong="36.8";
                            }

                            String createdAt = visits.getJSONObject(i).getString("created_at");
                            ZonedDateTime dateTime = Instant.parse(createdAt).atZone(ZoneId.of("Africa/Nairobi"));
                            String formatted = dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy \nHH:mm"));
                            obj.date = formatted;

                            String checkoutTime = visits.getJSONObject(i).getString("checkout_time");
                            String checkinTime = visits.getJSONObject(i).getString("checkin_time");

                            if(!checkoutTime.equals("")) {
                                ZonedDateTime checkoutDateTime = Instant.ofEpochSecond(Long.parseLong(checkoutTime)).atZone(ZoneId.of("Africa/Nairobi"));
                                String formattedCheckout = checkoutDateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm"));
                                obj.checkoutTime = formattedCheckout;
                            }else {
                                obj.checkoutTime = "Not Checked Out";
                            }

                            Integer timeSpent=0;
                            if(!checkoutTime.equals("")){
                                timeSpent=Integer.parseInt( checkoutTime)-Integer.parseInt(checkinTime);
                            }
                            LocalTime time = LocalTime.MIN.plusSeconds(timeSpent);
                            String formatted2 = time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                            obj.duration= formatted2;
                            obj.id = visits.getJSONObject(i).getString("id");
                            //obj.imageDrw = drw_arr.getDrawable(obj.image);
                            items.add(obj);
                        }

                        if (items.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        }
                        else {
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyView.setVisibility(View.GONE);
                        }


                        //set data and list adapter
                        mAdapter = new AdapterListVisits(items);
                        recyclerView.setAdapter(mAdapter);

                        // on item list clicked
                        mAdapter.setOnItemClickListener(new AdapterListVisits.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, Visit obj, int position) {

                                Visit visit = items.get(position);

                                PreventDoubleClickOnLists.
                                        showActivityWithoutDoubleClick(
                                                "visit",VisitsActivity.this,
                                                visit, IndividualVisitActivity.class);



                            }
                        });

                    }else {
                        FirebaseCrashlytics.getInstance().recordException(new Exception("Visits JSONOBJECT Error: "+result));

                        Intent intent = new Intent(VisitsActivity.this, ErrorLoadingActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("callingActivity", "Visits");

                        //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
                        startActivity(intent);
                        finish();

                        //Error
                        //Toast.makeText(ProductsListActivity.this,result,Toast.LENGTH_SHORT).show();

                    }

                }else{
                    FirebaseCrashlytics.getInstance().recordException(new Exception("Visits Null Contains Error: "+result));

                    Intent intent = new Intent(VisitsActivity.this, ErrorLoadingActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("callingActivity", "Visits");

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
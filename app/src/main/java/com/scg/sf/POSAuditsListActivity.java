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
import com.scg.sf.adapters.AdapterListPOSAudits;

import com.scg.sf.models.POSAudit;
import com.scg.sf.models.POSAuditItem;
import com.scg.sf.util.PreventDoubleClickOnLists;


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

public class POSAuditsListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;

    private ProgressDialog pd;
    private JSONArray posAudits = null;
    private AdapterListPOSAudits mAdapter;
    private String mAuthToken;
    private SharedPreferences mPrefs;
    private ArrayList<POSAuditItem> posAuditItemsList;
    private View parent_view;
    private TextView emptyView;

    private long lastIntentTime = 0;
    private static final long MIN_CLICK_INTERVAL = 1000; // 1000 milliseconds

    private List<POSAudit> items;
    private TextView mNoPOSAuditsTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posaudits_list);

        initToolbar();

        mPrefs = getSharedPreferences("label", 0);
        mAuthToken = mPrefs.getString("authToken", "");
        mNoPOSAuditsTextView= (TextView)findViewById(R.id.noPOSAuditsText);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        emptyView = (TextView) findViewById(R.id.empty_view);

        new JsonTask().execute(Constants.BASE_URL + "api/posaudits","GET");
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("POS Audits");
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
            List<POSAudit> newPOSAuditsList =new ArrayList<>();

            for (int counter = 0; counter < items.size(); counter++) {
                if(query.contains(" ")){
                    String[] querySplit = query.toLowerCase().split("\\s+");
                    String product_name_searched=items.get(counter).name.toLowerCase();


                    if(containsAllWords(product_name_searched,querySplit)){
                        //counterGroup.add(counter);
                        newPOSAuditsList.add(items.get(counter));
                    }

                }else {
                    if (items.get(counter).name.toLowerCase().contains(query.toLowerCase())) {
                        //mNoProductsTextView.setVisibility(View.GONE);
                        //counterGroup.add(counter);
                        newPOSAuditsList.add(items.get(counter));

                    }

                }

            }

            if (newPOSAuditsList.size()>0) {
                mNoPOSAuditsTextView.setVisibility(View.GONE);
            }else
            {
                mNoPOSAuditsTextView.setVisibility(View.VISIBLE);
            }

            if(recyclerView==null) {
                recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            }
            mAdapter = new AdapterListPOSAudits(newPOSAuditsList);
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
            mAdapter.setOnItemClickListener(new AdapterListPOSAudits.OnItemClickListener() {
                @Override
                public void onItemClick(View view, POSAudit obj, int position) {

                    POSAudit posAudit = newPOSAuditsList.get(position);

                    PreventDoubleClickOnLists.
                            showActivityWithoutDoubleClick(
                                    "posAudit",POSAuditsListActivity.this,
                                    posAudit, IndividualPOSAuditActivity.class);



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

            pd = new ProgressDialog(POSAuditsListActivity.this);

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
                if (result != null && result.contains("posAudits")) {
                    JSONObject jsonObject = new JSONObject(result);
                    //txtJson.setText(jsonObject.toString());
                    if (jsonObject.has("posAudits") &&
                            jsonObject.get("posAudits") instanceof JSONArray) {
                        items = new ArrayList<>();

                    posAudits = jsonObject.getJSONArray("posAudits");
                    System.out.println("POSAudits length: " + posAudits.length());


                    for (int i = 0; i < posAudits.length(); i++) {
                        POSAudit obj = new POSAudit();
                        //obj.image = drw_arr.getResourceId(i, -1);
                        obj.name="";
                        obj.price="";
                        if(!posAudits.getJSONObject(i).isNull("outlet")){
                            obj.name = posAudits.getJSONObject(i).
                                    getJSONObject("outlet").getString("name");
                            obj.price = posAudits.getJSONObject(i).
                                    getJSONObject("outlet").getString("contact_person_name");
                        }else{
                            obj.name="Outlet Not Found";
                            obj.price = "Not Found";
                        }

                        //obj.name = posAudits.getJSONObject(i).getJSONObject("outlet").getString("name");
                        String createdAt = posAudits.getJSONObject(i).getString("created_at");
                        ZonedDateTime dateTime = Instant.parse(createdAt).atZone(ZoneId.of("Africa/Nairobi"));
                        String formatted = dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy \nHH:mm "));
                        obj.time = formatted;
                        obj.image = posAudits.getJSONObject(i).getString("image");

                        JSONArray posAuditItems = posAudits.getJSONObject(i).getJSONArray("posaudititems");
                        String posAuditItemString = "";

                        posAuditItemsList = new ArrayList<>();

                        for (int j = 0; j < posAuditItems.length(); j++) {

                            String posAuditItemProductName="";
                            POSAuditItem posAuditItem = new POSAuditItem();

                            if(!posAuditItems.getJSONObject(j).isNull("product")){
                                posAuditItemProductName = posAuditItems.getJSONObject(j).
                                        getJSONObject("product").getString("name");
                                posAuditItem.productName = posAuditItems.getJSONObject(j).
                                        getJSONObject("product").getString("name");

                            }else{
                                posAuditItemProductName = "Product Not Found";
                                posAuditItem.productName = "Product Not Found";
                            }

                            String posAuditItemPieces = posAuditItems.
                                    getJSONObject(j).getString("pieces");

                            if (j == 2) {
                                posAuditItemString = posAuditItemString + "...";
                            } else if (j <= 1) {
                                posAuditItemString = posAuditItemString + posAuditItemProductName + " - " + posAuditItemPieces + "\n";
                            }

                            posAuditItem.pieces = posAuditItems.getJSONObject(j)
                                    .getString("pieces");
                            posAuditItemsList.add(posAuditItem);

                            System.out.println("POSAuditItem: " + posAuditItemString);

                        }

                        System.out.println("POSAuditItems: " + posAuditItems.length());
                        obj.price = posAuditItemString;
                        obj.posAuditItems = posAuditItemsList;

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
                    mAdapter = new AdapterListPOSAudits(items);
                    recyclerView.setAdapter(mAdapter);

                    // on item list clicked
                    mAdapter.setOnItemClickListener(new AdapterListPOSAudits.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, POSAudit obj, int position) {

                            POSAudit posAudit = items.get(position);

                            PreventDoubleClickOnLists.
                                    showActivityWithoutDoubleClick(
                                            "posAudit",POSAuditsListActivity.this,
                                            posAudit, IndividualPOSAuditActivity.class);


                        }
                    });


//                    outletsNumber.setText(outletsTotal);
//                    posAuditsNumber.setText(posAuditsTotal);
//                    salesNumber.setText(salesTotal);
//                    feedbackNumber.setText(feedbackTotal);
//                    targetSales.setText("KSh " + NumberFormat.getNumberInstance(Locale.US).format(parseInt(dailySalesTotal)));
//                    currentSales.setText("KSh " + NumberFormat.getNumberInstance(Locale.US).format(parseInt(currentSalesTarget)));
                }else{
                        FirebaseCrashlytics.getInstance().recordException(new Exception("POSAuditsList Error: "+result));

                        Intent intent = new Intent(POSAuditsListActivity.this, ErrorLoadingActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("callingActivity", "POSAudits");

                        //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
                        startActivity(intent);
                        finish();
                    }

                }else{
                    FirebaseCrashlytics.getInstance().recordException(new Exception("POSAuditsList Error: "+result));

                    Intent intent = new Intent(POSAuditsListActivity.this, ErrorLoadingActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("callingActivity", "POSAudits");

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
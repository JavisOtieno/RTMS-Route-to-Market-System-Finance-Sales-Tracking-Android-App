package com.scg.rtmssf;

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
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.scg.rtmssf.adapters.AdapterListFollowup;
import com.scg.rtmssf.models.Followup;
import com.scg.rtmssf.models.Followup;
import com.scg.rtmssf.util.EncryptedPrefsUtil;
import com.scg.rtmssf.util.PreventDoubleClickOnLists;
import com.scg.rtmssf.util.Toasts;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FollowupsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;

    private ProgressDialog pd;
    private JSONArray followup = null;
    private AdapterListFollowup mAdapter;
    private String mAuthToken;
    //    private SharedPreferences mPrefs;
    private TextView emptyView;
    private long lastIntentTime = 0;
    private static final long MIN_CLICK_INTERVAL = 1000; // 1000 milliseconds

    private List<Followup> items;
    private TextView mNoFollowupTextView;
    private SharedPreferences mPrefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followup);
        initToolbar();

        mPrefs = getSharedPreferences("label", 0);

//        mPrefs = getSharedPreferences("label", 0);
//        mAuthToken = mPrefs.getString("authToken", "");
        mAuthToken = EncryptedPrefsUtil.getString("authToken", "");
        mNoFollowupTextView = (TextView) findViewById(R.id.noFollowupText);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        emptyView = (TextView) findViewById(R.id.empty_view);

        new TokenManager(FollowupsActivity.this,  (success, result) -> {
            if (success) {
                // Handle the JSON result, update UI, etc.
                System.out.println("API Result: " + result);

                handleResult(result);

            } else {
                // Handle errors (e.g., token refresh failed)
                Toasts.toastIconError(FollowupsActivity.this, result);
//                Intent intent = new Intent(FollowupListActivity.this, LoginActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
            }
        }).executeRequest(Constants.BASE_URL +"api/followup", "GET");



//        new JsonTask().execute(Constants.BASE_URL + "api/followup","GET");

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Follow ups");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Tools.setSystemBarColor(this);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
                onBackPressed();
                return true;
        }
        else if (item.getItemId() == R.id.action_add) {
            Intent intent = new Intent(FollowupsActivity.this, CheckinActivity.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            //finish();

            SharedPreferences.Editor mEditor = mPrefs.edit();
            mEditor.putString("callingActivityFollowup", "FollowupActivity").commit();

        }

        return super.onOptionsItemSelected(item);
    }

    private void handleResult(String result){
        try {

            System.out.println("Result: " + result);
            if (result != null && result.contains("followup")){
                JSONObject jsonObject = new JSONObject(result);
                //txtJson.setText(jsonObject.toString());
                items = new ArrayList<>();

                if (jsonObject.has("followup") &&
                        jsonObject.get("followup") instanceof JSONArray) {


                    followup = jsonObject.getJSONArray("followup");
                    System.out.println("Outlets length: "+followup.length());

                    for (int i = 0; i < followup.length(); i++) {
                        Followup obj = new Followup();
                        //obj.image = drw_arr.getResourceId(i, -1);
                        obj.description = followup.getJSONObject(i).getString("description");
                        String createdAt = followup.getJSONObject(i).getString("created_at");
                        ZonedDateTime dateTime = Instant.parse(createdAt).atZone(ZoneId.of("Africa/Nairobi"));
                        String formatted = dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy \nHH:mm "));
                        obj.created_at = formatted;
                        obj.created_outlet = "";
                        obj.status = followup.getJSONObject(i).getString("status");
                        obj.date = followup.getJSONObject(i).getString("date");
                        obj.time = followup.getJSONObject(i).getString("time");
                        obj.type = followup.getJSONObject(i).getString("type");
                        if(!followup.getJSONObject(i).isNull("outlet")){
                            obj.created_outlet = followup.getJSONObject(i).
                                    getJSONObject("outlet").getString("name");
                        }else{
                            obj.created_outlet="Outlet Not Found";
                        }

                        //    obj.created_outlet = followup.getJSONObject(i).getJSONObject("outlet").getString("name");

                        obj.id = followup.getJSONObject(i).getString("id");
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
                    mAdapter = new AdapterListFollowup(items);
                    recyclerView.setAdapter(mAdapter);

                    // on item list clicked
                    mAdapter.setOnItemClickListener(new AdapterListFollowup.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, Followup obj, int position) {

                            Followup followup = items.get(position);


                            PreventDoubleClickOnLists.
                                    showActivityWithoutDoubleClick(
                                            "followup",FollowupsActivity.this,
                                            followup, IndividualFollowupActivity.class);


                        }
                    });

                }else {
                    FirebaseCrashlytics.getInstance().recordException(new Exception("FollowupList Error: "+result));
                    Intent intent = new Intent(FollowupsActivity.this, ErrorLoadingActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("callingActivity", "Followup");

                    //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
                    startActivity(intent);
                    finish();
                }

            }else{
                FirebaseCrashlytics.getInstance().recordException(new Exception("FollowupList Error: "+result));
                Intent intent = new Intent(FollowupsActivity.this, ErrorLoadingActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("callingActivity", "Followup");

                //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
                startActivity(intent);
                finish();
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

//    private class JsonTask extends AsyncTask<String, String, String> {
//
//        protected void onPreExecute() {
//            super.onPreExecute();
//
//            pd = new ProgressDialog(FollowupListActivity.this);
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
//
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
//
//            try {
//
//                System.out.println("Result: " + result);
//                if (result != null && result.contains("followup")){
//                        JSONObject jsonObject = new JSONObject(result);
//                        //txtJson.setText(jsonObject.toString());
//                        items = new ArrayList<>();
//
//                        if (jsonObject.has("followup") &&
//                                jsonObject.get("followup") instanceof JSONArray) {
//
//
//                        followup = jsonObject.getJSONArray("followup");
//                        System.out.println("Outlets length: "+followup.length());
//
//                        for (int i = 0; i < followup.length(); i++) {
//                            Followup obj = new Followup();
//                            //obj.image = drw_arr.getResourceId(i, -1);
//                            obj.followup = followup.getJSONObject(i).getString("followup");
//                            String createdAt = followup.getJSONObject(i).getString("created_at");
//                            ZonedDateTime dateTime = Instant.parse(createdAt).atZone(ZoneId.of("Africa/Nairobi"));
//                            String formatted = dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy \nHH:mm "));
//                            obj.created_at = formatted;
//
//                        //    obj.created_outlet = followup.getJSONObject(i).getJSONObject("outlet").getString("name");
//                            obj.created_outlet = "";
//                            if(!followup.getJSONObject(i).isNull("outlet")){
//                                obj.created_outlet = followup.getJSONObject(i).
//                                        getJSONObject("outlet").getString("name");
//                            }else{
//                                obj.created_outlet="Outlet Not Found";
//                            }
//                            obj.id = followup.getJSONObject(i).getString("id");
//                            //obj.imageDrw = drw_arr.getDrawable(obj.image);
//                            items.add(obj);
//                        }
//
//                        if (items.isEmpty()) {
//                            recyclerView.setVisibility(View.GONE);
//                            emptyView.setVisibility(View.VISIBLE);
//                        }
//                        else {
//                            recyclerView.setVisibility(View.VISIBLE);
//                            emptyView.setVisibility(View.GONE);
//                        }
//
//
//                        //set data and list adapter
//                        mAdapter = new AdapterListFollowup(items);
//                        recyclerView.setAdapter(mAdapter);
//
//                        // on item list clicked
//                        mAdapter.setOnItemClickListener(new AdapterListFollowup.OnItemClickListener() {
//                            @Override
//                            public void onItemClick(View view, Followup obj, int position) {
//
//                                Followup followup = items.get(position);
//
//
//                                PreventDoubleClickOnLists.
//                                        showActivityWithoutDoubleClick(
//                                                "followup",FollowupListActivity.this,
//                                                followup, IndividualFollowupActivity.class);
//
//
//                            }
//                        });
//
//                    }else {
//                        FirebaseCrashlytics.getInstance().recordException(new Exception("FollowupList Error: "+result));
//                        Intent intent = new Intent(FollowupListActivity.this, ErrorLoadingActivity.class);
//                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                        intent.putExtra("callingActivity", "Followup");
//
//                        //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
//                        startActivity(intent);
//                        finish();
//                    }
//
//                }else{
//                    FirebaseCrashlytics.getInstance().recordException(new Exception("FollowupList Error: "+result));
//                    Intent intent = new Intent(FollowupListActivity.this, ErrorLoadingActivity.class);
//                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                    intent.putExtra("callingActivity", "Followup");
//
//                    //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
//                    startActivity(intent);
//                    finish();
//                }
//
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            }
//
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_search_add, menu);
        getMenuInflater().inflate(R.menu.menu_search_add, menu);

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
        List<Followup> newFollowupList=new ArrayList<>();

        for (int counter = 0; counter < items.size(); counter++) {
            if(query.contains(" ")){
                String[] querySplit = query.toLowerCase().split("\\s+");
                String product_name_searched=items.get(counter).created_outlet.toLowerCase();


                if(containsAllWords(product_name_searched,querySplit)){
                    //counterGroup.add(counter);
                    newFollowupList.add(items.get(counter));
                }

            }else {
                if (items.get(counter).created_outlet.toLowerCase().contains(query.toLowerCase())) {
                    //mNoProductsTextView.setVisibility(View.GONE);
                    //counterGroup.add(counter);
                    newFollowupList.add(items.get(counter));

                }

            }

        }

        if (newFollowupList.size()>0) {
            mNoFollowupTextView.setVisibility(View.GONE);
        }else
        {
            mNoFollowupTextView.setVisibility(View.VISIBLE);
        }

        if(recyclerView==null) {
            recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        }
        mAdapter = new AdapterListFollowup(newFollowupList);
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
        mAdapter.setOnItemClickListener(new AdapterListFollowup.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Followup obj, int position) {

                Followup followup = newFollowupList.get(position);


                PreventDoubleClickOnLists.
                        showActivityWithoutDoubleClick(
                                "followup",FollowupsActivity.this,
                                followup, IndividualFollowupActivity.class);



            }
        });
        //    }
    }
    public static boolean containsAllWords(String word, String ...keywords) {
        for (String k : keywords)
            if (!word.contains(k)) return false;
        return true;
    }


}
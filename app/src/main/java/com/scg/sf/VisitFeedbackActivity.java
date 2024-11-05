package com.scg.sf;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.scg.sf.adapters.AdapterListFeedback;
import com.scg.sf.models.Feedback;
import com.scg.sf.util.EncryptedPrefsUtil;
import com.scg.sf.util.PreventDoubleClickOnLists;
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
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class VisitFeedbackActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private ProgressDialog pd;
    private JSONArray feedback = null;
    private AdapterListFeedback mAdapter;
    private String mAuthToken;
//    private SharedPreferences mPrefs;
    private TextView emptyView;

    private String visitId;
    private String outletName;
    private String outletId;
    private long lastIntentTime = 0;
    private static final long MIN_CLICK_INTERVAL = 1000; // 1000 milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_list);

        initToolbar();

//        mPrefs = getSharedPreferences("label", 0);
//        mAuthToken = mPrefs.getString("authToken", "");
        mAuthToken = EncryptedPrefsUtil.getString("authToken", "");

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        emptyView = (TextView) findViewById(R.id.empty_view);

        Intent i = getIntent();
        visitId=i.getStringExtra("visitId");
        outletId=i.getStringExtra("outletId");
        //callingActivity=i.getStringExtra("callingActivity");
        outletName=i.getStringExtra("outletName");

//        new JsonTask().execute( Constants.BASE_URL + "api/visitfeedback","POST",visitId);

        new TokenManager(VisitFeedbackActivity.this,  (success, result) -> {
            if (success) {
                // Handle the JSON result, update UI, etc.
                System.out.println("API Result: " + result);

                handleResult(result);

            } else {
                // Handle errors (e.g., token refresh failed)
                Toasts.toastIconError(VisitFeedbackActivity.this, result);
                Intent intent = new Intent(VisitFeedbackActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }).executeRequest(Constants.BASE_URL +"api/visitfeedback", "POST",
                "visitId",visitId);

    }

    private void handleResult(String result){

        if(pd != null && pd.isShowing() && !isFinishing()){
            pd.dismiss();
        }

        try {
            System.out.println("Result: " + result);
            if (result != null && result.contains("feedback")){
                JSONObject jsonObject = new JSONObject(result);
                //txtJson.setText(jsonObject.toString());
                List<Feedback> items = new ArrayList<>();

                if (jsonObject.has("feedback") &&
                        jsonObject.get("feedback") instanceof JSONArray) {

                    feedback = jsonObject.getJSONArray("feedback");
                    System.out.println("Outlets length: "+feedback.length());

                    for (int i = 0; i < feedback.length(); i++) {
                        Feedback obj = new Feedback();
                        //obj.image = drw_arr.getResourceId(i, -1);
                        obj.feedback = feedback.getJSONObject(i).getString("feedback");
                        String createdAt = feedback.getJSONObject(i).getString("created_at");
                        ZonedDateTime dateTime = Instant.parse(createdAt).atZone(ZoneId.of("Africa/Nairobi"));
                        String formatted = dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy \nHH:mm "));
                        obj.created_at = formatted;
                        if(!feedback.getJSONObject(i).isNull("outlet"))
                        {
                            obj.created_outlet = feedback.getJSONObject(i).getJSONObject("outlet").getString("name");
                        }
                        else{
                            obj.created_outlet="Outlet Not Found";
                        }
                        //obj.created_outlet = feedback.getJSONObject(i).getJSONObject("outlet").getString("name");
                        obj.id = feedback.getJSONObject(i).getString("id");
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
                    mAdapter = new AdapterListFeedback(items);
                    recyclerView.setAdapter(mAdapter);

                    // on item list clicked
                    mAdapter.setOnItemClickListener(new AdapterListFeedback.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, Feedback obj, int position) {

                            //Snackbar.make(parent_view, "Item " + obj.name + " clicked", Snackbar.LENGTH_SHORT).show();
//                                Intent intent = new Intent(VisitFeedbackActivity.this, IndividualFeedbackActivity.class);
                            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            //intent.putExtra("visitId", visitId);
                            //intent.putExtra("outletId", outletId);
//                                intent.putExtra("feedback", items.get(position));
//                                startActivity(intent);

                            Feedback feedback = items.get(position);

                            PreventDoubleClickOnLists.
                                    showActivityWithoutDoubleClick(
                                            "feedback",VisitFeedbackActivity.this,
                                            feedback, IndividualFeedbackActivity.class);


                        }
                    });

                }else {
                    FirebaseCrashlytics.getInstance().recordException(new Exception("VisitFeedback Error: "+result));

                    Intent intent = new Intent(VisitFeedbackActivity.this, ErrorLoadingActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("callingActivity", "VisitFeedback");

                    //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
                    startActivity(intent);
                    finish();
                }

            }else {
                FirebaseCrashlytics.getInstance().recordException(new Exception("VisitFeedback Error: "+result));

                Intent intent = new Intent(VisitFeedbackActivity.this, ErrorLoadingActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("callingActivity", "VisitFeedback");

                //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
                startActivity(intent);
                finish();
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }



    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(VisitFeedbackActivity.this);

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

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("visitId", params[2]);

                String query = builder.build().getEncodedQuery();

                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                System.out.println("Params " + params[2]);

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
                System.out.println("Result: " + result);
                if (result != null && result.contains("feedback")){
                        JSONObject jsonObject = new JSONObject(result);
                        //txtJson.setText(jsonObject.toString());
                        List<Feedback> items = new ArrayList<>();

                    if (jsonObject.has("feedback") &&
                            jsonObject.get("feedback") instanceof JSONArray) {

                        feedback = jsonObject.getJSONArray("feedback");
                        System.out.println("Outlets length: "+feedback.length());

                        for (int i = 0; i < feedback.length(); i++) {
                            Feedback obj = new Feedback();
                            //obj.image = drw_arr.getResourceId(i, -1);
                            obj.feedback = feedback.getJSONObject(i).getString("feedback");
                            String createdAt = feedback.getJSONObject(i).getString("created_at");
                            ZonedDateTime dateTime = Instant.parse(createdAt).atZone(ZoneId.of("Africa/Nairobi"));
                            String formatted = dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy \nHH:mm "));
                            obj.created_at = formatted;
                            if(!feedback.getJSONObject(i).isNull("outlet"))
                            {
                                obj.created_outlet = feedback.getJSONObject(i).getJSONObject("outlet").getString("name");
                            }
                            else{
                                obj.created_outlet="Outlet Not Found";
                            }
                            //obj.created_outlet = feedback.getJSONObject(i).getJSONObject("outlet").getString("name");
                            obj.id = feedback.getJSONObject(i).getString("id");
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
                        mAdapter = new AdapterListFeedback(items);
                        recyclerView.setAdapter(mAdapter);

                        // on item list clicked
                        mAdapter.setOnItemClickListener(new AdapterListFeedback.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, Feedback obj, int position) {

                                //Snackbar.make(parent_view, "Item " + obj.name + " clicked", Snackbar.LENGTH_SHORT).show();
//                                Intent intent = new Intent(VisitFeedbackActivity.this, IndividualFeedbackActivity.class);
                                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                //intent.putExtra("visitId", visitId);
                                //intent.putExtra("outletId", outletId);
//                                intent.putExtra("feedback", items.get(position));
//                                startActivity(intent);

                                Feedback feedback = items.get(position);

                                PreventDoubleClickOnLists.
                                        showActivityWithoutDoubleClick(
                                                "feedback",VisitFeedbackActivity.this,
                                                feedback, IndividualFeedbackActivity.class);


                            }
                        });

                    }else {
                        FirebaseCrashlytics.getInstance().recordException(new Exception("VisitFeedback Error: "+result));

                        Intent intent = new Intent(VisitFeedbackActivity.this, ErrorLoadingActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("callingActivity", "VisitFeedback");

                        //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
                        startActivity(intent);
                        finish();
                    }

                }else {
                    FirebaseCrashlytics.getInstance().recordException(new Exception("VisitFeedback Error: "+result));

                    Intent intent = new Intent(VisitFeedbackActivity.this, ErrorLoadingActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("callingActivity", "VisitFeedback");

                    //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
                    startActivity(intent);
                    finish();
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Visit Feedback");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Tools.setSystemBarColor(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(outletId.equals("IndividualVisitNotRequired")){
            return false;

        }else{
            getMenuInflater().inflate(R.menu.menu_add, menu);
            return super.onCreateOptionsMenu(menu);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        else if (item.getItemId() == R.id.action_add) {
            Intent intent = new Intent(VisitFeedbackActivity.this, AddFeedbackActivity.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("visitId", visitId);
            intent.putExtra("outletId", outletId);
            intent.putExtra("outletName", outletName);


            startActivity(intent);

            //finish();
        }else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }


    public static class ViewLoadingDotsBounce {
    }
}

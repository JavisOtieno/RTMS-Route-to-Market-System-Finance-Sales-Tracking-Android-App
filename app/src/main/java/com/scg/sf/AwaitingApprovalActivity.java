package com.scg.sf;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.scg.sf.util.EncryptedPrefsUtil;
import com.scg.sf.util.Toasts;
import com.scg.sf.util.ViewLoadingDotsBounce;

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

public class AwaitingApprovalActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ActionBar actionBar;
    private ProgressDialog pd;
    private String mAuthToken;
    private SharedPreferences mPrefs;
    private String mUserId;
    private String routePlanId;
    private boolean isTaskRunning = false;
    private Runnable runnable;
    private Handler handler;
    private Button existingOutletButton;
    private Button newOutletButton;
    private ViewLoadingDotsBounce viewLoadingDotsBounce;
    private Button backToCheckinButton;
    private CardView offplanCard;
    private TextView offplanStatusTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_awaiting_approval);

        initToolbar();

//        mPrefs = getSharedPreferences("label", 0);
//        mAuthToken = mPrefs.getString("authToken", "");
//        mUserId = mPrefs.getString("userId", "0");
        mUserId = EncryptedPrefsUtil.getString("userId","0");
        mAuthToken = EncryptedPrefsUtil.getString("authToken", "");
        Intent i = getIntent();
        routePlanId = i.getStringExtra("routePlanId");

        existingOutletButton = (Button) findViewById(R.id.existingOutletButton);
        newOutletButton = (Button) findViewById(R.id.newOutletButton);
        viewLoadingDotsBounce = (ViewLoadingDotsBounce) findViewById(R.id.loadingDots);
        backToCheckinButton = (Button) findViewById(R.id.backToCheckinButton);
        offplanCard = (CardView) findViewById(R.id.offplanCard);
        offplanStatusTextView = (TextView) findViewById(R.id.offplanStatusTextView);


//        Toasts.toastIconError(AwaitingApprovalActivity.this,
//                routePlanId);
        existingOutletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AwaitingApprovalActivity.this, SelectExistingCustomerActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        backToCheckinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AwaitingApprovalActivity.this, CheckinActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });


        newOutletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AwaitingApprovalActivity.this, NewCustomerActivity.class);
                //Intent intent = new Intent(CheckinActivity.this, TakeOutletPhotoActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });


        new JsonTask().execute(Constants.BASE_URL +
                "api/addoffplanrequest", "POST", mUserId,routePlanId);




    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(AwaitingApprovalActivity.this);

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

                if (requestMethod.equals("POST")) {
                    Uri.Builder builder = new Uri.Builder()

                            .appendQueryParameter("userId", params[2])
                            .appendQueryParameter("routeplanId", params[3]);

                    String query = builder.build().getEncodedQuery();

                    OutputStream os = connection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
                    writer.write(query);
                    writer.flush();
                    writer.close();
                    os.close();

                    System.out.println("Params " + params[2] + " " + params[3] );
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
            isTaskRunning = false;

            System.out.println("Result: " + result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                //Submit Failed means server returns nothing
                //Error saving means server responds with something other than success
                if (result != null) {
                    if(result.contains("errors")){
                        String message = jsonObject.getString("message");
                        Toasts.toastIconError(AwaitingApprovalActivity.this,message);
                        //Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                    }
                    else  if (jsonObject.has("status") &&
                            jsonObject.get("status") instanceof String &&
                            jsonObject.get("status").equals("success")
                    ) {

//                        Intent intent = new Intent(AwaitingApprovalActivity.this, SelectActionActivity.class);
//                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                        intent.putExtra("lastUpdatedActivity","feedback");
//                        intent.putExtra("visitId",visitId);
//                        intent.putExtra("outletId",outletId);
//                        intent.putExtra("outletName",outletName);
//                        startActivity(intent);

                        // Create a periodic work request
//                        PeriodicWorkRequest periodicWorkRequest =
//                                new PeriodicWorkRequest.Builder(CheckUpdateWorker.class,
//                                        15,
//                                        TimeUnit.MINUTES)
//                                        .build();
//
//// Enqueue the work
//                        WorkManager.getInstance().enqueue(periodicWorkRequest);
                        // Run the task after 15 seconds (15000 milliseconds)
                        String message = jsonObject.getString("message");
//                        Toasts.toastIconError(AwaitingApprovalActivity.this,message);


                        String equalRequestStatus = jsonObject.getString("equalrequeststatus");
//                        Toasts.toastIconError(AwaitingApprovalActivity.this,equalRequestStatus);
                        if(equalRequestStatus.equals("approved")){
                            viewLoadingDotsBounce.setVisibility(View.GONE);

                            backToCheckinButton.setVisibility(View.GONE);
                            existingOutletButton.setVisibility(View.VISIBLE);
                            newOutletButton.setVisibility(View.VISIBLE);
                            offplanCard.setVisibility(View.VISIBLE);
                            offplanStatusTextView.setText("Approved");

//                            Toasts.toastIconSuccess(AwaitingApprovalActivity.this,
//                                    "Offplan request approved");
                        }else if(equalRequestStatus.equals("disapproved")){
                            viewLoadingDotsBounce.setVisibility(View.GONE);

                            backToCheckinButton.setVisibility(View.VISIBLE);
                            offplanCard.setVisibility(View.VISIBLE);
                            existingOutletButton.setVisibility(View.GONE);
                            newOutletButton.setVisibility(View.GONE);
                            offplanStatusTextView.setText("Rejected");

//                            Toasts.toastIconError(AwaitingApprovalActivity.this,
//                                    "Offplan request had already been rejected");
//                            Intent intent = new Intent(AwaitingApprovalActivity.this, CheckinActivity.class);
//                            startActivity(intent);
                        }else {
                            //equalRequestStatus is null or pending
                            offplanCard.setVisibility(View.VISIBLE);

                            String offplanrequestId = jsonObject.getString("offplanrequestId");
                            handler = new Handler(Looper.getMainLooper());

                            runnable = new Runnable() {
                                @Override
                                public void run() {
                                    // Code to run every 60 seconds
                                    new JsonTaskApproval().execute(Constants.BASE_URL +
                                            "api/checkifoffplanrequestapproved/" + offplanrequestId, "GET");

                                    // Schedule the task again after 60 seconds
                                    handler.postDelayed(this, 30000); // 60000 ms = 60 seconds
                                }
                            };

                            // Start the periodic task
                            handler.post(runnable);

                        }

//                        Toasts.toastIconError(AwaitingApprovalActivity.this,
//                                "Successful");

                    }else{
                        Toasts.toastIconError(AwaitingApprovalActivity.this,
                                "Error Saving. Please Try Again");
                        FirebaseCrashlytics.getInstance().recordException(new Exception("Awaiting Approval Error: "+result));

                    }

                }else
                {
                    Toasts.toastIconError(AwaitingApprovalActivity.this,
                            "Approval Failed. Please Try Again");
                    FirebaseCrashlytics.getInstance().recordException(new Exception("Awaiting Approval Error: "+result));

                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
    }



    private class JsonTaskApproval extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

//            pd = new ProgressDialog(AwaitingApprovalActivity.this);
//
//            pd.setMessage("Please wait");
//            pd.setCancelable(false);
//            pd.show();

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

//            if(pd != null && pd.isShowing() && !isFinishing()){
//                pd.dismiss();
//            }
            isTaskRunning = false;

            System.out.println("Result: " + result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                //Submit Failed means server returns nothing
                //Error saving means server responds with something other than success
                if (result != null) {
                    if(result.contains("errors")){
                        String message = jsonObject.getString("message");
                        Toasts.toastIconError(AwaitingApprovalActivity.this,message);
                        //Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                    }
                    else if(result.contains("status")){
//                        Intent intent = new Intent(AwaitingApprovalActivity.this, SelectActionActivity.class);
//                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                        intent.putExtra("lastUpdatedActivity","feedback");
//                        intent.putExtra("visitId",visitId);
//                        intent.putExtra("outletId",outletId);
//                        intent.putExtra("outletName",outletName);
//                        startActivity(intent);

                        // Create a periodic work request
//                        PeriodicWorkRequest periodicWorkRequest =
//                                new PeriodicWorkRequest.Builder(CheckUpdateWorker.class,
//                                        15,
//                                        TimeUnit.MINUTES)
//                                        .build();
//
//// Enqueue the work
//                        WorkManager.getInstance().enqueue(periodicWorkRequest);
                        // Run the task after 15 seconds (15000 milliseconds)
                        String status = jsonObject.getString("status");
                        String statusCapitalized = status.substring(0, 1).toUpperCase() + status.substring(1);
                        if(status.equals("pending")){


//                            Toasts.toastIconError(AwaitingApprovalActivity.this, status);
                        }else if(status.equals("approved")){

                            viewLoadingDotsBounce.setVisibility(View.GONE);
                            existingOutletButton.setVisibility(View.VISIBLE);
                            newOutletButton.setVisibility(View.VISIBLE);
                            offplanCard.setVisibility(View.VISIBLE);
                            backToCheckinButton.setVisibility(View.GONE);
                            offplanStatusTextView.setText("Approved");

//                            Toasts.toastIconSuccess(AwaitingApprovalActivity.this,statusCapitalized);
//                            Intent intent = new Intent(AwaitingApprovalActivity.this, CheckinActivity.class);
//                            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
////                            intent.putExtra("callingActivity", "Feedback");
//                            startActivity(intent);
                        }else if(status.equals("disapproved")){

                            viewLoadingDotsBounce.setVisibility(View.GONE);
                            offplanCard.setVisibility(View.VISIBLE);
                            existingOutletButton.setVisibility(View.GONE);
                            newOutletButton.setVisibility(View.GONE);
                            backToCheckinButton.setVisibility(View.VISIBLE);
                            offplanStatusTextView.setText("Rejected");

//                            Toasts.toastIconError(AwaitingApprovalActivity.this,
//                                    "Offplan request has been rejected!");
//                            Intent intent = new Intent(AwaitingApprovalActivity.this, CheckinActivity.class);
//                            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
////                            intent.putExtra("callingActivity", "Feedback");
//                            startActivity(intent);

                        }

                    }else{
                        Toasts.toastIconError(AwaitingApprovalActivity.this,
                                "Error Saving. Please Try Again");
                        FirebaseCrashlytics.getInstance().recordException(new Exception("AddFeedback Error: "+result));

                    }

                }else
                {
                    Toasts.toastIconError(AwaitingApprovalActivity.this,
                            "Submit Feedback Failed. Please Try Again");
                    FirebaseCrashlytics.getInstance().recordException(new Exception("AddFeedback Error: "+result));

                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
    }



    private void initToolbar() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Awaiting Approval");
        //Tools.setSystemBarColor(this);;

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

    protected void onDestroy() {
        super.onDestroy();

        if (handler != null) {
            handler.removeCallbacks(runnable); // Stop the task when the activity is destroyed
        }
    }



}
package com.scg.rtmssf;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.scg.rtmssf.util.EncryptedPrefsUtil;
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

public class AddCompetitiveAnalysisActivity extends AppCompatActivity {

    private EditText brandVisibilityEditText;
    private EditText pricingEditText;
    private Button submitCompetitiveAnalysisButton;
    private String brandVisibility;
    private String pricing;
    private SharedPreferences mPrefs;
    private String mUserId;
    private String mAuthToken;
    private String visitId;
    private String outletId;
    private String outletName;

    private String userId;

    private ProgressDialog pd;
    private boolean isTaskRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_competitive_analysis);

        initToolbar();

//        mPrefs = getSharedPreferences("label", 0);
//        mUserId = mPrefs.getString("userId", "0");
        mUserId = EncryptedPrefsUtil.getString("userId","0");
        mAuthToken = EncryptedPrefsUtil.getString("authToken", "");

        submitCompetitiveAnalysisButton = (Button)findViewById(R.id.submitAnalysisButton);
        pricingEditText = (EditText)findViewById(R.id.pricingEditText);
        brandVisibilityEditText = (EditText)findViewById(R.id.brandVisibilityEditText);

        submitCompetitiveAnalysisButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isTaskRunning) {
                    isTaskRunning = true;
                    brandVisibility = brandVisibilityEditText.getText().toString();
                    pricing = pricingEditText.getText().toString();
                    //test

                    Intent i = getIntent();
                    visitId = i.getStringExtra("visitId");
                    userId = mUserId;
                    outletId = i.getStringExtra("outletId");
                    outletName = i.getStringExtra("outletName");


//                    new JsonTask().execute(Constants.BASE_URL + "api/addcompetitoranalysis", "POST", userId, outletId, visitId, brandVisibility, pricing);

                    new TokenManager(AddCompetitiveAnalysisActivity.this,  (success, result) -> {
                        if (success) {
                            // Handle the JSON result, update UI, etc.
                            System.out.println("API Result: " + result);

                            handleResult(result);

                        } else {
                            // Handle errors (e.g., token refresh failed)
                            Toasts.toastIconError(AddCompetitiveAnalysisActivity.this, result);
//                            Intent intent = new Intent(AddCompetitiveAnalysisActivity.this, LoginActivity.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(intent);

                        }
                    }).executeRequest(Constants.BASE_URL +"api/addcompetitoranalysis", "POST",
                            "userId",mUserId,"outletId",outletId,"visitId",visitId,"brandVisibility",
                            brandVisibility,"pricing",pricing);


                }
            }
        });

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
        getSupportActionBar().setTitle("Competitive Analysis");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Tools.setSystemBarColor(this);
    }

    private void handleResult(String result){

        isTaskRunning = false;
        System.out.println("Result: " + result);
        try {

            JSONObject jsonObject = new JSONObject(result);
            //Submit Failed means server returns nothing
            //Error saving means server responds with something other than success
            if (result != null) {
                if(result.contains("errors")){
                    String message = jsonObject.getString("message");
                    Toasts.toastIconError(AddCompetitiveAnalysisActivity.this,
                            message);
                    //Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                }
                else if(result.contains("success")){
                    Intent intent = new Intent(AddCompetitiveAnalysisActivity.this, SelectActionActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("visitId",visitId);
                    intent.putExtra("lastUpdatedActivity","competitiveanalysis");
                    intent.putExtra("outletName",outletName);
                    intent.putExtra("outletId",outletId);
                    startActivity(intent);
                }else{
                    Toasts.toastIconError(AddCompetitiveAnalysisActivity.this,
                            "Error Saving. Please Try Again");
                    FirebaseCrashlytics.getInstance().recordException(new Exception("AddCompetitiveAnalysis Error: "+result));

                }

            }else
            {
                FirebaseCrashlytics.getInstance().recordException(new Exception("AddCompetitiveAnalysis Error: "+result));
                Toasts.toastIconError(AddCompetitiveAnalysisActivity.this,
                        "Submit Feedback Failed. Please Try Again");
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(AddCompetitiveAnalysisActivity.this);

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

                if (requestMethod.equals("POST")) {
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("userId", params[2]);
                    jsonParam.put("outletId", params[3]);
                    jsonParam.put("visitId", params[4]);
                    jsonParam.put("brandVisibility", params[5]);
                    jsonParam.put("pricing", params[6]);



//                    Uri.Builder builder = new Uri.Builder()
//
//                            .appendQueryParameter("userId", params[2])
//                            .appendQueryParameter("outletId", params[3])
//                            .appendQueryParameter("visitId", params[4])
//                            .appendQueryParameter("brandVisibility", params[5])
//                            .appendQueryParameter("pricing", params[6]);;
//

//                    String query = builder.build().getEncodedQuery();

                    OutputStream os = connection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
//                    writer.write(query);
                    writer.write(jsonParam.toString());
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
            System.out.println("Result: " + result);
            try {

                JSONObject jsonObject = new JSONObject(result);
                //Submit Failed means server returns nothing
                //Error saving means server responds with something other than success
                if (result != null) {
                    if(result.contains("errors")){
                        String message = jsonObject.getString("message");
                        Toasts.toastIconError(AddCompetitiveAnalysisActivity.this,
                                message);
                        //Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                    }
                    else if(result.contains("success")){
                        Intent intent = new Intent(AddCompetitiveAnalysisActivity.this, SelectActionActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("visitId",visitId);
                        intent.putExtra("lastUpdatedActivity","competitiveanalysis");
                        intent.putExtra("outletName",outletName);
                        intent.putExtra("outletId",outletId);
                        startActivity(intent);
                    }else{
                        Toasts.toastIconError(AddCompetitiveAnalysisActivity.this,
                                "Error Saving. Please Try Again");
                        FirebaseCrashlytics.getInstance().recordException(new Exception("AddCompetitiveAnalysis Error: "+result));

                    }

                }else
                {
                    FirebaseCrashlytics.getInstance().recordException(new Exception("AddCompetitiveAnalysis Error: "+result));
                    Toasts.toastIconError(AddCompetitiveAnalysisActivity.this,
                            "Submit Feedback Failed. Please Try Again");
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
    }

}
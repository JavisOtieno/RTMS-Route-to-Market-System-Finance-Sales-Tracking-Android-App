package com.scg.sf;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.scg.sf.util.Toasts;

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

public class CompleteSaleActivity extends AppCompatActivity {
    private Button completeSaleButton;
    private Boolean isTaskRunning;
    private ProgressDialog pd;
    private String mAuthToken;
    private SharedPreferences mPrefs;
    private String saleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_complete_sale);

        initToolbar();

        mPrefs = getSharedPreferences("label", 0);
        mAuthToken = mPrefs.getString("authToken", "");

        Intent intent = getIntent();
//        saleId=intent.getStringExtra("saleId");
        saleId = intent.getStringExtra("saleId");

        completeSaleButton = (Button) findViewById(R.id.completeSaleButton);

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        completeSaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            new JsonTask().execute(
                    Constants.BASE_URL + "api/completesale/" +
                    saleId, "PUT");

            }
        });
    }

    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Complete Deal");
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

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(CompleteSaleActivity.this);

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


                Uri.Builder builder= new Uri.Builder();

//                System.out.println("Params " + params[2] + " " + params[3] + " " + params[4] );

//                String query = builder.build().getEncodedQuery();
//
                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
//                writer.write(query);

                writer.flush();
                writer.close();
                os.close();

//                System.out.println("Params " + params[2] + " " + params[3] + " " + params[4] );

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

            try {

                System.out.println("Result: " + result);
                if (result != null) {
                    JSONObject jsonObject = new JSONObject(result);

                    //Checkout Failed means server returns nothing
                    //Error checking out means server responds with something other than success

                    if(result.contains("errors")){
                        String message = jsonObject.getString("message");
                        Toasts.toastIconError(CompleteSaleActivity.this,message);
                        //Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                    }
                    else if (result.contains("success") ){


                        //txtJson.setText(jsonObject.toString());
                        //String visitId = jsonObject.getString("visitId");
                        //String outletId = jsonObject.getString("outletId");
                        //long unixTimestamp = Instant.now().getEpochSecond();



                        Intent intent = new Intent(CompleteSaleActivity.this,SalesActivity.class);
                        startActivity(intent);


                    }else{

                        Toasts.toastIconError(CompleteSaleActivity.this,
                                "Error Completing Sale. Please Try Again");
                        FirebaseCrashlytics.getInstance().recordException(new Exception("Sale Mark Complete Error: "+result));

                    }



                }else
                {
                    Toasts.toastIconError(CompleteSaleActivity.this,
                            "Sale Completion Failed. Please Try Again");
                    FirebaseCrashlytics.getInstance().recordException(new Exception("Sale Mark Complete Error: "+result));

                }


            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
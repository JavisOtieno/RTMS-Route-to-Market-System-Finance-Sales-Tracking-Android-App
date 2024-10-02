package com.scg.sf;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class UNUSEDMakeSaleActivity extends AppCompatActivity {

    private Button submitSaleButton;
    private ProgressDialog pd;

    private EditText piecesEditText;
    private String visitId;
    private String outletId;
    private String userId;
    private String productId;
    private SharedPreferences mPrefs;
    private String mUserId;
    private String mAuthToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_sale_old_unused);
        initToolbar();

        mPrefs = getSharedPreferences("label", 0);
        mUserId = mPrefs.getString("userId", "0");
        mAuthToken = mPrefs.getString("authToken", "");

        submitSaleButton = (Button) findViewById(R.id.submitSaleButton);
        piecesEditText = (EditText) findViewById(R.id.piecesEditText);
        submitSaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String pieces = piecesEditText.getText().toString();
                Intent i = getIntent();
                visitId=i.getStringExtra("visitId");
                productId = i.getStringExtra("productId");
                userId=mUserId;
                outletId =i.getStringExtra("outletId");

                //Toast.makeText(MakeSaleActivity.this,"visitid: "+visitId+" outletid: "+outletId,Toast.LENGTH_SHORT).show();

                new JsonTask().execute(Constants.BASE_URL + "api/makesale","POST",pieces,productId,userId,outletId,visitId);

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
        getSupportActionBar().setTitle("Make Sale");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Tools.setSystemBarColor(this);
    }
    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(UNUSEDMakeSaleActivity.this);

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
                            .appendQueryParameter("pieces", params[2])
                            .appendQueryParameter("productId", params[3])
                            .appendQueryParameter("userId", params[4])
                            .appendQueryParameter("outletId", params[5])
                            .appendQueryParameter("visitId", params[6]);;

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
            System.out.println("Result: " + result);
            try {

                if (result != null) {

                    JSONObject jsonObject = new JSONObject(result);
                    //txtJson.setText(jsonObject.toString());
                    //Submit Sale Failed means server returns nothing
                    //Error saving means server responds with something other than success

                        if(result.contains("errors")){
                            String message = jsonObject.getString("message");
                            toastIconError(message);
                            //Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                        }
                        else if(result.contains("success")){
                            Intent intent = new Intent(UNUSEDMakeSaleActivity.this, SelectActionActivity.class);
                            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("visitId",visitId);
                            intent.putExtra("outletId",outletId);
                            startActivity(intent);
                        }else{
                            toastIconError("Error Saving. Please Try Again");
                        }



                }else
                {
                    toastIconError("Submit Sale Failed. Please Try Again");
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
    }
    private void toastIconError(String error) {
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);

        //inflate view
        View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
        ((TextView) custom_view.findViewById(R.id.message)).setText(error);
        ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_close);
        ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.red_600));

        toast.setView(custom_view);
        toast.show();
    }
}
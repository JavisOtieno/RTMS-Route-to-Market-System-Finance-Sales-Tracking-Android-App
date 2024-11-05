package com.scg.sf;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.scg.sf.models.Outlet;
import com.scg.sf.util.EncryptedPrefsUtil;
import com.scg.sf.util.Toasts;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class CheckinActivity extends AppCompatActivity {

    private Button existingOutletButton;
    private Button newOutletButton;
    private Button startRoutePlanButton;
    private Button clockOutButton;
    private CardView routePlanCard;
    private ProgressDialog pd;
    private String mAuthToken;
    private SharedPreferences mPrefs;
    private Outlet outlet;
    private String routePlanItemId;
    private Button offplanRequestButton;
    private TextView outletNameTextView;
    private TextView routePlanStatusTextView;
    private static final int REQUEST_CODE = 1;
    private static final int WRITE_REQUEST_CODE = 2;
    private static final int  MEGABYTE = 1024 * 1024;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin);
        initToolbar();

        mPrefs = getSharedPreferences("label", 0);
//        mAuthToken = mPrefs.getString("authToken", "");

//        mUserId = EncryptedPrefsUtil.getString("userId","0");
        mAuthToken = EncryptedPrefsUtil.getString("authToken", "");
        // SharedPreferences.Editor mEditor = mPrefs.edit();
        // mEditor.putString("userId", "0").commit();


        existingOutletButton = (Button) findViewById(R.id.existingOutletButton);
        newOutletButton = (Button) findViewById(R.id.newOutletButton);
        startRoutePlanButton = (Button) findViewById(R.id.startRouteplanButton);
        offplanRequestButton = (Button) findViewById(R.id.requestOffplanVisitButton);
        clockOutButton = (Button) findViewById(R.id.clockoutButton);
        routePlanCard = (CardView) findViewById(R.id.routePlanCard);
        outletNameTextView = (TextView) findViewById(R.id.outletNameTextView);
        routePlanStatusTextView = (TextView) findViewById(R.id.routePlanStatusLabel);


//        new JsonTask().execute( Constants.BASE_URL + "api/userhasrouteplan","GET");

        new TokenManager(CheckinActivity.this,  (success, result) -> {
            if (success) {
                // Handle the JSON result, update UI, etc.
                System.out.println("API Result: " + result);

                handleResult(result);

            } else {
                // Handle errors (e.g., token refresh failed)
                Toasts.toastIconError(CheckinActivity.this, result);
                Intent intent = new Intent(CheckinActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }).executeRequest(Constants.BASE_URL +"api/userhasrouteplan", "GET");

        existingOutletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CheckinActivity.this, SelectExistingCustomerActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });


        newOutletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CheckinActivity.this, NewCustomerActivity.class);
                //Intent intent = new Intent(CheckinActivity.this, TakeOutletPhotoActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                //prevent going back to clock in
                Intent intent = new Intent(CheckinActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                //return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Check in");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Tools.setSystemBarColor(this);
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(CheckinActivity.this);

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

            try {
                System.out.println("Result: " + result);
                if (result != null && result.contains("routeplan")){
                        JSONObject jsonObject = new JSONObject(result);
                        //txtJson.setText(jsonObject.toString());

                        startRoutePlanButton.setVisibility(View.VISIBLE);
                        existingOutletButton.setVisibility(View.GONE);
                        newOutletButton.setVisibility(View.GONE);
//                        existingOutletButton.setText("OFFPLAN EXISTING OUTLET");
//                        newOutletButton.setText("OFFPLAN NEW OUTLET");

                        routePlanCard.setVisibility(View.VISIBLE);

                    if (jsonObject.has("routeplan") &&
                            jsonObject.get("routeplan") instanceof JSONArray) {

                        JSONArray routePlan = jsonObject.getJSONArray("routeplan");



                        if(routePlan.length()!=0)
                        {
                            JSONObject routePlanJsonObject = routePlan.getJSONObject(0);
                            String routePlanId=routePlanJsonObject.getString("id");

                        offplanRequestButton.setVisibility(View.VISIBLE);
                        offplanRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(CheckinActivity.this, AwaitingApprovalActivity.class);
                                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("routePlanId",routePlanId);
                                startActivity(intent);
                            }
                        });



                        JSONArray routePlanItems = routePlanJsonObject.getJSONArray("routeplanitems");
                        System.out.println("Route Plan Items Length: "+routePlanItems.length());
                        System.out.print("ResultRoutePlan: "+routePlan);

//                        Toast.makeText(CheckinActivity.this,
//                                routePlan.toString(),
//                                Toast.LENGTH_SHORT).show();
                        routePlanItemId= "0";

                        for(int i=0;i<routePlanItems.length();i++){
                            String visitOnRouteMap=routePlanItems.getJSONObject(i).getString("visit_id");
                            if(visitOnRouteMap.equals("null")){
                                //Toast.makeText(CheckinActivity.this,"We're good",Toast.LENGTH_SHORT).show();
                                JSONObject outletOnRouteMap=routePlanItems.getJSONObject(i).getJSONObject("outlet");
                                routePlanItemId = routePlanItems.getJSONObject(i).getString("id");

                                outlet = new Outlet();
                                outlet.name = outletOnRouteMap.getString("name");
                                outlet.id = outletOnRouteMap.getString("id");
                                outlet.lat = outletOnRouteMap.getString("lat");
                                outlet.lon = outletOnRouteMap.getString("long");
                                outlet.image = outletOnRouteMap.getString("image");
                                outlet.contact_person_name = outletOnRouteMap.getString("contact_person_name");
                                outlet.phone = outletOnRouteMap.getString("phone");
                                String createdAt = outletOnRouteMap.getString("created_at");
                                ZonedDateTime dateTime = Instant.parse(createdAt).atZone(ZoneId.of("Africa/Nairobi"));
                                String formatted = dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy \nHH:mm"));
                                outlet.date = formatted;

                                outletNameTextView.setText(outlet.name);

                                break;

                            }
                            //&& !visitOnRouteMap.equals("null") meaning visit id is already not null
                            else if(i==routePlanItems.length()-1 ){
                                startRoutePlanButton.setVisibility(View.GONE);
                                existingOutletButton.setText("EXISTING CUSTOMER");
                                newOutletButton.setText("NEW CUSTOMER");
                                outletNameTextView.setText("Route Plan Complete");
                                clockOutButton.setVisibility(View.VISIBLE);
                                routePlanStatusTextView.setText("Route Plan Status:");

                                clockOutButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(CheckinActivity.this, ClockoutActivity.class);
                                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);


                                    }
                                });




                            }
                        }




                        startRoutePlanButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

//                                Intent intentNote = new Intent(CheckinActivity.this, GenerateOrderNote.class);
//                                startActivity(intentNote);



                                Intent intent = new Intent(CheckinActivity.this, CheckinToExistingOutlet.class);
                                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("outletName",outlet.name);
                                intent.putExtra("outletId", outlet.id);
                                intent.putExtra("routePlanItemId",routePlanItemId);
                                intent.putExtra("lat",outlet.lat);
                                intent.putExtra("long",outlet.lon);
                                intent.putExtra("outlet",outlet);
                                startActivity(intent);

                            }
                        });

                        }else{
                            //array has no values
                            startRoutePlanButton.setVisibility(View.GONE);
                            routePlanCard.setVisibility(View.GONE);
                            existingOutletButton.setVisibility(View.VISIBLE);
                            newOutletButton.setVisibility(View.VISIBLE);
                            existingOutletButton.setText("EXISTING CUSTOMER");
                            newOutletButton.setText("NEW CUSTOMER");


                        }





                    }else {
                        //array returned null
                        Intent intent = new Intent(CheckinActivity.this, ErrorLoadingActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("callingActivity", "CheckInActivity");
                        FirebaseCrashlytics.getInstance().recordException(new Exception("CheckinActivity UserHasRoutePlan Error: "+result));
                        startActivity(intent);
                        finish();
                    }

                }else{
                    //api returned something weird
                    Intent intent = new Intent(CheckinActivity.this, ErrorLoadingActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("callingActivity", "CheckInActivity");
                    FirebaseCrashlytics.getInstance().recordException(new Exception("CheckinActivity UserHasRoutePlan Error: "+result));
                    startActivity(intent);
                    finish();
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private void handleResult(String result){
        try {
            System.out.println("Result: " + result);
            if (result != null && result.contains("routeplan")){
                JSONObject jsonObject = new JSONObject(result);
                //txtJson.setText(jsonObject.toString());

                startRoutePlanButton.setVisibility(View.VISIBLE);
                existingOutletButton.setVisibility(View.GONE);
                newOutletButton.setVisibility(View.GONE);
//                        existingOutletButton.setText("OFFPLAN EXISTING OUTLET");
//                        newOutletButton.setText("OFFPLAN NEW OUTLET");

                routePlanCard.setVisibility(View.VISIBLE);

                if (jsonObject.has("routeplan") &&
                        jsonObject.get("routeplan") instanceof JSONArray) {

                    JSONArray routePlan = jsonObject.getJSONArray("routeplan");



                    if(routePlan.length()!=0)
                    {
                        JSONObject routePlanJsonObject = routePlan.getJSONObject(0);
                        String routePlanId=routePlanJsonObject.getString("id");

                        offplanRequestButton.setVisibility(View.VISIBLE);
                        offplanRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(CheckinActivity.this, AwaitingApprovalActivity.class);
                                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("routePlanId",routePlanId);
                                startActivity(intent);
                            }
                        });



                        JSONArray routePlanItems = routePlanJsonObject.getJSONArray("routeplanitems");
                        System.out.println("Route Plan Items Length: "+routePlanItems.length());
                        System.out.print("ResultRoutePlan: "+routePlan);

//                        Toast.makeText(CheckinActivity.this,
//                                routePlan.toString(),
//                                Toast.LENGTH_SHORT).show();
                        routePlanItemId= "0";

                        for(int i=0;i<routePlanItems.length();i++){
                            String visitOnRouteMap=routePlanItems.getJSONObject(i).getString("visit_id");
                            if(visitOnRouteMap.equals("null")){
                                //Toast.makeText(CheckinActivity.this,"We're good",Toast.LENGTH_SHORT).show();
                                JSONObject outletOnRouteMap=routePlanItems.getJSONObject(i).getJSONObject("outlet");
                                routePlanItemId = routePlanItems.getJSONObject(i).getString("id");

                                outlet = new Outlet();
                                outlet.name = outletOnRouteMap.getString("name");
                                outlet.id = outletOnRouteMap.getString("id");
                                outlet.lat = outletOnRouteMap.getString("lat");
                                outlet.lon = outletOnRouteMap.getString("long");
                                outlet.image = outletOnRouteMap.getString("image");
                                outlet.contact_person_name = outletOnRouteMap.getString("contact_person_name");
                                outlet.phone = outletOnRouteMap.getString("phone");
                                String createdAt = outletOnRouteMap.getString("created_at");
                                ZonedDateTime dateTime = Instant.parse(createdAt).atZone(ZoneId.of("Africa/Nairobi"));
                                String formatted = dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy \nHH:mm"));
                                outlet.date = formatted;

                                outletNameTextView.setText(outlet.name);

                                break;

                            }
                            //&& !visitOnRouteMap.equals("null") meaning visit id is already not null
                            else if(i==routePlanItems.length()-1 ){
                                startRoutePlanButton.setVisibility(View.GONE);
                                existingOutletButton.setText("EXISTING CUSTOMER");
                                newOutletButton.setText("NEW CUSTOMER");
                                outletNameTextView.setText("Route Plan Complete");
                                clockOutButton.setVisibility(View.VISIBLE);
                                routePlanStatusTextView.setText("Route Plan Status:");

                                clockOutButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(CheckinActivity.this, ClockoutActivity.class);
                                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);


                                    }
                                });




                            }
                        }




                        startRoutePlanButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

//                                Intent intentNote = new Intent(CheckinActivity.this, GenerateOrderNote.class);
//                                startActivity(intentNote);



                                Intent intent = new Intent(CheckinActivity.this, CheckinToExistingOutlet.class);
                                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("outletName",outlet.name);
                                intent.putExtra("outletId", outlet.id);
                                intent.putExtra("routePlanItemId",routePlanItemId);
                                intent.putExtra("lat",outlet.lat);
                                intent.putExtra("long",outlet.lon);
                                intent.putExtra("outlet",outlet);
                                startActivity(intent);

                            }
                        });

                    }else{
                        //array has no values
                        startRoutePlanButton.setVisibility(View.GONE);
                        routePlanCard.setVisibility(View.GONE);
                        existingOutletButton.setVisibility(View.VISIBLE);
                        newOutletButton.setVisibility(View.VISIBLE);
                        existingOutletButton.setText("EXISTING CUSTOMER");
                        newOutletButton.setText("NEW CUSTOMER");


                    }





                }else {
                    //array returned null
                    Intent intent = new Intent(CheckinActivity.this, ErrorLoadingActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("callingActivity", "CheckInActivity");
                    FirebaseCrashlytics.getInstance().recordException(new Exception("CheckinActivity UserHasRoutePlan Error: "+result));
                    startActivity(intent);
                    finish();
                }

            }else{
                //api returned something weird
                Intent intent = new Intent(CheckinActivity.this, ErrorLoadingActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("callingActivity", "CheckInActivity");
                FirebaseCrashlytics.getInstance().recordException(new Exception("CheckinActivity UserHasRoutePlan Error: "+result));
                startActivity(intent);
                finish();
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }






}
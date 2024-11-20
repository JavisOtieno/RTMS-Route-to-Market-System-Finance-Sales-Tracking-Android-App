package com.scg.rtmssf;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.scg.rtmssf.util.EncryptedPrefsUtil;
import com.scg.rtmssf.util.Toasts;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ProfileActivity extends AppCompatActivity {
    private ProgressDialog pd;
    private JSONObject profileDetails = null;
    private TextView postTextView;
    private TextView emailTextView;
    private TextView phoneTextView;
    private TextView postTopTextView;
    private TextView nameTextView;
    private TextView salesTargetTextView;
    private TextView organizationTextView;
    private TextView regionTextView;
    private TextView territoryTextView;
    private TextView teamTextView;

    private TextView nameTopTextView;
    private String mAuthToken;
//    private SharedPreferences mPrefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initToolbar();

//        mPrefs = getSharedPreferences("label", 0);
//        mAuthToken = mPrefs.getString("authToken", "");
        mAuthToken = EncryptedPrefsUtil.getString("authToken", "");

        postTopTextView = (TextView)findViewById(R.id.postTopTextView);
//        postTextView = (TextView) findViewById(R.id.postTextView);
        nameTextView = (TextView) findViewById(R.id.nameTextView);
        emailTextView = (TextView) findViewById(R.id.emailTextView);
        phoneTextView = (TextView) findViewById(R.id.phoneTextView);
        nameTopTextView = (TextView) findViewById(R.id.nameTopTextView);
//        salesTargetTextView = (TextView) findViewById(R.id.salesTargetTextView);
        organizationTextView = (TextView) findViewById(R.id.organizationTextView);
        regionTextView = (TextView) findViewById(R.id.regionTextView);
        territoryTextView = (TextView) findViewById(R.id.territoryTextView);
        teamTextView = (TextView) findViewById(R.id.teamTextView);

//        new JsonTask().execute(Constants.BASE_URL + "api/profile","GET");

        new TokenManager(ProfileActivity.this,  (success, result) -> {
            if (success) {
                // Handle the JSON result, update UI, etc.
                System.out.println("API Result: " + result);

                handleResult(result);

            } else {
                // Handle errors (e.g., token refresh failed)
                Toasts.toastIconError(ProfileActivity.this, result);
//                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
            }
        }).executeRequest(Constants.BASE_URL +"api/profile", "GET");


    }

    private void handleResult(String result){
        if(pd != null && pd.isShowing() && !isFinishing()){
            pd.dismiss();
        }

        try {

            System.out.println("Result: "+result);
            if (result != null && result.contains("user")){
                JSONObject jsonObject = new JSONObject(result);
                //txtJson.setText(jsonObject.toString());
//                    List<Sale> items = new ArrayList<>();
                if (jsonObject.has("user") &&
                        jsonObject.get("user") instanceof JSONObject) {

                    profileDetails = jsonObject.getJSONObject("user");

                    nameTextView.setText(profileDetails.getString("name"));
                    phoneTextView.setText(profileDetails.getString("phone"));
                    emailTextView.setText(profileDetails.getString("email"));
                    String post = profileDetails.getString("usertype");
                    String postCapitalFirst = post.substring(0, 1).toUpperCase() + post.substring(1).toLowerCase();
//                    postTextView.setText(postCapitalFirst);
                    postTopTextView.setText(postCapitalFirst);
                    nameTopTextView.setText(profileDetails.getString("name"));

//                    if(!profileDetails.isNull("targetgroup")){
//                        String salesTarget = "KSh. "+ NumberFormat.getNumberInstance(Locale.US).format(
//                                Integer.valueOf(profileDetails.getJSONObject("targetgroup").getString("amount")));
//                        salesTargetTextView.setText(salesTarget);
//
//                    }else{
//                        salesTargetTextView.setText("N/A");
//                    }

                    if(!profileDetails.isNull("organization")){
                        organizationTextView.setText(profileDetails.getJSONObject("organization").getString("name"));
                    }else{
                        organizationTextView.setText("N/A");
                    }

                    if(!profileDetails.isNull("region")){
                        regionTextView.setText(profileDetails.getJSONObject("region").getString("name"));
                    }else{
                        regionTextView.setText("N/A");
                    }
                    if(!profileDetails.isNull("team")){
                        teamTextView.setText(profileDetails.getJSONObject("team").getString("name"));
                    }else{
                        teamTextView.setText("N/A");
                    }

                    if(!profileDetails.isNull("territory")){
                        territoryTextView.setText(profileDetails.getJSONObject("territory").getString("name"));
                    }else{
                        territoryTextView.setText("N/A");
                    }

                    //System.out.println("ProfileDetails length: "+profileDetails.length());

//                    outletsNumber.setText(outletsTotal);
//                    posAuditsNumber.setText(posAuditsTotal);
//                    salesNumber.setText(salesTotal);
//                    feedbackNumber.setText(feedbackTotal);
//                    targetSales.setText("KSh " + NumberFormat.getNumberInstance(Locale.US).format(parseInt(dailySalesTotal)));
//                    currentSales.setText("KSh " + NumberFormat.getNumberInstance(Locale.US).format(parseInt(currentSalesTarget)));
                }else{
                    Intent intent = new Intent(ProfileActivity.this, ErrorLoadingActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("callingActivity", "Profile");
                    FirebaseCrashlytics.getInstance().recordException(new Exception("Profile Error: "+result));

                    startActivity(intent);
                    finish();

                }
            }else{
                Intent intent = new Intent(ProfileActivity.this, ErrorLoadingActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("callingActivity", "Profile");
                FirebaseCrashlytics.getInstance().recordException(new Exception("Profile Error: "+result));

                //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
                startActivity(intent);
                finish();

            }
//                }else{
//                    Intent intent = new Intent(ProfileActivity.this, ErrorLoadingActivity.class);
//                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                    intent.putExtra("callingActivity", "Profile");
////
//                    FirebaseCrashlytics.getInstance().recordException(new Exception("Profile Error: "+result));
//                    //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
//                    startActivity(intent);
//                    finish();
//                }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(ProfileActivity.this);

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
                if (result != null && result.contains("user")){
                        JSONObject jsonObject = new JSONObject(result);
                    //txtJson.setText(jsonObject.toString());
//                    List<Sale> items = new ArrayList<>();
                    if (jsonObject.has("user") &&
                                jsonObject.get("user") instanceof JSONObject) {

                    profileDetails = jsonObject.getJSONObject("user");

                    nameTextView.setText(profileDetails.getString("name"));
                    phoneTextView.setText(profileDetails.getString("phone"));
                    emailTextView.setText(profileDetails.getString("email"));
                    String post = profileDetails.getString("usertype");
                    String postCapitalFirst = post.substring(0, 1).toUpperCase() + post.substring(1).toLowerCase();
//                    postTextView.setText(postCapitalFirst);
                    postTopTextView.setText(postCapitalFirst);
                    nameTopTextView.setText(profileDetails.getString("name"));

//                    if(!profileDetails.isNull("targetgroup")){
//                        String salesTarget = "KSh. "+ NumberFormat.getNumberInstance(Locale.US).format(
//                                Integer.valueOf(profileDetails.getJSONObject("targetgroup").getString("amount")));
//                        salesTargetTextView.setText(salesTarget);
//
//                    }else{
//                        salesTargetTextView.setText("N/A");
//                    }

                    if(!profileDetails.isNull("organization")){
                        organizationTextView.setText(profileDetails.getJSONObject("organization").getString("name"));
                    }else{
                        organizationTextView.setText("N/A");
                    }

                    if(!profileDetails.isNull("region")){
                        regionTextView.setText(profileDetails.getJSONObject("region").getString("name"));
                    }else{
                        regionTextView.setText("N/A");
                    }
                    if(!profileDetails.isNull("team")){
                            teamTextView.setText(profileDetails.getJSONObject("team").getString("name"));
                        }else{
                            teamTextView.setText("N/A");
                        }

                    if(!profileDetails.isNull("territory")){
                        territoryTextView.setText(profileDetails.getJSONObject("territory").getString("name"));
                    }else{
                        territoryTextView.setText("N/A");
                    }

                    //System.out.println("ProfileDetails length: "+profileDetails.length());

//                    outletsNumber.setText(outletsTotal);
//                    posAuditsNumber.setText(posAuditsTotal);
//                    salesNumber.setText(salesTotal);
//                    feedbackNumber.setText(feedbackTotal);
//                    targetSales.setText("KSh " + NumberFormat.getNumberInstance(Locale.US).format(parseInt(dailySalesTotal)));
//                    currentSales.setText("KSh " + NumberFormat.getNumberInstance(Locale.US).format(parseInt(currentSalesTarget)));
                        }else{
                            Intent intent = new Intent(ProfileActivity.this, ErrorLoadingActivity.class);
                            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("callingActivity", "Profile");
                            FirebaseCrashlytics.getInstance().recordException(new Exception("Profile Error: "+result));

                            startActivity(intent);
                            finish();

                        }
                        }else{
                        Intent intent = new Intent(ProfileActivity.this, ErrorLoadingActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("callingActivity", "Profile");
                        FirebaseCrashlytics.getInstance().recordException(new Exception("Profile Error: "+result));

                        //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
                        startActivity(intent);
                        finish();

                    }
//                }else{
//                    Intent intent = new Intent(ProfileActivity.this, ErrorLoadingActivity.class);
//                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                    intent.putExtra("callingActivity", "Profile");
////
//                    FirebaseCrashlytics.getInstance().recordException(new Exception("Profile Error: "+result));
//                    //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
//                    startActivity(intent);
//                    finish();
//                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }





    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Tools.setSystemBarColor(this);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                //getSupportActionBar().setTitle("Home");
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
package com.scg.sf;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.scg.sf.util.EncryptedPrefsUtil;
import com.scg.sf.util.Toasts;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SettingsActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ActionBar actionBar;
    private LinearLayout reportLayout;
    private String mUserId;
//    private SharedPreferences mPrefs;
    private LinearLayout termsAndConditionsLayout;
    private TextView organizationName;
    private TextView organizationPhone;
    private TextView organizationEmail;
    private ProgressDialog pd;
    private String mAuthToken;
    private JSONObject organizationDetails = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initToolbar();
//        mPrefs = getSharedPreferences("label", 0);
//        mUserId = mPrefs.getString("userId", "0");
//        mAuthToken = mPrefs.getString("authToken", "");

        mAuthToken = EncryptedPrefsUtil.getString("authToken", "");


       // LinearLayout contactUsLayout = (LinearLayout)findViewById(R.id.contactUsLayout);
        LinearLayout faqLayout = (LinearLayout) findViewById(R.id.faqLayout);
        LinearLayout privacyPolicyLayout = (LinearLayout) findViewById(R.id.privacyPolicyLayout);
        reportLayout = (LinearLayout) findViewById(R.id.reportLayout);
        termsAndConditionsLayout = (LinearLayout) findViewById(R.id.termsAndConditionsLayout);
        organizationName = (TextView) findViewById(R.id.organizationName);
        organizationPhone = (TextView) findViewById(R.id.organizationPhone);
        organizationEmail = (TextView) findViewById(R.id.organizationEmail);

//        new JsonTask().execute(Constants.BASE_URL + "api/organization","GET");

        new TokenManager(SettingsActivity.this,  (success, result) -> {
            if (success) {
                // Handle the JSON result, update UI, etc.
                System.out.println("API Result: " + result);

                handleResult(result);

            } else {
                // Handle errors (e.g., token refresh failed)
                Toasts.toastIconError(SettingsActivity.this, result);
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }).executeRequest(Constants.BASE_URL +"api/organization", "GET");


//        contactUsLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

        faqLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, FAQActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                
            }
        });
        
        privacyPolicyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, PrivacyPolicyActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                
            }
        });

        termsAndConditionsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SettingsActivity.this, TermsAndConditionsActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }
        });

        reportLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "technology@scgafrica.com" });
                intent.putExtra(Intent.EXTRA_SUBJECT, "Issue with my app #"+mUserId);
                intent.putExtra(Intent.EXTRA_TEXT, "Describe your issue here:\n");

                startActivity(Intent.createChooser(intent, "Email via..."));
//                String email="technology@scgafrica.com";
//                String subject="Issue with my app #";
//                String body="Describe your issue here:";
//                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
//                emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
//                emailIntent.putExtra(Intent.EXTRA_TEXT, body);
//                startActivity(Intent.createChooser(emailIntent, "Chooser Title"));
//

//                String email = "example@example.com";
//
//                Uri uri = Uri.parse("mailto:" +email)
//                        .buildUpon()
//                        .appendQueryParameter("email",email)
//                        .appendQueryParameter("subject", "Issue with my app #"+mUserId)
//                        .appendQueryParameter("body", "Describe your issue here:\n")
//                        .build();
//
//                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, uri);
//                startActivity(Intent.createChooser(emailIntent, "Choose Email Client"));

                // Replace with actual email address, subject, and body text
//                String email = "technology@scgafrica.com";
//                String subject = "Subject Here";
//                String body = "Body of the email";
//
//                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
//                emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
//                emailIntent.putExtra(Intent.EXTRA_TEXT, body);
//                // emailIntent.putExtra(Intent.EXTRA_HTML_TEXT, body); // Uncomment if using HTML in body text
//
//                // Create a chooser dialog with a custom title
//                Intent chooser = Intent.createChooser(emailIntent, "Choose an email client");
//
//                // Check if there is an app that can handle the intent
//                if (chooser.resolveActivity(getPackageManager()) != null) {
//                    startActivity(chooser);
//                } else {
//                    // Handle the case where no email apps are available
//                    Toast.makeText(SettingsActivity.this, "No email clients installed.", Toast.LENGTH_SHORT).show();
//                }
            }
        });

    }

    private void handleResult(String result){
        if(pd != null && pd.isShowing() && !isFinishing()){
            pd.dismiss();
        }

        try {

            System.out.println("Result: "+result);
            if (result != null && result.contains("organization")){
                JSONObject jsonObject = new JSONObject(result);
                //txtJson.setText(jsonObject.toString());
//                    List<Sale> items = new ArrayList<>();
                if (jsonObject.has("organization") &&
                        jsonObject.get("organization") instanceof JSONObject) {

                    organizationDetails = jsonObject.getJSONObject("organization");

                    organizationName.setText(organizationDetails.getString("name"));
                    organizationPhone.setText(organizationDetails.getString("phone"));
                    organizationEmail.setText(organizationDetails.getString("email"));
                }else{
                    Intent intent = new Intent(SettingsActivity.this, ErrorLoadingActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("callingActivity", "Settings");
                    FirebaseCrashlytics.getInstance().recordException(new Exception("Settings Error: "+result));

                    startActivity(intent);
                    finish();

                }
            }else{
                Intent intent = new Intent(SettingsActivity.this, ErrorLoadingActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("callingActivity", "Settings");
                FirebaseCrashlytics.getInstance().recordException(new Exception("Settings Error: "+result));

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
//
//    private class JsonTask extends AsyncTask<String, String, String> {
//
//        protected void onPreExecute() {
//            super.onPreExecute();
//
//            pd = new ProgressDialog(SettingsActivity.this);
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
//                Log.e(TAG,"Malf Message");
//            } catch (IOException e) {
//                Log.e("Message here: ",e.getMessage());
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
//                       if(pd != null && pd.isShowing() && !isFinishing()){
//                pd.dismiss();
//            }
//
//            try {
//
//                System.out.println("Result: "+result);
//                if (result != null && result.contains("organization")){
//                    JSONObject jsonObject = new JSONObject(result);
//                    //txtJson.setText(jsonObject.toString());
////                    List<Sale> items = new ArrayList<>();
//                    if (jsonObject.has("organization") &&
//                            jsonObject.get("organization") instanceof JSONObject) {
//
//                        organizationDetails = jsonObject.getJSONObject("organization");
//
//                        organizationName.setText(organizationDetails.getString("name"));
//                        organizationPhone.setText(organizationDetails.getString("phone"));
//                        organizationEmail.setText(organizationDetails.getString("email"));
//                    }else{
//                        Intent intent = new Intent(SettingsActivity.this, ErrorLoadingActivity.class);
//                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                        intent.putExtra("callingActivity", "Settings");
//                        FirebaseCrashlytics.getInstance().recordException(new Exception("Settings Error: "+result));
//
//                        startActivity(intent);
//                        finish();
//
//                    }
//                }else{
//                    Intent intent = new Intent(SettingsActivity.this, ErrorLoadingActivity.class);
//                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                    intent.putExtra("callingActivity", "Settings");
//                    FirebaseCrashlytics.getInstance().recordException(new Exception("Settings Error: "+result));
//
//                    //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
//                    startActivity(intent);
//                    finish();
//
//                }
////                }else{
////                    Intent intent = new Intent(ProfileActivity.this, ErrorLoadingActivity.class);
////                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
////                    intent.putExtra("callingActivity", "Profile");
//////
////                    FirebaseCrashlytics.getInstance().recordException(new Exception("Profile Error: "+result));
////                    //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
////                    startActivity(intent);
////                    finish();
////                }
//
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            }
//
//        }
//
//
//
//
//
//    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");
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


}
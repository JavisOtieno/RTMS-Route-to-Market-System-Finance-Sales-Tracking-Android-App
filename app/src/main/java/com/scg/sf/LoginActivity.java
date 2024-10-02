package com.scg.sf;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
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

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private Button signInButton;
    private String email;
    private String password;

    private ProgressDialog pd;
    private SharedPreferences mPrefs;

    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = (TextInputEditText) findViewById(R.id.emailField);
        passwordEditText = (TextInputEditText) findViewById(R.id.passwordField);
        signInButton = (Button) findViewById(R.id.signInButton);

        mPrefs = getSharedPreferences("label", 0);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email = emailEditText.getText().toString();
                password = passwordEditText.getText().toString();

                //Toast.makeText(getBaseContext(),"email: "+email+"password: "+password,Toast.LENGTH_SHORT).show();

                new JsonTask().execute(Constants.BASE_URL + "api/login","POST",email,password);


            }
        });




    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(LoginActivity.this);

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
                //connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Content-Type", "application/json");
                if (requestMethod.equals("POST")) {
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("email", params[2]);
                    jsonParam.put("password", params[3]);

//                    Uri.Builder builder = new Uri.Builder()
//                            .appendQueryParameter("email", params[2])
//                            .appendQueryParameter("password", params[3]
//                            );;
//
//                    String query = builder.build().getEncodedQuery();

                    OutputStream os = connection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
                    writer.write(jsonParam.toString());
                    writer.flush();
                    writer.close();
                    os.close();

                    System.out.println("Params " + params[2] + " " + params[3]);
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
            System.out.println("We are here");
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {

                System.out.println("Result: " + result);
                if (result != null) {
                    JSONObject jsonObject = new JSONObject(result);

                    //txtJson.setText(jsonObject.toString());
                    if(result.contains("errors")){
                        String message = jsonObject.getString("message");
                        Toasts.toastIconError(LoginActivity.this,message);
                        //Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                    }else if (
                            jsonObject.has("message") &&
                            jsonObject.get("message") instanceof String &&
                            jsonObject.has("status") &&
                            jsonObject.get("status") instanceof String
                    ) {
                        String message = jsonObject.getString("message");
                        String status = jsonObject.getString("status");

                        if (status.equals("success")) {
                            //Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                            SharedPreferences.Editor mEditor = mPrefs.edit();
                            mEditor.putString("userId", jsonObject.getString("userId")).commit();
                            mEditor.putString("authToken", jsonObject.getString("authToken")).commit();
                            //Toast.makeText(getBaseContext(), jsonObject.getString("authToken") , Toast.LENGTH_SHORT).show();

                            startActivity(intent);

                        } else if(status.equals("failed")) {
                            Toasts.toastIconError(LoginActivity.this, message);

                        } else{
                            Toasts.toastIconError(LoginActivity.this,
                                    "Login Failed! Please Try Again");
                            FirebaseCrashlytics.getInstance().recordException(new Exception("Login  Error: "+result));
                        }
                    }else{
                        Toasts.toastIconError(LoginActivity.this,
                                "Login Failed! Please Try Again");
                        FirebaseCrashlytics.getInstance().recordException(new Exception("Login Error JSON Object: "+result));


                    }



                }else{
                    Toasts.toastIconError(LoginActivity.this,
                            "Login Failed! Please Try Again");
                    FirebaseCrashlytics.getInstance().recordException(new Exception("Login Null Error: "+result));

                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
    }

}
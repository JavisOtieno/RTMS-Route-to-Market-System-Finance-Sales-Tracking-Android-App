package com.scg.sf;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.scg.sf.util.EncryptedPrefsUtil;
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


public class RefreshToken {
    private static RefreshToken instance;

    private RefreshToken() {
        // Private constructor for Singleton
    }

    public static synchronized RefreshToken getInstance() {
        if (instance == null) {
            instance = new RefreshToken();
        }
        return instance;
    }

    public void executeLoginTask(String url, String method,
                                 String email, String password, Context context,
                                 RefreshTokenCallback refreshTokenCallback) {
        System.out.println("refreshState: executeLoginTask");
//        new JsonTask(context).execute(url, method, email, password);

        // Define a callback to execute after JsonTask is complete
        new JsonTask(context, new JsonTaskCallback() {
            @Override
            public void onTaskComplete() {
                // After the token refresh is done, call the callback
                if (refreshTokenCallback != null) {
                    refreshTokenCallback.onRefreshComplete();
                }
            }
        }).execute(url, method, email, password);

        // After the token refresh is done, call the callback
//        if (refreshTokenCallback != null) {
//            refreshTokenCallback.onRefreshComplete();
//        }
    }

    // Define an interface for JsonTask completion callback
    public interface JsonTaskCallback {
        void onTaskComplete();
    }

//    public void executeLoginTask(String s, String post, String mEmail, String mPassword,
//                                 MainActivity mainActivity,
//                                 RefreshTokenCallback refreshTokenCallback) {
//    }

    private static class JsonTask2 extends AsyncTask<String, String, String> {
        private ProgressDialog pd;
        private Context context;

        public JsonTask2(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(context);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
            System.out.println("refreshState: Inside Pre");
        }

        @Override
        protected String doInBackground(String... params) {
            // (Your original code for making the HTTP connection)
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Handle the result and update the UI in the given context
        }
    }




    public class JsonTask extends AsyncTask<String, Void, String> {
        private ProgressDialog pd;
        private Context context;
        private  JsonTaskCallback jsonCallback;


        public JsonTask(Context context) {
            this.context = context;
        }

        public JsonTask(Context context, JsonTaskCallback callback) {
            this.context = context;
            this.jsonCallback = callback;
        }

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(context);

            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
            System.out.println("refreshState: Inside Pre");

        }

        protected String doInBackground(String... params) {

            System.out.println("refreshState: Inside doinback");


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {

                System.out.println("refreshState: Inside try");

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
//                runOnUiThread(() -> {
//                    if (pd != null && pd.isShowing() && !isFinishing()) {
//                        pd.dismiss();
//                    }
//                });

                ((Activity) context).runOnUiThread(() -> {
                    if (pd != null && pd.isShowing() && !((Activity) context).isFinishing()) {
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

            System.out.println("refreshState: Inside result");

            try {

                System.out.println("Result: " + result);
                if (result != null) {
                    JSONObject jsonObject = new JSONObject(result);

                    //txtJson.setText(jsonObject.toString());
                    if(result.contains("errors")){
                        String message = jsonObject.getString("message");
                        Toasts.toastIconError(context,message);
                        logout(context);
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
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

//                            SharedPreferences.Editor mEditor = mPrefs.edit();
//                            mEditor.putString("userId", jsonObject.getString("userId")).commit();
//                            mEditor.putString("authToken", jsonObject.getString("authToken")).commit();
//

//                            Toast.makeText(getBaseContext(), jsonObject.getString("authToken") , Toast.LENGTH_SHORT).show();

                            EncryptedPrefsUtil.saveString("authToken", jsonObject.getString("authToken"));
                            EncryptedPrefsUtil.saveString("userId", jsonObject.getString("userId"));
                            long currentTimeMillis = System.currentTimeMillis();
                            long expiryMillis = Constants.EXPIRY_MILLIS+currentTimeMillis;

                            EncryptedPrefsUtil.saveString("tokenExpiry", expiryMillis+"");
                            System.out.println("refreshState: token"+jsonObject.getString("authToken"));

//                            EncryptedPrefsUtil.saveString("email", email);
//                            EncryptedPrefsUtil.saveString("password",password);

                            // Notify that the task is complete
                            if (jsonCallback != null) {
                                jsonCallback.onTaskComplete();
                            }






//                            startActivity(intent);

                        } else if(status.equals("failed")) {
//                            Toasts.toastIconError(context, message);
                            logout(context);
                            FirebaseCrashlytics.getInstance().recordException(new Exception("Refresh Token  Error: "+result));


                        } else{
                            logout(context);
//                            Toasts.toastIconError(context,
//                                    "Login Failed! Please Try Again");
                            FirebaseCrashlytics.getInstance().recordException(new Exception("Login  Error: "+result));
                        }
                    }else{
                        logout(context);
//                        Toasts.toastIconError(context,
//                                "Login Failed! Please Try Again");
                        FirebaseCrashlytics.getInstance().recordException(new Exception("Login Error JSON Object: "+result));


                    }



                }else{
                    logout(context);
//                    Toasts.toastIconError(context,
//                            "Login Failed! Please Try Again");
                    FirebaseCrashlytics.getInstance().recordException(new Exception("Login Null Error: "+result));

                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
    }
    private void logout(Context context) {
        EncryptedPrefsUtil.saveString("userId", "0");
        EncryptedPrefsUtil.saveString("authToken", "");
        EncryptedPrefsUtil.saveString("email", "");
        EncryptedPrefsUtil.saveString("password","");
        Intent intent = new Intent(context,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

    }
}



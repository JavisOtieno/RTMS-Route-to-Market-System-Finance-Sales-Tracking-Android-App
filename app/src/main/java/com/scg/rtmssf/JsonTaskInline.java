package com.scg.rtmssf;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.scg.rtmssf.util.EncryptedPrefsUtil;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class JsonTaskInline extends AsyncTask<Object, Void, String> {
    private Context context;

    public interface JsonTaskListener {
        void onTaskComplete(String result);
    }

    private JsonTaskListener listener;

    public JsonTaskInline(JsonTaskListener listener) {
        this.listener = listener;
    }
    public JsonTaskInline(Context context, JsonTaskListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(Object... params) {
        String url =  (String) params[0];
        String method = (String) params[1];
        String response = "";

        try {
            URL apiUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();

            // Set the request method (GET/POST/PUT)
            conn.setRequestMethod(method);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
//            String authToken = sharedPreferences.getString("auth_token", null);
            String authToken = EncryptedPrefsUtil.getString("authToken", "");
            if (authToken != null) {
                conn.setRequestProperty("Authorization", "Bearer " + authToken);
            }

            // Handle POST and PUT methods
            if (method.equals("POST") || method.equals("PUT")) {
                conn.setDoOutput(true);
                // Create JSON object for request body
                JSONObject jsonParam = new JSONObject();

                // Assuming params[2] and onwards are parameter names and values

//                if (params[2] instanceof String) {
//                    // params[2] is a String
//                    String value = (String) params[2];
//                    System.out.println("params: string " + value);
//                    String value2 = (String) params[3];
//                    String value3 = (String) params[4];
//                    String value4 = (String) params[5];
//                    System.out.println("params: string " + value+" "+value2+" "+value3+" "+value4);
//                    jsonParam.put(value, value2);
//                    jsonParam.put(value3, value4);
//                    // Do something with value
//                } else {
                    params= (String[]) params[2];
                    for (int i = 0; i < params.length; i += 2) {
                        String paramName = (String) params[i]; // parameter name
                        String paramValue = (i + 1 < params.length) ? (String) params[i + 1] : ""; // parameter value
                        jsonParam.put(paramName, paramValue);
                        System.out.println("params: " + paramName + paramValue);
                    }
//                }
                

                // Write the JSON data to the output stream
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(jsonParam.toString());
                writer.flush();
                writer.close();
                os.close();
            }

            // Connect and read the response
            conn.connect();
            int responseCode = conn.getResponseCode();

            BufferedReader reader = null;

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            } else {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));

            }

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            response = sb.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(TAG, "Malf Message");
        } catch (IOException e) {
            Log.e("Message here: ", e.getMessage());
            e.printStackTrace();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
//        finally
//        {
//            if (conn != null) {
//                conn.disconnect();
//            }
//            try {
//                if (reader != null) {
//                    reader.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        return response;
    }

    @Override
    protected void onPostExecute(String result) {
        // Notify listener of the result
        System.out.println("Initial Result: " + result);
        if (listener != null) {
            listener.onTaskComplete(result);
        }
    }
}

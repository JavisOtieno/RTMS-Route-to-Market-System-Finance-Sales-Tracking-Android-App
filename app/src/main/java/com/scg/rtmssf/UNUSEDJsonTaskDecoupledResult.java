package com.scg.rtmssf;

import android.os.AsyncTask;

import com.scg.rtmssf.util.EncryptedPrefsUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


public class UNUSEDJsonTaskDecoupledResult extends AsyncTask<String, Void, String> {
    private JsonTaskCallback callback;

    public UNUSEDJsonTaskDecoupledResult(JsonTaskCallback callback) {
        this.callback = callback;
    }



    @Override
    protected String doInBackground(String... params) {
        String url = params[0];
        String method = params[1];
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
                for (int i = 2; i < params.length; i += 2) {
                    String paramName = params[i]; // parameter name
                    String paramValue = (i + 1 < params.length) ? params[i + 1] : ""; // parameter value
                    jsonParam.put(paramName, paramValue);
                }

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

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
                in.close();
                response = sb.toString();
            } else {
                response = "Error: " + responseCode;
            }

        } catch (Exception e) {
            e.printStackTrace();
            response = "Exception: " + e.getMessage();
        }

        return response;
    }


    @Override
    protected void onPostExecute(String result) {
        // Notify the caller of the result
        if (callback != null) {
            callback.onTaskComplete(result);
        }
    }

//    private JSONObject fetchJsonFromUrl(String url) {
//        // Network request code here
//    }
}


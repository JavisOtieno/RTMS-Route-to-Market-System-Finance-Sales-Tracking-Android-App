package com.scg.sf;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.scg.sf.util.EncryptedPrefsUtil;
import com.scg.sf.util.Toasts;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GenerateOrderNote extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private Button downloadOrderNoteButton;
    private Button skipButton;
    private Button pendingButton;
    private String outletId;
    private String visitId;
    private String outletName;
    private String saleId;
    private String mAuthToken;
//    private SharedPreferences mPrefs;
    private BroadcastReceiver onComplete;
    private boolean isTaskRunning = false;
    private ProgressDialog pd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_generate_order_note);

//        mPrefs = getSharedPreferences("label", 0);
//        mAuthToken = mPrefs.getString("authToken", "");
        mAuthToken = EncryptedPrefsUtil.getString("authToken", "");

        Intent intent = getIntent();
        visitId=intent.getStringExtra("visitId");
        outletName=intent.getStringExtra("outletName");
        outletId=intent.getStringExtra("outletId");
        saleId = intent.getStringExtra("saleId");


        initToolbar();

        downloadOrderNoteButton = (Button) findViewById(R.id.downloadPDFButton);
        skipButton = (Button) findViewById(R.id.skipButton);
        pendingButton = (Button) findViewById(R.id.pendingButton);

        downloadOrderNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // new DownloadFile().execute("http://maven.apache.org/maven-1.x/maven.pdf", "maven.pdf");

                String downloadUrl = Constants.BASE_URL+"api/download-pdf-mobile/"+saleId+".pdf";

                Uri Download_Uri = Uri.parse(downloadUrl);
                DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
                DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

                // Add the Bearer token to the request headers
                request.addRequestHeader("Authorization", "Bearer " + mAuthToken);

                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                request.setAllowedOverRoaming(false);
                request.setTitle(saleId+" - Order Note");
                request.setDescription("Downloading " + "order" + " note. Please wait...");
                request.setVisibleInDownloadsUi(true);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, saleId+"_order_note.pdf");
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                long refid = manager.enqueue(request);



                //Toast.makeText(GenerateOrderNote.this, "PDF Dow", Toast.LENGTH_SHORT).show();
                Toasts.toastIconDownload(GenerateOrderNote.this,
                        "Downloading Order Note PDF...");

                onComplete = new BroadcastReceiver() {
                    public void onReceive(Context ctxt, Intent intent) {
                        // Do something on download complete
                    }
                };

//                registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));




            }
        });

//        String testUrl ="https://css4.pub/2015/icelandic/dictionary.pdf";
//        String currentUrl = Constants.BASE_URL+"api/download-pdf-mobile/"+saleId+".pdf";
//        System.out.println(currentUrl);
//        Toast.makeText(GenerateOrderNote.this,currentUrl,Toast.LENGTH_SHORT).show();
        // startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl)));


//        WebView webView = (WebView) findViewById(R.id.webview);
//        webView.getSettings().setJavaScriptEnabled(true);
//        webView.loadUrl(URL);

//        WebView webView = findViewById(R.id.webview);
//        webView.setWebViewClient(new WebViewClient() {
//            @Override
//            public boolean shouldOverrideUrlLoading(@NonNull WebView view, @NonNull WebResourceRequest request) {
//                view.loadUrl(request.getUrl().toString());
//                return true; // Indicates WebView should handle the URL loading
//            }
//        });

//        WebSettings webSettings = webView.getSettings();
//        webSettings.setJavaScriptEnabled(true); // Enable JavaScript if needed
//
//        // Set the URL and Bearer token
//        String url = "https://www.example.com";


        // Create headers
//        Map<String, String> headers = new HashMap<>();
//        headers.put("Authorization", "Bearer " + mAuthToken);

//        webView.loadUrl(testUrl);
        // Load URL with headers
        //webView.loadUrl(currentUrl, headers);
//        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
//        Uri uri = Uri.parse("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf");
//        DownloadManager.Request request = new DownloadManager.Request(uri);
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
//        long reference = manager.enqueue(request);




//        String googleDocsUrl = "https://docs.google.com/viewer?url=" + currentUrl;
//        webView.loadUrl(currentUrl);
//        webView.loadUrl(googleDocsUrl, headers);



        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(GenerateOrderNote.this,SelectActionActivity.class);
                intent.putExtra("visitId",visitId);
                intent.putExtra("outletName",outletName);
                intent.putExtra("outletId",outletId);
                intent.putExtra("lastUpdatedActivity","sale");
                startActivity(intent);

            }
        });
        pendingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new JsonTask().execute(Constants.BASE_URL + "api/pendingsale/" +
                        saleId, "PUT");
//                Toasts.toastIconError(GenerateOrderNote.this,"Test");

            }
        });


        // new DownloadFile().execute("http://maven.apache.org/maven-1.x/maven.pdf", "maven.pdf");


//
//                                // Check for storage permissions
//                                if (Build.VERSION.SDK_INT >= 23) {
//                                    if (!hasStoragePermissions()) {
//                                        requestStoragePermissions();
//                                    } else {
//                                        new DownloadFile().execute("http://maven.apache.org/maven-1.x/maven.pdf", "maven.pdf");
//                                    }
//                                } else {
//                                        new DownloadFile().execute("http://maven.apache.org/maven-1.x/maven.pdf", "maven.pdf");
//                                }

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            GenerateOrderNote.this.registerReceiver(onComplete, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            GenerateOrderNote.this.registerReceiver(onComplete, filter);
        }
        // registerReceiver(onComplete, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        unregisterReceiver(onComplete);
    }








    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Order Note");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Tools.setSystemBarColor(this);
    }

    private boolean hasStoragePermissions() {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermissions() {
        ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new DownloadFile().execute();
            } else {
                // Permission was denied, show a message to the user
                // You can use a Toast or Snackbar to inform the user
            }
        }
    }

    private class DownloadFile extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            try {
                URL url = new URL("http://maven.apache.org/maven-1.x/maven.pdf");
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.setDoOutput(true);
                c.connect();

                String PATH = Environment.DIRECTORY_DOWNLOADS + "/download2/";
                Log.d("Abhan", "PATH: " + PATH);

                File directory = new File(PATH);
                if (!directory.exists()) {
                    directory.mkdirs();  // Creates all necessary directories if they don't exist
                    Log.d("Abhan", "Directory created");
                } else {
                    Log.d("Abhan", "Directory exists");
                }

                File outputFile = new File(directory, "testing.pdf");
                FileOutputStream fos = new FileOutputStream(outputFile);
                InputStream is = c.getInputStream();
                byte[] buffer = new byte[1024];
                int len1;
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);
                }
                fos.flush();
                fos.close();
                is.close();
                Log.d("Abhan", "File downloaded to " + outputFile.getAbsolutePath());
            } catch (IOException e) {
                Log.e("Abhan", "Error: " + e.getMessage());
            }
            return null;
        }
    }
    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(GenerateOrderNote.this);

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
                        Toasts.toastIconError(GenerateOrderNote.this,message);
                        //Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                    }
                    else if (result.contains("success") ){


                        //txtJson.setText(jsonObject.toString());
                        //String visitId = jsonObject.getString("visitId");
                        //String outletId = jsonObject.getString("outletId");
                        //long unixTimestamp = Instant.now().getEpochSecond();



                        Intent intent = new Intent(GenerateOrderNote.this,SelectActionActivity.class);
                        intent.putExtra("visitId",visitId);
                        intent.putExtra("outletName",outletName);
                        intent.putExtra("outletId",outletId);
                        intent.putExtra("lastUpdatedActivity","sale");
                        startActivity(intent);


                    }else{

                        Toasts.toastIconError(GenerateOrderNote.this,
                                "Error Marking as Pending. Please Try Again");
                        FirebaseCrashlytics.getInstance().recordException(new Exception("Sale Mark Pending Error: "+result));

                    }



                }else
                {
                    Toasts.toastIconError(GenerateOrderNote.this,
                            "Sale Pending Failed. Please Try Again");
                    FirebaseCrashlytics.getInstance().recordException(new Exception("Checkout Error: "+result));

                }


            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
    }





}

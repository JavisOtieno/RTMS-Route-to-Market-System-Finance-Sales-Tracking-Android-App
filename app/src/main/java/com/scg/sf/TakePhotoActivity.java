package com.scg.sf;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.scg.sf.util.ImageUtil;
import com.scg.sf.util.Toasts;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;

public class TakePhotoActivity extends AppCompatActivity {

    Button btpic, btnup;
    private Uri fileUri;
    String picturePath;
    Uri selectedImage;
    Bitmap photo;
    private String mAuthToken;
    private ByteArrayOutputStream bao;
    private Bitmap bm;

    String ba1;
    byte[] ba;
    //public static String URL = "https://av.ke/testfileupload.php";
    public static String URL;

    private SharedPreferences mPrefs;
    private String visitId;
    private String outletPOSAuditId;
    private String outletId;
    private String outletName;
    private String mCallingActivity;
    private String posAuditId;
    private Button skipPhotoTakingButton;
    private String mActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);
        //you need to get calling activity before you declare the Init Toolbar and have the title
        Intent i = getIntent();
        mCallingActivity = i.getStringExtra("callingActivity");

        initToolbar();

        mPrefs = getSharedPreferences("label", 0);
        mAuthToken = mPrefs.getString("authToken", "");

        btpic = (Button) findViewById(R.id.cpic);
        skipPhotoTakingButton = (Button) findViewById(R.id.skipPhotoButton);

        visitId=i.getStringExtra("visitId");
        outletName=i.getStringExtra("outletName");
        outletId=i.getStringExtra("outletId");
        outletPOSAuditId=outletId;
        if(mCallingActivity.equals("POSAudit")){
            posAuditId=i.getStringExtra("posAuditId");
            outletPOSAuditId=posAuditId;
            btpic.setText("Capture POS Audit Photo");
        }

        mActivity="outlet";
        if(mCallingActivity.equals("POSAudit")){
            mActivity="posaudit";
        }


        skipPhotoTakingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TakePhotoActivity.this, SelectActionActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);


                intent.putExtra("visitId",visitId);
                intent.putExtra("lastUpdatedActivity",mActivity);
                intent.putExtra("outletName",outletName);
                intent.putExtra("outletId",outletId);

                startActivity(intent);

            }
        });

     //   Toast.makeText(TakePhotoActivity.this,"outletId: "+outletId,Toast.LENGTH_SHORT).show();



        btpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickpic();
            }
        });

//        btnup = (Button) findViewById(R.id.up);
//        btnup.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                upload();
//            }
//        });

    }
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Take Outlet Photo");
        if (mCallingActivity.equals("POSAudit")){
            getSupportActionBar().setTitle("Take POS Audit Photo");
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Tools.setSystemBarColor(this);
    }

    private void upload() {
        // Image location URL
        Log.e("path", "----------------" +
                picturePath);

        // Image
        Bitmap bm = BitmapFactory.decodeFile(picturePath);
        bao = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 98, bao);
        ba = bao.toByteArray();
        //ba1 = Base64.encodeBytes(ba);

        Log.e("base64", "-----" + ba1);

        // Upload image to server
        // new uploadToServer().execute();

        //"https://av.ke/testfileupload.php"
        new uploadToServer2().execute("https://testscg.triontechnologyltd.com/api/uploadimage",
                "POST",System.currentTimeMillis() + ".jpg",ba1,outletPOSAuditId,mCallingActivity);

    }

    private void clickpic() {
        // Check Camera
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // Open default camera
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

            // start the image capture Intent
            startActivityForResult(intent, 100);

        } else {
            Toast.makeText(getApplication(), "Camera not supported", Toast.LENGTH_LONG).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode == RESULT_OK) {

            selectedImage = data.getData();
            photo = (Bitmap) data.getExtras().get("data");

            // Cursor to get image uri to display

//            String[] filePathColumn = {MediaStore.Images.Media.DATA};
//            Cursor cursor = getContentResolver().query(selectedImage,
//                    filePathColumn, null, null, null);
//            cursor.moveToFirst();
//
//            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//            picturePath = cursor.getString(columnIndex);
//            cursor.close();

            Bitmap photo = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 95, bao);
            byte[] ba = bao.toByteArray();
            ba1 = ImageUtil.convert(photo);

            //byte[] byteArray = {0x41, 0x42, 0x43, 0x44}; // Sample byte array

            // Convert byte array to Base64
            String base64String = Base64.getEncoder().encodeToString(ba);
            System.out.println("Base64 String: " + base64String);

            Log.e("base64", "-----" + base64String);

            URL = Constants.BASE_URL+"api/uploadimage";

            // Upload image to server
            //new uploadToServer().execute();
            //"https://av.ke/testfileupload.php"
            //new uploadToServer2().execute("https://av.ke/testfileupload.php","POST",System.currentTimeMillis() + ".jpg",ba1);
            new uploadToServer2().execute(URL,
                    "POST",System.currentTimeMillis() + ".jpg",ba1,outletPOSAuditId,mCallingActivity);

            ImageView imageView = (ImageView) findViewById(R.id.Imageprev);
            imageView.setImageBitmap(photo);
        }
    }



    public class uploadToServer extends AsyncTask<Void, Void, String> {

        private ProgressDialog pd = new ProgressDialog(TakePhotoActivity.this);
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Wait image uploading!");
            pd.show();
        }

        @Override
        protected String doInBackground(Void... params) {

//            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
//            nameValuePairs.add(new BasicNameValuePair("base64", ba1));
//            nameValuePairs.add(new BasicNameValuePair("ImageName", System.currentTimeMillis() + ".jpg"));
//            try {
//                HttpClient httpclient = new DefaultHttpClient();
//                HttpPost httppost = new HttpPost(URL);
//                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//                HttpResponse response = httpclient.execute(httppost);
//                String st = EntityUtils.toString(response.getEntity());
//                Log.v("log_tag", "In the try Loop" + st);
//
//            } catch (Exception e) {
//                Log.v("log_tag", "Error in http connection " + e.toString());
//            }
            return "Success";

        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
//            Toast.makeText(TakePhotoActivity.this,result,Toast.LENGTH_SHORT).show();
            pd.hide();
            pd.dismiss();

        }
    }
    public class uploadToServer2 extends AsyncTask<String, String, String> {
        private ProgressDialog pd;
        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(TakePhotoActivity.this);

            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();

        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {

//                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
//                nameValuePairs.add(new BasicNameValuePair("base64", ba1));
//                nameValuePairs.add(new BasicNameValuePair("ImageName", System.currentTimeMillis() + ".jpg"));
//                try {
//                    HttpClient httpclient = new DefaultHttpClient();
//                    HttpPost httppost = new HttpPost(URL);
//                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//                    HttpResponse response = httpclient.execute(httppost);
//                    String st = EntityUtils.toString(response.getEntity());
//                    Log.v("log_tag", "In the try Loop" + st);
//
//                } catch (Exception e) {
//                    Log.v("log_tag", "Error in http connection " + e.toString());
//                }
//
//
//                return "Success";

                URL url = new URL(params[0]);
                String requestMethod = params[1];
                String imageName= params[2];
                String base64=params[3];
                String outletPOSAuditId=params[4];
                String mCallingActivity = params[5];

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod(requestMethod);
                //connection.setRequestProperty("Content-Type", "image/jpeg");
                connection.setRequestProperty("Accept","application/json");
                connection.setRequestProperty("Authorization", "Bearer " + mAuthToken);
                connection.setRequestProperty("Content-Type", "application/json");

//                String urlSt = "http://phoenixcoding.tk/SavePicture.php";
//                URL url = new URL(urlSt);
//                connection = (HttpURLConnection) url.openConnection();
//
//                connection.setRequestMethod("POST");

            /*Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("name", name)
                    .appendQueryParameter("image", encodedImage);

            String query = builder.build().getEncodedQuery();*/



//                OutputStream os = connection.getOutputStream();
//                BufferedWriter writer = new BufferedWriter(
//                        new OutputStreamWriter(os, "UTF-8"));
                String folder = "outlets";
                if(mCallingActivity.equals("POSAudit")){
                    folder = "posaudits";
                }

//                Uri.Builder builder = new Uri.Builder()
//                        .appendQueryParameter("folder", folder)
//                        .appendQueryParameter("outletPOSAuditId", outletPOSAuditId)
//                        .appendQueryParameter("ImageName", imageName)
//                        .appendQueryParameter("base64", URLEncoder.encode(base64, "UTF-8"));
//
//                String query = builder.build().getEncodedQuery();

                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                //writer.write(query);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("folder", folder);
                jsonParam.put("outletPOSAuditId", outletPOSAuditId);
                jsonParam.put("ImageName", imageName);
                jsonParam.put("base64", URLEncoder.encode(base64, "UTF-8"));


                writer.write(jsonParam.toString());

                System.out.println("base64:"+base64);
                System.out.println("base64encoded:"+URLEncoder.encode(base64));

                writer.flush();
                writer.close();
                os.close();

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

//                outputStream.flush();
//                outputStream.close();
//                connection.disconnect();

//                connection.connect();

//                InputStream stream;
//
//                if (connection.getResponseCode() == 200) {
//                    stream = connection.getInputStream();
//
//                } else {
//                    /* error from server */
//                    //_is = httpConn.getErrorStream();
//                    stream = connection.getErrorStream();
//h
//                }

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


            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.e(TAG, "Malf Message");
                FirebaseCrashlytics.getInstance().recordException(e);

                // Additional error handling or dialog interaction
                if (pd != null && pd.isShowing() && !isFinishing()) {
                    pd.dismiss();
                }

            } catch (IOException e) {
                Log.e("Message here: ", e.getMessage());
                e.printStackTrace();
                FirebaseCrashlytics.getInstance().recordException(e);

                // Additional error handling or dialog interaction
                if (pd != null && pd.isShowing() && !isFinishing()) {
                    pd.dismiss();
                }

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
                    FirebaseCrashlytics.getInstance().recordException(e);

                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            System.out.println("Result: " + result);

//            Toast.makeText(TakePhotoActivity.this,result,Toast.LENGTH_SHORT).show();
            pd.hide();
            pd.dismiss();


            if (result != null) {

                try {
                    JSONObject jsonObject = new JSONObject(result);
                    //Submit Failed means server returns nothing
                    //Error saving means server responds with something other than success

                    if(result.contains("errors")){
                        String message = jsonObject.getString("message");
                        Toasts.toastIconError(TakePhotoActivity.this,message);
                        //Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                    }
                    else if(result.contains("success")){
                        Intent intent = new Intent(TakePhotoActivity.this, SelectActionActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);


                        intent.putExtra("visitId",visitId);
                        intent.putExtra("lastUpdatedActivity",mActivity);
                        intent.putExtra("outletName",outletName);
                        intent.putExtra("outletId",outletId);

                        startActivity(intent);

                    }else{
                        Toasts.toastIconError(TakePhotoActivity.this,
                                "Error Saving. Please Try Again");
                        //toastIconError(result);
                        //toastIconError(mAuthToken);
                        FirebaseCrashlytics.getInstance().recordException(new Exception("TakePhoto  Error: "+result));
                        System.out.println("Result: "+result);
                        System.out.println("Token: "+mAuthToken);
                    }



                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }else
            {
                Toasts.toastIconError(TakePhotoActivity.this,
                        "Photo Upload Failed. Please Try Again");
                FirebaseCrashlytics.getInstance().recordException(new Exception("TakePhoto Error: "+result));

            }

//            if (pd.isShowing()) {
//                pd.dismiss();
//            }
//
//            try {
//
//                System.out.println("Result: " + result);
//                if (result != null) {
//                    if (result.contains("products")){
//                        JSONObject jsonObject = new JSONObject(result);
//                        //txtJson.setText(jsonObject.toString());
//                        List<Product> items = new ArrayList<>();
//
//                        products = jsonObject.getJSONArray("products");
//                        System.out.println("Products length: "+products.length());
//
//                        for (int i = 0; i < products.length(); i++) {
//                            Product obj = new Product();
//                            //obj.image = drw_arr.getResourceId(i, -1);
//                            obj.name = products.getJSONObject(i).getString("name");
//                            obj.sku = products.getJSONObject(i).getString("sku");
//                            obj.price = products.getJSONObject(i).getString("price");
//                            obj.id = products.getJSONObject(i).getString("id");
//                            //obj.imageDrw = drw_arr.getDrawable(obj.image);
//                            items.add(obj);
//                        }
//
//                        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
//
//                        recyclerView.setHasFixedSize(true);
//
//
//                        //set data and list adapter
//                        mAdapter = new AdapterListProducts(items);
//                        recyclerView.setAdapter(mAdapter);
//
//
//
//                        // on item list clicked
////                        mAdapter.setOnItemClickListener(new AdapterListOutlets.OnItemClickListener() {
////                            @Override
////                            public void onItemClick(View view, Outlet obj, int position) {
////                                //Snackbar.make(parent_view, "Item " + obj.name + " clicked - id"+obj.id, Snackbar.LENGTH_SHORT).show();
////                                System.out.println("Item clicked " + obj.name + " clicked - id"+obj.id);
////                                long unixTime = System.currentTimeMillis() / 1000L;
////                                new JsonTask().execute(Constants.BASE_URL + "api/checkin","POST","1",unixTime+"","32.45567","44.4594",obj.id);
////                            }
////                        });
//
//                    }else {
//
//                        //Error
//                        //Toast.makeText(ProductsListActivity.this,result,Toast.LENGTH_SHORT).show();
//
//                    }
//
//                }
//
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            }

        }
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
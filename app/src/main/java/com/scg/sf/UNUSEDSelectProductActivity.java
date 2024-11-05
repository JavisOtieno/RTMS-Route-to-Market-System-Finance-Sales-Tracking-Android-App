package com.scg.sf;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.scg.sf.adapters.AdapterListProducts;
import com.scg.sf.models.Product;
import com.scg.sf.util.EncryptedPrefsUtil;

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
import java.util.ArrayList;
import java.util.List;

//ACTITIVITY NO LONGER IN USE
public class UNUSEDSelectProductActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressDialog pd;
    private JSONArray products = null;
    private AdapterListProducts mAdapter;
    private String visitId;
    private String outletId;
    private String mAuthToken;
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_product);

        initToolbar();
        mPrefs = getSharedPreferences("label", 0);
       mAuthToken= EncryptedPrefsUtil.getString("authToken", "");

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        Intent i = getIntent();
        visitId=i.getStringExtra("visitId");
        outletId =i.getStringExtra("outletId");

        new JsonTask().execute(Constants.BASE_URL + "api/products","GET");


    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Products");
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


    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(UNUSEDSelectProductActivity.this);

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

            try {

                System.out.println("Result: " + result);
                if (result != null) {
                //    if (result.contains("outlets")){
                        JSONObject jsonObject = new JSONObject(result);
                        //txtJson.setText(jsonObject.toString());
                        List<Product> items = new ArrayList<>();

                        products = jsonObject.getJSONArray("products");
                        System.out.println("Products length: "+products.length());

                        for (int i = 0; i < products.length(); i++) {
                            Product obj = new Product();
                            //obj.image = drw_arr.getResourceId(i, -1);
                            obj.name = products.getJSONObject(i).getString("name");
                            obj.sku = products.getJSONObject(i).getString("sku");
                            obj.price = products.getJSONObject(i).getString("price");
                            obj.id = products.getJSONObject(i).getString("id");
                            //obj.imageDrw = drw_arr.getDrawable(obj.image);
                            items.add(obj);
                        }

//                        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
//
//                        recyclerView.setHasFixedSize(true);

                        //set data and list adapter
                        mAdapter = new AdapterListProducts(items);
                        recyclerView.setAdapter(mAdapter);

                    //Toast.makeText(SelectProductActivity.this,"visitid: "+visitId+" outletid: "+outletId,Toast.LENGTH_SHORT).show();

                        // on item list clicked
                        mAdapter.setOnItemClickListener(new AdapterListProducts.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, Product obj, int position) {
                                //Snackbar.make(parent_view, "Item " + obj.name + " clicked - id"+obj.id, Snackbar.LENGTH_SHORT).show();
                                System.out.println("Item clicked " + obj.name + " clicked - id"+obj.id);
                                Intent intent = new Intent(UNUSEDSelectProductActivity.this, UNUSEDMakeSaleActivity.class);
                                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                //Toast.makeText(SelectProductActivity.this,"visitid: "+visitId+" outletid: "+outletId,Toast.LENGTH_SHORT).show();
                                intent.putExtra("productId", obj.id);
                                intent.putExtra("productName", obj.name);
                                intent.putExtra("visitId", visitId);
                                intent.putExtra("outletId", outletId);
                                startActivity(intent);
                            }
                        });



                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
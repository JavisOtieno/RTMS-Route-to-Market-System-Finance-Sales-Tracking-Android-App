package com.scg.sf;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.scg.sf.adapters.AdapterListProducts;
import com.scg.sf.models.Product;
import com.scg.sf.util.PreventDoubleClickOnLists;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProductsListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;

    private ProgressDialog pd;
    private JSONArray products = null;
    private AdapterListProducts mAdapter;
    private String mAuthToken;
    private SharedPreferences mPrefs;

    private List<Product> items;
    private TextView emptyView;

    private long lastIntentTime = 0;
    private static final long MIN_CLICK_INTERVAL = 1000; // 1000 milliseconds
    private TextView mNoProductsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products_list);
        initToolbar();

        mPrefs = getSharedPreferences("label", 0);
        mAuthToken = mPrefs.getString("authToken", "");
        mNoProductsTextView=(TextView)findViewById(R.id.noProductsText);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        emptyView = (TextView) findViewById(R.id.empty_view);

        new JsonTask().execute(Constants.BASE_URL + "api/products","GET");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Search by product name...");
        AutoCompleteTextView searchTextView = (AutoCompleteTextView) searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        try {
            @SuppressLint("SoonBlockedPrivateApi") Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(searchTextView, R.drawable.cursor); //This sets the cursor resource ID to 0 or @null which will make it visible on white background
        } catch (Exception e) {

        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                populateAdapter(s);
                return false;
            }
        });

        return true;
    }

    //searchview
    private void populateAdapter(String query) {
//        if(query.equals("")){
//            Toast.makeText(ProductsListActivity.this,"Nothing",Toast.LENGTH_SHORT).show();
//
//        }
//        else {

            //final ArrayList<Integer> counterGroup = new ArrayList<Integer>();
            List<Product> newProductsList =new ArrayList<>();

            for (int counter = 0; counter < items.size(); counter++) {
                if(query.contains(" ")){
                    String[] querySplit = query.toLowerCase().split("\\s+");
                    String product_name_searched=items.get(counter).name.toLowerCase();
                 //   Toast.makeText(ProductsListActivity.this,"Contains space",Toast.LENGTH_SHORT).show();



                    if(containsAllWords(product_name_searched,querySplit)){
                        //counterGroup.add(counter);
                        newProductsList.add(items.get(counter));
                    //    Toast.makeText(ProductsListActivity.this,product_name_searched+"-"+items.get(counter).name,Toast.LENGTH_SHORT).show();

                    }

                }else {
                   // Toast.makeText(ProductsListActivity.this,"No space",Toast.LENGTH_SHORT).show();

                    if (items.get(counter).name.toLowerCase().contains(query.toLowerCase())) {
                        //mNoProductsTextView.setVisibility(View.GONE);
                        //counterGroup.add(counter);
                        newProductsList.add(items.get(counter));

                    }

                }

            }

            if (newProductsList.size()>0) {
                mNoProductsTextView.setVisibility(View.GONE);
            }else
            {
                mNoProductsTextView.setVisibility(View.VISIBLE);
            }

            if(recyclerView==null) {
                recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            }
            mAdapter = new AdapterListProducts(newProductsList);
            recyclerView.setAdapter(mAdapter);
//            recyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View v,
//                                        int position, long id) {
//
//                    // Sending image id to FullScreenActivity
//                    Intent i = new Intent(MainActivity.this, DisplayProductsActivity.class);
//                    // passing array index
//                    i.putExtra("productClicked",productsList.get(counterGroup.get(position)) );
//                    startActivity(i);
//
//                }
//            });
            mAdapter.setOnItemClickListener(new AdapterListProducts.OnItemClickListener() {
                @Override
                public void onItemClick(View view, Product obj, int position) {

                    Product product = newProductsList.get(position);

                    PreventDoubleClickOnLists.
                            showActivityWithoutDoubleClick(
                                    "product",ProductsListActivity.this,
                                    product, IndividualProductActivity.class);



                }
            });
    //    }
    }
    public static boolean containsAllWords(String word, String ...keywords) {
        for (String k : keywords)
            if (!word.contains(k)) return false;
        return true;
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

            pd = new ProgressDialog(ProductsListActivity.this);

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



                System.out.println("Result: " + result);
                if (result != null && result.contains("products")){
                        try {
                        JSONObject jsonObject = new JSONObject(result);
                        //txtJson.setText(jsonObject.toString());

                            if (jsonObject.has("products") &&
                                    jsonObject.get("products") instanceof JSONArray) {


                        items = new ArrayList<>();

                        products = jsonObject.getJSONArray("products");
                        System.out.println("Products length: "+products.length());

                        for (int i = 0; i < products.length(); i++) {
                            Product obj = new Product();
                            //obj.image = drw_arr.getResourceId(i, -1);
                            obj.name = products.getJSONObject(i).getString("name");
                            obj.sku = products.getJSONObject(i).getString("sku");
                            obj.price =  products.getJSONObject(i).getString("price");
                            obj.id = products.getJSONObject(i).getString("id");
                            obj.description = products.getJSONObject(i).getString("description");
                            obj.image = products.getJSONObject(i).getString("image");
                            obj.stock = products.getJSONObject(i).getInt("stock");
                            obj.productType = products.getJSONObject(i).getString("producttype");
                            obj.targetType = products.getJSONObject(i).getString("targettype");
                            obj.target = products.getJSONObject(i).getString("target");
                            System.out.println("salestest"+
                                            products.getJSONObject(i).getString("total_sales")+" "+
                                            products.getJSONObject(i).getString("total_quantity")
                                    );
                            if(!products.getJSONObject(i).getString("total_quantity").equals("null")) {
                                obj.currentSalesQuantity = products.getJSONObject(i).getString("total_quantity");
                            }else{
                                obj.currentSalesQuantity="0";
                            }
                            if(!products.getJSONObject(i).getString("total_sales").equals("null")) {
                                obj.currentSalesTotal = products.getJSONObject(i).getString("total_sales");
                            }else{
                                obj.currentSalesTotal="0";
                            }

                            String createdAt = products.getJSONObject(i).getString("created_at");
                            ZonedDateTime dateTime = Instant.parse(createdAt).atZone(ZoneId.of("Africa/Nairobi"));
                            String formatted = dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy \nHH:mm"));
                            obj.date = formatted;
                            //obj.image = products.getJSONObject(i).getString("image");
                            //obj.imageDrw = drw_arr.getDrawable(obj.image);
                            items.add(obj);
                        }

                        if (items.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        }
                        else {
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyView.setVisibility(View.GONE);
                        }


                        //set data and list adapter
                        mAdapter = new AdapterListProducts(items);
                        recyclerView.setAdapter(mAdapter);

                        // on item list clicked
                        mAdapter.setOnItemClickListener(new AdapterListProducts.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, Product obj, int position) {

                                Product product = items.get(position);

                                PreventDoubleClickOnLists.
                                        showActivityWithoutDoubleClick(
                                                "product",ProductsListActivity.this,
                                                product, IndividualProductActivity.class);


                            }
                        });



                    }else {
                        Intent intent = new Intent(ProductsListActivity.this, ErrorLoadingActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("callingActivity", "Products");
                        FirebaseCrashlytics.getInstance().recordException(new Exception("ProductsList Error: "+result));


                        //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
                        startActivity(intent);
                        finish();

                        //Error
                        //Toast.makeText(ProductsListActivity.this,result,Toast.LENGTH_SHORT).show();

                    }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                }else{
                    Intent intent = new Intent(ProductsListActivity.this, ErrorLoadingActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("callingActivity","Products");
                    FirebaseCrashlytics.getInstance().recordException(new Exception("ProductsList Error: "+result));
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
                    finish();
                    //startActivity(intent);
                }




        }
    }

}
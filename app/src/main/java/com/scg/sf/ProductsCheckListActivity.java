package com.scg.sf;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.scg.sf.adapters.AdapterListCheckProducts;
import com.scg.sf.models.Product;
import com.scg.sf.util.EncryptedPrefsUtil;
import com.scg.sf.util.Toasts;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ProductsCheckListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;

    private ProgressDialog pd;
    private JSONArray products = null;
    private AdapterListCheckProducts mAdapter;
    private String mAuthToken;
//    private SharedPreferences mPrefs;

    private Button auditSelectedButton;
    private String visitId;
    private String outletId;
    private String outletName;
    private String userId;
    private String mUserId;
    private String mCallingActivity;
    private TextView emptyView;
    private List<Product> items;
    private long lastIntentTime = 0;
    private static final long MIN_CLICK_INTERVAL = 1000; // 1000 milliseconds
    private TextView mNoProductsTextView;

    private List<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products_check_list);
        initToolbar();
        Intent i = getIntent();
        mCallingActivity=i.getStringExtra("callingActivity");
        mNoProductsTextView=(TextView)findViewById(R.id.noProductsText);

//        mPrefs = getSharedPreferences("label", 0);
//        mAuthToken = mPrefs.getString("authToken", "");
        mAuthToken = EncryptedPrefsUtil.getString("authToken", "");
        auditSelectedButton = (Button) findViewById(R.id.auditSelectedButton);

        if (mCallingActivity.equals("makeSale")){
            auditSelectedButton.setText("Sell Selected");
        }else{
            auditSelectedButton.setText("Audit Selected");
        }

        emptyView = (TextView) findViewById(R.id.empty_view);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

//        new JsonTask().execute(Constants.BASE_URL + "api/productsinstock","GET");

        new TokenManager(ProductsCheckListActivity.this,  (success, result) -> {
            if (success) {
                // Handle the JSON result, update UI, etc.
                System.out.println("API Result: " + result);

                handleResult(result);

            } else {
                // Handle errors (e.g., token refresh failed)
                Toasts.toastIconError(ProductsCheckListActivity.this, result);
                Intent intent = new Intent(ProductsCheckListActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }).executeRequest(Constants.BASE_URL +"api/productsinstock", "GET"
                );
    }

    private void handleResult(String result){

        if(pd != null && pd.isShowing() && !isFinishing()){
            pd.dismiss();
        }

        try {

            System.out.println("Result: " + result);
            if (result != null && result.contains("products")){

                JSONObject jsonObject = new JSONObject(result);
                //txtJson.setText(jsonObject.toString());
                items = new ArrayList<>();

                if (jsonObject.has("products") &&
                        jsonObject.get("products") instanceof JSONArray) {

                    products = jsonObject.getJSONArray("products");
                    System.out.println("Products length: "+products.length());

                    for (int i = 0; i < products.length(); i++) {
                        Product obj = new Product();
                        //obj.image = drw_arr.getResourceId(i, -1);
                        obj.name = products.getJSONObject(i).getString("name");
                        obj.sku = products.getJSONObject(i).getString("sku");
                        obj.price = products.getJSONObject(i).getString("price");
                        obj.id = products.getJSONObject(i).getString("id");
                        obj.stock = products.getJSONObject(i).getInt("stock");
                        obj.productType = products.getJSONObject(i).getString("producttype");
                        obj.targetType = products.getJSONObject(i).getString("targettype");
                        System.out.println("targettype: "+obj.targetType);
                        obj.target = products.getJSONObject(i).getString("target");
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
                    mAdapter = new AdapterListCheckProducts(items);
                    recyclerView.setAdapter(mAdapter);

                    mAdapter.setOnItemClickListener(new AdapterListCheckProducts.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, Product obj, int position) {
                            //Snackbar.make(parent_view, "Item " + obj.name + " clicked - id"+obj.id, Snackbar.LENGTH_SHORT).show();
                            System.out.println("Item clicked " + obj.name + " clicked - id"+obj.id);

                            CheckBox checkBox=view.findViewById(R.id.checkProduct);
                            if(checkBox.isChecked()) {
                                checkBox.setChecked(false);
                                obj.checked=false;
                            }else{
                                checkBox.setChecked(true);
                                obj.checked=true;
                            }
                        }
                    });

                    auditSelectedButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            productList = new ArrayList<>();


                            for (int i = 0; i < items.size(); i++) {
                                Product product = items.get(i);
                                //String productId = product.id;
//                                    CheckBox checkProduct = (CheckBox) vie.findViewById(R.id.checkProduct);
//                                    Boolean productSelected = checkProduct.isChecked();
                                if(product.checked){
                                    productList.add(product);
                                }

                                Log.d("productCount: ", productList+"");



//                                    for (int i = 0; i < recyclerView.getChildCount(); i++) {
//                                    View vie = recyclerView.getChildAt(i);
//                                    Product product = items.get(i);
//                                    //String productId = product.id;
//                                    CheckBox checkProduct = (CheckBox) vie.findViewById(R.id.checkProduct);
//                                    Boolean productSelected = checkProduct.isChecked();
//                                    if(productSelected){
//                                        productList.add(product);
//                                    }
//                                    Log.d("productCount: ", productList+"");


                            }

                            Intent i = getIntent();
                            visitId = i.getStringExtra("visitId");
                            outletId = i.getStringExtra("outletId");
                            outletName = i.getStringExtra("outletName");
                            mCallingActivity = i.getStringExtra("callingActivity");

                            if(productList.isEmpty()){
                                Toasts.toastIconError(ProductsCheckListActivity.this,"You have not selected any products");
                            }else{
                                Intent intent = new Intent(ProductsCheckListActivity.this, AddSalePOSAuditActivity.class);

                                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("visitId", visitId);
                                intent.putExtra("outletId", outletId);
                                intent.putExtra("outletName", outletName);
                                intent.putExtra("productList", (Serializable) productList);
                                intent.putExtra("callingActivity",mCallingActivity);
                                startActivity(intent);

                                //new JsonTask().execute(Constants.BASE_URL + "api/addposaudit", "POST", productString, userId, outletId, visitId);
                                //Toast.makeText(getBaseContext(), productString, Toast.LENGTH_SHORT).show();
                                System.out.println("productString: " + productList);

                            }




                        }
                    });

                    // on item list clicked
//                        mAdapter.setOnItemClickListener(new AdapterListOutlets.OnItemClickListener() {
//                            @Override
//                            public void onItemClick(View view, Outlet obj, int position) {
//                                //Snackbar.make(parent_view, "Item " + obj.name + " clicked - id"+obj.id, Snackbar.LENGTH_SHORT).show();
//                                System.out.println("Item clicked " + obj.name + " clicked - id"+obj.id);
//                                long unixTime = System.currentTimeMillis() / 1000L;
//                                new JsonTask().execute(Constants.BASE_URL + "api/checkin","POST","1",unixTime+"","32.45567","44.4594",obj.id);
//                            }
//                        });

                }else {
                    FirebaseCrashlytics.getInstance().recordException(new Exception("ProductsCheckList Error: "+result));
                    Toasts.toastIconError(ProductsCheckListActivity.this,"Error Loading Products");

                    //Error
                    //Toast.makeText(ProductsListActivity.this,result,Toast.LENGTH_SHORT).show();

                }

            }else{
                FirebaseCrashlytics.getInstance().recordException(new Exception("ProductsCheckList Error: "+result));
                Toasts.toastIconError(ProductsCheckListActivity.this,"Error Loading Products");


            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

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

            pd = new ProgressDialog(ProductsCheckListActivity.this);

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
                if (result != null && result.contains("products")){

                        JSONObject jsonObject = new JSONObject(result);
                        //txtJson.setText(jsonObject.toString());
                        items = new ArrayList<>();

                        if (jsonObject.has("products") &&
                                jsonObject.get("products") instanceof JSONArray) {

                        products = jsonObject.getJSONArray("products");
                        System.out.println("Products length: "+products.length());

                        for (int i = 0; i < products.length(); i++) {
                            Product obj = new Product();
                            //obj.image = drw_arr.getResourceId(i, -1);
                            obj.name = products.getJSONObject(i).getString("name");
                            obj.sku = products.getJSONObject(i).getString("sku");
                            obj.price = products.getJSONObject(i).getString("price");
                            obj.id = products.getJSONObject(i).getString("id");
                            obj.stock = products.getJSONObject(i).getInt("stock");
                            obj.productType = products.getJSONObject(i).getString("producttype");
                            obj.targetType = products.getJSONObject(i).getString("targettype");
                            System.out.println("targettype: "+obj.targetType);
                            obj.target = products.getJSONObject(i).getString("target");
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
                        mAdapter = new AdapterListCheckProducts(items);
                        recyclerView.setAdapter(mAdapter);

                        mAdapter.setOnItemClickListener(new AdapterListCheckProducts.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, Product obj, int position) {
                                //Snackbar.make(parent_view, "Item " + obj.name + " clicked - id"+obj.id, Snackbar.LENGTH_SHORT).show();
                                System.out.println("Item clicked " + obj.name + " clicked - id"+obj.id);

                                CheckBox checkBox=view.findViewById(R.id.checkProduct);
                                if(checkBox.isChecked()) {
                                    checkBox.setChecked(false);
                                    obj.checked=false;
                                }else{
                                    checkBox.setChecked(true);
                                    obj.checked=true;
                                }
                            }
                        });

                        auditSelectedButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                productList = new ArrayList<>();


                                for (int i = 0; i < items.size(); i++) {
                                    Product product = items.get(i);
                                    //String productId = product.id;
//                                    CheckBox checkProduct = (CheckBox) vie.findViewById(R.id.checkProduct);
//                                    Boolean productSelected = checkProduct.isChecked();
                                    if(product.checked){
                                        productList.add(product);
                                    }

                                    Log.d("productCount: ", productList+"");



//                                    for (int i = 0; i < recyclerView.getChildCount(); i++) {
//                                    View vie = recyclerView.getChildAt(i);
//                                    Product product = items.get(i);
//                                    //String productId = product.id;
//                                    CheckBox checkProduct = (CheckBox) vie.findViewById(R.id.checkProduct);
//                                    Boolean productSelected = checkProduct.isChecked();
//                                    if(productSelected){
//                                        productList.add(product);
//                                    }
//                                    Log.d("productCount: ", productList+"");


                                }

                                Intent i = getIntent();
                                visitId = i.getStringExtra("visitId");
                                outletId = i.getStringExtra("outletId");
                                outletName = i.getStringExtra("outletName");
                                mCallingActivity = i.getStringExtra("callingActivity");

                                if(productList.isEmpty()){
                                    Toasts.toastIconError(ProductsCheckListActivity.this,"You have not selected any products");
                                }else{
                                    Intent intent = new Intent(ProductsCheckListActivity.this, AddSalePOSAuditActivity.class);

                                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("visitId", visitId);
                                    intent.putExtra("outletId", outletId);
                                    intent.putExtra("outletName", outletName);
                                    intent.putExtra("productList", (Serializable) productList);
                                    intent.putExtra("callingActivity",mCallingActivity);
                                    startActivity(intent);

                                    //new JsonTask().execute(Constants.BASE_URL + "api/addposaudit", "POST", productString, userId, outletId, visitId);
                                    //Toast.makeText(getBaseContext(), productString, Toast.LENGTH_SHORT).show();
                                    System.out.println("productString: " + productList);

                                }




                            }
                        });

                        // on item list clicked
//                        mAdapter.setOnItemClickListener(new AdapterListOutlets.OnItemClickListener() {
//                            @Override
//                            public void onItemClick(View view, Outlet obj, int position) {
//                                //Snackbar.make(parent_view, "Item " + obj.name + " clicked - id"+obj.id, Snackbar.LENGTH_SHORT).show();
//                                System.out.println("Item clicked " + obj.name + " clicked - id"+obj.id);
//                                long unixTime = System.currentTimeMillis() / 1000L;
//                                new JsonTask().execute(Constants.BASE_URL + "api/checkin","POST","1",unixTime+"","32.45567","44.4594",obj.id);
//                            }
//                        });

                    }else {
                            FirebaseCrashlytics.getInstance().recordException(new Exception("ProductsCheckList Error: "+result));
                            Toasts.toastIconError(ProductsCheckListActivity.this,"Error Loading Products");

                        //Error
                        //Toast.makeText(ProductsListActivity.this,result,Toast.LENGTH_SHORT).show();

                    }

                }else{
                    FirebaseCrashlytics.getInstance().recordException(new Exception("ProductsCheckList Error: "+result));
                    Toasts.toastIconError(ProductsCheckListActivity.this,"Error Loading Products");


                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
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
//
//        }
//        else {

            //final ArrayList<Integer> counterGroup = new ArrayList<Integer>();
            List<Product> newProductsList =new ArrayList<>();

            for (int counter = 0; counter < items.size(); counter++) {
                if(query.contains(" ")){
                    String[] querySplit = query.toLowerCase().split("\\s+");
                    String product_name_searched=items.get(counter).name.toLowerCase();


                    if(containsAllWords(product_name_searched,querySplit)){
                        //counterGroup.add(counter);
                        newProductsList.add(items.get(counter));
                    }

                }else {
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
            mAdapter = new AdapterListCheckProducts(newProductsList);
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
            mAdapter.setOnItemClickListener(new AdapterListCheckProducts.OnItemClickListener() {
                @Override
                public void onItemClick(View view, Product obj, int position) {

                    System.out.println("Item clicked " + obj.name + " clicked - id"+obj.id);

                    CheckBox checkBox=view.findViewById(R.id.checkProduct);
                    if(checkBox.isChecked()) {
                        checkBox.setChecked(false);
                        obj.checked=false;
                    }else{
                        checkBox.setChecked(true);
                        obj.checked=true;
                    }



                }
            });
//        }
    }
    public static boolean containsAllWords(String word, String ...keywords) {
        for (String k : keywords)
            if (!word.contains(k)) return false;
        return true;
    }





}
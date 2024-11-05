package com.scg.sf;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static java.lang.Integer.parseInt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.scg.sf.adapters.AdapterListSales;
import com.scg.sf.models.Sale;
import com.scg.sf.models.SaleItem;
import com.scg.sf.util.EncryptedPrefsUtil;
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
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SalesActivity extends AppCompatActivity {

    private View parent_view;

    private RecyclerView recyclerView;
    private AdapterListSales mAdapter;
    private Button clockOutButton;
    private ProgressDialog pd;

    private JSONArray sales = null;
    private List<Sale> items;
    private ArrayList<SaleItem> saleItemsList;
    private Context context;
    private TypedArray drw_arr;
    private String name_arr[];
    private String mAuthToken;
//    private SharedPreferences mPrefs;
    private TextView emptyView;
    private long lastIntentTime = 0;
    private static final long MIN_CLICK_INTERVAL = 1000; // 1000 milliseconds
    private RelativeLayout errorLoadingRelativeLayout;
    private LinearLayout lyt_no_connection;
    private TextView mNoSalesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales);
        parent_view = findViewById(android.R.id.content);

//        mPrefs = getSharedPreferences("label", 0);
//        mAuthToken = mPrefs.getString("authToken", "");
        mAuthToken = EncryptedPrefsUtil.getString("authToken", "");


        //drw_arr = this.getResources().obtainTypedArray(R.array.people_images);
        //name_arr = this.getResources().getStringArray(R.array.people_names);
        emptyView = (TextView) findViewById(R.id.empty_view);
        errorLoadingRelativeLayout = findViewById(R.id.errorLoading);
        mNoSalesTextView = (TextView) findViewById(R.id.noSalesText);

        initToolbar();
        initComponent();

        lyt_no_connection = (LinearLayout) findViewById(R.id.lyt_no_connection);
        //lyt_no_connection.setVisibility(View.VISIBLE);

        lyt_no_connection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                //   lyt_no_connection.setVisibility(View.GONE);
                new JsonTask().execute(Constants.BASE_URL + "api/sales","GET");
            }
        });

        new JsonTask().execute(Constants.BASE_URL + "api/sales","GET");


    }
    private void initComponent() {

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);



        //List<Sale> items = DataGenerator.getPeopleData(this);
        //items.addAll(DataGenerator.getPeopleData(this));
        //items.addAll(DataGenerator.getPeopleData(this));

        //set data and list adapter
        //mAdapter = new AdapterListBasic(this, items);
        //recyclerView.setAdapter(mAdapter);

        // on item list clicked
//        mAdapter.setOnItemClickListener(new AdapterListBasic.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, Sale obj, int position) {
//                Snackbar.make(parent_view, "Item " + obj.name + " clicked", Snackbar.LENGTH_SHORT).show();
//            }
//        });

    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(SalesActivity.this);

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
                Log.e(TAG,"Malf Message");
            } catch (IOException e) {
                Log.e("Message here: ",e.getMessage());
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

                System.out.println("Result: "+result);
                if (result != null && result.contains("sales")) {
                        JSONObject jsonObject = new JSONObject(result);
                        //txtJson.setText(jsonObject.toString());

                    if (jsonObject.has("sales") &&
                            jsonObject.get("sales") instanceof JSONArray) {

                        items = new ArrayList<>();

                        sales = jsonObject.getJSONArray("sales");

                        System.out.println("Sales length: " + sales.length());


                        for (int i = 0; i < sales.length(); i++) {
                            Sale obj = new Sale();
                            //obj.image = drw_arr.getResourceId(i, -1);

                            JSONArray saleItems = sales.getJSONObject(i).getJSONArray("saleitems");

                            String allProductsSalesitemString = "";

                            String firstsaleItemProductName =
                                    saleItems.getJSONObject(0).getJSONObject("product")
                                            .getString("name");
                            String firstsaleItemPieces =
                                    saleItems.getJSONObject(0).getString("pieces");
                            String singleProductsSalesitemString = firstsaleItemProductName + " - " + firstsaleItemPieces;

                            saleItemsList = new ArrayList<>();

                            for (int j = 0; j < saleItems.length(); j++) {

                                String saleItemProductName = saleItems.getJSONObject(j).getJSONObject("product").getString("name");
                                String saleItemPieces = saleItems.getJSONObject(j).getString("pieces");
                                allProductsSalesitemString = allProductsSalesitemString + saleItemProductName + " - " + saleItemPieces + " ";

                                System.out.println("SaleItem: " + allProductsSalesitemString);

                                SaleItem saleItem = new SaleItem();
                                saleItem.productName = saleItems.getJSONObject(j).getJSONObject("product").getString("name");
                                saleItem.pieces = saleItems.getJSONObject(j).getString("pieces");
                                saleItem.price = saleItems.getJSONObject(j).getString("price");
                                saleItem.value = saleItems.getJSONObject(j).getString("value");
                                saleItemsList.add(saleItem);

                            }
                            if (saleItems.length() > 1) {
                                singleProductsSalesitemString += "...";
                            }


                            System.out.println("SaleItems: " + saleItems.length());
                            obj.name = singleProductsSalesitemString;
                            obj.allproductsname = allProductsSalesitemString;
                            obj.saleItems = saleItemsList;
                            //saleItemsList.clear();


                            //obj.name = sales.getJSONObject(i).getJSONObject("product").getString("name");
                            String createdAt = sales.getJSONObject(i).getString("created_at");
                            ZonedDateTime dateTime = Instant.parse(createdAt).atZone(ZoneId.of("Africa/Nairobi"));
                            String formatted = dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy \nHH:mm "));
                            obj.time = formatted;


                            String status = sales.getJSONObject(i).getString("status");
                            obj.status=status.substring(0, 1).toUpperCase() + status.substring(1);

                            Integer pieces = Integer.valueOf(sales.getJSONObject(i).getString("pieces"));

                            String outletName = "";
                            String sellerName = "";

                            if(!sales.getJSONObject(i).isNull("outlet")){
                                outletName = sales.getJSONObject(i).getJSONObject("outlet").getString("name");
                            }else{
                                outletName="Outlet Not Found";
                            }

                            if(!sales.getJSONObject(i).isNull("user")){
                                sellerName = sales.getJSONObject(i).getJSONObject("user").getString("name");
                            }else{
                                sellerName="Seller Not Found";
                            }

                            String allProductsName = allProductsSalesitemString;
                            obj.pieces = pieces + "";
                            obj.sellerName = sellerName;
                            obj.outletName = outletName;
                            obj.allproductsname = allProductsName;
                            obj.id = sales.getJSONObject(i).getString("id");
                            //Integer price = Integer.valueOf(sales.getJSONObject(i).getString("price"));
                            Integer value = Integer.valueOf(sales.getJSONObject(i).getString("value"));
                            obj.price = "KSh. " + NumberFormat.getNumberInstance(Locale.US).format(value);
                            //obj.imageDrw = drw_arr.getDrawable(obj.image);
                            items.add(obj);
                        }

                        if (items.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyView.setVisibility(View.GONE);
                        }

                        //set data and list adapter
                        mAdapter = new AdapterListSales(items);
                        recyclerView.setAdapter(mAdapter);

                        // on item list clicked
                        mAdapter.setOnItemClickListener(new AdapterListSales.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, Sale obj, int position) {
//                            //Snackbar.make(parent_view, "Item " + obj.name + " clicked", Snackbar.LENGTH_SHORT).show();
//                            Intent intent = new Intent(SalesActivity.this, IndividualSaleActivity.class);
//                            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                            //intent.putExtra("visitId", visitId);
//                            //intent.putExtra("outletId", outletId);
//                            intent.putExtra("sale", items.get(position));
//                            startActivity(intent);
                                Sale sale=items.get(position);

                                PreventDoubleClickOnLists.
                                        showActivityWithoutDoubleClick(
                                                "sale",SalesActivity.this,
                                                sale, IndividualSaleActivity.class);



                            }
                        });

//                    recyclerView.setMotionEventSplittingEnabled(false);


//                    outletsNumber.setText(outletsTotal);
//                    posAuditsNumber.setText(posAuditsTotal);
//                    salesNumber.setText(salesTotal);
//                    feedbackNumber.setText(feedbackTotal);
//                    targetSales.setText("KSh " + NumberFormat.getNumberInstance(Locale.US).format(parseInt(dailySalesTotal)));
//                    currentSales.setText("KSh " + NumberFormat.getNumberInstance(Locale.US).format(parseInt(currentSalesTarget)));


                    } else {
                        recyclerView.setVisibility(View.GONE);
                        FirebaseCrashlytics.getInstance().recordException(new Exception("Sales Error: "+result));
                        errorLoadingRelativeLayout.setVisibility(View.VISIBLE);
//                        Toasts.toastIconError(SalesActivity.this,
//                                "Does not Contain Sales");
                        //Toast.makeText(SalesActivity.this,"Does not contain Sales",Toast.LENGTH_SHORT).show();

                    }
                }
                else{
                        recyclerView.setVisibility(View.GONE);
                        FirebaseCrashlytics.getInstance().recordException(new Exception("Sales Error: "+result));
                        errorLoadingRelativeLayout.setVisibility(View.VISIBLE);
//                        Toasts.toastIconError(SalesActivity.this,"Is Null");
                    //Toast.makeText(SalesActivity.this,"Is Null",Toast.LENGTH_SHORT).show();

                    }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

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
        searchView.setQueryHint("Search by outlet name...");
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
    public static boolean containsAllWords(String word, String ...keywords) {
        for (String k : keywords)
            if (!word.contains(k)) return false;
        return true;
    }
    //searchview
    private void populateAdapter(String query) {
//        if(query.equals("")){
//            Toast.makeText(SalesActivity.this,"Nothing",Toast.LENGTH_SHORT).show();
//
//        }
//        else {

        //final ArrayList<Integer> counterGroup = new ArrayList<Integer>();
        List<Sale> newSalesList =new ArrayList<>();

        for (int counter = 0; counter < items.size(); counter++) {
            if(query.contains(" ")){
                String[] querySplit = query.toLowerCase().split("\\s+");
                String outlet_name_searched=items.get(counter).outletName.toLowerCase();
//                    Toast.makeText(SalesActivity.this,"Contains space",Toast.LENGTH_SHORT).show();


                if(containsAllWords(outlet_name_searched,querySplit)){
                    //counterGroup.add(counter);
                    newSalesList.add(items.get(counter));
//                        Toast.makeText(SalesActivity.this,outlet_name_searched+"-"+items.get(counter).name,Toast.LENGTH_SHORT).show();
                }

            }else {
                // Toast.makeText(SalesActivity.this,"No space",Toast.LENGTH_SHORT).show();
                if (items.get(counter).outletName.toLowerCase().contains(query.toLowerCase())) {
                    //mNoProductsTextView.setVisibility(View.GONE);
                    //counterGroup.add(counter);
                    newSalesList.add(items.get(counter));

                }

//                }

            }

            if (newSalesList.size()>0) {
                mNoSalesTextView.setVisibility(View.GONE);
            }else
            {
                mNoSalesTextView.setVisibility(View.VISIBLE);
            }

            if(recyclerView==null) {
                recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            }
            mAdapter = new AdapterListSales(newSalesList);
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
            mAdapter.setOnItemClickListener(new AdapterListSales.OnItemClickListener() {
                @Override
                public void onItemClick(View view, Sale obj, int position) {

                    Sale sale = newSalesList.get(position);

                    PreventDoubleClickOnLists.
                            showActivityWithoutDoubleClick(
                                    "sale",SalesActivity.this,
                                    sale, IndividualSaleActivity.class);



                }
            });
        }
    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Deals");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Tools.setSystemBarColor(this);
    }



}
package com.scg.rtmssf;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static java.lang.Integer.parseInt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.scg.rtmssf.adapters.AdapterListSales;
import com.scg.rtmssf.models.Sale;
import com.scg.rtmssf.models.SaleItem;
import com.scg.rtmssf.util.EncryptedPrefsUtil;
import com.scg.rtmssf.util.PreventDoubleClickOnLists;
import com.scg.rtmssf.util.Toasts;

import org.json.JSONArray;
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
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VisitSalesActivity extends AppCompatActivity {

    private View parent_view;

    private RecyclerView recyclerView;
    private AdapterListSales mAdapter;
    private Button clockOutButton;
    private ProgressDialog pd;

    private JSONArray sales = null;
    private Context context;
    private TypedArray drw_arr;
    private String name_arr[];
    private String mAuthToken;
//    private SharedPreferences mPrefs;
    private TextView emptyView;
    private String visitId;
    private String outletName;
    private String outletId;
    private ArrayList<SaleItem> saleItemsList;
    private long lastIntentTime = 0;
    private static final long MIN_CLICK_INTERVAL = 1000; // 1000 milliseconds


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales);
        parent_view = findViewById(android.R.id.content);

//        mPrefs = getSharedPreferences("label", 0);
//        mAuthToken = mPrefs.getString("authToken", "");
        mAuthToken = EncryptedPrefsUtil.getString("authToken", "");

        emptyView = (TextView) findViewById(R.id.empty_view);

        Intent i = getIntent();
        visitId=i.getStringExtra("visitId");
        outletId=i.getStringExtra("outletId");
        //callingActivity=i.getStringExtra("callingActivity");
        outletName=i.getStringExtra("outletName");


        //drw_arr = this.getResources().obtainTypedArray(R.array.people_images);
        //name_arr = this.getResources().getStringArray(R.array.people_names);

        initToolbar();
        initComponent();

//        new JsonTask().execute( Constants.BASE_URL + "api/visitsales","POST",visitId);

        new TokenManager(VisitSalesActivity.this,  (success, result) -> {
            if (success) {
                // Handle the JSON result, update UI, etc.
                System.out.println("API Result: " + result);

                handleResult(result);

            } else {
                // Handle errors (e.g., token refresh failed)
                Toasts.toastIconError(VisitSalesActivity.this, result);
//                Intent intent = new Intent(VisitSalesActivity.this, LoginActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
            }
        }).executeRequest(Constants.BASE_URL +"api/visitsales", "POST",
                "visitId",visitId);




    }
    private void handleResult(String result){

        if(pd != null && pd.isShowing() && !isFinishing()){
            pd.dismiss();
        }

        try {

            System.out.println("Result: "+result);
            if (result != null && result.contains("sales")) {
                JSONObject jsonObject = new JSONObject(result);
                //txtJson.setText(jsonObject.toString());
                List<Sale> items = new ArrayList<>();

                if (jsonObject.has("sales") &&
                        jsonObject.get("sales") instanceof JSONArray) {

                    sales = jsonObject.getJSONArray("sales");
                    System.out.println("Sales length: " + sales.length());

                    for (int i = 0; i < sales.length(); i++) {
                        Sale obj = new Sale();
                        //obj.image = drw_arr.getResourceId(i, -1);

                        JSONArray saleItems = sales.getJSONObject(i).getJSONArray("saleitems");

                        String allProductsSalesitemString = "";

                        String firstsaleItemProductName = "";

                        if(!saleItems.getJSONObject(0).isNull("product")){
                            firstsaleItemProductName =
                                    saleItems.getJSONObject(0).getJSONObject("product")
                                            .getString("name");
                        }else{
                            firstsaleItemProductName = "Product Not Found";
                        }

                        String firstsaleItemPieces =
                                saleItems.getJSONObject(0).getString("pieces");
                        String singleProductsSalesitemString = firstsaleItemProductName + " - " + firstsaleItemPieces;
                        if (saleItems.length() > 1) {
                            singleProductsSalesitemString += "...";
                        }


//                            for (int j = 0; j < saleItems.length(); j++) {
//                                String saleItemProductName = saleItems.getJSONObject(j).getJSONObject("product").getString("name");
//                                String saleItemPieces = saleItems.getJSONObject(j).getString("pieces");
//                                allProductsSalesitemString = allProductsSalesitemString + saleItemProductName + " - " + saleItemPieces + " ";
//
//                                System.out.println("SaleItem: " + allProductsSalesitemString);
//
//                            }

                        saleItemsList = new ArrayList<>();

                        for (int j = 0; j < saleItems.length(); j++) {
                            String saleItemProductName="";
                            if(!saleItems.getJSONObject(j).isNull("product")){
                                //obj.name = posAudits.getJSONObject(j).getJSONObject("outlet").getString("name");
                                saleItemProductName = saleItems.getJSONObject(j).
                                        getJSONObject("product").getString("name");
                            }else{
                                saleItemProductName="Product Not Found";
                            }

                            // String saleItemProductName = saleItems.getJSONObject(j).getJSONObject("product").getString("name");
                            String saleItemPieces = saleItems.getJSONObject(j).getString("pieces");
                            allProductsSalesitemString = allProductsSalesitemString + saleItemProductName + " - " + saleItemPieces + " ";

                            System.out.println("SaleItem: " + allProductsSalesitemString);

                            SaleItem saleItem = new SaleItem();

                            if(!saleItems.getJSONObject(j).isNull("product")){
                                saleItem.productName = saleItems.getJSONObject(j).
                                        getJSONObject("product").getString("name");
                            }
                            else{
                                saleItem.productName = saleItems.getJSONObject(j).
                                        getJSONObject("product").getString("name");
                            }
                            saleItem.pieces = saleItems.getJSONObject(j).getString("pieces");
                            saleItem.price = saleItems.getJSONObject(j).getString("price");
                            saleItem.value = saleItems.getJSONObject(j).getString("value");
                            saleItemsList.add(saleItem);

                        }

                        System.out.println("SaleItems: " + saleItems.length());
                        obj.name = singleProductsSalesitemString;
                        obj.allproductsname = allProductsSalesitemString;
                        obj.saleItems = saleItemsList;

                        //obj.name = sales.getJSONObject(i).getJSONObject("product").getString("name");
                        String createdAt = sales.getJSONObject(i).getString("created_at");
                        ZonedDateTime dateTime = Instant.parse(createdAt).atZone(ZoneId.of("Africa/Nairobi"));
                        String formatted = dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy \nHH:mm "));
                        obj.time = formatted;

                        String status = sales.getJSONObject(i).getString("status");
                        obj.status=status.substring(0, 1).toUpperCase() + status.substring(1);



                        Integer pieces = Integer.valueOf(sales.getJSONObject(i).getString("pieces"));
                        String outletName="";
                        if(!sales.getJSONObject(i).isNull("outlet")){
                            //obj.name = posAudits.getJSONObject(j).getJSONObject("outlet").getString("name");
                            outletName = sales.getJSONObject(i).getJSONObject("outlet").getString("name");
                        }else{
                            outletName="Outlet Not Found";
                        }
                        //String sellerName = sales.getJSONObject(i).getJSONObject("user").getString("name");
                        //String outletName = sales.getJSONObject(i).getJSONObject("outlet").getString("name");
                        String allProductsName = allProductsSalesitemString;
                        obj.pieces = pieces + "";
                        //obj.sellerName = sellerName;
                        obj.outletName = outletName;
                        obj.allproductsname = allProductsName;
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
                            //Snackbar.make(parent_view, "Item " + obj.name + " clicked", Snackbar.LENGTH_SHORT).show();
//                                Intent intent = new Intent(VisitSalesActivity.this, IndividualSaleActivity.class);
                            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            //intent.putExtra("visitId", visitId);
                            //intent.putExtra("outletId", outletId);
//                                intent.putExtra("sale", items.get(position));
//                                startActivity(intent);
                            Sale sale=items.get(position);

                            PreventDoubleClickOnLists.
                                    showActivityWithoutDoubleClick(
                                            "sale",VisitSalesActivity.this,
                                            sale, IndividualSaleActivity.class);
                        }
                    });



//                    outletsNumber.setText(outletsTotal);
//                    posAuditsNumber.setText(posAuditsTotal);
//                    salesNumber.setText(salesTotal);
//                    feedbackNumber.setText(feedbackTotal);
//                    targetSales.setText("KSh " + NumberFormat.getNumberInstance(Locale.US).format(parseInt(dailySalesTotal)));
//                    currentSales.setText("KSh " + NumberFormat.getNumberInstance(Locale.US).format(parseInt(currentSalesTarget)));


                }else{
                    FirebaseCrashlytics.getInstance().recordException(new Exception("VisitSales Error: "+result));
                    Intent intent = new Intent(VisitSalesActivity.this, ErrorLoadingActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("callingActivity", "VisitSales");

                    //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
                    startActivity(intent);
                    finish();
                }
            }else{
                FirebaseCrashlytics.getInstance().recordException(new Exception("VisitSales Error: "+result));
                Intent intent = new Intent(VisitSalesActivity.this, ErrorLoadingActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("callingActivity", "VisitSales");

                //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
                startActivity(intent);
                finish();
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

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

            pd = new ProgressDialog(VisitSalesActivity.this);

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

                Uri.Builder builder = new Uri.Builder()
                            .appendQueryParameter("visitId", params[2]);

                String query = builder.build().getEncodedQuery();

                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                System.out.println("Params " + params[2]);

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
                        List<Sale> items = new ArrayList<>();

                    if (jsonObject.has("sales") &&
                            jsonObject.get("sales") instanceof JSONArray) {

                        sales = jsonObject.getJSONArray("sales");
                        System.out.println("Sales length: " + sales.length());

                        for (int i = 0; i < sales.length(); i++) {
                            Sale obj = new Sale();
                            //obj.image = drw_arr.getResourceId(i, -1);

                            JSONArray saleItems = sales.getJSONObject(i).getJSONArray("saleitems");

                            String allProductsSalesitemString = "";

                            String firstsaleItemProductName = "";

                            if(!saleItems.getJSONObject(0).isNull("product")){
                                firstsaleItemProductName =
                                        saleItems.getJSONObject(0).getJSONObject("product")
                                                .getString("name");
                            }else{
                                firstsaleItemProductName = "Product Not Found";
                            }

                            String firstsaleItemPieces =
                                    saleItems.getJSONObject(0).getString("pieces");
                            String singleProductsSalesitemString = firstsaleItemProductName + " - " + firstsaleItemPieces;
                            if (saleItems.length() > 1) {
                                singleProductsSalesitemString += "...";
                            }


//                            for (int j = 0; j < saleItems.length(); j++) {
//                                String saleItemProductName = saleItems.getJSONObject(j).getJSONObject("product").getString("name");
//                                String saleItemPieces = saleItems.getJSONObject(j).getString("pieces");
//                                allProductsSalesitemString = allProductsSalesitemString + saleItemProductName + " - " + saleItemPieces + " ";
//
//                                System.out.println("SaleItem: " + allProductsSalesitemString);
//
//                            }

                            saleItemsList = new ArrayList<>();

                            for (int j = 0; j < saleItems.length(); j++) {
                                String saleItemProductName="";
                                if(!saleItems.getJSONObject(j).isNull("product")){
                                    //obj.name = posAudits.getJSONObject(j).getJSONObject("outlet").getString("name");
                                    saleItemProductName = saleItems.getJSONObject(j).
                                            getJSONObject("product").getString("name");
                                }else{
                                    saleItemProductName="Product Not Found";
                                }

                                // String saleItemProductName = saleItems.getJSONObject(j).getJSONObject("product").getString("name");
                                String saleItemPieces = saleItems.getJSONObject(j).getString("pieces");
                                allProductsSalesitemString = allProductsSalesitemString + saleItemProductName + " - " + saleItemPieces + " ";

                                System.out.println("SaleItem: " + allProductsSalesitemString);

                                SaleItem saleItem = new SaleItem();

                                if(!saleItems.getJSONObject(j).isNull("product")){
                                    saleItem.productName = saleItems.getJSONObject(j).
                                            getJSONObject("product").getString("name");
                                }
                                else{
                                    saleItem.productName = saleItems.getJSONObject(j).
                                            getJSONObject("product").getString("name");
                                }
                                saleItem.pieces = saleItems.getJSONObject(j).getString("pieces");
                                saleItem.price = saleItems.getJSONObject(j).getString("price");
                                saleItem.value = saleItems.getJSONObject(j).getString("value");
                                saleItemsList.add(saleItem);

                            }

                            System.out.println("SaleItems: " + saleItems.length());
                            obj.name = singleProductsSalesitemString;
                            obj.allproductsname = allProductsSalesitemString;
                            obj.saleItems = saleItemsList;

                            //obj.name = sales.getJSONObject(i).getJSONObject("product").getString("name");
                            String createdAt = sales.getJSONObject(i).getString("created_at");
                            ZonedDateTime dateTime = Instant.parse(createdAt).atZone(ZoneId.of("Africa/Nairobi"));
                            String formatted = dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy \nHH:mm "));
                            obj.time = formatted;

                            String status = sales.getJSONObject(i).getString("status");
                            obj.status=status.substring(0, 1).toUpperCase() + status.substring(1);



                            Integer pieces = Integer.valueOf(sales.getJSONObject(i).getString("pieces"));
                            String outletName="";
                            if(!sales.getJSONObject(i).isNull("outlet")){
                                //obj.name = posAudits.getJSONObject(j).getJSONObject("outlet").getString("name");
                                outletName = sales.getJSONObject(i).getJSONObject("outlet").getString("name");
                            }else{
                                outletName="Outlet Not Found";
                            }
                            //String sellerName = sales.getJSONObject(i).getJSONObject("user").getString("name");
                            //String outletName = sales.getJSONObject(i).getJSONObject("outlet").getString("name");
                            String allProductsName = allProductsSalesitemString;
                            obj.pieces = pieces + "";
                            //obj.sellerName = sellerName;
                            obj.outletName = outletName;
                            obj.allproductsname = allProductsName;
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
                                //Snackbar.make(parent_view, "Item " + obj.name + " clicked", Snackbar.LENGTH_SHORT).show();
//                                Intent intent = new Intent(VisitSalesActivity.this, IndividualSaleActivity.class);
                                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                //intent.putExtra("visitId", visitId);
                                //intent.putExtra("outletId", outletId);
//                                intent.putExtra("sale", items.get(position));
//                                startActivity(intent);
                                Sale sale=items.get(position);

                                PreventDoubleClickOnLists.
                                        showActivityWithoutDoubleClick(
                                                "sale",VisitSalesActivity.this,
                                                sale, IndividualSaleActivity.class);
                            }
                        });



//                    outletsNumber.setText(outletsTotal);
//                    posAuditsNumber.setText(posAuditsTotal);
//                    salesNumber.setText(salesTotal);
//                    feedbackNumber.setText(feedbackTotal);
//                    targetSales.setText("KSh " + NumberFormat.getNumberInstance(Locale.US).format(parseInt(dailySalesTotal)));
//                    currentSales.setText("KSh " + NumberFormat.getNumberInstance(Locale.US).format(parseInt(currentSalesTarget)));


                    }else{
                        FirebaseCrashlytics.getInstance().recordException(new Exception("VisitSales Error: "+result));
                        Intent intent = new Intent(VisitSalesActivity.this, ErrorLoadingActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("callingActivity", "VisitSales");

                        //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
                        startActivity(intent);
                        finish();
                    }
                }else{
                    FirebaseCrashlytics.getInstance().recordException(new Exception("VisitSales Error: "+result));
                    Intent intent = new Intent(VisitSalesActivity.this, ErrorLoadingActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("callingActivity", "VisitSales");

                    //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
                    startActivity(intent);
                    finish();
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }





    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(outletId.equals("IndividualVisitNotRequired")){
            return false;

        }else{
            getMenuInflater().inflate(R.menu.menu_add, menu);
            return super.onCreateOptionsMenu(menu);
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_add) {
            Intent intent = new Intent(VisitSalesActivity.this, ProductsCheckListActivity.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("visitId", visitId);
            intent.putExtra("outletId", outletId);
            intent.putExtra("outletName", outletName);
            intent.putExtra("callingActivity","makeSale");

            startActivity(intent);

            //finish();
        }else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Visit Deals");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Tools.setSystemBarColor(this);
    }
}
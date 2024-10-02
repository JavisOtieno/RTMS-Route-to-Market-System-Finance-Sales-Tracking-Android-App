
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.scg.sf.adapters.AdapterListInputProducts;
import com.scg.sf.models.Product;
import com.scg.sf.util.Toasts;

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
import java.util.List;
import java.util.Locale;

public class AddSalePOSAuditActivity extends AppCompatActivity implements OnEditTextChangeListener   {
    //test checkout branch git

    private RecyclerView recyclerView;
    private ProgressDialog pd;
    private JSONArray products = null;
    private AdapterListInputProducts mAdapter;
    private String visitId;
    private String outletId;
    private String outletName;
    private String userId;
    private String mUserId;
    private String mAuthToken;
    private SharedPreferences mPrefs;
    private Button submitPosAuditButton;
    public String mCallingActivity;
    public TextView totalValueTextView;
    private long totalValue;
    private LinearLayout totalLayout;
    private List<Product> list;
    private Integer mPosition;
    private long mValue;
    private String mAction;
    private Long currentTotal;
    private boolean isTaskRunning = false;


    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_posaudit);

        Intent i = getIntent();
        visitId=i.getStringExtra("visitId");
        outletName=i.getStringExtra("outletName");
        mCallingActivity=i.getStringExtra("callingActivity");

        initToolbar();

        mPrefs = getSharedPreferences("label", 0);
        mAuthToken = mPrefs.getString("authToken", "");
        mUserId = mPrefs.getString("userId", "0");
        submitPosAuditButton = (Button) findViewById(R.id.posAuditButton);
        totalValueTextView = (TextView) findViewById(R.id.totalValueText);
        totalLayout = (LinearLayout) findViewById(R.id.totalLayout);

        totalValue=0;
        currentTotal=0L;


        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);


        if(mCallingActivity.equals("makeSale")){
            submitPosAuditButton.setText("Submit Sale");
            totalLayout.setVisibility(View.VISIBLE);
        }

        outletId =i.getStringExtra("outletId");
        outletName =i.getStringExtra("outletName");

        list = (List<Product>) i.getSerializableExtra("productList");

        //set data and list adapter

        mAdapter = new AdapterListInputProducts(this,mCallingActivity,list);

        if(mCallingActivity.equals("makeSale")){
            mAdapter = new AdapterListInputProducts(this,mCallingActivity,list,this);
        }

        recyclerView.setAdapter(mAdapter);

        //Toast.makeText(SelectProductActivity.this,"visitid: "+visitId+" outletid: "+outletId,Toast.LENGTH_SHORT).show();

        // on item list clicked
        mAdapter.setOnItemClickListener(new AdapterListInputProducts.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Product obj, int position) {
                //Snackbar.make(parent_view, "Item " + obj.name + " clicked - id"+obj.id, Snackbar.LENGTH_SHORT).show();
                System.out.println("Item clicked " + obj.name + " clicked - id" + obj.id);
//                            Intent intent = new Intent(AddPOSAuditActivity.this, MakeSaleActivity.class);
//                            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                            //Toast.makeText(SelectProductActivity.this,"visitid: "+visitId+" outletid: "+outletId,Toast.LENGTH_SHORT).show();
//                            intent.putExtra("productId", obj.id);
//                            intent.putExtra("productName", obj.name);
//                            intent.putExtra("visitId", visitId);
//                            intent.putExtra("outletId", outletId);
//                            startActivity(intent);

            }
        });






//        recyclerView.post(new Runnable() {
//            @Override
//            public void run() {
//                int childCount = recyclerView.getChildCount();
//                Log.d("RecyclerView", "Child count: " + childCount);
//                System.out.println("recyclerView: "+recyclerView.getChildCount());
//
//
//
//
//                for (int j = 0; j < recyclerView.getChildCount(); j++) {
//                    View vie = recyclerView.getChildAt(j);
//                    Product product = list.get(j);
//                    System.out.println("productList: "+product.stock);
//                    System.out.println("productId: "+product.id);
//                    String productId = product.id;
//                    EditText productCountEditText = (EditText) vie.findViewById(R.id.productCount);
//                    if(mCallingActivity.equals("makeSale")){
//                        //TextView productStockTextView = (TextView) vie.findViewById(R.id.productStock);
//                        Toast.makeText(AddSalePOSAuditActivity.this,"Stock "+product.stock, Toast.LENGTH_SHORT).show();
//                        //productStockTextView.setText("Stock : "+NumberFormat.getNumberInstance(Locale.US).format(product.stock));
//
//                    }
//
//
//                    productCountEditText.setFilters(new InputFilter[]{ new MinMaxFilter("0", product.stock+"")});
//                    //productCountEditText.setText("40");
//                    System.out.println("EditText: Registers Loop");
//
//
//
//                    productCountEditText.addTextChangedListener(new TextWatcher() {
//
//                        public void afterTextChanged(Editable s) {
//
//                            // you can call or do what you want with your EditText here
//
//                            // yourEditText...
//                            System.out.println("productCountAfterChange: "+s);
//                            //toastIconError(String.valueOf(s));
//                            Integer pieces=0;
//                            if(!s.toString().equals("")){
//                                pieces =Integer.parseInt(s.toString());
//                            }
//                            Integer value =pieces*Integer.parseInt(product.price);
//                            totalValue=totalValue+value;
//                            totalValueTextView.setText("KSh. "+ NumberFormat.getNumberInstance(Locale.US).format(totalValue));
//
//                            //);
//                        }
//
//                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                            Integer pieces=0;
//                            if(!s.toString().equals("")){
//                                pieces =Integer.parseInt(s.toString());
//                            }
//                            Integer value =pieces*Integer.parseInt(product.price);
//                            totalValue=totalValue-value;
//                            totalValueTextView.setText("KSh. "+ NumberFormat.getNumberInstance(Locale.US).format(totalValue));
//
//                        }
//
//                        public void onTextChanged(CharSequence s, int start, int before, int count) {
////                            System.out.println("productCountChange: "+s);
////                            toastIconError(String.valueOf(s));
//
//                        }
//                    });
//
//                }
//               // totalValueTextView.setText(valueTest[0]);
//
//
//            }
//        });






            submitPosAuditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isTaskRunning) {
                    isTaskRunning = true;

                String productString = "";

                System.out.println("recyclerView: "+recyclerView.getChildCount());

                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    View vie = recyclerView.getChildAt(i);
                    Product product = list.get(i);
                    String productId = product.id;
                    EditText productCountEditText = (EditText) vie.findViewById(R.id.productCount);

                    String productCount = productCountEditText.getText().toString();
                    if(productCount.equals("")){
                        Toasts.toastIconError(AddSalePOSAuditActivity.this,
                                "Missing Product Quantities");
                        break;
                    }
                    if(productString.length()==0){
                        productString = productId + "-" + productCount;
                    }else{
                        productString = productString + "_" + productId + "-" + productCount;
                    }

                    Log.d("productCount: ", productCount);


                }

                Intent i = getIntent();
                visitId = i.getStringExtra("visitId");
                userId = mUserId;
                outletId = i.getStringExtra("outletId");
                outletName = i.getStringExtra("outletName");

                String url=Constants.BASE_URL + "api/addposaudit";

                if(mCallingActivity.equals("makeSale")){
                    url=Constants.BASE_URL + "api/addsale";
                }

                new JsonTask().execute(url, "POST", productString, userId, outletId, visitId);
                //Toast.makeText(getBaseContext(), productString, Toast.LENGTH_SHORT).show();
                System.out.println("productString: " + productString);

                }

            }
        });



        //new JsonTask().execute(Constants.BASE_URL + "api/products","GET");


    }



    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String activityTitle="Add POS Audit";
        if(mCallingActivity.equals("makeSale")){
            activityTitle="Add Sale";
        }
        getSupportActionBar().setTitle(activityTitle);
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

//    @Override
//    public void onEditTextChanged(int position, String text, String action) {

    @Override
    public void onEditTextChanged(Long updatedValue, Long previousValue) {


        currentTotal = currentTotal+updatedValue-previousValue;
        totalValueTextView.setText("KSh. "+NumberFormat.getNumberInstance(Locale.US).format(currentTotal));



//        for (Product product:list) {
//            if(!(product.currentNumberOfProducts==null)){
//                currentTotal=currentTotal+
//                        product.currentNumberOfProducts*Integer.parseInt(product.price);
//            }
//        }

//        long pieces = 0;
//        if (!text.toString().equals("")) {
//            pieces = Integer.parseInt(text.toString());
//        }
//
//        String price=list.get(position).price;
//        String name=list.get(position).name;
//        long value = pieces * Long.parseLong(price);
//
//        if(mPosition!=null){
//
//            if(mPosition==position&&mValue==value&&mAction==action){
//
//            }else{
//                System.out.println("totalValuebefore: name"+name+" action"+action+" pieces "+pieces+" Price "+price+" Value "+value+" total"+totalValue);
//
//                if(action.equals("before")){
//                    totalValue = totalValue - value;
//                }else{
//                    totalValue = totalValue + value;
//                }
//                System.out.println("totalValueafter: name"+name+" action"+action+" pieces "+pieces+" Price "+price+" Value "+value+" total"+totalValue);
//
//
//            }
//
//        }


//        mPosition=position;
//        mValue=value;
//        mAction=action;


    }


    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(AddSalePOSAuditActivity.this);

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
                connection.setRequestProperty("Content-Type", "application/json");

                if (requestMethod.equals("POST")) {
//                    Uri.Builder builder = new Uri.Builder()
//                            .appendQueryParameter("products", params[2])
//                            .appendQueryParameter("userId", params[3])
//                            .appendQueryParameter("outletId", params[4])
//                            .appendQueryParameter("visitId", params[5]);

//                    String query = builder.build().getEncodedQuery();
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("products", params[2]);
                    jsonParam.put("userId", params[3]);
                    jsonParam.put("outletId", params[4]);
                    jsonParam.put("visitId", params[5]);


                    OutputStream os = connection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
//                    writer.write(query);
                    writer.write(jsonParam.toString());
                    writer.flush();
                    writer.close();
                    os.close();

                    System.out.println("Params " + params[2]);
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

            isTaskRunning=false;
            System.out.println("Result: " + result);
            try {

                if (result != null) {

                    JSONObject jsonObject = new JSONObject(result);
                    //txtJson.setText(jsonObject.toString());
                    //Add POS Failed means server returns nothing
                    //Error saving means server responds with something other than success or errors

                    if(result.contains("errors")){
                        String message = jsonObject.getString("message");
                        Toasts.toastIconError(AddSalePOSAuditActivity.this,message);
                        //Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                    }
                    else if(result.contains("success")){
                        Intent intent = new Intent(AddSalePOSAuditActivity.this, GenerateOrderNote.class);
                        // Intent intent = new Intent(AddSalePOSAuditActivity.this, SelectActionActivity.class);

                        if (mCallingActivity.equals("addPOSAudit")) {
                            //toastIconError(mCallingActivity);
                            String posAuditId = jsonObject.getString("posAuditId");
                            intent = new Intent(AddSalePOSAuditActivity.this, TakePhotoActivity.class);
                            intent.putExtra("callingActivity", "POSAudit");
                            intent.putExtra("posAuditId",posAuditId);
                        }else{
                            String saleId = jsonObject.getString("saleId");
                            intent.putExtra("saleId",saleId);
//                            intent.putExtra("lastUpdatedActivity","sale");
                        }
                        intent.putExtra("visitId",visitId);
                        intent.putExtra("outletName",outletName);
                        intent.putExtra("outletId",outletId);
                        startActivity(intent);
                    }else{
                        Toasts.toastIconError(AddSalePOSAuditActivity.this,
                                "Error Saving. Please Try Again");
                        FirebaseCrashlytics.getInstance().recordException(new Exception("Add "+mCallingActivity+" Error: "+result));

                    }



                }else
                {
                    Toasts.toastIconError(AddSalePOSAuditActivity.this,
                            "Submit POS Audit Failed. Please Try Again");
                    FirebaseCrashlytics.getInstance().recordException(new Exception("Add "+mCallingActivity+" Error: "+result));

                }


            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
    }

}
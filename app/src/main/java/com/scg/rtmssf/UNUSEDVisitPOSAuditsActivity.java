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
import com.scg.rtmssf.adapters.AdapterListPOSAudits;
import com.scg.rtmssf.models.POSAudit;
import com.scg.rtmssf.models.POSAuditItem;
import com.scg.rtmssf.util.EncryptedPrefsUtil;
import com.scg.rtmssf.util.PreventDoubleClickOnLists;

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
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class UNUSEDVisitPOSAuditsActivity extends AppCompatActivity {

    private View parent_view;

    private RecyclerView recyclerView;
    private AdapterListPOSAudits mAdapter;
    private Button clockOutButton;
    private ProgressDialog pd;

    private JSONArray posAudits = null;
    private Context context;
    private TypedArray drw_arr;
    private String name_arr[];
    private String mAuthToken;
//    private SharedPreferences mPrefs;
    private TextView emptyView;
    private String visitId;
    private String outletName;
    private String outletId;
    private ArrayList<POSAuditItem> posAuditItemsList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_posaudits_list);
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

        new JsonTask().execute( Constants.BASE_URL + "api/visitposaudits","POST",visitId);


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

            pd = new ProgressDialog(UNUSEDVisitPOSAuditsActivity.this);

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
                if (result != null && result.contains("posaudits")) {
                        JSONObject jsonObject = new JSONObject(result);
                        //txtJson.setText(jsonObject.toString());
                        List<POSAudit> items = new ArrayList<>();
                    if (jsonObject.has("posaudits") &&
                            jsonObject.get("posaudits") instanceof JSONArray) {


                        posAudits = jsonObject.getJSONArray("posaudits");
                        System.out.println("POSAudits length: " + posAudits.length());


                        for (int i = 0; i < posAudits.length(); i++) {
                            POSAudit obj = new POSAudit();
                            //obj.image = drw_arr.getResourceId(i, -1);
                            if(!posAudits.getJSONObject(i).isNull("outlet")){
                                obj.name = posAudits.getJSONObject(i).getJSONObject("outlet").getString("name");
                                obj.price = posAudits.getJSONObject(i).getJSONObject("outlet").getString("contact_person_name");

                            }else{
                                obj.name="Outlet Not Found";
                                obj.price = "Not Found";
                            }

                            //obj.name = posAudits.getJSONObject(i).getJSONObject("outlet").getString("name");
                            String createdAt = posAudits.getJSONObject(i).getString("created_at");
                            ZonedDateTime dateTime = Instant.parse(createdAt).atZone(ZoneId.of("Africa/Nairobi"));
                            String formatted = dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy \nHH:mm "));
                            obj.time = formatted;
                            obj.image = posAudits.getJSONObject(i).getString("image");
                            JSONArray posAuditItems = posAudits.getJSONObject(i).getJSONArray("posaudititems");
                            String posAuditItemString = "";

                            posAuditItemsList = new ArrayList<>();

                            for (int j = 0; j < posAuditItems.length(); j++) {

                                String posAuditItemProductName="";

                                if(!posAuditItems.getJSONObject(j).isNull("product")){
                                    //obj.name = posAudits.getJSONObject(j).getJSONObject("outlet").getString("name");
                                    posAuditItemProductName = posAuditItems.getJSONObject(j).getJSONObject("product").getString("name");
                                }else{
                                    posAuditItemProductName="Product Not Found";
                                }

                                String posAuditItemPieces = posAuditItems.getJSONObject(j).getString("pieces");
                                if (j > 1) {
                                    posAuditItemString = posAuditItemString + "...";
                                    break;
                                } else {
                                    posAuditItemString = posAuditItemString + posAuditItemProductName + " - " + posAuditItemPieces + "\n";
                                }

                                POSAuditItem posAuditItem = new POSAuditItem();
                                if(!posAuditItems.getJSONObject(j).isNull("product")){
                                    //obj.name = posAudits.getJSONObject(j).getJSONObject("outlet").getString("name");

                                    posAuditItem.productName = posAuditItems.getJSONObject(j).
                                            getJSONObject("product").getString("name");
                                }else{
                                    posAuditItem.productName = "Product Not Found";
                                }

                                posAuditItem.pieces = posAuditItems.getJSONObject(j).getString("pieces");
                                posAuditItemsList.add(posAuditItem);

                                System.out.println("POSAuditItem: " + posAuditItemString);

                            }

                            System.out.println("POSAuditItems: " + posAuditItems.length());
                            obj.price = posAuditItemString;
                            obj.posAuditItems = posAuditItemsList;

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
                        mAdapter = new AdapterListPOSAudits(items);
                        recyclerView.setAdapter(mAdapter);

                        // on item list clicked
                        mAdapter.setOnItemClickListener(new AdapterListPOSAudits.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, POSAudit obj, int position) {

                                //Snackbar.make(parent_view, "Item " + obj.name + " clicked", Snackbar.LENGTH_SHORT).show();
//                                Intent intent = new Intent(VisitPOSAuditsActivity.this, IndividualPOSAuditActivity.class);
                                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                //intent.putExtra("visitId", visitId);
                                //intent.putExtra("outletId", outletId);
//                                intent.putExtra("posAudit", items.get(position));
//                                startActivity(intent);
                                POSAudit posAudit = items.get(position);
                                PreventDoubleClickOnLists.
                                        showActivityWithoutDoubleClick(
                                                "posAudit", UNUSEDVisitPOSAuditsActivity.this,
                                                posAudit, UNUSEDIndividualPOSAuditActivity.class);


                            }
                        });


//                    outletsNumber.setText(outletsTotal);
//                    posAuditsNumber.setText(posAuditsTotal);
//                    salesNumber.setText(salesTotal);
//                    feedbackNumber.setText(feedbackTotal);
//                    targetSales.setText("KSh " + NumberFormat.getNumberInstance(Locale.US).format(parseInt(dailySalesTotal)));
//                    currentSales.setText("KSh " + NumberFormat.getNumberInstance(Locale.US).format(parseInt(currentSalesTarget)));


                    }else{
                    FirebaseCrashlytics.getInstance().recordException(new Exception("VisitPOSAudits Error: "+result));

                    Intent intent = new Intent(UNUSEDVisitPOSAuditsActivity.this, ErrorLoadingActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("callingActivity", "VisitPOSAudits");

                        //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
                        startActivity(intent);
                        finish();
                    }
                }else{
                FirebaseCrashlytics.getInstance().recordException(new Exception("VisitPOSAudits Error: "+result));

                Intent intent = new Intent(UNUSEDVisitPOSAuditsActivity.this, ErrorLoadingActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("callingActivity", "VisitPOSAudits");

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
            Intent intent = new Intent(UNUSEDVisitPOSAuditsActivity.this, ProductsCheckListActivity.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("visitId", visitId);
            intent.putExtra("outletId", outletId);
            intent.putExtra("outletName", outletName);
            intent.putExtra("callingActivity","addPOSAudit");

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
        getSupportActionBar().setTitle("Visit POS Audits");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Tools.setSystemBarColor(this);
    }
}
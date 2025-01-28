package com.scg.rtmssf;

import static java.lang.Integer.parseInt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.scg.rtmssf.adapters.AdapterListSales;
import com.scg.rtmssf.models.Sale;
import com.scg.rtmssf.models.SaleItem;
import com.scg.rtmssf.util.EncryptedPrefsUtil;
import com.scg.rtmssf.util.PreventDoubleClickOnLists;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.scg.rtmssf.util.Toasts;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class DealsFragment extends Fragment  {

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
    private View view;

    private static final String ARG_URL = "arg_url";
    private String endpointUrl;

    // Use this factory method to create a new instance of this fragment
    public static DealsFragment newInstance(String url) {
        DealsFragment fragment = new DealsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url); // Put the URL in the bundle
        fragment.setArguments(args); // Set the arguments to the fragment
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_sales);
        view = inflater.inflate(R.layout.fragment_deals, container, false);
        parent_view = view.findViewById(android.R.id.content);

        if (getArguments() != null) {
            endpointUrl = getArguments().getString(ARG_URL);
        }

//        mPrefs = getActivity().getSharedPreferences("label", 0);
//        mUserId = EncryptedPrefsUtil.getString("userId","0");
        mAuthToken = EncryptedPrefsUtil.getString("authToken", "");
//        mAuthToken = mPrefs.getString("authToken", "");

        //drw_arr = this.getResources().obtainTypedArray(R.array.people_images);
        //name_arr = this.getResources().getStringArray(R.array.people_names);
        emptyView = (TextView) view.findViewById(R.id.empty_view);
        errorLoadingRelativeLayout = view.findViewById(R.id.errorLoading);
        mNoSalesTextView = (TextView) view.findViewById(R.id.noSalesText);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);


        context = getContext();

//        initToolbar();
//        initComponent();

        lyt_no_connection = (LinearLayout) view.findViewById(R.id.lyt_no_connection);
        //lyt_no_connection.setVisibility(View.VISIBLE);

        lyt_no_connection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                //   lyt_no_connection.setVisibility(View.GONE);
//                new JsonTask().execute(Constants.BASE_URL + endpointUrl,"GET");
                showDealsFromApi();

            }
        });

        showDealsFromApi();


//        new JsonTask().execute(Constants.BASE_URL + endpointUrl,"GET");
//        fetchData(mAuthToken);

        return view;

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Enable the fragment to have its own menu options
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);

        SearchManager searchManager = (SearchManager) requireContext().getSystemService(Context.SEARCH_SERVICE);

        // Update this cast to use androidx.appcompat.widget.SearchView
        androidx.appcompat.widget.SearchView searchView =
                (androidx.appcompat.widget.SearchView) menu.findItem(R.id.action_search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
        searchView.setQueryHint("Search by outlet name...");

        // Set the search query listener directly in the fragment
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // Handle submit if needed
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                filterData(newText);
                populateAdapter(newText);
//                Toasts.toastIconError(getContext(),newText);
                return true;
            }
        });
    }

    private void showDealsFromApi(){
        new TokenManager(context,  (success, result) -> {
            if (success) {
                // Handle the JSON result, update UI, etc.
                System.out.println("API Result: " + result);

                handleResult(result);

            } else {
                // Handle errors (e.g., token refresh failed)
                Toasts.toastIconError(context, result);
//                Intent intent = new Intent(context, LoginActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
            }
        }).executeRequest(Constants.BASE_URL +endpointUrl, "GET");

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
                recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
            }
            mAdapter = new AdapterListSales(newSalesList);
            recyclerView.setAdapter(mAdapter);
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
                                    "sale",getContext(),
                                    sale, IndividualSaleActivity.class);



                }
            });
        }
    }
    public static boolean containsAllWords(String word, String ...keywords) {
        for (String k : keywords)
            if (!word.contains(k)) return false;
        return true;
    }




    private void fetchData(String token) {
        ApiService apiService = ApiClient.getInstance().getApiService();

        // Call the API
//        Call<MyResponse> call = apiService.getData("application/json", "Bearer " + token);
        Call<ResponseBody> call = apiService.getData("Bearer " + token,"application/json");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
//                    MyResponse myResponse = response.body();

                    // Handle the response data
                    Toasts.toastIconSuccess(getContext(),"we got something");
                    Log.d("API Response", response.body().toString());

                    try {
                        // Get the response body as a string
                        String responseBody = response.body().string();

                        // Create a JSONObject from the response string
                        JSONObject jsonObject = new JSONObject(responseBody);

                        // Extract values from the JSON object
                        JSONArray sales = jsonObject.getJSONArray("sales");
                        String value = sales.getJSONObject(0).getString("value");
//                        String name = jsonObject.getString("name");

                        // Show a toast with the message
                        Toasts.toastIconSuccess(getContext(), value);

                        // Log the values for debugging
                        Log.d("API Response", "Message: " + value + ", Name: " + value);
                    } catch (Exception e) {
                        Log.e("API Response", "JSON Parsing error: " + e.getMessage());
                    }

                } else {
                    // Handle the error
                    Log.e("API Error", "Response code: " + response.code());
                    Log.e("API Error", "Response code: " + response.body());
                    Toasts.toastIconError(getContext(),"We got nothing");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Handle the failure
                Log.e("API Failure", t.getMessage());

                // Check if the throwable is an instance of HttpException
                if (t instanceof HttpException) {
                    HttpException httpException = (HttpException) t;
                    int responseCode = httpException.code();

                    // Log the HTTP status code
                    Log.e("API Failure", "Response code: " + responseCode);

                    // Try to extract the error body (if present)
                    try {
                        Response<?> response = httpException.response();
                        if (response != null && response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("API Failure", "Error body: " + errorBody);
                        }
                    } catch (IOException e) {
                        Log.e("API Failure", "Error reading error body: " + e.getMessage());
                    }
                } else {
                    // Log the stack trace for other types of exceptions
                    Log.e("API Failure", "Unknown error: ", t);
                }

            }
        });
    }

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        // Do not save specific data that you want to prevent from being retained
//    }

    private void handleResult(String result){

        if(pd != null && pd.isShowing()){
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
                        if(value==0){
                            value = Integer.valueOf(sales.getJSONObject(i).getString("pieces"));
                            obj.price = NumberFormat.getNumberInstance(Locale.US).format(value)+" units";
                        }
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
                                            "sale",getContext(),
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



//    private class JsonTask extends AsyncTask<String, String, String> {
//
//        protected void onPreExecute() {
//            super.onPreExecute();
//
//            pd = new ProgressDialog(getContext());
//
//            pd.setMessage("Please wait");
//            pd.setCancelable(false);
//            pd.show();
//
//        }
//
//        protected String doInBackground(String... params) {
//
//
//            HttpURLConnection connection = null;
//            BufferedReader reader = null;
//
//            try {
//
//                URL url = new URL(params[0]);
//                String requestMethod = params[1];
//
//                connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod(requestMethod);
//                connection.setRequestProperty("Accept","application/json");
//                connection.setRequestProperty("Authorization", "Bearer " + mAuthToken);
//
//                connection.connect();
//
//                InputStream stream;
//
//                if (connection.getResponseCode() == 200) {
//                    stream = connection.getInputStream();
//
//                } else {
//                    /* error from server */
//                    //_is = httpConn.getErrorStream();
//                    stream = connection.getErrorStream();
//
//                }
//
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
//
//
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//                Log.e(TAG,"Malf Message");
//            } catch (IOException e) {
//                Log.e("Message here: ",e.getMessage());
//                e.printStackTrace();
//            } finally {
//                if (connection != null) {
//                    connection.disconnect();
//                }
//                try {
//                    if (reader != null) {
//                        reader.close();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                // Dismiss the dialog on the UI thread
////                runOnUiThread(() -> {
////                    if (pd != null && pd.isShowing() && !isFinishing()) {
////                        pd.dismiss();
////                    }
////                });
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            super.onPostExecute(result);
//
//            if(pd != null && pd.isShowing()){
//                pd.dismiss();
//            }
//
//            try {
//
//                System.out.println("Result: "+result);
//                if (result != null && result.contains("sales")) {
//                    JSONObject jsonObject = new JSONObject(result);
//                    //txtJson.setText(jsonObject.toString());
//
//                    if (jsonObject.has("sales") &&
//                            jsonObject.get("sales") instanceof JSONArray) {
//
//                        items = new ArrayList<>();
//
//                        sales = jsonObject.getJSONArray("sales");
//
//                        System.out.println("Sales length: " + sales.length());
//
//
//                        for (int i = 0; i < sales.length(); i++) {
//                            Sale obj = new Sale();
//                            //obj.image = drw_arr.getResourceId(i, -1);
//
//                            JSONArray saleItems = sales.getJSONObject(i).getJSONArray("saleitems");
//
//                            String allProductsSalesitemString = "";
//
//                            String firstsaleItemProductName =
//                                    saleItems.getJSONObject(0).getJSONObject("product")
//                                            .getString("name");
//                            String firstsaleItemPieces =
//                                    saleItems.getJSONObject(0).getString("pieces");
//                            String singleProductsSalesitemString = firstsaleItemProductName + " - " + firstsaleItemPieces;
//
//                            saleItemsList = new ArrayList<>();
//
//                            for (int j = 0; j < saleItems.length(); j++) {
//
//                                String saleItemProductName = saleItems.getJSONObject(j).getJSONObject("product").getString("name");
//                                String saleItemPieces = saleItems.getJSONObject(j).getString("pieces");
//                                allProductsSalesitemString = allProductsSalesitemString + saleItemProductName + " - " + saleItemPieces + " ";
//
//                                System.out.println("SaleItem: " + allProductsSalesitemString);
//
//                                SaleItem saleItem = new SaleItem();
//                                saleItem.productName = saleItems.getJSONObject(j).getJSONObject("product").getString("name");
//                                saleItem.pieces = saleItems.getJSONObject(j).getString("pieces");
//                                saleItem.price = saleItems.getJSONObject(j).getString("price");
//                                saleItem.value = saleItems.getJSONObject(j).getString("value");
//                                saleItemsList.add(saleItem);
//
//                            }
//                            if (saleItems.length() > 1) {
//                                singleProductsSalesitemString += "...";
//                            }
//
//
//                            System.out.println("SaleItems: " + saleItems.length());
//                            obj.name = singleProductsSalesitemString;
//                            obj.allproductsname = allProductsSalesitemString;
//                            obj.saleItems = saleItemsList;
//                            //saleItemsList.clear();
//
//
//                            //obj.name = sales.getJSONObject(i).getJSONObject("product").getString("name");
//                            String createdAt = sales.getJSONObject(i).getString("created_at");
//                            ZonedDateTime dateTime = Instant.parse(createdAt).atZone(ZoneId.of("Africa/Nairobi"));
//                            String formatted = dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy \nHH:mm "));
//                            obj.time = formatted;
//
//
//                            String status = sales.getJSONObject(i).getString("status");
//                            obj.status=status.substring(0, 1).toUpperCase() + status.substring(1);
//
//                            Integer pieces = Integer.valueOf(sales.getJSONObject(i).getString("pieces"));
//
//                            String outletName = "";
//                            String sellerName = "";
//
//                            if(!sales.getJSONObject(i).isNull("outlet")){
//                                outletName = sales.getJSONObject(i).getJSONObject("outlet").getString("name");
//                            }else{
//                                outletName="Outlet Not Found";
//                            }
//
//                            if(!sales.getJSONObject(i).isNull("user")){
//                                sellerName = sales.getJSONObject(i).getJSONObject("user").getString("name");
//                            }else{
//                                sellerName="Seller Not Found";
//                            }
//
//                            String allProductsName = allProductsSalesitemString;
//                            obj.pieces = pieces + "";
//                            obj.sellerName = sellerName;
//                            obj.outletName = outletName;
//                            obj.allproductsname = allProductsName;
//                            obj.id = sales.getJSONObject(i).getString("id");
//                            //Integer price = Integer.valueOf(sales.getJSONObject(i).getString("price"));
//                            Integer value = Integer.valueOf(sales.getJSONObject(i).getString("value"));
//                            obj.price = "KSh. " + NumberFormat.getNumberInstance(Locale.US).format(value);
//                            //obj.imageDrw = drw_arr.getDrawable(obj.image);
//                            items.add(obj);
//                        }
//
//                        if (items.isEmpty()) {
//                            recyclerView.setVisibility(View.GONE);
//                            emptyView.setVisibility(View.VISIBLE);
//                        } else {
//                            recyclerView.setVisibility(View.VISIBLE);
//                            emptyView.setVisibility(View.GONE);
//                        }
//
//                        //set data and list adapter
//                        mAdapter = new AdapterListSales(items);
//                        recyclerView.setAdapter(mAdapter);
//
//                        // on item list clicked
//                        mAdapter.setOnItemClickListener(new AdapterListSales.OnItemClickListener() {
//                            @Override
//                            public void onItemClick(View view, Sale obj, int position) {
////                            //Snackbar.make(parent_view, "Item " + obj.name + " clicked", Snackbar.LENGTH_SHORT).show();
////                            Intent intent = new Intent(SalesActivity.this, IndividualSaleActivity.class);
////                            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
////                            //intent.putExtra("visitId", visitId);
////                            //intent.putExtra("outletId", outletId);
////                            intent.putExtra("sale", items.get(position));
////                            startActivity(intent);
//                                Sale sale=items.get(position);
//
//                                PreventDoubleClickOnLists.
//                                        showActivityWithoutDoubleClick(
//                                                "sale",getContext(),
//                                                sale, IndividualSaleActivity.class);
//
//
//
//                            }
//                        });
//
////                    recyclerView.setMotionEventSplittingEnabled(false);
//
//
////                    outletsNumber.setText(outletsTotal);
////                    posAuditsNumber.setText(posAuditsTotal);
////                    salesNumber.setText(salesTotal);
////                    feedbackNumber.setText(feedbackTotal);
////                    targetSales.setText("KSh " + NumberFormat.getNumberInstance(Locale.US).format(parseInt(dailySalesTotal)));
////                    currentSales.setText("KSh " + NumberFormat.getNumberInstance(Locale.US).format(parseInt(currentSalesTarget)));
//
//
//                    } else {
//                        recyclerView.setVisibility(View.GONE);
//                        FirebaseCrashlytics.getInstance().recordException(new Exception("Sales Error: "+result));
//                        errorLoadingRelativeLayout.setVisibility(View.VISIBLE);
////                        Toasts.toastIconError(SalesActivity.this,
////                                "Does not Contain Sales");
//                        //Toast.makeText(SalesActivity.this,"Does not contain Sales",Toast.LENGTH_SHORT).show();
//
//                    }
//                }
//                else{
//                    recyclerView.setVisibility(View.GONE);
//                    FirebaseCrashlytics.getInstance().recordException(new Exception("Sales Error: "+result));
//                    errorLoadingRelativeLayout.setVisibility(View.VISIBLE);
////                        Toasts.toastIconError(SalesActivity.this,"Is Null");
//                    //Toast.makeText(SalesActivity.this,"Is Null",Toast.LENGTH_SHORT).show();
//
//                }
//
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            }
//
//        }
//
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                onBackPressed();
//                return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_search, menu);
//
//        SearchManager searchManager =
//                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView =
//                (SearchView) menu.findItem(R.id.action_search).getActionView();
//        searchView.setSearchableInfo(
//                searchManager.getSearchableInfo(getComponentName()));
//        searchView.setQueryHint("Search by outlet name...");
//        AutoCompleteTextView searchTextView = (AutoCompleteTextView) searchView.findViewById(androidx.appcompat.R.id.search_src_text);
//        try {
//            @SuppressLint("SoonBlockedPrivateApi") Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
//            mCursorDrawableRes.setAccessible(true);
//            mCursorDrawableRes.set(searchTextView, R.drawable.cursor); //This sets the cursor resource ID to 0 or @null which will make it visible on white background
//        } catch (Exception e) {
//
//        }
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String s) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String s) {
//                populateAdapter(s);
//                return false;
//            }
//        });
//
//        return true;
//    }
//    public static boolean containsAllWords(String word, String ...keywords) {
//        for (String k : keywords)
//            if (!word.contains(k)) return false;
//        return true;
//    }
//    private void initToolbar() {
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setTitle("Deals");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        //Tools.setSystemBarColor(this);
//    }


    //searchview
//    private void populateAdapter(String query) {
////        if(query.equals("")){
////            Toast.makeText(SalesActivity.this,"Nothing",Toast.LENGTH_SHORT).show();
////
////        }
////        else {
//
//        //final ArrayList<Integer> counterGroup = new ArrayList<Integer>();
//        List<Sale> newSalesList =new ArrayList<>();
//
//        for (int counter = 0; counter < items.size(); counter++) {
//            if(query.contains(" ")){
//                String[] querySplit = query.toLowerCase().split("\\s+");
//                String outlet_name_searched=items.get(counter).outletName.toLowerCase();
////                    Toast.makeText(SalesActivity.this,"Contains space",Toast.LENGTH_SHORT).show();
//
//
//                if(containsAllWords(outlet_name_searched,querySplit)){
//                    //counterGroup.add(counter);
//                    newSalesList.add(items.get(counter));
////                        Toast.makeText(SalesActivity.this,outlet_name_searched+"-"+items.get(counter).name,Toast.LENGTH_SHORT).show();
//                }
//
//            }else {
//                // Toast.makeText(SalesActivity.this,"No space",Toast.LENGTH_SHORT).show();
//                if (items.get(counter).outletName.toLowerCase().contains(query.toLowerCase())) {
//                    //mNoProductsTextView.setVisibility(View.GONE);
//                    //counterGroup.add(counter);
//                    newSalesList.add(items.get(counter));
//
//                }
//
////                }
//
//            }
//
//            if (newSalesList.size()>0) {
//                mNoSalesTextView.setVisibility(View.GONE);
//            }else
//            {
//                mNoSalesTextView.setVisibility(View.VISIBLE);
//            }
//
//            if(recyclerView==null) {
//                recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
//            }
//            mAdapter = new AdapterListSales(newSalesList);
//            recyclerView.setAdapter(mAdapter);
////            recyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
////                @Override
////                public void onItemClick(AdapterView<?> parent, View v,
////                                        int position, long id) {
////
////                    // Sending image id to FullScreenActivity
////                    Intent i = new Intent(MainActivity.this, DisplayProductsActivity.class);
////                    // passing array index
////                    i.putExtra("productClicked",productsList.get(counterGroup.get(position)) );
////                    startActivity(i);
////
////                }
////            });
//            mAdapter.setOnItemClickListener(new AdapterListSales.OnItemClickListener() {
//                @Override
//                public void onItemClick(View view, Sale obj, int position) {
//
//                    Sale sale = newSalesList.get(position);
//
//                    PreventDoubleClickOnLists.
//                            showActivityWithoutDoubleClick(
//                                    "sale",getContext(),
//                                    sale, IndividualSaleActivity.class);
//
//
//
//                }
//            });
//        }
//    }
}
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
import android.content.SharedPreferences;
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
import com.scg.rtmssf.adapters.AdapterListFollowup;
import com.scg.rtmssf.models.Followup;
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

public class FollowupsFragment extends Fragment  {

    private View parent_view;

    private RecyclerView recyclerView;
    private AdapterListFollowup mAdapter;
    private Button clockOutButton;
    private ProgressDialog pd;

    private JSONArray followup = null;
    private List<Followup> items;

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
    private TextView mNoFollowupTextView;
    private View view;

    private static final String ARG_URL = "arg_url";
    private String endpointUrl;




    // Use this factory method to create a new instance of this fragment
    public static FollowupsFragment newInstance(String url) {
        FollowupsFragment fragment = new FollowupsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url); // Put the URL in the bundle
        fragment.setArguments(args); // Set the arguments to the fragment
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_followups);
        view = inflater.inflate(R.layout.fragment_followups, container, false);
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
        mNoFollowupTextView = (TextView) view.findViewById(R.id.noFollowupText);
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
                showFollowupsFromApi();

            }
        });

        showFollowupsFromApi();


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
        inflater.inflate(R.menu.menu_search_add, menu);

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

    private void showFollowupsFromApi(){
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
//
//        }
//        else {

        //final ArrayList<Integer> counterGroup = new ArrayList<Integer>();
        List<Followup> newFollowupList=new ArrayList<>();

        for (int counter = 0; counter < items.size(); counter++) {
            if(query.contains(" ")){
                String[] querySplit = query.toLowerCase().split("\\s+");
                String product_name_searched=items.get(counter).created_outlet.toLowerCase();


                if(containsAllWords(product_name_searched,querySplit)){
                    //counterGroup.add(counter);
                    newFollowupList.add(items.get(counter));
                }

            }else {
                if (items.get(counter).created_outlet.toLowerCase().contains(query.toLowerCase())) {
                    //mNoProductsTextView.setVisibility(View.GONE);
                    //counterGroup.add(counter);
                    newFollowupList.add(items.get(counter));

                }

            }

        }

        if (newFollowupList.size()>0) {
            mNoFollowupTextView.setVisibility(View.GONE);
        }else
        {
            mNoFollowupTextView.setVisibility(View.VISIBLE);
        }

        if(recyclerView==null) {
            recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        }
        mAdapter = new AdapterListFollowup(newFollowupList);
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
        mAdapter.setOnItemClickListener(new AdapterListFollowup.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Followup obj, int position) {

                Followup followup = newFollowupList.get(position);


                PreventDoubleClickOnLists.
                        showActivityWithoutDoubleClick(
                                "followup", getContext(),
                                followup, IndividualFollowupActivity.class);



            }
        });
        //    }
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
                        JSONArray followups = jsonObject.getJSONArray("followups");
                        String value = followups.getJSONObject(0).getString("value");
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
        try {

            System.out.println("Result: " + result);
            if (result != null && result.contains("followup")){
                JSONObject jsonObject = new JSONObject(result);
                //txtJson.setText(jsonObject.toString());
                items = new ArrayList<>();

                if (jsonObject.has("followup") &&
                        jsonObject.get("followup") instanceof JSONArray) {


                    followup = jsonObject.getJSONArray("followup");
                    System.out.println("Outlets length: "+followup.length());

                    for (int i = 0; i < followup.length(); i++) {
                        Followup obj = new Followup();
                        //obj.image = drw_arr.getResourceId(i, -1);
                        obj.description = followup.getJSONObject(i).getString("description");
                        String createdAt = followup.getJSONObject(i).getString("created_at");
                        ZonedDateTime dateTime = Instant.parse(createdAt).atZone(ZoneId.of("Africa/Nairobi"));
                        String formatted = dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy \nHH:mm "));
                        obj.created_at = formatted;
                        obj.created_outlet = "";
                        obj.status = followup.getJSONObject(i).getString("status");
                        obj.date = followup.getJSONObject(i).getString("date");
                        obj.time = followup.getJSONObject(i).getString("time");
                        obj.type = followup.getJSONObject(i).getString("type");
                        if(!followup.getJSONObject(i).isNull("outlet")){
                            obj.created_outlet = followup.getJSONObject(i).
                                    getJSONObject("outlet").getString("name");
                        }else{
                            obj.created_outlet="Outlet Not Found";
                        }

                        //    obj.created_outlet = followup.getJSONObject(i).getJSONObject("outlet").getString("name");

                        obj.id = followup.getJSONObject(i).getString("id");
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
                    mAdapter = new AdapterListFollowup(items);
                    recyclerView.setAdapter(mAdapter);

                    // on item list clicked
                    mAdapter.setOnItemClickListener(new AdapterListFollowup.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, Followup obj, int position) {

                            Followup followup = items.get(position);


                            PreventDoubleClickOnLists.
                                    showActivityWithoutDoubleClick(
                                            "followup",getContext(),
                                            followup, IndividualFollowupActivity.class);


                        }
                    });

                }else {
                    FirebaseCrashlytics.getInstance().recordException(new Exception("FollowupList Error: "+result));
                    Intent intent = new Intent(getContext(), ErrorLoadingActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("callingActivity", "Followup");

                    //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
                    startActivity(intent);
                    getActivity().finish();
                }

            }else{
                FirebaseCrashlytics.getInstance().recordException(new Exception("FollowupList Error: "+result));
                Intent intent = new Intent(getContext(), ErrorLoadingActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("callingActivity", "Followup");

                //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ProductsListActivity.this).toBundle());
                startActivity(intent);
                getActivity().finish();
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
//                if (result != null && result.contains("followups")) {
//                    JSONObject jsonObject = new JSONObject(result);
//                    //txtJson.setText(jsonObject.toString());
//
//                    if (jsonObject.has("followups") &&
//                            jsonObject.get("followups") instanceof JSONArray) {
//
//                        items = new ArrayList<>();
//
//                        followups = jsonObject.getJSONArray("followups");
//
//                        System.out.println("Followup length: " + followups.length());
//
//
//                        for (int i = 0; i < followups.length(); i++) {
//                            Followup obj = new Followup();
//                            //obj.image = drw_arr.getResourceId(i, -1);
//



//                            JSONArray saleItems = followups.getJSONObject(i).getJSONArray("saleitems");
//
//                            String allProductsFollowupitemString = "";
//
//                            String firstsaleItemProductName =
//                                    saleItems.getJSONObject(0).getJSONObject("product")
//                                            .getString("name");
//                            String firstsaleItemPieces =
//                                    saleItems.getJSONObject(0).getString("pieces");
//                            String singleProductsFollowupitemString = firstsaleItemProductName + " - " + firstsaleItemPieces;
//
//                            saleItemsList = new ArrayList<>();
//
//                            for (int j = 0; j < saleItems.length(); j++) {
//
//                                String saleItemProductName = saleItems.getJSONObject(j).getJSONObject("product").getString("name");
//                                String saleItemPieces = saleItems.getJSONObject(j).getString("pieces");
//                                allProductsFollowupitemString = allProductsFollowupitemString + saleItemProductName + " - " + saleItemPieces + " ";
//
//                                System.out.println("FollowupItem: " + allProductsFollowupitemString);
//
//                                FollowupItem saleItem = new FollowupItem();
//                                saleItem.productName = saleItems.getJSONObject(j).getJSONObject("product").getString("name");
//                                saleItem.pieces = saleItems.getJSONObject(j).getString("pieces");
//                                saleItem.price = saleItems.getJSONObject(j).getString("price");
//                                saleItem.value = saleItems.getJSONObject(j).getString("value");
//                                saleItemsList.add(saleItem);
//
//                            }
//                            if (saleItems.length() > 1) {
//                                singleProductsFollowupitemString += "...";
//                            }
//
//
//                            System.out.println("FollowupItems: " + saleItems.length());
//                            obj.name = singleProductsFollowupitemString;
//                            obj.allproductsname = allProductsFollowupitemString;
//                            obj.saleItems = saleItemsList;
//                            //saleItemsList.clear();
//
//
//                            //obj.name = followups.getJSONObject(i).getJSONObject("product").getString("name");
//                            String createdAt = followups.getJSONObject(i).getString("created_at");
//                            ZonedDateTime dateTime = Instant.parse(createdAt).atZone(ZoneId.of("Africa/Nairobi"));
//                            String formatted = dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy \nHH:mm "));
//                            obj.time = formatted;
//
//
//                            String status = followups.getJSONObject(i).getString("status");
//                            obj.status=status.substring(0, 1).toUpperCase() + status.substring(1);
//
//                            Integer pieces = Integer.valueOf(followups.getJSONObject(i).getString("pieces"));
//
//                            String outletName = "";
//                            String sellerName = "";
//
//                            if(!followups.getJSONObject(i).isNull("outlet")){
//                                outletName = followups.getJSONObject(i).getJSONObject("outlet").getString("name");
//                            }else{
//                                outletName="Outlet Not Found";
//                            }
//
//                            if(!followups.getJSONObject(i).isNull("user")){
//                                sellerName = followups.getJSONObject(i).getJSONObject("user").getString("name");
//                            }else{
//                                sellerName="Seller Not Found";
//                            }
//
//                            String allProductsName = allProductsFollowupitemString;
//                            obj.pieces = pieces + "";
//                            obj.sellerName = sellerName;
//                            obj.outletName = outletName;
//                            obj.allproductsname = allProductsName;
//                            obj.id = followups.getJSONObject(i).getString("id");
//                            //Integer price = Integer.valueOf(followups.getJSONObject(i).getString("price"));
//                            Integer value = Integer.valueOf(followups.getJSONObject(i).getString("value"));
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
//                        mAdapter = new AdapterListFollowup(items);
//                        recyclerView.setAdapter(mAdapter);
//
//                        // on item list clicked
//                        mAdapter.setOnItemClickListener(new AdapterListFollowup.OnItemClickListener() {
//                            @Override
//                            public void onItemClick(View view, Followup obj, int position) {
////                            //Snackbar.make(parent_view, "Item " + obj.name + " clicked", Snackbar.LENGTH_SHORT).show();
////                            Intent intent = new Intent(FollowupActivity.this, IndividualFollowupActivity.class);
////                            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
////                            //intent.putExtra("visitId", visitId);
////                            //intent.putExtra("outletId", outletId);
////                            intent.putExtra("sale", items.get(position));
////                            startActivity(intent);
//                                Followup sale=items.get(position);
//
//                                PreventDoubleClickOnLists.
//                                        showActivityWithoutDoubleClick(
//                                                "sale",getContext(),
//                                                sale, IndividualFollowupActivity.class);
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
////                    followupsNumber.setText(followupsTotal);
////                    feedbackNumber.setText(feedbackTotal);
////                    targetFollowup.setText("KSh " + NumberFormat.getNumberInstance(Locale.US).format(parseInt(dailyFollowupTotal)));
////                    currentFollowup.setText("KSh " + NumberFormat.getNumberInstance(Locale.US).format(parseInt(currentFollowupTarget)));
//
//
//                    } else {
//                        recyclerView.setVisibility(View.GONE);
//                        FirebaseCrashlytics.getInstance().recordException(new Exception("Followup Error: "+result));
//                        errorLoadingRelativeLayout.setVisibility(View.VISIBLE);
////                        Toasts.toastIconError(FollowupActivity.this,
////                                "Does not Contain Followup");
//                        //Toast.makeText(FollowupActivity.this,"Does not contain Followup",Toast.LENGTH_SHORT).show();
//
//                    }
//                }
//                else{
//                    recyclerView.setVisibility(View.GONE);
//                    FirebaseCrashlytics.getInstance().recordException(new Exception("Followup Error: "+result));
//                    errorLoadingRelativeLayout.setVisibility(View.VISIBLE);
////                        Toasts.toastIconError(FollowupActivity.this,"Is Null");
//                    //Toast.makeText(FollowupActivity.this,"Is Null",Toast.LENGTH_SHORT).show();
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
////            Toast.makeText(FollowupActivity.this,"Nothing",Toast.LENGTH_SHORT).show();
////
////        }
////        else {
//
//        //final ArrayList<Integer> counterGroup = new ArrayList<Integer>();
//        List<Followup> newFollowupList =new ArrayList<>();
//
//        for (int counter = 0; counter < items.size(); counter++) {
//            if(query.contains(" ")){
//                String[] querySplit = query.toLowerCase().split("\\s+");
//                String outlet_name_searched=items.get(counter).outletName.toLowerCase();
////                    Toast.makeText(FollowupActivity.this,"Contains space",Toast.LENGTH_SHORT).show();
//
//
//                if(containsAllWords(outlet_name_searched,querySplit)){
//                    //counterGroup.add(counter);
//                    newFollowupList.add(items.get(counter));
////                        Toast.makeText(FollowupActivity.this,outlet_name_searched+"-"+items.get(counter).name,Toast.LENGTH_SHORT).show();
//                }
//
//            }else {
//                // Toast.makeText(FollowupActivity.this,"No space",Toast.LENGTH_SHORT).show();
//                if (items.get(counter).outletName.toLowerCase().contains(query.toLowerCase())) {
//                    //mNoProductsTextView.setVisibility(View.GONE);
//                    //counterGroup.add(counter);
//                    newFollowupList.add(items.get(counter));
//
//                }
//
////                }
//
//            }
//
//            if (newFollowupList.size()>0) {
//                mNoFollowupTextView.setVisibility(View.GONE);
//            }else
//            {
//                mNoFollowupTextView.setVisibility(View.VISIBLE);
//            }
//
//            if(recyclerView==null) {
//                recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
//            }
//            mAdapter = new AdapterListFollowup(newFollowupList);
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
//            mAdapter.setOnItemClickListener(new AdapterListFollowup.OnItemClickListener() {
//                @Override
//                public void onItemClick(View view, Followup obj, int position) {
//
//                    Followup sale = newFollowupList.get(position);
//
//                    PreventDoubleClickOnLists.
//                            showActivityWithoutDoubleClick(
//                                    "sale",getContext(),
//                                    sale, IndividualFollowupActivity.class);
//
//
//
//                }
//            });
//        }
//    }
}
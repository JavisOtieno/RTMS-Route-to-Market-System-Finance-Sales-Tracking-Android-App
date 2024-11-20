package com.scg.rtmssf;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.scg.rtmssf.models.RouteplanItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapRoutePlanActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    private ArrayList<RouteplanItem> routeplanItems;
    private MapView mapView;
    private List<LatLng> waypoints;
    private LatLng marker1;
    private LatLng marker2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map_route_plan);
        initToolbar();

        Intent intent=getIntent();
        routeplanItems = (ArrayList<RouteplanItem>) intent.getSerializableExtra("routeplanitems");

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Route Plan Map");
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

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.mMap = googleMap;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        waypoints = new ArrayList<>();
        List<Marker> markers = new ArrayList<>();


        for (int i = 0; i < routeplanItems.size(); i++) {
            RouteplanItem routeplanItem = routeplanItems.get(i);
            LatLng point = new LatLng(Double.parseDouble(routeplanItem.outletLat),
                    Double.parseDouble(routeplanItem.outletLong));
            if(i==0){
            marker1 = new LatLng(Double.parseDouble(routeplanItem.outletLat),
                    Double.parseDouble(routeplanItem.outletLong));
           }else if(i==routeplanItems.size()-1 ){
                marker2 = new LatLng(Double.parseDouble(routeplanItem.outletLat),
                        Double.parseDouble(routeplanItem.outletLong));
            }else{
                waypoints.add(new LatLng(Double.parseDouble(routeplanItem.outletLat),
                        Double.parseDouble(routeplanItem.outletLong)));
            }

            String name = routeplanItem.outletName;

            Marker marker = mMap.addMarker(new MarkerOptions().position(point).title(name).snippet("Outlet Number:"+(i+1)));
            markers.add(marker);
            builder.include(point);

            showCustomInfoWindow(MapRoutePlanActivity.this, marker);
        }


        LatLngBounds bounds = builder.build();
        int padding = 50; // Offset from edges of the map in pixels
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

        if(routeplanItems.size()>1){
            getDirections(marker1, marker2, waypoints);
        }else if(routeplanItems.size()==1){
//            Toasts.toastIconError(MapRoutePlanActivity.this,"Just one");
        }else{
            FirebaseCrashlytics.getInstance().
                    recordException(new Exception("MapRoutePlanActivity error:" +
                            "No Points on RTMS Route Plan Map"));

        }



        // Show the info window for each marker
//        for (Marker marker : markers) {
//            marker.showInfoWindow();
//        }


//        // Get directions
//        getDirections(marker1, marker2);
    }
    private void getDirections(LatLng origin, LatLng dest, List<LatLng> waypoints) {
        StringBuilder waypointsParam = new StringBuilder();
        for (LatLng point : waypoints) {
            if (waypointsParam.length() > 0) {
                waypointsParam.append("|");
            }
            waypointsParam.append(point.latitude).append(",").append(point.longitude);
        }

        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin.latitude + "," + origin.longitude
                + "&destination=" + dest.latitude + "," + dest.longitude
                + "&waypoints=" + waypointsParam.toString()
                + "&key=AIzaSyALLsNWwOC09xsRAqrK0S7dINi6BpNc7iw";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        JSONArray routes = jsonObject.getJSONArray("routes");
                        if (routes.length() > 0) {
                            JSONObject route = routes.getJSONObject(0);
                            JSONArray legs = route.getJSONArray("legs");
                            if (legs.length() > 0) {
                                JSONObject leg = legs.getJSONObject(0);
                                JSONObject distance = leg.getJSONObject("distance");
                                int distanceInMeters = distance.getInt("value");

                                System.out.println("distanceInMeters: "+distanceInMeters);

                                // Check if the distance is greater than 50 meters

                                if (distanceInMeters > 50) {
                                    runOnUiThread(() -> drawRoute(jsonResponse));
                                }else{
                                System.out.println("RouteDistance"+
                                        " Route not drawn. " +
                                                "Distance is less than or equal to 50 meters.");

                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    runOnUiThread(() -> drawRoute(jsonResponse));
                }
            }
        });


    }



    private void showCustomInfoWindow(Context context, Marker marker) {
        View infoWindow = LayoutInflater.from(this).inflate(R.layout.custom_info_window, null);

        TextView title = infoWindow.findViewById(R.id.title);
        TextView snippet = infoWindow.findViewById(R.id.snippet);

        title.setText(marker.getTitle());
        snippet.setText(marker.getSnippet());

        // Create a marker at the same position with a custom info window layout
        mMap.addMarker(new MarkerOptions()
                .position(marker.getPosition())
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(context, infoWindow)))
                .anchor(0.5f, 2.0f)
        )
        ;
    }

    private Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    private void drawRoute(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray routes = jsonObject.getJSONArray("routes");
            JSONObject route = routes.getJSONObject(0);

            if (routes.length() > 0) {

                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                String encodedPolyline = overviewPolyline.getString("points");

                List<LatLng> points = decodePoly(encodedPolyline);
                PolylineOptions polylineOptions = new PolylineOptions().addAll(points).color(Color.BLUE).width(5);
                mMap.addPolyline(polylineOptions);
                // Proceed with processing the route
            } else {
                // Handle the case when there are no routes available
                Log.e("MapRoutePlanActivity", "No routes found in the response.");
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }


}
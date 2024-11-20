package com.scg.rtmssf;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.scg.rtmssf.adapters.AdapterListRoutePlanItems;
import com.scg.rtmssf.models.Routeplan;
import com.scg.rtmssf.models.RouteplanItem;

public class IndividualRouteplanActivity extends AppCompatActivity {

    private  Routeplan mRouteplan;
    private EditText mRouteplanNameText;
    private EditText mRouteplanDateText;
    private RecyclerView recyclerView;
    private AdapterListRoutePlanItems mAdapter;
    private Button viewOnMapsButton;
    private Button viewOnRTMSButton;
    private LatLng origin;
    private LatLng destination;
    private String waypoints;
    private String originName;
    private String destinationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_individual_routeplan);

        initToolbar();

        viewOnMapsButton = (Button)findViewById(R.id.viewOnMapsButton);
        viewOnRTMSButton = (Button)findViewById(R.id.viewOnRTMSMapButton);

        mRouteplan=(Routeplan)getIntent().getSerializableExtra("routeplan");

        mRouteplanNameText = (EditText) findViewById(R.id.routeplanNameText);
        mRouteplanDateText=(EditText) findViewById(R.id.scheduledDateText);

        mRouteplanNameText.setText(mRouteplan.name);
        mRouteplanDateText.setText(mRouteplan.scheduledDate);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(IndividualRouteplanActivity.this));
        recyclerView.setHasFixedSize(true);
        waypoints="";

        for(int i=0;i<mRouteplan.routeplanItems.size();i++){
            //set origin, waypoints and endpoint
            RouteplanItem routeplanItem = mRouteplan.routeplanItems.get(i);
            if(i==0){
                origin = new LatLng(Double.parseDouble(routeplanItem.outletLat),
                        Double.parseDouble(routeplanItem.outletLong));
                originName = routeplanItem.outletName;
               // Toast.makeText(IndividualRouteplanActivity.this,origin.toString(),Toast.LENGTH_SHORT).show();
            }else if(i==(mRouteplan.routeplanItems.size()-1)){
                destination = new LatLng(Double.parseDouble(routeplanItem.outletLat),
                        Double.parseDouble(routeplanItem.outletLong));
                destinationName = routeplanItem.outletName;
            }else if(i==1){
                waypoints=waypoints+routeplanItem.outletLat + "," +
                        routeplanItem.outletLong;;
            }else if(i==(mRouteplan.routeplanItems.size()-2)){
                waypoints=waypoints+"|"+routeplanItem.outletLat + "," +
                        routeplanItem.outletLong;
            }else{
                waypoints=waypoints+"|"+routeplanItem.outletLat + "," +
                        routeplanItem.outletLong;
            }
        }
        System.out.println("waypoints"+waypoints);

        viewOnMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strUri="";

                if(mRouteplan.routeplanItems.size()==1){
                    strUri = "http://maps.google.com/maps?q=loc:" +
                            origin.latitude + "," + origin.longitude + " (" + originName + ")";

                }else{
                    strUri = "https://www.google.com/maps/dir/?api=1" +
                            "&origin=" + origin.latitude + "," + origin.longitude +
                            "&destination=" + destination.latitude + "," + destination.longitude +
                            "&waypoints=" + waypoints;
                }



                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));
                intent.setPackage("com.google.android.apps.maps");

                startActivity(intent);

            }
        });

        viewOnRTMSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(IndividualRouteplanActivity.this,MapRoutePlanActivity.class);
                intent.putExtra("routeplanitems",mRouteplan.routeplanItems);
                startActivity(intent);
            }
        });


        //set data and list adapter
        mAdapter = new AdapterListRoutePlanItems(mRouteplan.routeplanItems);
        System.out.println("RouteplanItemName: "+mRouteplan.routeplanItems.get(0).outletName);
        recyclerView.setAdapter(mAdapter);

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }

    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Route Plan");
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

}
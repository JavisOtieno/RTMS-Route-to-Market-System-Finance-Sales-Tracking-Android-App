package com.scg.sf;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.scg.sf.adapters.AdapterListPOSAuditItems;
import com.scg.sf.models.POSAudit;
import com.scg.sf.models.POSAuditItem;
import com.squareup.picasso.Picasso;

public class IndividualPOSAuditActivity extends AppCompatActivity {
    private POSAudit mPOSAudit;
    private String mOutletName;
    private String mSellerName;
    private String mPieces;
    private String mValue;
    private String mProducts;
    private EditText mOutletNameText;
    private EditText mValueText;
    private EditText mProductsText;
    private EditText mDateTimeText;
    private ImageView productImageView;

    private RecyclerView recyclerView;
    private AdapterListPOSAuditItems mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_posaudit);

        initToolbar();

        mPOSAudit=(POSAudit)getIntent().getSerializableExtra("posAudit");
        mOutletNameText = (EditText) findViewById(R.id.outletNameText);
        mProductsText=(EditText) findViewById(R.id.productsText);
        mDateTimeText=(EditText) findViewById(R.id.dateTimeText);
        productImageView = (ImageView) findViewById(R.id.outletImageView);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(IndividualPOSAuditActivity.this));
        recyclerView.setHasFixedSize(true);


        //set data and list adapter
        mAdapter = new AdapterListPOSAuditItems(mPOSAudit.posAuditItems);
        recyclerView.setAdapter(mAdapter);

        // on item list clicked
        mAdapter.setOnItemClickListener(new AdapterListPOSAuditItems.OnItemClickListener() {
            @Override
            public void onItemClick(View view, POSAuditItem obj, int position) {
                //Toast.makeText(getBaseContext(),"Test",Toast.LENGTH_SHORT).show();
                //Snackbar.make(parent_view, "Item " + obj.name + " clicked", Snackbar.LENGTH_SHORT).show();
                //Intent intent = new Intent(SelectActionActivity.this, IndividualSaleActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                //intent.putExtra("visitId", visitId);
                //intent.putExtra("outletId", outletId);
                //intent.putExtra("activity", activities.get(position));
                //startActivity(intent);

            }
        });

        System.out.println("ImageUrl: "+Constants.STORAGE_URL+mPOSAudit.image);

        Picasso.get()
                .load(Constants.STORAGE_URL+mPOSAudit.image)
//                .placeholder(R.drawable.placeholder) // Optional placeholder image
                .error(R.drawable.no_image_available) // Optional error image
                .into(productImageView);


        mDateTimeText.setText(mPOSAudit.time);
        mProductsText.setText(mPOSAudit.price);
        mOutletNameText.setText(mPOSAudit.name);

        mDateTimeText.setEnabled(false);
        mProductsText.setEnabled(false);
        mOutletNameText.setEnabled(false);



    }

    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("POS Audit");
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
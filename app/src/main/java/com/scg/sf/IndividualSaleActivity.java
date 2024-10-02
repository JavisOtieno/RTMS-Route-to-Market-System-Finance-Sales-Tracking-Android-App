package com.scg.sf;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.scg.sf.adapters.AdapterListSaleItems;
import com.scg.sf.models.Sale;
import com.scg.sf.models.SaleItem;

public class IndividualSaleActivity extends AppCompatActivity {
    private Sale mSale;
    private String mOutletName;
    private String mSellerName;
    private String mPieces;
    private String mValue;
    private String mProducts;
    private EditText mOutletNameText;
    private EditText mPiecesText;
    private EditText mValueText;
    private EditText mStatusText;
    private EditText mProductsText;
    private EditText mDateTimeText;

    private RecyclerView recyclerView;
    private AdapterListSaleItems mAdapter;
    private LinearLayout mPiecesLayout;
    private LinearLayout mValueLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_sale);

        initToolbar();

        mSale=(Sale)getIntent().getSerializableExtra("sale");
        mPiecesText=(EditText) findViewById(R.id.piecesText);
        mValueText=(EditText) findViewById(R.id.valueText);
        mOutletNameText=(EditText) findViewById(R.id.outletNameText);
        mProductsText=(EditText) findViewById(R.id.productsText);
        mDateTimeText=(EditText) findViewById(R.id.dateTimeText);
        mStatusText=(EditText) findViewById(R.id.statusText);

        mPiecesLayout = (LinearLayout) findViewById(R.id.piecesLayout);
        mValueLayout = (LinearLayout) findViewById(R.id.valueLayout);

        mDateTimeText.setText(mSale.time);

       // mProductsText.setText(mSale.allproductsname);
        mOutletNameText.setText(mSale.outletName);
        mValueText.setText(mSale.price);
        mPiecesText.setText(mSale.pieces);
        mStatusText.setText(mSale.status);

        mPiecesText.setEnabled(false);
        mDateTimeText.setEnabled(false);
        mValueText.setEnabled(false);
       // mProductsText.setEnabled(false);
        mOutletNameText.setEnabled(false);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(IndividualSaleActivity.this));
        recyclerView.setHasFixedSize(true);


        //set data and list adapter
        mAdapter = new AdapterListSaleItems(mSale.saleItems);
        recyclerView.setAdapter(mAdapter);

        // on item list clicked
        mAdapter.setOnItemClickListener(new AdapterListSaleItems.OnItemClickListener() {
            @Override
            public void onItemClick(View view, SaleItem obj, int position) {
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




    }

    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Deal");
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
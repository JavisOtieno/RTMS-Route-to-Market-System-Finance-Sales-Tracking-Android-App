package com.scg.rtmssf;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SalesFragment extends Fragment {

    private RecyclerView recyclerView;
    private SalesAdapter adapter;
    private List<SaleItem> saleItemList;

    public SalesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_sales, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Load sales data
        saleItemList = loadSalesData();

        // Set adapter for RecyclerView
        adapter = new SalesAdapter(saleItemList);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private List<SaleItem> loadSalesData() {
        // Dummy data for sales items (you can fetch this from an API or database)
        List<SaleItem> items = new ArrayList<>();
        items.add(new SaleItem("Deal 1", "$100"));
        items.add(new SaleItem("Deal 2", "$200"));
        items.add(new SaleItem("Deal 3", "$150"));

        return items;
    }

    // RecyclerView Adapter for Sales Items
    private class SalesAdapter extends RecyclerView.Adapter<SalesAdapter.SalesViewHolder> {

        private List<SaleItem> saleItems;

        public SalesAdapter(List<SaleItem> saleItems) {
            this.saleItems = saleItems;
        }

        @Override
        public SalesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sale_list, parent, false);
            return new SalesViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SalesViewHolder holder, int position) {
            SaleItem item = saleItems.get(position);
            holder.title.setText(item.getTitle());
            holder.price.setText(item.getPrice());

            holder.itemView.setOnClickListener(v -> {
                Toast.makeText(getActivity(), "Clicked: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return saleItems.size();
        }

        // ViewHolder class for sales items
        public class SalesViewHolder extends RecyclerView.ViewHolder {

            public TextView title, price;

            public SalesViewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.name);
                price = itemView.findViewById(R.id.outletNameTextView);
            }
        }
    }

    // Model class for sales items
    private class SaleItem {
        private String title;
        private String price;

        public SaleItem(String title, String price) {
            this.title = title;
            this.price = price;
        }

        public String getTitle() {
            return title;
        }

        public String getPrice() {
            return price;
        }
    }
}




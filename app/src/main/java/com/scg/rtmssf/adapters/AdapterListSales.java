package com.scg.rtmssf.adapters;


import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.scg.rtmssf.R;
import com.scg.rtmssf.models.Sale;

import java.util.ArrayList;
import java.util.List;

public class AdapterListSales extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Sale> items = new ArrayList<>();

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Sale obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }



    public AdapterListSales(Context context, List<Sale> items) {
        this.items = items;
        ctx = context;
    }
    public AdapterListSales(List<Sale> items) {
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView name;
        public TextView outletNameTextView;
        public TextView date;
        public TextView value;
        public TextView price;
        public View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            name = (TextView) v.findViewById(R.id.name);
            outletNameTextView = (TextView) v.findViewById(R.id.outletNameTextView);
            date = (TextView) v.findViewById(R.id.description);
            price = (TextView) v.findViewById(R.id.price);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sale_list, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;

            Sale p = items.get(position);
            view.name.setText(p.outletName);
            view.date.setText(p.status);
            view.price.setText(p.time);
            view.outletNameTextView.setText(p.price);

            //Tools.displayImageRound(ctx, view.image, p.image);

            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, items.get(position), position);
                    }
                }
            });
//            view.lyt_parent.setOnClickListener(new DebouncedOnClickListener() {
//                @Override
//                public void onDebouncedClick(View view) {
//                    // Handle click event
//                    if (mOnItemClickListener != null) {
//                        mOnItemClickListener.onItemClick(view, items.get(position), position);
//                    }
//                }
//            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}

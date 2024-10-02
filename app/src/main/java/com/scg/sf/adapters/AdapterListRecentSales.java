package com.scg.sf.adapters;


import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.scg.sf.R;
import com.scg.sf.models.Sale;

import java.util.ArrayList;
import java.util.List;

public class AdapterListRecentSales extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Sale> items = new ArrayList<>();

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Sale obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListRecentSales(Context context, List<Sale> items) {
        this.items = items;
        ctx = context;
    }
    public AdapterListRecentSales(List<Sale> items) {
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {

        public TextView outletName;
        public TextView saleValue;
        public TextView saleDate;
        public View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            outletName = (TextView) v.findViewById(R.id.outletName);
            saleDate = (TextView) v.findViewById(R.id.saleDate);
            saleValue = (TextView) v.findViewById(R.id.saleValue);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recentsales_item, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;

            Sale p = items.get(position);
            view.outletName.setText(p.outletName);
            view.saleDate.setText(p.time);
            view.saleValue.setText(p.status.substring(0, 1).toUpperCase() + p.status.substring(1));

            //Tools.displayImageRound(ctx, view.image, p.image);
            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, items.get(position), position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}

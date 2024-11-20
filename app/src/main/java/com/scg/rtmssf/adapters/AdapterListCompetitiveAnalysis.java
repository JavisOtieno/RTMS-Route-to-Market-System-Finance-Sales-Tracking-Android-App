package com.scg.rtmssf.adapters;


import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.scg.rtmssf.models.CompetitiveAnalysis;
import com.scg.rtmssf.R;

import java.util.ArrayList;
import java.util.List;

public class AdapterListCompetitiveAnalysis extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CompetitiveAnalysis> items = new ArrayList<>();

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, CompetitiveAnalysis obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListCompetitiveAnalysis(Context context, List<CompetitiveAnalysis> items) {
        this.items = items;
        ctx = context;
    }
    public AdapterListCompetitiveAnalysis(List<CompetitiveAnalysis> items) {
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView name;
        public TextView date;
        public TextView value;
        public TextView price;
        public View lyt_parent;
        public TextView brandVisibility;
        public TextView pricing;

        public OriginalViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            name = (TextView) v.findViewById(R.id.name);
            brandVisibility = (TextView) v.findViewById(R.id.brandVisibility);
            pricing = (TextView) v.findViewById(R.id.pricing);
            price = (TextView) v.findViewById(R.id.price);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_competitive_analysis, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;

            CompetitiveAnalysis p = items.get(position);
            view.name.setText(p.created_outlet);
            view.brandVisibility.setText(p.brandVisibility);
            view.price.setText(p.created_at);
            view.pricing.setText(p.pricing);

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

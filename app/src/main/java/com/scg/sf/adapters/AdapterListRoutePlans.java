package com.scg.sf.adapters;


import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.scg.sf.R;
import com.scg.sf.models.Routeplan;

import java.util.ArrayList;
import java.util.List;

public class AdapterListRoutePlans extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Routeplan> items = new ArrayList<>();

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Routeplan obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListRoutePlans(Context context, List<Routeplan> items) {
        this.items = items;
        ctx = context;
    }
    public AdapterListRoutePlans(List<Routeplan> items) {
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView name;
        public TextView outletsPreview;
        public TextView value;
        public TextView scheduledDate;
        public View lyt_parent;
//        public TextView outletNameTextView;

        public OriginalViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            name = (TextView) v.findViewById(R.id.name);
            outletsPreview = (TextView) v.findViewById(R.id.description);
//            outletNameTextView = (TextView) v.findViewById(R.id.outletNameTextView);
            scheduledDate = (TextView) v.findViewById(R.id.price);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_routeplan, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;

            Routeplan routeplan = items.get(position);
            view.name.setText(routeplan.name);
            view.outletsPreview.setText(routeplan.routeplanItemsPreview);
            view.scheduledDate.setText(routeplan.scheduledDate);
//            view.outletNameTextView.setText(p.name);

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

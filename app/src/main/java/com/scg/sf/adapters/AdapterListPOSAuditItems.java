package com.scg.sf.adapters;

import static java.lang.Integer.parseInt;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


import com.scg.sf.R;
import com.scg.sf.models.POSAuditItem;

import java.util.ArrayList;
import java.util.List;

public class AdapterListPOSAuditItems extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<POSAuditItem> items = new ArrayList<>();

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, POSAuditItem obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListPOSAuditItems(Context context, List<POSAuditItem> items) {
        this.items = items;
        ctx = context;
    }
    public AdapterListPOSAuditItems(List<POSAuditItem> items) {
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public EditText productNameText;
        public EditText piecesText;

        public View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            productNameText = (EditText) v.findViewById(R.id.productNameText);
            piecesText = (EditText) v.findViewById(R.id.piecesText);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_posaudit_item, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;

            POSAuditItem posAuditItem = items.get(position);
            view.productNameText.setText(posAuditItem.productName);
            view.piecesText.setText(posAuditItem.pieces);

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



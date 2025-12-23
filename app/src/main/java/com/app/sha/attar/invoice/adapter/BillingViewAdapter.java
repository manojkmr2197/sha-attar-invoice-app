package com.app.sha.attar.invoice.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.listener.BillingClickListener;
import com.app.sha.attar.invoice.listener.ClickListener;
import com.app.sha.attar.invoice.model.BillingItemModel;
import com.app.sha.attar.invoice.model.ProductModel;
import com.app.sha.attar.invoice.viewholder.BillingViewHolder;
import com.app.sha.attar.invoice.viewholder.ProductViewHolder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class BillingViewAdapter extends RecyclerView.Adapter<BillingViewHolder> {

    Context context;
    List<BillingItemModel> contentList = new ArrayList<>();
    BillingClickListener clickListener;

    DecimalFormat df = new DecimalFormat("#.00");

    public BillingViewAdapter(Context context, List<BillingItemModel> contentList, BillingClickListener clickListener) {
        this.context = context;
        this.contentList = contentList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public BillingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BillingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.billing_item_design, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BillingViewHolder holder, int position) {
        int index = holder.getAdapterPosition();

        if("PRODUCT".equalsIgnoreCase(contentList.get(index).getType())){
            holder.product_ll.setVisibility(View.VISIBLE);
            holder.accessories_ll.setVisibility(View.GONE);
            holder.product_name.setText(contentList.get(index).getName()+"-"+contentList.get(index).getProductCategory().substring(0,1));
            holder.product_code.setText(contentList.get(index).getCode());
            holder.product_units.setText(contentList.get(index).getUnits()+" ML");
            holder.product_total_price.setText("₹ "+df.format(contentList.get(index).getPieces() * contentList.get(index).getSellingItemPrice()));

            if(contentList.get(0).getPieces()>0) {
                holder.product_pieces.setText( "["+contentList.get(index).getPieces()+" x ₹"+df.format(contentList.get(index).getSellingItemPrice())+"]");
            }else{
                holder.product_pieces.setVisibility(View.GONE);
            }
        }else if("NON_PRODUCT".equalsIgnoreCase(contentList.get(index).getType())){
            holder.product_ll.setVisibility(View.GONE);
            holder.accessories_ll.setVisibility(View.VISIBLE);
            holder.accessories_name.setText(contentList.get(index).getName());
            holder.accessories_price.setText("₹ "+df.format(contentList.get(index).getPieces() * contentList.get(index).getSellingItemPrice()));

            if(contentList.get(0).getPieces()>0) {
                holder.accessories_pieces.setText( "["+contentList.get(index).getPieces()+" x ₹"+df.format(contentList.get(index).getSellingItemPrice())+"]");
            }else{
                holder.accessories_pieces.setVisibility(View.GONE);
            }
        }

        holder.product_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.click(index,"REMOVE");
            }
        });
        holder.accessories_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.click(index,"REMOVE");
            }
        });

        holder.product_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.click(index,"UPDATE");
            }
        });
        holder.accessories_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.click(index,"UPDATE");
            }
        });

    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }
}

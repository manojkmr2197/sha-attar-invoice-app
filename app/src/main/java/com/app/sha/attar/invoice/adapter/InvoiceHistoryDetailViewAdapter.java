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
import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.BillingItemModel;
import com.app.sha.attar.invoice.viewholder.InvoiceHistoryDetailViewHolder;
import com.app.sha.attar.invoice.viewholder.InvoiceHistoryViewHolder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class InvoiceHistoryDetailViewAdapter extends RecyclerView.Adapter<InvoiceHistoryDetailViewHolder>{

    Context context;
    List<BillingItemModel> contentList = new ArrayList<>();
    BillingClickListener clickListener;
    DecimalFormat df = new DecimalFormat("#.00");
    public InvoiceHistoryDetailViewAdapter(Context context, List<BillingItemModel> contentList, BillingClickListener clickListener) {
        this.context = context;
        this.contentList = contentList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public InvoiceHistoryDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new InvoiceHistoryDetailViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.invoice_history_detail_item_design, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceHistoryDetailViewHolder holder, int position) {
        int index = holder.getAdapterPosition();

        if("PRODUCT".equalsIgnoreCase(contentList.get(index).getType())) {
            holder.itemType.setText(contentList.get(index).getType());
            holder.itemQuantity.setText(contentList.get(index).getUnits() + " ML");
        }else{
            holder.itemType.setText("ACCESSORIES");
            holder.itemQuantity.setVisibility(View.GONE);
        }

        holder.itemName.setText(contentList.get(index).getName());
        holder.itemCode.setText(contentList.get(index).getCode());
        holder.itemSellingPrice.setText(df.format(contentList.get(index).getSellingItemPrice()));

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.click(index,"EDIT");
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.click(index,"DELETE");
            }
        });


    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }
}

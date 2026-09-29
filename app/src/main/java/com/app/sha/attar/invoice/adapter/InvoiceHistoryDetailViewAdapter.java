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

import org.apache.commons.lang3.StringUtils;

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
            holder.itemName.setText(contentList.get(index).getName()+"-"+contentList.get(index).getProductCategory().substring(0,1));
        }else{
            holder.itemType.setText("ACCESSORIES");
            holder.itemName.setText(contentList.get(index).getName());
            holder.itemQuantity.setVisibility(View.GONE);
        }


        if(StringUtils.isNoneBlank(contentList.get(index).getCode())) {
            holder.itemCode.setText(contentList.get(index).getCode());
        }else{
            holder.itemCode.setVisibility(View.GONE);
        }
        Double sellingPrice = contentList.get(index).getSellingItemPrice() != null ? contentList.get(index).getSellingItemPrice() : 0.0;
        Integer pieces = contentList.get(index).getPieces() != null ? contentList.get(index).getPieces() : 1;
        holder.itemSellingPrice.setText("₹ " + df.format(pieces * sellingPrice));

        if (pieces > 1) {
            holder.pieces.setVisibility(View.VISIBLE);
            holder.pieces.setText("[" + pieces + " x ₹" + df.format(sellingPrice) + "]");
        } else {
            holder.pieces.setVisibility(View.GONE);
        }

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

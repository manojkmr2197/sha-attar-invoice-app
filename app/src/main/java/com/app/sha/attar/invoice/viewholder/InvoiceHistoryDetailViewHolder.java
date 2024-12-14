package com.app.sha.attar.invoice.viewholder;


import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;

public class InvoiceHistoryDetailViewHolder  extends RecyclerView.ViewHolder {

    public Button edit,delete;
    public TextView itemType,itemName,itemCode,itemQuantity,itemSellingPrice;

    public InvoiceHistoryDetailViewHolder(@NonNull View itemView) {
        super(itemView);
        edit =(Button) itemView.findViewById(R.id.invoice_history_detail_item_edit);
        delete =(Button) itemView.findViewById(R.id.invoice_history_detail_item_delete);

        itemType = (TextView) itemView.findViewById(R.id.invoice_history_detail_item_type);
        itemName = (TextView) itemView.findViewById(R.id.invoice_history_detail_item_name);
        itemCode = (TextView) itemView.findViewById(R.id.invoice_history_detail_item_code);
        itemQuantity = (TextView) itemView.findViewById(R.id.invoice_history_detail_item_quantity);
        itemSellingPrice = (TextView) itemView.findViewById(R.id.invoice_history_detail_item_selling_price);
    }
}

package com.app.sha.attar.invoice.viewholder;


import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;

public class InvoiceHistoryDetailViewHolder  extends RecyclerView.ViewHolder {

    public Button edit,delete;
    public TextView itemType,itemName,itemCode,itemQuantity,itemSellingPrice,pieces;

    public InvoiceHistoryDetailViewHolder(@NonNull View itemView) {
        super(itemView);
        edit = itemView.findViewById(R.id.invoice_history_detail_item_edit);
        delete = itemView.findViewById(R.id.invoice_history_detail_item_delete);

        itemType = itemView.findViewById(R.id.invoice_history_detail_item_type);
        itemName = itemView.findViewById(R.id.invoice_history_detail_item_name);
        itemCode = itemView.findViewById(R.id.invoice_history_detail_item_code);
        itemQuantity = itemView.findViewById(R.id.invoice_history_detail_item_quantity);
        itemSellingPrice = itemView.findViewById(R.id.invoice_history_detail_item_selling_price);
        itemSellingPrice = itemView.findViewById(R.id.invoice_history_detail_item_selling_price);
        pieces = itemView.findViewById(R.id.invoice_history_detail_item_pieces);
    }
}

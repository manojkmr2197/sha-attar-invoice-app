package com.app.sha.attar.invoice.viewholder;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;

public class InvoiceHistoryViewHolder extends RecyclerView.ViewHolder {

    public Button edit,delete;
    public TextView id,date,name,phone,discount,sellingPrice,actualPrice,profit,courierAmount,paymentMode;
    public ImageView printBt,shareBt,whatsappBt;
    public LinearLayout owner_view,courier_view;

    public InvoiceHistoryViewHolder(@NonNull View itemView) {
        super(itemView);
        edit = (Button) itemView.findViewById(R.id.invoice_history_item_edit);
        delete =(Button) itemView.findViewById(R.id.invoice_history_item_delete);
        id = (TextView) itemView.findViewById(R.id.invoice_history_id);
        date = (TextView) itemView.findViewById(R.id.invoice_history_item_date);
        name = (TextView) itemView.findViewById(R.id.invoice_history_item_name);
        phone = (TextView) itemView.findViewById(R.id.invoice_history_item_phone);
        discount = (TextView) itemView.findViewById(R.id.invoice_history_item_discount);
        sellingPrice = (TextView) itemView.findViewById(R.id.invoice_history_item_selling_amount);
        actualPrice = (TextView) itemView.findViewById(R.id.invoice_history_item_actual_amount);
        profit = (TextView) itemView.findViewById(R.id.invoice_history_item_profit);
        owner_view = (LinearLayout) itemView.findViewById(R.id.invoice_history_item_owner_detail_ll);
        courier_view = (LinearLayout) itemView.findViewById(R.id.invoice_history_item_courier_ll);
        courierAmount = (TextView) itemView.findViewById(R.id.invoice_history_item_courier_charge);
        printBt = (ImageView) itemView.findViewById(R.id.invoice_history_item_print);
        shareBt = (ImageView) itemView.findViewById(R.id.invoice_history_item_share);
        whatsappBt = (ImageView) itemView.findViewById(R.id.invoice_history_item_whatsapp);
        paymentMode = (TextView) itemView.findViewById(R.id.invoice_history_item_payement_mode);
    }
}

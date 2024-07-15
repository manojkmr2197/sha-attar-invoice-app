package com.app.sha.attar.invoice.viewholder;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;

public class CustomerHistoryDetailViewHolder extends RecyclerView.ViewHolder {


    public TextView product_name, product_code,product_units, product_total_price;
    public LinearLayout product_ll,accessories_ll;
    public TextView accessories_name,accessories_price;

    public CustomerHistoryDetailViewHolder(@NonNull View itemView) {
        super(itemView);

        product_ll = (LinearLayout) itemView.findViewById(R.id.cust_history_product_ll);
        accessories_ll = (LinearLayout) itemView.findViewById(R.id.cust_history_accessories_ll);
        product_name = (TextView) itemView.findViewById(R.id.cust_history_product_name);
        product_code = (TextView) itemView.findViewById(R.id.cust_history_product_code);
        product_units = (TextView) itemView.findViewById(R.id.cust_history_product_units);
        product_total_price = (TextView) itemView.findViewById(R.id.cust_history_product_total_price);
        accessories_name = (TextView) itemView.findViewById(R.id.cust_history_accessories_name);
        accessories_price = (TextView) itemView.findViewById(R.id.cust_history_accessories_price);
    }
}

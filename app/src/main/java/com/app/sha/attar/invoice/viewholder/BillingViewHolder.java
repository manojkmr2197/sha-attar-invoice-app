package com.app.sha.attar.invoice.viewholder;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;

public class BillingViewHolder extends RecyclerView.ViewHolder {

    public Button product_edit,product_close;
    public Button accessories_edit,accessories_close;
    public TextView product_name, product_code,product_units, product_total_price;
    public LinearLayout product_ll,accessories_ll;
    public TextView accessories_name,accessories_price;

    public BillingViewHolder(@NonNull View itemView) {
        super(itemView);

        product_ll = (LinearLayout) itemView.findViewById(R.id.billing_product_ll);
        accessories_ll = (LinearLayout) itemView.findViewById(R.id.billing_accessories_ll);
        product_edit = (Button) itemView.findViewById(R.id.billing_product_edit);
        product_close = (Button) itemView.findViewById(R.id.billing_product_close);
        accessories_edit = (Button) itemView.findViewById(R.id.billing_accessories_edit);
        accessories_close = (Button) itemView.findViewById(R.id.billing_accessories_close);
        product_name = (TextView) itemView.findViewById(R.id.billing_product_name);
        product_code = (TextView) itemView.findViewById(R.id.billing_product_code);
        product_units = (TextView) itemView.findViewById(R.id.billing_product_units);
        product_total_price = (TextView) itemView.findViewById(R.id.billing_product_total_price);
        accessories_name = (TextView) itemView.findViewById(R.id.billing_accessories_name);
        accessories_price = (TextView) itemView.findViewById(R.id.billing_accessories_price);

    }
}

package com.app.sha.attar.invoice.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;

public class ReportViewHolder extends RecyclerView.ViewHolder {

    public TextView name,quantity,actual_amt,selling_amount,profit;
    public ReportViewHolder(@NonNull View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.report_item_name);
        quantity = (TextView) itemView.findViewById(R.id.report_item_quantity);
        actual_amt = (TextView) itemView.findViewById(R.id.report_item_actual_amt);
        selling_amount = (TextView) itemView.findViewById(R.id.report_item_selling_amt);
        profit = (TextView) itemView.findViewById(R.id.report_item_profit);
    }
}

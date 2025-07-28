package com.app.sha.attar.invoice.viewholder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;

public class ExpenseViewHolder extends RecyclerView.ViewHolder {

    public Button delete, edit;
    public TextView date,title,amount,type;

    public ExpenseViewHolder(@NonNull View itemView) {
        super(itemView);

        delete = (Button) itemView.findViewById(R.id.expense_item_delete);
        edit = (Button) itemView.findViewById(R.id.expense_item_edit);

        date = (TextView) itemView.findViewById(R.id.expense_item_date);
        title = (TextView) itemView.findViewById(R.id.expense_item_title);
        type = (TextView) itemView.findViewById(R.id.expense_item_type);
        amount = (TextView) itemView.findViewById(R.id.expense_item_amount);
    }
}

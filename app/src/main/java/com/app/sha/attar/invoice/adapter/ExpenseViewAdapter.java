package com.app.sha.attar.invoice.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.listener.BillingClickListener;
import com.app.sha.attar.invoice.model.ExpenseModel;
import com.app.sha.attar.invoice.viewholder.ExpenseViewHolder;
import com.app.sha.attar.invoice.viewholder.ProductViewHolder;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ExpenseViewAdapter extends RecyclerView.Adapter<ExpenseViewHolder>{

    Context context;
    List<ExpenseModel> contentList = new ArrayList<>();
    BillingClickListener clickListener;

    NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    ZoneOffset istOffset = ZoneOffset.ofHoursMinutes(5, 30);

    public ExpenseViewAdapter(Context context, List<ExpenseModel> contentList, BillingClickListener clickListener) {
        this.context = context;
        this.contentList = contentList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ExpenseViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_item_design, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        int index = holder.getAdapterPosition();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            OffsetDateTime offsetDateTime = Instant.ofEpochSecond(contentList.get(index).getExpenseDate()).atOffset(istOffset);
            holder.date.setText(offsetDateTime.format(formatter));
        }
        holder.title.setText(contentList.get(index).getTitle());
        holder.type.setText(contentList.get(index).getType());

        holder.amount.setText(numberFormat.format(contentList.get(index).getAmount()).replace("\u00A0", ""));

        List<String> specialList = Arrays.asList(context.getResources().getStringArray(R.array.spinner_special_expense_type));

        if(specialList.contains(contentList.get(index).getType())){
            holder.delete.setVisibility(View.INVISIBLE);
            holder.edit.setVisibility(View.INVISIBLE);
        }

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.click(index,"DELETE");
            }
        });

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.click(index,"EDIT");
            }
        });
    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }
}

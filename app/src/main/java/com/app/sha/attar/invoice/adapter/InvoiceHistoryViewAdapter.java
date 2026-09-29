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
import com.app.sha.attar.invoice.model.ProductModel;
import com.app.sha.attar.invoice.viewholder.InvoiceHistoryViewHolder;
import com.app.sha.attar.invoice.viewholder.ProductViewHolder;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class InvoiceHistoryViewAdapter extends RecyclerView.Adapter<InvoiceHistoryViewHolder> {

    Context context;
    List<BillingInvoiceModel> contentList = new ArrayList<>();
    BillingClickListener clickListener;
    Boolean owner;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
    ZoneOffset istOffset = ZoneOffset.ofHoursMinutes(5, 30);
    DecimalFormat df = new DecimalFormat("#.00");

    public InvoiceHistoryViewAdapter(Context context, List<BillingInvoiceModel> contentList, BillingClickListener clickListener, Boolean owner) {
        this.context = context;
        this.contentList = contentList;
        this.clickListener = clickListener;
        this.owner = owner;
    }

    @NonNull
    @Override
    public InvoiceHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new InvoiceHistoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.invoice_history_item_design, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceHistoryViewHolder holder, int position) {

        int index = holder.getAdapterPosition();

        holder.id.setText(contentList.get(index).getBillingDate() + "");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            OffsetDateTime offsetDateTime = Instant.ofEpochSecond(contentList.get(index).getBillingDate()).atOffset(istOffset);
            holder.date.setText(offsetDateTime.format(formatter));
        }

        String clientName = contentList.get(index).getClientName();
        String clientPhone = contentList.get(index).getClientPhoneNo();
        String custName = contentList.get(index).getCustomerName();
        String custPhone = contentList.get(index).getCustomerPhone();

        String displayName = (clientName != null && !clientName.trim().isEmpty()) ? clientName.trim() : ((custName != null && !custName.trim().isEmpty()) ? custName.trim() : "Walk-in");
        String displayPhone = (clientPhone != null && !clientPhone.trim().isEmpty()) ? clientPhone.trim() : ((custPhone != null && !custPhone.trim().isEmpty()) ? custPhone.trim() : "-");

        holder.name.setText(displayName);
        holder.phone.setText(displayPhone);
        holder.billBy.setText("Bill By : "+contentList.get(index).getCustomerName());

        String pMode = contentList.get(index).getPaymentMode() != null ? contentList.get(index).getPaymentMode() : "CASH";
        Double cardCharges = contentList.get(index).getCardCharges() != null ? contentList.get(index).getCardCharges() : 0.0;
        Double sellCost = contentList.get(index).getSellingCost() != null ? contentList.get(index).getSellingCost() : 0.0;
        Double grandTotal = contentList.get(index).getGrandTotal();

        if ("CARD".equalsIgnoreCase(pMode)) {
            holder.paymentMode.setText(pMode + " [Charge Inc. ₹" + df.format(cardCharges) + "]");
            holder.sellingPrice.setText("₹ " + df.format(grandTotal + cardCharges));
        } else {
            holder.paymentMode.setText(pMode);
            holder.sellingPrice.setText("₹ " + df.format(grandTotal));
        }
        holder.discount.setText(contentList.get(index).getDiscount() + " %");


        if (contentList.get(index).getIsPrint() != null && Boolean.TRUE.equals(contentList.get(index).getIsPrint())) {
            holder.printBt.setImageDrawable(context.getDrawable(R.drawable.baseline_print_inactive24));
        } else {
            holder.printBt.setImageDrawable(context.getDrawable(R.drawable.baseline_print_active24));
        }

        if (owner) {
            holder.owner_view.setVisibility(View.VISIBLE);

            final Double[] actualPrice = {0.0};
            if (contentList.get(index).getBillingItemModelList() != null) {
                contentList.get(index).getBillingItemModelList().forEach(data -> {
                    if (data != null && data.getTotalPrice() != null) {
                        actualPrice[0] += data.getTotalPrice();
                    }
                });
            }

            holder.actualPrice.setText("Rs. " + df.format(actualPrice[0]));
            holder.profit.setText("Rs. " + df.format(sellCost - actualPrice[0]));
        } else {
            holder.owner_view.setVisibility(View.GONE);
        }
        if(contentList.get(index).getIsCourier() != null && Boolean.TRUE.equals(contentList.get(index).getIsCourier())){
            holder.courier_view.setVisibility(View.VISIBLE);
            holder.courierAmount.setText(""+contentList.get(index).getCourierAmount());
        }else{
            holder.courier_view.setVisibility(View.GONE);
        }

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.click(index, "EDIT");
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.click(index, "DELETE");
            }
        });

        holder.printBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.click(index, "PRINT");
            }
        });

        holder.shareBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.click(index, "SHARE");
            }
        });
        holder.qrShareBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.click(index, "QR_SHARE");
            }
        });


    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }
}

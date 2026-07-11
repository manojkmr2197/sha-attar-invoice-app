package com.app.sha.attar.invoice.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.BillingItemModel;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClientOrdersAdapter extends RecyclerView.Adapter<ClientOrdersAdapter.OrderViewHolder> {

    private final Context context;
    private final List<BillingInvoiceModel> invoiceList;

    public ClientOrdersAdapter(Context context, List<BillingInvoiceModel> invoiceList) {
        this.context = context;
        this.invoiceList = invoiceList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.client_order_item_design, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        BillingInvoiceModel invoice = invoiceList.get(position);

        // Format date
        String dateStr = "";
        if (invoice.getBillingDate() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    OffsetDateTime odt = Instant.ofEpochSecond(invoice.getBillingDate())
                            .atZone(ZoneId.systemDefault())
                            .toOffsetDateTime();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a", Locale.getDefault());
                    dateStr = odt.format(formatter);
                } catch (Exception e) {
                    dateStr = String.valueOf(invoice.getBillingDate());
                }
            } else {
                try {
                    Date date = new Date(invoice.getBillingDate() * 1000L);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault());
                    dateStr = sdf.format(date);
                } catch (Exception e) {
                    dateStr = String.valueOf(invoice.getBillingDate());
                }
            }
        }
        holder.dateTv.setText(dateStr);

        holder.paymentModeTv.setText(invoice.getPaymentMode() != null ? invoice.getPaymentMode().toUpperCase() : "-");
        holder.priceTv.setText(String.format(Locale.getDefault(), "₹%.2f", invoice.getSellingCost() != null ? invoice.getSellingCost() : 0.0));

        // Dynamically add items to container
        holder.itemsContainer.removeAllViews();
        List<BillingItemModel> items = invoice.getBillingItemModelList();
        if (items != null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            for (BillingItemModel item : items) {
                View itemView = inflater.inflate(R.layout.client_order_sub_item, holder.itemsContainer, false);
                TextView nameTv = itemView.findViewById(R.id.sub_item_name);
                TextView qtyTv = itemView.findViewById(R.id.sub_item_qty);
                TextView totalTv = itemView.findViewById(R.id.sub_item_total);

                String name = item.getName() != null ? item.getName() : "";
                nameTv.setText(name);

                double pieces = item.getPieces() != null ? item.getPieces() : 0.0;
                double sellingPrice = item.getSellingItemPrice() != null ? item.getSellingItemPrice() : 0.0;
                double total = pieces * sellingPrice;

                if ("PRODUCT".equalsIgnoreCase(item.getType())) {
                    int ml = item.getUnits() != null ? item.getUnits() : 0;
                    qtyTv.setText(String.format(Locale.getDefault(), "%d ML x %.0f pcs", ml, pieces));
                } else {
                    qtyTv.setText(String.format(Locale.getDefault(), "%.0f pcs x ₹%.2f", pieces, sellingPrice));
                }

                totalTv.setText(String.format(Locale.getDefault(), "₹%.2f", total));

                holder.itemsContainer.addView(itemView);
            }
        }
    }

    @Override
    public int getItemCount() {
        return invoiceList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView dateTv, paymentModeTv, priceTv;
        LinearLayout itemsContainer;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTv = itemView.findViewById(R.id.order_item_date);
            paymentModeTv = itemView.findViewById(R.id.order_item_payment_mode);
            priceTv = itemView.findViewById(R.id.order_item_price);
            itemsContainer = itemView.findViewById(R.id.order_items_container);
        }
    }
}

package com.app.sha.attar.invoice.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.adapter.CustomerHistoryAdapter;
import com.app.sha.attar.invoice.adapter.CustomerHistoryDetailViewAdapter;
import com.app.sha.attar.invoice.listener.BillingClickListener;
import com.app.sha.attar.invoice.listener.ClickListener;
import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.BillingItemModel;
import com.app.sha.attar.invoice.utils.DBUtil;
import com.app.sha.attar.invoice.utils.FirestoreCallback;
import com.google.android.material.textfield.TextInputEditText;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class CustomerHistoryActivity extends AppCompatActivity implements View.OnClickListener {

    Context context;
    Activity activity;

    CustomerHistoryAdapter customerHistoryAdapter;
    List<BillingInvoiceModel> billingInvoiceModelList =new ArrayList<>();
    RecyclerView recyclerView;
    ClickListener listener;

    TextInputEditText search_et;
    Button search;
    TextView back;

    LinearLayout data_ll,no_data_ll;
    DBUtil dbObj;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_history);

        context = CustomerHistoryActivity.this;
        activity = CustomerHistoryActivity.this;

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
        }


        back = (TextView) findViewById(R.id.customer_history_back);
        back.setOnClickListener(this);
        search = (Button) findViewById(R.id.customer_history_search);
        search.setOnClickListener(this);
        search_et = (TextInputEditText) findViewById(R.id.customer_history_search_et);

        data_ll = (LinearLayout) findViewById(R.id.customer_history_data_ll);
        no_data_ll = (LinearLayout) findViewById(R.id.customer_history_no_data_ll);
        dbObj = new DBUtil();
        listener =new ClickListener() {
            @Override
            public void click(int index) {
                if(billingInvoiceModelList.get(index) != null && !billingInvoiceModelList.get(index).getBillingItemModelList().isEmpty()) {
                    createDetailDialogView(billingInvoiceModelList.get(index));
                }
            }
        };

        recyclerView = (RecyclerView) findViewById(R.id.customer_history_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        customerHistoryAdapter = new CustomerHistoryAdapter(context,billingInvoiceModelList,listener);
        recyclerView.setAdapter(customerHistoryAdapter);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("phone_no")) {
            String phone_no = intent.getStringExtra("phone_no");
            search_et.setText(phone_no);
            searchCustomerDetails(phone_no);
        }
    }

    private void searchCustomerDetails(String phoneNo) {

        //input  - phone no
        //output - list of BillingInvoiceModel
        //modify the logic
        //billingInvoiceModelList

         dbObj.getBillingInvoiceDetail(new FirestoreCallback<List<BillingInvoiceModel>>() {
            @Override
            public void onCallback(List<BillingInvoiceModel> aCustomerDetails) {
                billingInvoiceModelList = aCustomerDetails;
            }
        }, phoneNo);

        IntStream.range(0, billingInvoiceModelList.size()).forEach(i -> {
            dbObj.getBillingItemModelDetail(new FirestoreCallback<List<BillingItemModel>>() {
                @Override
                public void onCallback(List<BillingItemModel> billingItemModels) {
                    // Directly set the billing item models list for the specific invoice model
                    billingInvoiceModelList.get(i).setBillingItemModelList(billingItemModels);
                    System.out.println("customerHistorySize: " + billingItemModels.size());
                }
            }, billingInvoiceModelList.get(i).getSaleId());
        });


//
//        List<BillingItemModel> billingItemModelList = new ArrayList<>();
//
//        IntStream.range(0, customerHistoryModelList.size()).forEach(i -> dbObj.getBillingItemModelDetail(new FirestoreCallback<List<BillingItemModel>>() {
//            @Override
//            public void onCallback(List<BillingItemModel> aBillingItemModel) {
//                customerHistoryModelList.get(i).setBillingItemModelList(aBillingItemModel);
//
//                // You can now use customerDetails here
//                System.out.println("aBillingItemModel: " + aBillingItemModel.size());
//            }
//        }, customerHistoryModelList.get(i).getSaleId()));
/*



        billingItemModelList.add(new BillingItemModel("PRODUCT","ATTAR","A12",12,23,1200));
        billingItemModelList.add(new BillingItemModel("NON_PRODUCT","ATTAR","",0,0,80));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            customerHistoryModelList.add(new CustomerHistoryModel(2300,10, OffsetDateTime.now(),billingItemModelList));
            customerHistoryModelList.add(new CustomerHistoryModel(300,10, OffsetDateTime.now(),billingItemModelList));
        }
*/
        if(billingInvoiceModelList != null){
            data_ll.setVisibility(View.VISIBLE);
            no_data_ll.setVisibility(View.GONE);
            customerHistoryAdapter.notifyDataSetChanged();
        }else{
            no_data_ll.setVisibility(View.VISIBLE);
            data_ll.setVisibility(View.GONE);
        }
    }

    public void createDetailDialogView(BillingInvoiceModel billingInvoiceModel) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_customer_history_design, null);
        builder.setView(dialogView);
        builder.setTitle("Billing History");

        RecyclerView recyclerView = dialogView.findViewById(R.id.customer_dialog_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        BillingClickListener listener1 =new BillingClickListener() {
            @Override
            public void click(int index, String type) {

            }
        };

        CustomerHistoryDetailViewAdapter adapter = new CustomerHistoryDetailViewAdapter(dialogView.getContext(),billingInvoiceModel.getBillingItemModelList(),listener1);
        recyclerView.setAdapter(adapter);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public void onClick(View view) {
        if(R.id.customer_history_search == view.getId()){
            if(StringUtils.isEmpty(search_et.getText().toString())){
                Toast.makeText(CustomerHistoryActivity.this, "Please Enter Phone Number ..! ", Toast.LENGTH_LONG).show();
                return;
            }
            searchCustomerDetails(search_et.getText().toString());
        }else if (R.id.customer_history_back == view.getId()){
            finish();
        }
    }
}
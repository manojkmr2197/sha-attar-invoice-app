package com.app.sha.attar.invoice.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.adapter.SalesPersonViewAdapter;
import com.app.sha.attar.invoice.listener.BillingClickListener;
import com.app.sha.attar.invoice.model.ExpenseModel;
import com.app.sha.attar.invoice.model.SalesPersonModel;
import com.app.sha.attar.invoice.utils.DBUtil;
import com.app.sha.attar.invoice.utils.DatabaseConstants;
import com.app.sha.attar.invoice.utils.FirestoreCallback;
import com.app.sha.attar.invoice.utils.SingleTon;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class SalesPersonActivity extends AppCompatActivity implements View.OnClickListener {

    Context context;
    Activity activity;
    FrameLayout data_fl, no_data_fl;

    List<SalesPersonModel> itemList = new ArrayList<>();

    SalesPersonViewAdapter salesPersonViewAdapter;
    RecyclerView recyclerView;
    BillingClickListener clickListener;

    DBUtil dbObj;
    private Gson gson;
    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_sales_person);

        context = SalesPersonActivity.this;
        activity = SalesPersonActivity.this;

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
        }
        no_data_fl = (FrameLayout) findViewById(R.id.sales_person_no_data_ll);
        data_fl = (FrameLayout) findViewById(R.id.sales_person_data_ll);

        recyclerView = (RecyclerView) findViewById(R.id.sales_person_recyclerView);
        TextView back = (TextView) findViewById(R.id.sales_person_back);
        back.setOnClickListener(this);

        FloatingActionButton add_fab = (FloatingActionButton) findViewById(R.id.sales_person_add_fab);
        add_fab.setOnClickListener(this);

        dbObj = new DBUtil();
        db = DBUtil.getInstance();
        gson = new Gson();

        clickListener = new BillingClickListener() {
            @Override
            public void click(int index, String type) {
                if (type.equalsIgnoreCase("EDIT")) {
                    //editdialog box
                    createDialogBox(context, itemList.get(index));
                } else if (type.equalsIgnoreCase("DELETE")) {
                    //delete confirmation
                    deleteConfirmationPopup(index);
                }
            }
        };

        salesPersonViewAdapter = new SalesPersonViewAdapter(context, itemList, clickListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(salesPersonViewAdapter);

        if (checkInternet()) {
            getSalesPersonInfo();
        }
    }


    private void deleteConfirmationPopup(int index) {
        // Create and configure the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure you want to delete?");
        builder.setCancelable(true);

        // Set positive button
        builder.setPositiveButton("Yes", (dialog, which) -> {
            deleteSalesPersonDetail(index);
            dialog.dismiss();
        });

        // Set negative button
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });

        // Create and show the dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void deleteSalesPersonDetail(int index) {
        db.collection(DatabaseConstants.SALES_PERSON_COLLECTION)
                .document(String.valueOf(itemList.get(index).getId()))
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SalesPersonActivity.this, "Sales Person deleted .!", Toast.LENGTH_LONG).show();
                    itemList.remove(index);
                    salesPersonViewAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(SalesPersonActivity.this, "Sales Person deleted failed..!", Toast.LENGTH_LONG).show());

    }


    private void getSalesPersonInfo() {
        dbObj.getSalePersonDetails(new FirestoreCallback<List<SalesPersonModel>>() {
            @Override
            public void onCallback(List<SalesPersonModel> result) {
                if (result.isEmpty()) {
                    Toast.makeText(SalesPersonActivity.this, "No Sales Person Data found .!", Toast.LENGTH_LONG).show();
                    return;
                }
                itemList.clear();
                itemList.addAll(result);
                Toast.makeText(SalesPersonActivity.this, "Sales Person Data Loaded .!", Toast.LENGTH_LONG).show();

                data_fl.setVisibility(View.VISIBLE);
                no_data_fl.setVisibility(View.GONE);
                salesPersonViewAdapter.notifyDataSetChanged();
            }
        });
    }

    private boolean checkInternet() {
        if (SingleTon.isNetworkConnected(activity)) {
            return true;
        } else {
            Toast.makeText(context, "No Internet connection. Please try again .! ", Toast.LENGTH_LONG).show();
            return false;
        }

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.sales_person_back) {
            finish();
        } else if (view.getId() == R.id.sales_person_add_fab) {
            //add dialog will show
            createDialogBox(context, null);
        }
    }

    private void createDialogBox(Context context, SalesPersonModel salesPersonModel) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(R.layout.dialog_sales_person_create);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        TextInputEditText name = (TextInputEditText) dialog.findViewById(R.id.sales_person_add_name);
        TextInputEditText phone = (TextInputEditText) dialog.findViewById(R.id.sales_person_add_phone);
        TextInputEditText password = (TextInputEditText) dialog.findViewById(R.id.sales_person_add_password);

        Spinner owner = (Spinner) dialog.findViewById(R.id.sales_person_add_type);
        CheckBox available = (CheckBox) dialog.findViewById(R.id.sales_person_add_checkbox);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_type, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        owner.setAdapter(adapter);

        Button submitDialog = (Button) dialog.findViewById(R.id.sales_person_add_submit);
        TextView delete = (TextView) dialog.findViewById(R.id.sales_person_add_delete);
        TextView close = (TextView) dialog.findViewById(R.id.sales_person_add_close);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        OffsetDateTime currentTime = OffsetDateTime.now();

        if (salesPersonModel != null) {
            delete.setVisibility(View.VISIBLE);
            name.setText(salesPersonModel.getName());
            phone.setText(salesPersonModel.getPhoneNo());
            password.setText(salesPersonModel.getPassword());
            if ("SALES".equalsIgnoreCase(salesPersonModel.getType())) {
                owner.setSelection(0);
            } else if ("ADMIN".equalsIgnoreCase(salesPersonModel.getType())) {
                owner.setSelection(1);
            }
            available.setChecked(salesPersonModel.isActive());

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context, "Loading .! ", Toast.LENGTH_LONG).show();

                    db.collection(DatabaseConstants.SALES_PERSON_COLLECTION).document(salesPersonModel.getId())
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Call the callback with null since the task was successful
                                    Toast.makeText(context, "Sales Person successfully deleted!", Toast.LENGTH_LONG).show();
                                    System.out.println("Sales Person successfully deleted!");
                                    getSalesPersonInfo();
                                    dialog.dismiss();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Error while deleting Sales Person. Please try again.!", Toast.LENGTH_LONG).show();
                                    System.err.println("Error while deleting Sales Person. " + e);
                                    dialog.dismiss();
                                }
                            });

                }
            });
        } else {
            delete.setVisibility(View.GONE);
        }

        submitDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (salesPersonModel != null) {
                    salesPersonModel.setName(name.getText().toString());
                    salesPersonModel.setPhoneNo(phone.getText().toString());
                    salesPersonModel.setPassword(password.getText().toString());
                    salesPersonModel.setType(owner.getSelectedItem().toString());
                    salesPersonModel.setActive(available.isChecked());
                    db.collection(DatabaseConstants.SALES_PERSON_COLLECTION)
                            .document(salesPersonModel.getId())
                            .set(salesPersonModel)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(context, "New Person - " + salesPersonModel.getName() + " Updated", Toast.LENGTH_LONG).show();
                                    System.out.println("Person Updated successfully.");
                                    dialog.dismiss();
                                    getSalesPersonInfo();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Error while Updating Person. Please try again", Toast.LENGTH_LONG).show();
                                    System.out.println("Error while updating Person." + e);
                                }
                            });
                } else {
                    SalesPersonModel newSalesPersonModel = new SalesPersonModel();
                    newSalesPersonModel.setName(name.getText().toString());
                    newSalesPersonModel.setPhoneNo(phone.getText().toString());
                    newSalesPersonModel.setPassword(password.getText().toString());
                    newSalesPersonModel.setType(owner.getSelectedItem().toString());
                    newSalesPersonModel.setActive(available.isChecked());
                    newSalesPersonModel.setId(SingleTon.generateSalesPersonDetailDocument());
                    db.collection(DatabaseConstants.SALES_PERSON_COLLECTION)
                            .document(newSalesPersonModel.getId())
                            .set(newSalesPersonModel)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(context, "New Person - " + newSalesPersonModel.getName() + " Added", Toast.LENGTH_LONG).show();
                                    System.out.println("Person Added successfully.");
                                    dialog.dismiss();
                                    getSalesPersonInfo();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Error while saving Person. Please try again", Toast.LENGTH_LONG).show();
                                    System.out.println("Error while saving Person." + e);
                                }
                            });


                }
            }
        });


        dialog.show();

    }
}
package com.app.sha.attar.invoice.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.adapter.ExpenseViewAdapter;
import com.app.sha.attar.invoice.listener.BillingClickListener;
import com.app.sha.attar.invoice.model.ExpenseModel;
import com.app.sha.attar.invoice.model.ProductModel;
import com.app.sha.attar.invoice.utils.AppConstants;
import com.app.sha.attar.invoice.utils.DBUtil;
import com.app.sha.attar.invoice.utils.DatabaseConstants;
import com.app.sha.attar.invoice.utils.FirestoreCallback;
import com.app.sha.attar.invoice.utils.ReportGenerator;
import com.app.sha.attar.invoice.utils.SingleTon;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ExpenseTrackerActivity extends AppCompatActivity implements View.OnClickListener {

    Context context;
    Activity activity;
    FrameLayout data_fl, no_data_fl;

    List<ExpenseModel> itemList;

    ExpenseViewAdapter expenseViewAdapter;
    RecyclerView recyclerView;
    BillingClickListener clickListener;

    DBUtil dbObj;
    private Gson gson;
    FirebaseFirestore db;

    TextView startDatetv, endDatetv;
    TextView date;
    OffsetDateTime customStartDt = null, customEndDt = null,newExpenseDate = null;

    Spinner expenseType;
    Button search;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_expense_tracker);

        context = ExpenseTrackerActivity.this;
        activity = ExpenseTrackerActivity.this;

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
        }

        no_data_fl = (FrameLayout) findViewById(R.id.expense_no_data_ll);
        data_fl = (FrameLayout) findViewById(R.id.expense_data_ll);

        recyclerView = (RecyclerView) findViewById(R.id.expense_recyclerView);
        TextView back = (TextView) findViewById(R.id.expense_back);
        back.setOnClickListener(this);

        TextView download = (TextView) findViewById(R.id.expense_download_tv);
        download.setOnClickListener(this);

        FloatingActionButton add_fab = (FloatingActionButton) findViewById(R.id.expense_add_fab);
        add_fab.setOnClickListener(this);

        FloatingActionButton special_add_fab = (FloatingActionButton) findViewById(R.id.expense_special_add_fab);
        special_add_fab.setOnClickListener(this);

        startDatetv = findViewById(R.id.expense_start_date_tv);
        endDatetv = findViewById(R.id.expense_end_date_tv);

        startDatetv.setOnClickListener(view -> showStartDatePickerDialog());
        endDatetv.setOnClickListener(view -> showEndDatePickerDialog());

        expenseType = (Spinner) findViewById(R.id.expense_choose_type);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_expense_filter_type, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        expenseType.setAdapter(adapter);
        expenseType.setSelection(0);

        search = (Button) findViewById(R.id.expense_search);
        search.setOnClickListener(this);

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
        itemList = new ArrayList<>();
        expenseViewAdapter = new ExpenseViewAdapter(context, itemList, clickListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(expenseViewAdapter);

    }

    private void deleteConfirmationPopup(int index) {
        // Create and configure the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure you want to delete?");
        builder.setCancelable(true);

        // Set positive button
        builder.setPositiveButton("Yes", (dialog, which) -> {
            deleteExpenseDetail(index);
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

    private void deleteExpenseDetail(int index) {
        db.collection(DatabaseConstants.EXPENSE_COLLECTION)
                .document(String.valueOf(itemList.get(index).getId()))
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ExpenseTrackerActivity.this, "Expense item deleted .!", Toast.LENGTH_LONG).show();
                    itemList.remove(index);
                    expenseViewAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(ExpenseTrackerActivity.this, "Expense deleted failed..!", Toast.LENGTH_LONG).show());

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.expense_back) {
            finish();
        } else if (view.getId() == R.id.expense_add_fab) {
            //add dialog will show
            createDialogBox(context, null);
        } else if (view.getId() == R.id.expense_special_add_fab) {
            //add dialog will show
            createSpecialDialogBox(context);
        } else if (view.getId() == R.id.expense_download_tv) {
            //download xl
            downloadExpenseList();
        } else if (view.getId() == R.id.expense_search) {
            if (checkInternet()) {
                System.out.println(customStartDt + " --- " + customEndDt);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    if("ALL".equalsIgnoreCase(expenseType.getSelectedItem().toString())){
                        processExpense(customStartDt, customEndDt);
                    }else {
                        processExpenseFilter(customStartDt, customEndDt,expenseType.getSelectedItem().toString());
                    }

                }
            }
        }
    }

    private void downloadExpenseList() {
        try {
            saveExcelFile(itemList);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Report Generation failed ..!", Toast.LENGTH_LONG).show();
        }
    }

    private void saveExcelFile(List<ExpenseModel> productModelList) throws Exception {
        String fileName = "Expense-" + System.currentTimeMillis() + ".xlsx";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        ReportGenerator reportGenerator = new ReportGenerator();
        reportGenerator.createExpenseExcelReport(productModelList, file);

        // Notify the user
        Toast.makeText(this, "Report Generated: " + fileName, Toast.LENGTH_LONG).show();

        // Use FileProvider to get the URI
        Uri fileUri = FileProvider.getUriForFile(this, AppConstants.COM_APP_SHA_PERFUME_INVOICE_FILEPROVIDER, file);

        // Open the file using a file explorer
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/vnd.ms-excel");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    private void createSpecialDialogBox(Context context) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(R.layout.dialog_special_expense_create);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        TextInputEditText price = (TextInputEditText) dialog.findViewById(R.id.expense_special_add_price);
        Spinner type = (Spinner) dialog.findViewById(R.id.expense_special_add_type);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_special_expense_type, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        type.setAdapter(adapter);
        type.setSelection(0);

        TextView close = (TextView) dialog.findViewById(R.id.expense_special_add_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        Button submitDialog = (Button) dialog.findViewById(R.id.expense_special_add_submit);

        submitDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (StringUtils.isEmpty(price.getText().toString())) {
                    Toast.makeText(ExpenseTrackerActivity.this, "Please enter Price amount ..!", Toast.LENGTH_SHORT).show();
                    return;
                }

                OffsetDateTime now = OffsetDateTime.now();
                // Extract year and month
                YearMonth yearMonth = YearMonth.of(now.getYear(), now.getMonth());
                // Get the number of days in this month
                int daysInMonth = yearMonth.lengthOfMonth();
                if("STORE_RENT".equalsIgnoreCase(type.getSelectedItem().toString())){
                    double pricePerDay = Double.valueOf(price.getText().toString())/daysInMonth;
                    for(int i=0;i<daysInMonth;i++){
                        insertExpense(now.withDayOfMonth(i+1),type.getSelectedItem().toString(),pricePerDay);
                    }
                    Toast.makeText(context, "New Expense - " + type.getSelectedItem().toString() + " Added", Toast.LENGTH_LONG).show();
                    System.out.println("Expense Added successfully.");
                    if (customStartDt != null && customEndDt != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        processExpense(customStartDt, customEndDt);
                    }
                }else if("ELECTRICITY".equalsIgnoreCase(type.getSelectedItem().toString())){
                    YearMonth yearPreviousMonth = YearMonth.of(now.getYear(), now.getMonth().minus(1));
                    double pricePerDay = Double.valueOf(price.getText().toString())/(daysInMonth+yearPreviousMonth.lengthOfMonth());
                    for(int i=0;i<daysInMonth;i++){
                        insertExpense(now.withDayOfMonth(i+1),type.getSelectedItem().toString(),pricePerDay);
                    }
                    for(int i=0;i<yearPreviousMonth.lengthOfMonth();i++){
                        insertExpense(now.minusMonths(1).withDayOfMonth(i+1),type.getSelectedItem().toString(),pricePerDay);
                    }
                    Toast.makeText(context, "New Expense - " + type.getSelectedItem().toString() + " Added", Toast.LENGTH_LONG).show();
                    System.out.println("Expense Added successfully.");
                    if (customStartDt != null && customEndDt != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        processExpense(customStartDt, customEndDt);
                    }
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void insertExpense(OffsetDateTime offsetDateTime, String type, double pricePerDay) {
        ExpenseModel newExpenseModel = new ExpenseModel();
        newExpenseModel.setExpenseDate(offsetDateTime.toEpochSecond());
        newExpenseModel.setAmount(pricePerDay);
        newExpenseModel.setType(type);
        newExpenseModel.setId(SingleTon.generateExpenseDetailDocument());
        db.collection(DatabaseConstants.EXPENSE_COLLECTION)
                .document(newExpenseModel.getId())
                .set(newExpenseModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //Toast.makeText(context, "New Expense - " + newExpenseModel.getTitle() + " Added", Toast.LENGTH_LONG).show();
                        System.out.println("Expense Added successfully.");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Error while saving Expense. Please try again", Toast.LENGTH_LONG).show();
                        System.out.println("Error while saving Expense." + e);
                    }
                });

    }

    private void createDialogBox(Context context, ExpenseModel expenseModel) {

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(R.layout.dialog_expense_create);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        TextInputEditText title = (TextInputEditText) dialog.findViewById(R.id.expense_add_name);
        TextInputEditText price = (TextInputEditText) dialog.findViewById(R.id.expense_add_price);
        date = (TextView) dialog.findViewById(R.id.expense_add_date_tv);
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseDateTimePicker();
            }
        });
        Spinner type = (Spinner) dialog.findViewById(R.id.expense_add_type);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_expense_type, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        type.setAdapter(adapter);
        type.setSelection(0);

        Button submitDialog = (Button) dialog.findViewById(R.id.expense_add_submit);
        TextView delete = (TextView) dialog.findViewById(R.id.expense_add_delete);
        TextView close = (TextView) dialog.findViewById(R.id.expense_add_close);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        newExpenseDate = OffsetDateTime.now();

        if (expenseModel != null) {
            delete.setVisibility(View.VISIBLE);
            newExpenseDate = Instant.ofEpochSecond(expenseModel.getExpenseDate()).atOffset(ZoneOffset.ofHoursMinutes(5, 30));
            title.setText(expenseModel.getTitle());
            price.setText("" + expenseModel.getAmount());

            switch (expenseModel.getType()) {
                case "COURIER":
                    type.setSelection(0);
                    break;
                case "TEA_FOOD_EXPENSE":
                    type.setSelection(1);
                    break;
                case "STATIONARY":
                    type.setSelection(2);
                    break;
                case "DONATION":
                    type.setSelection(3);
                    break;
                case "STAFF_SALARY":
                    type.setSelection(4);
                    break;
                case "OTHER":
                    type.setSelection(5);
                    break;
            }

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context, "Loading .! ", Toast.LENGTH_LONG).show();

                    db.collection(DatabaseConstants.EXPENSE_COLLECTION).document(expenseModel.getId())
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Call the callback with null since the task was successful
                                    Toast.makeText(context, "Expense successfully deleted!", Toast.LENGTH_LONG).show();
                                    System.out.println("Expense successfully deleted!");
                                    if (customStartDt != null && customEndDt != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        processExpense(customStartDt, customEndDt);
                                    }
                                    dialog.dismiss();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Error while deleting Expense. Please try again.!", Toast.LENGTH_LONG).show();
                                    System.err.println("Error while deleting Expense. " + e);
                                    dialog.dismiss();
                                }
                            });

                }
            });

        } else {
            delete.setVisibility(View.GONE);
        }
        date.setText(newExpenseDate.format(formatter));

        OffsetDateTime finalCurrentTime = newExpenseDate;
        submitDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (StringUtils.isEmpty(price.getText().toString())) {
                    Toast.makeText(ExpenseTrackerActivity.this, "Please enter Price amount ..!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (expenseModel != null) {
                    expenseModel.setTitle(title.getText().toString());
                    expenseModel.setAmount(Double.valueOf(price.getText().toString()));
                    expenseModel.setType(type.getSelectedItem().toString());
                    db.collection(DatabaseConstants.EXPENSE_COLLECTION)
                            .document(expenseModel.getId())
                            .set(expenseModel)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(context, "New Expense - " + expenseModel.getTitle() + " Updated", Toast.LENGTH_LONG).show();
                                    System.out.println("Expense Updated successfully.");
                                    dialog.dismiss();
                                    if (customStartDt != null && customEndDt != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        processExpense(customStartDt, customEndDt);
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Error while Updating Expense. Please try again", Toast.LENGTH_LONG).show();
                                    System.out.println("Error while updating Expense." + e);
                                }
                            });
                } else {
                    ExpenseModel newExpenseModel = new ExpenseModel();
                    newExpenseModel.setExpenseDate(finalCurrentTime.toEpochSecond());
                    newExpenseModel.setTitle(title.getText().toString());
                    newExpenseModel.setAmount(Double.valueOf(price.getText().toString()));
                    newExpenseModel.setType(type.getSelectedItem().toString());
                    newExpenseModel.setId(SingleTon.generateExpenseDetailDocument());
                    db.collection(DatabaseConstants.EXPENSE_COLLECTION)
                            .document(newExpenseModel.getId())
                            .set(newExpenseModel)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(context, "New Expense - " + newExpenseModel.getTitle() + " Added", Toast.LENGTH_LONG).show();
                                    System.out.println("Expense Added successfully.");
                                    dialog.dismiss();
                                    if (customStartDt != null && customEndDt != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        processExpense(customStartDt, customEndDt);
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Error while saving Expense. Please try again", Toast.LENGTH_LONG).show();
                                    System.out.println("Error while saving Expense." + e);
                                }
                            });


                }
            }
        });


        dialog.show();
    }

    private void chooseDateTimePicker() {
        // Get current date and time
        Calendar calendar = Calendar.getInstance();

        // DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    // Update calendar with selected date
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // TimePickerDialog
                    new TimePickerDialog(
                            this,
                            (timeView, hourOfDay, minute) -> {
                                // Update calendar with selected time
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);

                                // Display selected date and time
                                LocalDateTime localDatetime = calendar.toInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDateTime();

                                // Create OffsetDateTime with 00:00 time
                                newExpenseDate = localDatetime.atZone(ZoneId.of("Asia/Kolkata")).toOffsetDateTime();
                                date.setText(newExpenseDate.format(formatter));
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false // 24-hour format
                    ).show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();

    }

    private void processExpense(OffsetDateTime customStartDt, OffsetDateTime customEndDt) {
        if(customStartDt == null || customEndDt == null){
            Toast.makeText(context, "Please choose the date range.!", Toast.LENGTH_SHORT).show();
            return;
        }
        dbObj.getExpenseDetail(new FirestoreCallback<List<ExpenseModel>>() {
            @Override
            public void onCallback(List<ExpenseModel> result) {
                if (result.isEmpty()) {
                    Toast.makeText(ExpenseTrackerActivity.this, "No Expense Data found .!", Toast.LENGTH_LONG).show();
                    return;
                }
                itemList.clear();
                itemList.addAll(result);
                Toast.makeText(ExpenseTrackerActivity.this, "Expense Data Loaded .!", Toast.LENGTH_LONG).show();

                data_fl.setVisibility(View.VISIBLE);
                no_data_fl.setVisibility(View.GONE);
                expenseViewAdapter.notifyDataSetChanged();
            }
        }, customStartDt.toEpochSecond(), customEndDt.toEpochSecond());
    }

    private void processExpenseFilter(OffsetDateTime customStartDt, OffsetDateTime customEndDt,String expenseType) {
        if(customStartDt == null || customEndDt == null){
            Toast.makeText(context, "Please choose the date range.!", Toast.LENGTH_SHORT).show();
            return;
        }
        dbObj.getExpenseDetail(new FirestoreCallback<List<ExpenseModel>>() {
            @Override
            public void onCallback(List<ExpenseModel> result) {
                itemList.clear();
                if (result.isEmpty()) {
                    Toast.makeText(ExpenseTrackerActivity.this, "No Expense Data found .!", Toast.LENGTH_LONG).show();
                    data_fl.setVisibility(View.GONE);
                    no_data_fl.setVisibility(View.VISIBLE);
                }else {
                    itemList.addAll(result);
                    Toast.makeText(ExpenseTrackerActivity.this, "Expense Data Loaded .!", Toast.LENGTH_LONG).show();

                    data_fl.setVisibility(View.VISIBLE);
                    no_data_fl.setVisibility(View.GONE);
                }
                expenseViewAdapter.notifyDataSetChanged();
            }
        }, customStartDt.toEpochSecond(), customEndDt.toEpochSecond(),expenseType);
    }

    private boolean checkInternet() {
        if (SingleTon.isNetworkConnected(activity)) {
            return true;
        } else {
            Toast.makeText(context, "No Internet connection. Please try again .! ", Toast.LENGTH_LONG).show();
            return false;
        }

    }

    private void showStartDatePickerDialog() {

        // Get the current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Show DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                ExpenseTrackerActivity.this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    // Update the TextView with the selected date
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    startDatetv.setText(dateFormat.format(calendar.getTime()));
                    LocalDate localDate = calendar.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    // Create OffsetDateTime with 00:00 time
                    customStartDt = localDate.atTime(LocalTime.MIN).atZone(ZoneId.of("Asia/Kolkata")).toOffsetDateTime();
                },
                year,
                month,
                day
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();

    }

    private void showEndDatePickerDialog() {

        // Get the current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Show DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                ExpenseTrackerActivity.this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    // Update the TextView with the selected date
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    endDatetv.setText(dateFormat.format(calendar.getTime()));
                    LocalDate localDate = calendar.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    customEndDt = localDate.atTime(LocalTime.MAX).atZone(ZoneId.of("Asia/Kolkata")).toOffsetDateTime();
                },
                year,
                month,
                day
        );
        datePickerDialog.getDatePicker().setMinDate((customStartDt != null) ? customStartDt.toInstant().toEpochMilli() : System.currentTimeMillis());
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();

    }
}
package com.developer.attd;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.developer.attd.Adapter.AttendanceAdapter;
import com.developer.attd.Model.AttendanceModel;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class History extends AppCompatActivity {
    TextView tv_date;
    Button btnSelectDate, btnSelectMonth;
    EditText edt_search;
    private RecyclerView rvAttendance;
    private AttendanceAdapter adapter;
    private ArrayList<AttendanceModel> attendanceList = new ArrayList<>();
    String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);
        tv_date = findViewById(R.id.tv_date);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectMonth = findViewById(R.id.btnSelectMonth);
        edt_search = findViewById(R.id.edt_search);
        rvAttendance = findViewById(R.id.rvAttendance);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        rvAttendance.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendanceAdapter(attendanceList);
        rvAttendance.setAdapter(adapter);


        btnSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
        btnSelectMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMonthYearPicker();
            }
        });




    }

    private void showDatePickerDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.datepicker_dialog);

        DatePicker datePicker = dialog.findViewById(R.id.datePicker);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnDone = dialog.findViewById(R.id.btnDone);

        // Cancel
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Done
        btnDone.setOnClickListener(v -> {
            int day = datePicker.getDayOfMonth();
            int month = datePicker.getMonth() + 1; // 0-based index
            int year = datePicker.getYear();
            String selectedDate = day + "/" + month + "/" + year;
            String textviewDate = "Date: " + day + "-" + month + "-" + year;
            tv_date.setText(textviewDate);
            fetchAttendanceByDate(selectedDate);
            Toast.makeText(this, "Selected: " + selectedDate, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void fetchAttendanceByDate(String selectedDate) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 🔹 selectedDate = "dd/MM/yyyy" (UI se mila)
        // Firestore me save format = "yyyy-MM-dd"
        try {
            Date date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(selectedDate);
            String firestoreDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);

            db.collection("attendance")
                    .whereEqualTo("date", firestoreDate)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        attendanceList.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            AttendanceModel model = doc.toObject(AttendanceModel.class);
                            attendanceList.add(model);
                        }
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Date parsing error", Toast.LENGTH_SHORT).show();
        }
    }

    private void showMonthYearPicker() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.datepicker_dialog);

        DatePicker datePicker = dialog.findViewById(R.id.datePicker);

        // Sirf Month-Year show karna
        int daySpinnerId = getResources().getIdentifier("day", "id", "android");
        if (daySpinnerId != 0) {
            View daySpinner = datePicker.findViewById(daySpinnerId);
            if (daySpinner != null) {
                daySpinner.setVisibility(View.GONE); // Hide Day
            }
        }

        Button btnDone = dialog.findViewById(R.id.btnDone);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDone.setOnClickListener(v -> {
            int month = datePicker.getMonth() + 1; // 0-based index
            int year = datePicker.getYear();

            String selectedMonthYear = month + "/" + year;
            String textviewDate = "Month: " + month + "/" + year;
            tv_date.setText(textviewDate);
            fetchMonthlyAttendance(userId, month, year);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void fetchMonthlyAttendance(String userId, int month, int year) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Month ke start aur end date banao
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1); // month-1 because Calendar month is 0-based
        Date startDate = calendar.getTime();

        calendar.set(year, month - 1, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = calendar.getTime();

        // Firestore query
        db.collection("attendance")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startDate))
                .whereLessThanOrEqualTo("date", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(endDate))
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        attendanceList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            AttendanceModel att = doc.toObject(AttendanceModel.class);
                            attendanceList.add(att);
                        }
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "Fetched " + attendanceList.size() + " records", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No records found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }




}
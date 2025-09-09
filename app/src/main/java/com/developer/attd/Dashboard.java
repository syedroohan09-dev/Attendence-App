package com.developer.attd;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.developer.attd.Utils.PrefsUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Dashboard extends AppCompatActivity {
    private TextView textEmail, textDate, textTime, tv_status, tv_startTime, tv_endTime;
    private Button btnAttendance, btnLeave, btnMyAttendance, btnReport, btnHistory, btnSettings;
    private ImageView btnLogout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Handler handler = new Handler();

    // For server time adjustment
    private long serverTime = 0;
    private long fetchTime = 0;
    private String startingTime, endingTime, halfDay;
    String attendenceStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        textEmail = findViewById(R.id.textEmail);
        textDate = findViewById(R.id.textDate);
        textTime = findViewById(R.id.textTime);
        tv_status = findViewById(R.id.tv_status);
        tv_startTime = findViewById(R.id.tv_startTime);
        tv_endTime = findViewById(R.id.tv_endTime);

        btnAttendance = findViewById(R.id.btnAttendance);
        btnLeave = findViewById(R.id.btnLeave);
        btnMyAttendance = findViewById(R.id.btnMyAttendance);
        btnReport = findViewById(R.id.btnReport);
        btnHistory = findViewById(R.id.btnHistory);
        btnLogout = findViewById(R.id.btnLogout);
        fetchShiftTime();
        attendenceStatus = PrefsUtils.getAttendanceStatus(Dashboard.this);
        if (attendenceStatus != null) {
            if (attendenceStatus.equalsIgnoreCase("checkin")) {
                btnAttendance.setText("Check - OUT");
            } else {
                btnAttendance.setText("Check - IN");
            }
        } else {
            btnAttendance.setText("Check-IN");
        }

        // Show user email
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            textEmail.setText("Welcome, \n" + user.getEmail());
        }

        // Update date
//        String currentDate = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(new Date());
//        textDate.setText(currentDate);

        getServerTime();

        // Buttons
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(Dashboard.this, MainActivity.class));
            finish();
        });

        btnAttendance.setOnClickListener(v -> {
            // startActivity(new Intent(this, AttendanceActivity.class));
            markAttendance(mAuth.getCurrentUser().getUid());
        });

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Dashboard.this, History.class));
            }
        });

        btnLeave.setOnClickListener(v -> {
            // startActivity(new Intent(this, LeaveRequestActivity.class));
        });

        btnMyAttendance.setOnClickListener(v -> {
            startActivity(new Intent(this, MyAttedence.class));
        });

        btnReport.setOnClickListener(v -> {
            // startActivity(new Intent(this, ReportActivity.class));
        });
    }

    private void fetchShiftTime() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("shifts").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                startingTime = doc.getString("startingTime");
                                endingTime = doc.getString("endingTime");
                                halfDay = doc.getString("halfDay");

                                // Ab yahan aap apne UI me set kar sakte ho
                                Log.d("SHIFT", "Start: " + startingTime + " End: " + endingTime + " Half: " + halfDay);

                                Toast.makeText(Dashboard.this,
                                        "Shift Loaded\nStart: " + startingTime + "\nEnd: " + endingTime + "\nHalf: " + halfDay,
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(Dashboard.this, "No shift found!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Dashboard.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void getServerTime() {
        Map<String, Object> data = new HashMap<>();
        data.put("time", FieldValue.serverTimestamp());

        db.collection("serverTime").document("now")
                .set(data)
                .addOnSuccessListener(unused ->
                        db.collection("serverTime").document("now").get()
                                .addOnSuccessListener(snap -> {
                                    Date date = snap.getDate("time");
                                    if (date != null) {
                                        serverTime = date.getTime();
                                        fetchTime = System.currentTimeMillis();
                                        runClock();
                                    }
                                })
                );
    }

    private void runClock() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                long now = serverTime + (System.currentTimeMillis() - fetchTime);
                long offset = System.currentTimeMillis() - fetchTime;
                long current = serverTime + offset;
                String currentDate = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(new Date(current));
                String currentTime = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(new Date(now));
                textDate.setText(currentDate);
                textTime.setText(currentTime);
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void markAttendance(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Local se sirf DATE nikalo (id ke liye) -> yyyy-MM-dd
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());

        // Attendance doc reference
        DocumentReference attendanceRef = db.collection("attendance")
                .document(userId + "_" + today);

        attendanceRef.get().addOnSuccessListener(attSnap -> {
            if (attSnap.exists()) {
                // Agar already checkIn hai aur checkOut null hai -> checkOut mark karo
                if (attSnap.get("checkOut") == null) {
                    Map<String, Object> update = new HashMap<>();
                    update.put("checkOut", FieldValue.serverTimestamp());
                    update.put("checkoutComments", "OK"); // CheckOut hua hai

                    attendanceRef.update(update)
                            .addOnSuccessListener(aVoid -> {

                                PrefsUtils.saveAttendanceStatus(Dashboard.this, "checkOUT");
                                btnAttendance.setText("Check - IN");
                                Date checkOutDate = new Date(serverTime + (System.currentTimeMillis() - fetchTime));
                                String checkOutTime = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                                        .format(checkOutDate);
                                tv_endTime.setText("END: " + checkOutTime);
                                Toast.makeText(this, "Check-Out marked", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(this, "Already Checked Out", Toast.LENGTH_SHORT).show();
                }
            } else {
                // 🔹 Pehli dafa checkIn mark karna -> status decide karo
                Date now = new Date(serverTime + (System.currentTimeMillis() - fetchTime));
                String currentTimeStr = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(now);

                String status = decideStatus(currentTimeStr);
                tv_status.setText("Status: " + status);

                Map<String, Object> data = new HashMap<>();
                data.put("userId", userId);
                data.put("date", today);
                data.put("checkIn", FieldValue.serverTimestamp());
                data.put("checkOut", null);
                data.put("status", status);
                data.put("checkoutComments", "Incomplete"); // default
                PrefsUtils.saveAttendanceStatus(Dashboard.this, "checkIN");
                btnAttendance.setText("Check - OUT");
                tv_startTime.setText("START: " + currentTimeStr);
                attendanceRef.set(data).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Check-In marked (" + status + ")", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private String decideStatus(String checkInTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());

            Date checkIn = sdf.parse(checkInTime);
            Date shiftStart = sdf.parse(startingTime);
            Date shiftHalf = sdf.parse(halfDay);
            Date shiftEnd = sdf.parse(endingTime);

            if (checkIn == null || shiftStart == null || shiftHalf == null) return "Present";

            if (checkIn.after(shiftStart) && checkIn.before(shiftHalf)) {
                return "Late"; // late but before half-day
            } else if (checkIn.after(shiftHalf) && checkIn.before(shiftEnd)) {
                return "Half Day"; // entered after half-day
            } else if (checkIn.after(shiftEnd)) {
                return "Absent"; // came after shift ended
            } else {
                return "Present"; // on time
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Present";
        }
    }


}
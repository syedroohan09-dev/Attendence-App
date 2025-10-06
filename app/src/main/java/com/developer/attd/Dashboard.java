package com.developer.attd;

import static com.developer.attd.Utils.PrefsUtils.getEndingTime;
import static com.developer.attd.Utils.PrefsUtils.getHalfDay;
import static com.developer.attd.Utils.PrefsUtils.getStartingTime;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.developer.attd.Model.SntpClient;
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
    private TextView textEmail, textDate, textTime, tv_status, tv_startTime, tv_endTime, tv_start_shift_time, tv_halfDay_shift_time, tv_end_shift_time;
    private Button btnAttendance, btnLeave, btnMyAttendance, btnReport, btnHistory;
    private ImageView btnLogout, btn_settings;
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
        tv_start_shift_time = findViewById(R.id.tv_start_shift_time);
        tv_halfDay_shift_time = findViewById(R.id.tv_halfDay_shift_time);
        tv_end_shift_time = findViewById(R.id.tv_end_shift_time);

        btnAttendance = findViewById(R.id.btnAttendance);
        btnLeave = findViewById(R.id.btnLeave);
        btnMyAttendance = findViewById(R.id.btnMyAttendance);
        btnReport = findViewById(R.id.btnReport);
        btnHistory = findViewById(R.id.btnHistory);
        btnLogout = findViewById(R.id.btnLogout);
        btn_settings = findViewById(R.id.btn_settings);

        new Thread(() -> {
            long ntpTime = SntpClient.getNtpTime();
            if (ntpTime != -1) {
                fetchTime = System.currentTimeMillis(); // NTP fetch ka local snapshot
                long baseTime = ntpTime;

                runOnUiThread(() -> {
                    runClock(baseTime);
                });
            } else {
                runOnUiThread(() -> {
                    textTime.setText("Failed to sync time");
                });
            }
        }).start();


        String startingTime = getStartingTime(Dashboard.this);
        String endingTime = getEndingTime(Dashboard.this);
        String halfDay = getHalfDay(Dashboard.this);
        if ((startingTime == null || startingTime.isEmpty()) &&
                (endingTime == null || endingTime.isEmpty()) &&
                (halfDay == null || halfDay.isEmpty())) {
            fetchShiftTime();
        }
        tv_start_shift_time.setText(startingTime);
        tv_halfDay_shift_time.setText(halfDay);
        tv_end_shift_time.setText(endingTime);



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

//        WorkRequest request = new OneTimeWorkRequest.Builder(BackgroundWorker.class).build();
//        WorkManager.getInstance(this).enqueue(request);

        // Show user email
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            textEmail.setText("Logged in as, \n" + user.getEmail());
        }

        // Update date
//        String currentDate = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(new Date());
//        textDate.setText(currentDate);

//        getServerTime();

        // Buttons

        btn_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Settings.class));
            }
        });
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
                                PrefsUtils.saveShiftTimings(Dashboard.this, startingTime, endingTime, halfDay);
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


//    private void getServerTime() {
//        Map<String, Object> data = new HashMap<>();
//        data.put("time", FieldValue.serverTimestamp());
//
//        db.collection("serverTime").document("now")
//                .set(data)
//                .addOnSuccessListener(unused ->
//                        db.collection("serverTime").document("now").get()
//                                .addOnSuccessListener(snap -> {
//                                    Date date = snap.getDate("time");
//                                    if (date != null) {
//                                        serverTime = date.getTime();
//                                        fetchTime = System.currentTimeMillis();
//                                        runClock();
//                                    }
//                                })
//                );
//    }

    private void runClock(long baseTime) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                long now = baseTime + (System.currentTimeMillis() - fetchTime);

                String currentDate = new SimpleDateFormat(
                        "EEE, dd MMM yyyy", Locale.getDefault()
                ).format(new Date(now));

                String currentTimeStr = new SimpleDateFormat(
                        "hh:mm:ss a", Locale.getDefault()
                ).format(new Date(now));

                textDate.setText(currentDate);
                textTime.setText(currentTimeStr);

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

    public void getWifiSSID(){
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();
            if(ssid.equalsIgnoreCase("StormFiber-3B60")){
                markAttendance(mAuth.getCurrentUser().getUid());
            }
        }
    }


}
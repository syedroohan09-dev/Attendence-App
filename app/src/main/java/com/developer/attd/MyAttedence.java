package com.developer.attd;

import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyAttedence extends AppCompatActivity {
    private RecyclerView rvAttendance;
    private AttendanceAdapter adapter;
    private List<AttendanceModel> attendanceList = new ArrayList<>();

    private FirebaseFirestore db;
    private String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_attedence);
        rvAttendance = findViewById(R.id.rvAttendance);
        rvAttendance.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendanceAdapter(attendanceList);
        rvAttendance.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("uid", "UID: "+userId);

        fetchUserAttendance();
    }
    private void fetchUserAttendance() {
        db.collection("attendance")
                .whereEqualTo("userId", userId)
                .get()  // remove orderBy for testing first
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        attendanceList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            AttendanceModel att = doc.toObject(AttendanceModel.class);
                            attendanceList.add(att);
                        }
                        Collections.sort(attendanceList, (a1, a2) -> a1.getDate().compareTo(a2.getDate()));
                        adapter.notifyDataSetChanged();
//                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "No attendance found or error occurred", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
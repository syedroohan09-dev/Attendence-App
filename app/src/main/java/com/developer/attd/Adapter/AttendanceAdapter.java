package com.developer.attd.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.developer.attd.Model.AttendanceModel;
import com.developer.attd.R;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private List<AttendanceModel> attendanceList;

    public AttendanceAdapter(List<AttendanceModel> attendanceList) {
        this.attendanceList = attendanceList;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        AttendanceModel att = attendanceList.get(position);
        holder.tvDate.setText(att.getDate());
        holder.tvStatus.setText(att.getStatus());
        holder.tvCheckIn.setText("Check-In: \n" + formatTime(att.getCheckIn()));
        holder.tvCheckOut.setText("Check-Out: \n" + formatTime(att.getCheckOut()));
        holder.tvCheckoutComment.setText("Checkout: " + att.getCheckoutComments());
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    private String formatTime(Timestamp ts) {
        if (ts == null) return "--:--";
        Date date = ts.toDate();
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date);
    }

    static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvStatus, tvCheckIn, tvCheckOut, tvCheckoutComment;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCheckIn = itemView.findViewById(R.id.tvCheckIn);
            tvCheckOut = itemView.findViewById(R.id.tvCheckOut);
            tvCheckoutComment = itemView.findViewById(R.id.tvCheckoutComment);
        }
    }
}


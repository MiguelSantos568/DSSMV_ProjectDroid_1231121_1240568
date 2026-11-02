package com.example.condosched.ui;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;
import com.example.condosched.MainActivity;
import com.example.condosched.R;

public final class BottomNavigation {
    private BottomNavigation() {}
    public static void bind(Activity activity, int selected) {
        int[] ids = {R.id.navHome, R.id.navBookings, R.id.navCondo, R.id.navProfile};
        for (int id : ids) {
            activity.findViewById(id).setSelected(id == selected);
            activity.findViewById(id).setOnClickListener(v -> {
                if (id == selected) return;
                if (id == R.id.navBookings) {
                    Toast.makeText(activity, R.string.bookings_pending, Toast.LENGTH_SHORT).show();
                    return;
                }
                Class<?> target = id == R.id.navHome ? MainActivity.class
                        : id == R.id.navCondo ? CondominiumActivity.class : ProfileActivity.class;
                activity.startActivity(new Intent(activity, target).addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            });
        }
    }
}

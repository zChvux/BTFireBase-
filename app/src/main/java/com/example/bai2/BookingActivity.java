package com.example.bai2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity {

    private TextView tvMovieTitle, tvSelectedSeat;
    private Spinner spinnerTheater, spinnerShowtime;
    private GridLayout gridSeats;
    private Button btnBook;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    
    private List<Theater> theaterList = new ArrayList<>();
    private List<String> theaterNames = new ArrayList<>();
    private List<Showtime> showtimeList = new ArrayList<>();
    private List<String> showtimeLabels = new ArrayList<>();
    private String movieId;
    private View lastSelectedSeatView = null;

    private final String COLOR_AVAILABLE = "#F5F5F5";
    private final String COLOR_BOOKED = "#BDBDBD";
    private final String COLOR_SELECTED = "#E91E63";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        movieId = getIntent().getStringExtra("movieId");
        String movieTitle = getIntent().getStringExtra("movieTitle");

        tvMovieTitle = findViewById(R.id.tvBookingMovieTitle);
        tvMovieTitle.setText(movieTitle);
        
        spinnerTheater = findViewById(R.id.spinnerTheater);
        spinnerShowtime = findViewById(R.id.spinnerShowtime);
        tvSelectedSeat = findViewById(R.id.tvSelectedSeat);
        gridSeats = findViewById(R.id.gridSeats);
        btnBook = findViewById(R.id.btnBook);

        setupSeatGrid();
        loadTheaters();

        spinnerTheater.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < theaterList.size()) {
                    checkAndLoadShowtimes(theaterList.get(position).id);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnBook.setOnClickListener(v -> bookTicket());
    }

    private void setupSeatGrid() {
        gridSeats.removeAllViews();
        int rows = 5; int cols = 6;
        for (int i = 0; i < rows * cols; i++) {
            TextView seat = new TextView(this);
            char rowChar = (char) ('A' + (i / cols));
            int seatNum = (i % cols) + 1;
            String seatId = rowChar + String.valueOf(seatNum);
            seat.setText(seatId);
            seat.setPadding(16, 16, 16, 16);
            seat.setBackgroundColor(Color.parseColor(COLOR_AVAILABLE));
            seat.setTextColor(Color.BLACK);
            seat.setGravity(android.view.Gravity.CENTER);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 100; params.height = 100; params.setMargins(8, 8, 8, 8);
            seat.setLayoutParams(params);
            seat.setOnClickListener(v -> {
                if (lastSelectedSeatView != null) lastSelectedSeatView.setBackgroundColor(Color.parseColor(COLOR_AVAILABLE));
                seat.setBackgroundColor(Color.parseColor(COLOR_SELECTED));
                tvSelectedSeat.setText(seatId);
                lastSelectedSeatView = seat;
            });
            gridSeats.addView(seat);
        }
    }

    private void loadTheaters() {
        db.collection("theaters").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                theaterList.clear(); theaterNames.clear();
                if (task.getResult().isEmpty()) seedTheaters();
                else {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        Theater t = doc.toObject(Theater.class); t.id = doc.getId();
                        theaterList.add(t); theaterNames.add(t.name);
                    }
                    setupSpinnerAdapter(spinnerTheater, theaterNames);
                }
            }
        });
    }

    private void seedTheaters() {
        db.collection("theaters").add(new Theater(null, "CGV Vincom", "D1")).addOnSuccessListener(d -> loadTheaters());
    }

    private void checkAndLoadShowtimes(String theaterId) {
        db.collection("showtimes").whereEqualTo("movieId", movieId).whereEqualTo("theaterId", theaterId)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) seedShowtimes(theaterId);
                        else displayShowtimes(task.getResult());
                    }
                });
    }

    private void seedShowtimes(String theaterId) {
        Showtime s1 = new Showtime(null, movieId, theaterId, "10:00 AM", 12.0);
        db.collection("showtimes").add(s1).addOnSuccessListener(d -> checkAndLoadShowtimes(theaterId));
    }

    private void displayShowtimes(com.google.firebase.firestore.QuerySnapshot result) {
        showtimeList.clear(); showtimeLabels.clear();
        for (QueryDocumentSnapshot doc : result) {
            Showtime s = doc.toObject(Showtime.class); s.id = doc.getId();
            showtimeList.add(s); showtimeLabels.add(s.time + " - $" + s.price);
        }
        setupSpinnerAdapter(spinnerShowtime, showtimeLabels);
    }

    private void setupSpinnerAdapter(Spinner spinner, List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items) {
            @NonNull @Override public View getView(int p, @Nullable View c, @NonNull ViewGroup pa) {
                TextView v = (TextView) super.getView(p, c, pa); v.setTextColor(Color.BLACK); return v;
            }
            @Override public View getDropDownView(int p, @Nullable View c, @NonNull ViewGroup pa) {
                TextView v = (TextView) super.getDropDownView(p, c, pa); v.setTextColor(Color.BLACK); v.setBackgroundColor(Color.WHITE); return v;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void bookTicket() {
        if (spinnerShowtime.getSelectedItemPosition() < 0 || showtimeList.isEmpty()) {
            Toast.makeText(this, "Select a showtime", Toast.LENGTH_SHORT).show(); return;
        }
        String seat = tvSelectedSeat.getText().toString();
        if (seat.equals("None")) {
            Toast.makeText(this, "Select a seat", Toast.LENGTH_SHORT).show(); return;
        }
        Showtime selected = showtimeList.get(spinnerShowtime.getSelectedItemPosition());
        String userId = mAuth.getCurrentUser().getUid();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        Ticket ticket = new Ticket(null, userId, selected.id, seat, now, selected.price);

        db.collection("tickets").add(ticket).addOnSuccessListener(doc -> {
            // 1. Gửi thông báo đặt vé thành công ngay lập tức
            NotificationHelper.showBookingNotification(this, tvMovieTitle.getText().toString(), selected.time, seat);
            
            // 2. Đặt lịch nhắc nhở (Ví dụ: 10 giây sau để bạn có thể test thử ngay)
            scheduleReminder(tvMovieTitle.getText().toString(), seat);

            Toast.makeText(this, "Booking Successful!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void scheduleReminder(String movieTitle, String seat) {
        Intent intent = new Intent(this, ShowtimeReminderReceiver.class);
        intent.putExtra("movieTitle", movieTitle);
        intent.putExtra("seat", seat);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        // Đặt lịch sau 10 giây để kiểm tra tính năng
        long triggerTime = System.currentTimeMillis() + 10000; 

        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }
}

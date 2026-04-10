package com.example.bai2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyTicketsActivity extends AppCompatActivity {

    private RecyclerView rvTickets;
    private TicketAdapter adapter;
    private List<Ticket> ticketList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tickets);

        Toolbar toolbar = findViewById(R.id.toolbar_tickets);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        rvTickets = findViewById(R.id.rvTickets);
        rvTickets.setLayoutManager(new LinearLayoutManager(this));

        ticketList = new ArrayList<>();
        adapter = new TicketAdapter(ticketList);
        rvTickets.setAdapter(adapter);

        loadMyTickets();
    }

    private void loadMyTickets() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("tickets")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ticketList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Ticket ticket = document.toObject(Ticket.class);
                            ticketList.add(ticket);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Inner class for Adapter to keep it simple
    private static class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.ViewHolder> {
        private List<Ticket> tickets;

        TicketAdapter(List<Ticket> tickets) { this.tickets = tickets; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Ticket t = tickets.get(position);
            holder.tvSeat.setText("Seat: " + t.seatNumber);
            holder.tvPrice.setText("Price: $" + t.totalPrice);
            holder.tvTime.setText("Booked on: " + t.bookingTime);
            // In a full app, you'd fetch the movie title using t.showtimeId
            holder.tvTitle.setText("Ticket ID: " + (t.id != null ? t.id.substring(0, 5) : "N/A"));
        }

        @Override
        public int getItemCount() { return tickets.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvShowtime, tvSeat, tvPrice, tvTime;
            ViewHolder(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvTicketMovieTitle);
                tvTime = v.findViewById(R.id.tvTicketShowtime);
                tvSeat = v.findViewById(R.id.tvTicketSeat);
                tvPrice = v.findViewById(R.id.tvTicketPrice);
            }
        }
    }
}
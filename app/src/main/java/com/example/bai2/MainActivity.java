package com.example.bai2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvMovies;
    private MovieAdapter movieAdapter;
    private List<Movie> movieList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private static final int PERMISSION_REQUEST_CODE = 112;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Movie App");
        }

        db = FirebaseFirestore.getInstance();
        rvMovies = findViewById(R.id.rvMovies);
        progressBar = findViewById(R.id.progressBar);
        
        // Sử dụng GridLayoutManager với 2 cột để giống ảnh bạn gửi
        rvMovies.setLayoutManager(new GridLayoutManager(this, 2));
        
        movieList = new ArrayList<>();
        movieAdapter = new MovieAdapter(movieList, movie -> {
            Intent intent = new Intent(MainActivity.this, BookingActivity.class);
            intent.putExtra("movieId", movie.id);
            intent.putExtra("movieTitle", movie.title);
            startActivity(intent);
        });
        rvMovies.setAdapter(movieAdapter);

        checkNotificationPermission();
        checkAndSeedData();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.add(0, 999, 0, "Reset Movie Data"); 
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return true;
        } else if (id == R.id.action_my_tickets) {
            startActivity(new Intent(MainActivity.this, MyTicketsActivity.class));
            return true;
        } else if (id == 999) {
            resetMoviesData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkAndSeedData() {
        db.collection("movies").limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().isEmpty()) {
                    seedMovies();
                } else {
                    loadMovies();
                }
            }
        });
    }

    private void seedMovies() {
        progressBar.setVisibility(View.VISIBLE);
        List<Movie> dummyMovies = new ArrayList<>();
        // Cập nhật lại danh sách phim giống ảnh mẫu
        dummyMovies.add(new Movie(null, "Spider-Man: Across the Spider-Verse", "Animation, Action", "Miles Morales catapults across...", "https://image.tmdb.org/t/p/w500/8Vtpi9pR7mhb9T6vC0p4LRMgpXq.jpg", 140));
        dummyMovies.add(new Movie(null, "Oppenheimer", "Biography, Drama", "The story of J. Robert Oppenheimer...", "https://image.tmdb.org/t/p/w500/8Gxv2mYgiFAh78YhS3BDp9QvIBp.jpg", 180));
        dummyMovies.add(new Movie(null, "Barbie", "Comedy, Fantasy", "Barbie and Ken are having...", "https://image.tmdb.org/t/p/w500/iuFNm9vNVmRDLiAmPxovY9vI4pP.jpg", 114));
        dummyMovies.add(new Movie(null, "Dune: Part Two", "Sci-Fi, Adventure", "Paul Atreides unites with Chani...", "https://image.tmdb.org/t/p/w500/8bFL3K31O6SWSNZA6Xz0MGP0ST7.jpg", 166));

        List<Task<Void>> tasks = new ArrayList<>();
        for (Movie movie : dummyMovies) {
            tasks.add(db.collection("movies").add(movie).continueWithTask(t -> Tasks.forResult(null)));
        }

        Tasks.whenAllComplete(tasks).addOnCompleteListener(t -> loadMovies());
    }

    private void resetMoviesData() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("movies").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Task<Void>> deleteTasks = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    deleteTasks.add(db.collection("movies").document(doc.getId()).delete());
                }
                Tasks.whenAllComplete(deleteTasks).addOnCompleteListener(t -> seedMovies());
            }
        });
    }

    private void loadMovies() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("movies")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        movieList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Movie movie = document.toObject(Movie.class);
                            movie.id = document.getId();
                            movieList.add(movie);
                        }
                        movieAdapter.notifyDataSetChanged();
                    }
                });
    }
}
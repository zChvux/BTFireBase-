package com.example.bai2;

import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<Movie> movieList;
    private OnMovieClickListener listener;

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }

    public MovieAdapter(List<Movie> movieList, OnMovieClickListener listener) {
        this.movieList = movieList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.tvTitle.setText(movie.title);
        holder.tvGenre.setText(movie.genre);
        
        Log.d("CinePass_Adapter", "Binding movie: " + movie.title + " | URL: " + movie.imageUrl);

        // Placeholder màu xám để biết ImageView đang tồn tại
        ColorDrawable placeholder = new ColorDrawable(ContextCompat.getColor(holder.itemView.getContext(), R.id.progressBar == 0 ? android.R.color.darker_gray : R.color.dividerColor));

        if (movie.imageUrl != null && !movie.imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(movie.imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Lưu cache toàn bộ
                    .placeholder(placeholder)
                    .error(android.R.drawable.stat_notify_error) // Hiện icon lỗi nếu không tải được
                    .centerCrop()
                    .into(holder.ivPoster);
        } else {
            holder.ivPoster.setImageDrawable(placeholder);
            Log.e("CinePass_Adapter", "ImageUrl is NULL for: " + movie.title);
        }

        holder.itemView.setOnClickListener(v -> listener.onMovieClick(movie));
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvGenre;
        ImageView ivPoster;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvMovieTitle);
            tvGenre = itemView.findViewById(R.id.tvMovieGenre);
            ivPoster = itemView.findViewById(R.id.ivMoviePoster);
        }
    }
}
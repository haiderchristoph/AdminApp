package com.example.canteenchecker.adminapp.ui;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.canteenchecker.adminapp.CanteenCheckerAdminApplication;
import com.example.canteenchecker.adminapp.core.Review;
import com.example.canteenchecker.adminapp.proxy.ServiceProxyFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.canteenchecker.adminapp.R;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EditReviewsActivity extends AppCompatActivity {
    private static final String TAG = EditReviewsActivity.class.toString();

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, EditReviewsActivity.class);
        return intent;
    }

    private final ReviewsAdapter reviewsAdapter = new ReviewsAdapter();

    //private SwipeRefreshLayout srlSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_reviews);

        RecyclerView rcvReviews = findViewById(R.id.rcvReviews);
        rcvReviews.setLayoutManager(new LinearLayoutManager(this));
        rcvReviews.setAdapter(reviewsAdapter);


        updateReviews();

        /*FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
         */


    }

    private void updateReviews() {
        //srlSwipeRefreshLayout.setRefreshing(true);

        new AsyncTask<String, Void, Collection<Review>>() {
            @Override
            protected Collection<Review> doInBackground(String... strings) {
                try {
                    String authToken = ((CanteenCheckerAdminApplication) getApplication()).getAuthenticationToken();
                    return ServiceProxyFactory.createProxy().getCanteenReviews(authToken);
                } catch (IOException e) {
                    Log.e(TAG, "Downloading of reviews failed.", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Collection<Review> canteens) {
                reviewsAdapter.displayCanteens(canteens);   // ToDo: rename
                Log.i(TAG, "Reviews loaded");
                if (canteens != null) {
                    Log.i(TAG, "Reviews loaded: " + canteens.size());
                }
                //srlSwipeRefreshLayout.setRefreshing(false);
            }
        }.execute();
    }

    private class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {
        private final List<Review> reviewList = new ArrayList<>();

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //Log.d(TAG, "onCreateViewHolder");
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            //Log.d(TAG, "onBindViewHolder " + position + " " + holder);
            holder.updateView(reviewList.get(position));
        }

        @Override
        public int getItemCount() {
            return reviewList.size();
        }

        void displayCanteens(Collection<Review> canteens) {
            reviewList.clear();
            if (canteens != null) {
                reviewList.addAll(canteens);
            }
            notifyDataSetChanged();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView txvCreator = itemView.findViewById(R.id.txvCreator);
            private final TextView txvRemark = itemView.findViewById(R.id.txvRemark);
            private final RatingBar rtbRating = itemView.findViewById(R.id.rtbRating);
            private final TextView txvRating = itemView.findViewById(R.id.txvRating);
            private final TextView txvCreationDate = itemView.findViewById(R.id.txvCreationDate);
            private final ImageButton btnRemoveRating = itemView.findViewById(R.id.btnRemoveRating);

            ViewHolder(@NonNull View itemView) {
                super(itemView);
            }

            void updateView(final Review review) {
                txvCreator.setText(review.getCreator());
                txvRemark.setText(review.getRemark());
                rtbRating.setRating(review.getRating());
                txvRating.setText(NumberFormat.getNumberInstance().format(review.getRating()));
                txvCreationDate.setText(review.getCreationDate());

                btnRemoveRating.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        removeRating(review.getId());
                    }
                });
            }

            private void removeRating(String id) {
                new AsyncTask<String, Void, Void>() {
                    @Override
                    protected Void doInBackground(String... strings) {
                        // ToDo check if parameters can be removed (Strings)
                        try {
                            // ToDo validating the data, also in the FE
                            String authToken = ((CanteenCheckerAdminApplication) getApplication()).getAuthenticationToken();
                            boolean done = ServiceProxyFactory.createProxy().removeCanteenReview(authToken, id);
                            // ToDo add a toast or something
                        } catch (IOException e) {
                            Log.e(TAG, String.format("Updating dish failed"), e);
                        }
                        // TODO add a toast or something
                        return null;
                    }
                }.execute();
            }
        }
    }
}
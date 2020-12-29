package com.example.canteenchecker.adminapp.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.canteenchecker.adminapp.CanteenCheckerAdminApplication;
import com.example.canteenchecker.adminapp.R;
import com.example.canteenchecker.adminapp.core.Broadcasting;
import com.example.canteenchecker.adminapp.core.CanteenDetails;
import com.example.canteenchecker.adminapp.core.Review;
import com.example.canteenchecker.adminapp.proxy.ServiceProxyFactory;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class EditReviewsActivity extends AppCompatActivity {
    private static final String TAG = EditReviewsActivity.class.toString();

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, EditReviewsActivity.class);
        return intent;
    }

    private final ReviewsAdapter reviewsAdapter = new ReviewsAdapter();

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            CanteenDetails canteen = ((CanteenCheckerAdminApplication) getApplication()).getCanteenDetails();
            String canteenId = canteen.getId();
            if (canteenId != null && canteenId.equals(Broadcasting.extractCanteenId(intent))) {
                updateReviews();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_reviews);

        RecyclerView rcvReviews = findViewById(R.id.rcvReviews);
        rcvReviews.setLayoutManager(new LinearLayoutManager(this));
        rcvReviews.setAdapter(reviewsAdapter);

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, Broadcasting.createCanteenChangedBroadcastIntentFilter());

        updateReviews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private void updateReviews() {
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
            }
        }.execute();
    }

    private class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {
        private final List<Review> reviewList = new ArrayList<>();

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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

        private String getFormattedDate(String value) {
            // same format as on web Admin Dashboard
            try {
                Date date = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")).parse(value);
                return new SimpleDateFormat("dd.mm.yyyy hh:mm:ss").format(date);
            } catch (Exception e) {
                return value;
            }
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
                txvCreationDate.setText(getFormattedDate(review.getCreationDate()));
                btnRemoveRating.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        removeRating(review.getId());
                    }
                });
            }

            private void removeRating(String id) {
                new AlertDialog.Builder(EditReviewsActivity.this)
                        .setTitle(R.string.dialog_title)
                        .setMessage(R.string.dialog_text)
                        .setIcon(R.drawable.baseline_warning_24)
                        .setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                new AsyncTask<String, Void, Void>() {
                                    @Override
                                    protected Void doInBackground(String... strings) {
                                        try {
                                            ServiceProxyFactory.createProxy().removeCanteenReview(strings[0], strings[1]);
                                            updateReviews();
                                        } catch (IOException e) {
                                            Log.e(TAG, String.format("Updating dish failed"), e);
                                        }
                                        return null;
                                    }
                                }.execute(((CanteenCheckerAdminApplication) getApplication()).getAuthenticationToken(), id);
                            }
                        })
                        .setNegativeButton(R.string.dialog_dismiss, null).show();

            }
        }
    }
}
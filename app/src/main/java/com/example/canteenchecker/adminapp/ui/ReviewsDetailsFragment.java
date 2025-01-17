package com.example.canteenchecker.adminapp.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

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

public class ReviewsDetailsFragment extends Fragment {
    private static final String TAG = ReviewsDetailsFragment.class.toString();

    public ReviewsDetailsFragment() {
        // Required empty public constructor
    }

    private final ReviewsAdapter reviewsAdapter = new ReviewsAdapter();

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            CanteenDetails canteen = ((CanteenCheckerAdminApplication) getActivity().getApplication()).getCanteenDetails();
            String canteenId = canteen.getId();
            if (canteenId != null && canteenId.equals(Broadcasting.extractCanteenId(intent))) {
                updateReviews();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reviews_details, container, false);

        RecyclerView rcvReviews = view.findViewById(R.id.rcvReviews);
        rcvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvReviews.setAdapter(reviewsAdapter);

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver, Broadcasting.createCanteenChangedBroadcastIntentFilter());

        updateReviews();
        return view;
    }

    private void updateReviews() {
        new AsyncTask<String, Void, Collection<Review>>() {
            @Override
            protected Collection<Review> doInBackground(String... strings) {
                try {
                    return ServiceProxyFactory.createProxy().getCanteenReviews(strings[0]);
                } catch (IOException e) {
                    Log.e(TAG, "Downloading of reviews failed.", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Collection<Review> reviews) {
                reviewsAdapter.displayReviews(reviews);
                Log.i(TAG, "Reviews loaded");
                if (reviews != null) {
                    Log.i(TAG, "Reviews loaded: " + reviews.size());
                }
            }
        }.execute(((CanteenCheckerAdminApplication) getActivity().getApplication()).getAuthenticationToken());
    }

    private class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {
        private final List<Review> reviewList = new ArrayList<>();

        @NonNull
        @Override
        public ReviewsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
            return new ReviewsAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReviewsAdapter.ViewHolder holder, int position) {
            holder.updateView(reviewList.get(position));
        }

        @Override
        public int getItemCount() {
            return reviewList.size();
        }

        void displayReviews(Collection<Review> reviews) {
            reviewList.clear();
            if (reviews != null) {
                reviewList.addAll(reviews);
            }
            notifyDataSetChanged();
        }

        private String getFormattedDate(String value) {
            // same format as on web Admin Dashboard
            try {
                Date date = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")).parse(value);
                return new SimpleDateFormat("dd.MM.yyyy hh:mm:ss").format(date);
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
                new AlertDialog.Builder(getContext())
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
                                }.execute(((CanteenCheckerAdminApplication) getActivity().getApplication()).getAuthenticationToken(), id);
                            }
                        })
                        .setNegativeButton(R.string.dialog_dismiss, null).show();
            }
        }
    }
}
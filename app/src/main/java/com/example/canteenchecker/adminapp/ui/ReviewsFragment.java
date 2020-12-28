package com.example.canteenchecker.adminapp.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;
/*import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.canteenchecker.consumerapp.CanteenCheckerApplication;
import com.example.canteenchecker.consumerapp.R;
import com.example.canteenchecker.consumerapp.core.Broadcasting;
import com.example.canteenchecker.consumerapp.core.ReviewData;
import com.example.canteenchecker.consumerapp.proxy.ServiceProxyFactory;
*/
import com.example.canteenchecker.adminapp.CanteenCheckerAdminApplication;
import com.example.canteenchecker.adminapp.R;
import com.example.canteenchecker.adminapp.core.ReviewData;
import com.example.canteenchecker.adminapp.proxy.ServiceProxyFactory;

import java.io.IOException;
import java.text.NumberFormat;

public class ReviewsFragment extends Fragment {
    private static final String TAG = ReviewsFragment.class.toString();
    private static final String CANTEEN_ID_KEY = "CanteenId";
    private static final int LOGIN_FOR_REVIEW_CREATION = 4711;  // just some number, nothing specific

    public static Fragment create() {
        ReviewsFragment reviewsFragment = new ReviewsFragment();
        Bundle arguments = new Bundle();
        //arguments.putString(CANTEEN_ID_KEY, canteenId);
        reviewsFragment.setArguments(arguments);
        return reviewsFragment;
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String canteenId = getCanteenId();
            /*if (canteenId != null && canteenId.equals(Broadcasting.extractCanteenId(intent))) {
                updateReviews();
            }

             */
        }
    };

    private TextView txvAverageRating;
    private RatingBar rtbAverageRating;
    private TextView txvTotalRatings;
    private ProgressBar prbRatingsOne;
    private ProgressBar prbRatingsTwo;
    private ProgressBar prbRatingsThree;
    private ProgressBar prbRatingsFour;
    private ProgressBar prbRatingsFive;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reviews, container, false);
        txvAverageRating = view.findViewById(R.id.txvAverageRating);
        rtbAverageRating = view.findViewById(R.id.rtbAverageRating);
        txvTotalRatings = view.findViewById(R.id.txvTotalRatings);
        prbRatingsOne = view.findViewById(R.id.prbRatingsOne);
        prbRatingsTwo = view.findViewById(R.id.prbRatingsTwo);
        prbRatingsThree = view.findViewById(R.id.prbRatingsThree);
        prbRatingsFour = view.findViewById(R.id.prbRatingsFour);
        prbRatingsFive = view.findViewById(R.id.prbRatingsFive);

        view.findViewById(R.id.btnShowReviews).setOnClickListener(v -> showReviews());
        //LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, Broadcasting.createCanteenChangedBroadcastIntentFilter());
        updateReviews();

        return view;
    }

    private void showReviews() {
    }

    private String getCanteenId(){
        return getArguments().getString(CANTEEN_ID_KEY);
    }

    @SuppressWarnings("StaticFieldLeak")
    private void updateReviews() {
        new AsyncTask<String, Void, ReviewData>() {

            @Override
            protected ReviewData doInBackground(String... strings) {
                try {
                    String authToken = ((CanteenCheckerAdminApplication) getActivity().getApplication()).getAuthenticationToken();
                    return ServiceProxyFactory.createProxy().getCanteenReviewsData(authToken);
                } catch(IOException e) {
                    Log.e(TAG, String.format("Download of reviews for canteen failed."), e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(ReviewData reviewData) {
                if (reviewData != null) {
                    txvAverageRating.setText(NumberFormat.getNumberInstance().format(reviewData.getAverageRating()));
                    rtbAverageRating.setRating(reviewData.getAverageRating());
                    txvTotalRatings.setText(NumberFormat.getNumberInstance().format(reviewData.getTotalRatings()));

                    prbRatingsOne.setMax(reviewData.getTotalRatings());
                    prbRatingsOne.setProgress(reviewData.getRatingsOne());

                    prbRatingsTwo.setMax(reviewData.getTotalRatings());
                    prbRatingsTwo.setProgress(reviewData.getRatingsTwo());

                    prbRatingsThree.setMax(reviewData.getTotalRatings());
                    prbRatingsThree.setProgress(reviewData.getRatingsThree());

                    prbRatingsFour.setMax(reviewData.getTotalRatings());
                    prbRatingsFour.setProgress(reviewData.getRatingsFour());

                    prbRatingsFive.setMax(reviewData.getTotalRatings());
                    prbRatingsFive.setProgress(reviewData.getRatingsFive());
                } else {
                    txvAverageRating.setText(null);
                    rtbAverageRating.setRating(0);
                    txvTotalRatings.setText(null);

                    prbRatingsOne.setMax(1);
                    prbRatingsOne.setProgress(0);

                    prbRatingsTwo.setMax(1);
                    prbRatingsTwo.setProgress(0);

                    prbRatingsThree.setMax(1);
                    prbRatingsThree.setProgress(0);

                    prbRatingsFour.setMax(1);
                    prbRatingsFour.setProgress(0);

                    prbRatingsFive.setMax(1);
                    prbRatingsFive.setProgress(0);
                }
            }
        }.execute(getCanteenId());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOGIN_FOR_REVIEW_CREATION && resultCode == Activity.RESULT_OK) {
            //createReview();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver); // ! ! !
    }

    /*@SuppressWarnings("StaticFieldLeak")
    private void createReview() {
        final Activity activity = getActivity();
        if (! ((CanteenCheckerAdminApplication) activity.getApplication()).isAuthenticated()) {
            //startActivityForResult(LoginActivity.createIntent(activity), LOGIN_FOR_REVIEW_CREATION);
        } else {
            final View view = LayoutInflater.from(activity).inflate(R.layout.dialog_add_review, null);
            new AlertDialog.Builder(activity).setTitle(R.string.dialog_add_review).setView(view).setPositiveButton(R.string.text_send, (dialog, which) -> {
                dialog.dismiss();
                new AsyncTask<Object, Void, Boolean>() {
                    @SuppressLint("StaticFieldLeak")
                    @Override
                    protected Boolean doInBackground(Object... objects) {
                        try {
                            ServiceProxyFactory.createProxy().createReview((String) objects[0], (String) objects[1], (int) objects[2], (String) objects[3]);
                            return true;
                        } catch (IOException e) {
                            Log.e(TAG, "Review creation failed.", e);
                            return false;
                        }
                    }

                    @Override
                    protected void onPostExecute(Boolean aBoolean) {
                        Toast.makeText(activity, aBoolean ? R.string.message_review_created : R.string.message_review_not_created, Toast.LENGTH_SHORT).show();
                        //LocalBroadcastManager.getInstance(activity).sendBroadcast(Broadcasting.createCanteenChangedBroadcastIntent(getCanteenId()));
                    }
                }.execute(((CanteenCheckerAdminApplication) activity.getApplication()).getAuthenticationToken(),
                        getCanteenId(),
                        Math.round(((RatingBar) view.findViewById(R.id.rtbRating)).getRating()),
                        ((EditText) view.findViewById(R.id.edtRemark)).getText().toString());
            }).create().show();
        }
    }

     */
}

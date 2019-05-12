package fragments.java;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.mostafa.e_commerce.NavigationHost;
import com.example.mostafa.e_commerce.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import database.JSONToObject;
import io.reactivex.disposables.CompositeDisposable;
import okhttp3.ResponseBody;
import product.structure.Product;
import product.structure.Review;
import wholesale.callback.DataCallBack;

public class ReviewsFragment extends Fragment implements View.OnClickListener, DataCallBack {

    private ReviewsAdapter adapter;
    private static Product product;
    private Group unavaGroup;
    private static final String TAG = ReviewsFragment.class.getSimpleName();

    public ReviewsFragment() {
    }

    public static ReviewsFragment newInstance(Product newProduct) {
        ReviewsFragment reviewsFragment = new ReviewsFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable("prod", product);
        reviewsFragment.setArguments(bundle);
        product = newProduct;


        return reviewsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.product_review_fragment, container, false);
        initViews(view);

        try {
            product.fetchReviews(getActivity(), this);

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return view;
    }

    private void initViews(View view) {
        FloatingActionButton addReview = view.findViewById(R.id.add_review_btn);
        addReview.setOnClickListener(this);

        unavaGroup = view.findViewById(R.id.uava_group);

        RecyclerView reviewsList = view.findViewById(R.id.reviews_list);
        reviewsList.setHasFixedSize(true);
        reviewsList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        adapter = new ReviewsAdapter(getActivity(), new ArrayList<>());
        reviewsList.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.add_review_btn) {
            ((NavigationHost) getActivity()).navigateTo(AddReviewFragment.newInstance(product.getId()), true);
        }
    }

    @Override
    public void onDataExtracted(ResponseBody responseBody, CompositeDisposable compositeDisposable) {
        try {
            JSONObject reviewData = new JSONObject(responseBody.string());

            if (reviewData.getBoolean("reviewState")) {
                ArrayList<Review> reviews = JSONToObject.convertToReviews(reviewData.getJSONArray("reviewResult"));
                Log.d(TAG, "onDataExtracted reviews size: " + reviews.size());
                adapter.addManyReviews(reviews);
                return;
            }
            Log.e(TAG, "Review not fetched");
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDataNotExtracted(Throwable error) {
        Log.e(TAG, error.getMessage(), error);
        if (adapter.getItemCount() > 0)
            unavaGroup.setVisibility(View.VISIBLE);
    }

    private class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {

        private ArrayList<Review> reviews;
        private Context context;
        private Handler handler = new Handler(Looper.getMainLooper());

        void addManyReviews(ArrayList<Review> newReviews) {
            Log.d(TAG, "adding reviews");
            reviews.addAll(newReviews);
            notifyDataSetChanged();
            Log.d(TAG, "length: " + getItemCount());
        }

        ReviewsAdapter(Context context, ArrayList<Review> reviews) {
            this.reviews = reviews;
            this.context = context;
        }

        @NonNull
        @Override
        public ReviewsAdapter.ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.review_item, parent, false);

            return new ReviewViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReviewsAdapter.ReviewViewHolder holder, int position) {
            Review review = reviews.get(position);

            ScaleAnimation anim = new ScaleAnimation(
                    0.0f
                    , 1.0f
                    , 0.0f
                    , 1.0f
                    , Animation.RELATIVE_TO_SELF
                    , 0.5f
                    , Animation.RELATIVE_TO_SELF
                    , 0.5f
            );
            anim.setDuration(500);
            new Thread(() ->
                    holder.reviewLayout.post(() ->
                            holder.reviewLayout.startAnimation(anim))).start();

            handler.post(() -> {
                holder.productRating.setRating(review.getReviewNum());
                holder.reviewerName.setText(review.getReviewerName());
                if (review.getReviewText() != null)
                    if (!review.getReviewText().isEmpty()) {
                        holder.reviewText.setText(review.getReviewText());
                    }
            });
        }

        @Override
        public int getItemCount() {
            return reviews.size();
        }

        class ReviewViewHolder extends RecyclerView.ViewHolder {

            TextView reviewerName;
            TextView reviewText;
            RatingBar productRating;
            ConstraintLayout reviewLayout;

            ReviewViewHolder(@NonNull View view) {
                super(view);

                reviewLayout = view.findViewById(R.id.review_layout);
                reviewerName = view.findViewById(R.id.reviewer_name);
                reviewText = view.findViewById(R.id.review_text);
                productRating = view.findViewById(R.id.review_rating_bar);

            }
        }
    }
}

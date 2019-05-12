package fragments.java;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.mostafa.e_commerce.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import io.reactivex.disposables.CompositeDisposable;
import okhttp3.ResponseBody;
import product.structure.Review;
import user.structure.Buyer;
import user.structure.Session;
import wholesale.callback.DataCallBack;

public class AddReviewFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = AddReviewFragment.class.getSimpleName();
    private Buyer buyer;
    private static String productId;
    private TextInputEditText reviewInput;
    private RatingBar ratingBar;

    public AddReviewFragment() {
    }

    public static AddReviewFragment newInstance(String newProductId) {
        AddReviewFragment addReviewFragment = new AddReviewFragment();

        productId = newProductId;

        return addReviewFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_review_fragment, container, false);
        try {
            buyer = (Buyer) Session.getUser(Buyer.class);
            Log.d(TAG, buyer.getUserId());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        initViews(view);

        return view;
    }

    private void initViews(View view) {
        MaterialButton submitReview = view.findViewById(R.id.submit_review_task);
        submitReview.setOnClickListener(this);
        MaterialButton cancelReview = view.findViewById(R.id.cancel_review_task);
        cancelReview.setOnClickListener(this);
        reviewInput = view.findViewById(R.id.review_text_edit);
        ratingBar = view.findViewById(R.id.product_details_rating_bar);
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.submit_review_task) {
            Review review = new Review(((int) ratingBar.getRating()) == 0 ? 1 : (int) ratingBar.getRating()
                    , String.valueOf(reviewInput.getText()));
            try {
                buyer.reviewAProduct(getActivity(), new DataCallBack() {
                    @Override
                    public void onDataExtracted(ResponseBody responseBody, CompositeDisposable compositeDisposable) {
                        try {
                            JSONObject addedReview = new JSONObject(responseBody.string());
                            if (addedReview.getBoolean("reviewState")) {
                                getActivity().getSupportFragmentManager().popBackStack();
                                return;
                            }
                            Toast.makeText(getActivity(), "Review not added.", Toast.LENGTH_LONG).show();
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onDataNotExtracted(Throwable error) {
                        Log.e(TAG, error.getMessage(), error);
                    }
                }, review, productId);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        } else if (view.getId() == R.id.cancel_review_task) {
            assert getFragmentManager() != null;
            getFragmentManager().popBackStack();
        }
    }
}

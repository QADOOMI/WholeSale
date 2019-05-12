package database;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import product.structure.Review;
import wholesale.callback.CallBack;
import wholesale.callback.DataCallBack;

public final class ProductReview extends DatabaseAction {

    private static final String TAG = ProductReview.class.getSimpleName();
    private Review review;
    private String productId;
    private String buyerId;

    public ProductReview(Activity activity, Review review, String buyerId, String productId) {
        super(activity);
        this.review = review;
        this.productId = productId;
        this.buyerId = buyerId;
    }

    public ProductReview(Activity activity, String productId) {
        super(activity);
        this.productId = productId;
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onAccess(CallBack dataCallBack) {
        backgroundTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                if (review != null) {
                    Log.d(TAG, "Buyer ID: " + buyerId);
                    compositeSubs.add(myAPI.addReview(
                            productId
                            , buyerId
                            , review.getReviewText()
                            , String.valueOf(review.getReviewNum())
                            , false).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(reviewState -> {   // onNext method
                                        Log.d(TAG, "onNext: " + reviewState.toString());
                                        ((DataCallBack) dataCallBack).onDataExtracted(reviewState, compositeSubs);
                                    }
                                    , throwable -> {   // onError method
                                        Log.e(TAG, throwable.getMessage(), throwable);
                                        ((DataCallBack) dataCallBack).onDataNotExtracted(throwable);
                                    }, () -> {          // onComplete method
                                        Log.d(TAG, "onComplete");
                                    }));
                } else {
                    compositeSubs.add(myAPI.fetchReview(productId, true)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(reviewState -> {   // onNext method
                                        Log.d(TAG, "onNext: " + reviewState.toString());
                                        ((DataCallBack) dataCallBack).onDataExtracted(reviewState, compositeSubs);
                                    }
                                    , throwable -> {   // onError method
                                        Log.e(TAG, throwable.getMessage(), throwable);
                                        ((DataCallBack) dataCallBack).onDataNotExtracted(throwable);
                                    }, () -> {          // onComplete method
                                        Log.d(TAG, "onComplete");
                                    }));
                }
                return null;
            }

        }.execute();
    }
}


package database;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import wholesale.callback.CallBack;
import wholesale.callback.DataCallBack;

public final class WishProduct extends DatabaseAction {

    private boolean wishIt;
    private String productId;
    private String productType;
    private String buyerId;
    private final static String TAG = WishProduct.class.getSimpleName();
    public static final int DELETE_ALL_WISHED = 1;
    public static final int WISH_IT_OR_NOT = 2;
    public static final int FETCH_WISHED_PRODUCTS = 3;
    private final int requestCode;

    // wish product or not
    public WishProduct(Activity activity, String productId, String buyerId, boolean wishIt) {
        super(activity);
        this.productId = productId;
        this.buyerId = buyerId;
        this.wishIt = wishIt;
        this.requestCode = WISH_IT_OR_NOT;
    }
    // delete all wished products
    public WishProduct(Activity activity, String buyerId, int requestCode) {
        super(activity);
        this.buyerId = buyerId;
        this.requestCode = requestCode;
    }


    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onAccess(CallBack callBack) {
        backgroundTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (requestCode == DELETE_ALL_WISHED) {
                    compositeSubs.add(myAPI.deleteWishedProducts(buyerId)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(wishMsg -> {   // onNext method
                                        ((DataCallBack) callBack).onDataExtracted(wishMsg, compositeSubs);
                                    }
                                    , throwable -> {   // onError method
                                        Log.e(TAG, throwable.getMessage(), throwable);
                                        ((DataCallBack) callBack).onDataNotExtracted(throwable);
                                    }, () -> {          // onComplete method
                                        Log.d(TAG, "onCompleted");
                                    }));
                } else if (requestCode == WISH_IT_OR_NOT) {
                    Log.d(TAG, "requestCode: " + requestCode + "wishIt: " + wishIt);
                    // onError method
                    compositeSubs.add(myAPI.wishOrUnwish(wishIt, buyerId, productId)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(wishMsg -> {   // onNext method
                                        ((DataCallBack) callBack).onDataExtracted(wishMsg, compositeSubs);
                                    }
                                    , ((DataCallBack) callBack)::onDataNotExtracted
                                    , () -> {          // onComplete method
                                        Log.d(TAG, "onCompleted");
                                    }));
                } else if(requestCode == FETCH_WISHED_PRODUCTS){
                    Log.d(TAG, "fetchAll");
                    // onError method
                    compositeSubs.add(myAPI.fetchAllWished(buyerId)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(wishResult -> {   // onNext method
                                        ((DataCallBack) callBack).onDataExtracted(wishResult, compositeSubs);
                                    }
                                    , ((DataCallBack) callBack)::onDataNotExtracted
                                    , () -> {          // onComplete method
                                        Log.d(TAG, "onCompleted");
                                    }));
                }
                return null;
            }
        }.execute();
    }
}

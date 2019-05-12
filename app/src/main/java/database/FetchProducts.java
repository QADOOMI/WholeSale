package database;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import product.structure.Product;
import wholesale.callback.CallBack;
import wholesale.callback.DataCallBack;

public final class FetchProducts extends DatabaseAction {

    private String buyerId;
    private static final String TAG = FetchProducts.class.getSimpleName();
    public static final int FETCH_MAIN_PRODUCTS = 530;
    public static final int FETCH_ALL_PRODUCTS = 533;
    public static final int FETCH_A_PRODUCT = 535;
    private int requestCode;

    // fetch all products
    public FetchProducts(Activity activity, Product product, String buyerId) {
        super(activity, product);
        this.requestCode = FETCH_ALL_PRODUCTS;
        if (buyerId != null)
            this.buyerId = buyerId.trim();
    }

    // fetch all main products AND product details =>(based on request code will run)
    public FetchProducts(Activity activity, Product product, String buyerId, int requestCode) {
        super(activity, product);
        if (buyerId != null)
            this.buyerId = buyerId;
        this.requestCode = requestCode;
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onAccess(final CallBack callBack) {
        backgroundTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(final Void... classes) {
                if (requestCode == FETCH_ALL_PRODUCTS) {
                    // onError method
                    compositeSubs.add(myAPI.fetchProductsByType(buyerId, product.getType())
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(fetchedData -> {   // onNext method
                                        ((DataCallBack) callBack).onDataExtracted(fetchedData, compositeSubs);
                                    }
                                    , ((DataCallBack) callBack)::onDataNotExtracted
                                    , () -> {          // onComplete method
                                        Log.d(TAG, "fetch all data completed");
                                    }));
                } else if (requestCode == FETCH_MAIN_PRODUCTS) {
                    // onError method
                    compositeSubs.add(myAPI.fetchMainPageData(product.getType())
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(fetchedData -> {   // onNext method
                                        ((DataCallBack) callBack).onDataExtracted(fetchedData, compositeSubs);
                                    }
                                    , ((DataCallBack) callBack)::onDataNotExtracted
                                    , () -> {          // onComplete method
                                        Log.d(TAG, "fetch main data completed");
                                    }));
                } else if (requestCode == FETCH_A_PRODUCT) {
                    // onError method
                    compositeSubs.add(myAPI.fetchAProduct(product.getId(), buyerId, product.getType())
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(fetchedData -> {   // onNext method
                                        ((DataCallBack) callBack).onDataExtracted(fetchedData, compositeSubs);
                                    }
                                    , ((DataCallBack) callBack)::onDataNotExtracted
                                    , () -> {          // onComplete method
                                        Log.d(TAG, "fetch main data completed");
                                    }));
                }
                return null;
            }

        }.execute();
    }
}

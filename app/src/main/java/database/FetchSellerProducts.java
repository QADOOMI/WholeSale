package database;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import user.structure.Seller;
import wholesale.callback.CallBack;
import wholesale.callback.DataCallBack;

public final class FetchSellerProducts extends DatabaseAction {

    private static final String TAG = FetchSellerProducts.class.getSimpleName();
    private String requestCode;
    public final static String FETCH_PUBLISHED = "fetchPublished";
    public final static String FETCH_SOLD = "fetchSold";

    public FetchSellerProducts(Activity activity, Seller seller, @NonNull String requestCode) {
        super(activity, seller);
        this.requestCode = requestCode.trim();
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onAccess(CallBack callBack) {
        backgroundTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                compositeSubs.add(myAPI.fetchSellerProducts(((Seller) user).getUserId(), requestCode)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(fetchState -> {
                                    ((DataCallBack) callBack).onDataExtracted(fetchState, compositeSubs);
                                }, ((DataCallBack) callBack)::onDataNotExtracted,
                                () -> {
                                    Log.d(TAG, "onCompleted");
                                }));
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                // this.cancel(false);
            }
        }.execute();
    }
}

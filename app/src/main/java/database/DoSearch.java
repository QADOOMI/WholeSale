package database;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import wholesale.callback.CallBack;
import wholesale.callback.DataCallBack;

public final class DoSearch extends DatabaseAction {

    private final String keyValue;
    private static final String TAG = DoSearch.class.getSimpleName();

    public DoSearch(Activity activity, String keyValue) {
        super(activity);
        this.keyValue = keyValue;
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onAccess(CallBack callBack) {
        backgroundTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                compositeSubs.add(myAPI.search(keyValue)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(searchResult -> {
                            ((DataCallBack) callBack).onDataExtracted(searchResult, compositeSubs);
                        }, ((DataCallBack) callBack)::onDataNotExtracted, () -> {
                            Log.d(TAG, "onComplete.");
                        }));
                return null;
            }
        }.execute();
    }
}

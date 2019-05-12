package database;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.example.mostafa.e_commerce.SellerMainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import user.structure.Buyer;
import user.structure.Seller;
import wholesale.callback.CallBack;
import wholesale.callback.DataCallBack;

public final class MessagesFunctions extends DatabaseAction {

    public static final int FETCH_MESSAGES = 11;
    public static final int FETCH_CONTACTS = 22;
    private String TAG = MessagesFunctions.class.getSimpleName();
    private int requestCode;
    private String receiverId;
    private String senderId;

    public MessagesFunctions(Activity activity, String senderId, String receiverId, int requestCode) {
        super(activity);
        this.requestCode = requestCode;
        this.receiverId = receiverId;
        this.senderId = senderId;
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onAccess(CallBack callBack) {
        backgroundTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (requestCode == FETCH_CONTACTS) {
                    try {
                        compositeSubs.add(myAPI.fetchContacts(convertToContacts(senderId, null))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(fetchedData -> {
                                            ((DataCallBack) callBack).onDataExtracted(fetchedData, compositeSubs);
                                        }, ((DataCallBack) callBack)::onDataNotExtracted
                                        , () -> {
                                            Log.d(TAG, "onComplete");
                                        }));
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                } else if (requestCode == FETCH_MESSAGES) {
                    try {
                        compositeSubs.add(myAPI.fetchMessages(convertToContacts(senderId, receiverId))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(fetchedData -> {
                                            ((DataCallBack) callBack).onDataExtracted(fetchedData, compositeSubs);
                                        }, ((DataCallBack) callBack)::onDataNotExtracted
                                        , () -> {
                                            Log.d(TAG, "onComplete");
                                        }));
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
                return null;
            }

            private JSONObject convertToContacts(String senderId, String receiverId) throws JSONException {
                JSONObject userData = new JSONObject();

                userData.put("type", (activity instanceof SellerMainActivity)
                        ? Seller.class.getSimpleName()
                        : Buyer.class.getSimpleName());
                userData.put("senderId", senderId);
                userData.put("receiverId", receiverId);

                return userData;
            }
        }.execute();
    }
}

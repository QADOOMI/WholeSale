package database;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.mostafa.e_commerce.R;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import user.structure.Buyer;
import user.structure.Seller;
import user.structure.User;
import wholesale.callback.AuthListener;
import wholesale.callback.CallBack;
import wholesale.callback.DataCallBack;

public class Authenticate extends DatabaseAction {

    private final int requestCode;
    private ProgressDialog progressDialog;
    private Handler handler = new Handler(Looper.getMainLooper());
    private static final String TAG = Authenticate.class.getSimpleName();
    public static final int SIGN_IN_REQ = 163;
    public static final int SIGN_UP_REQ = 164;
    public static final int DEL_ACC_REQ = 165;

    public Authenticate(Activity activity, User user, int requestCode) {
        super(activity, user);
        this.requestCode = requestCode;
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onAccess(final CallBack callBack) {
        backgroundTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                int title = 0;
                int msg = 0;
                if (requestCode == SIGN_IN_REQ) {
                    title = R.string.sign_in_user_title_dialog;
                    msg = R.string.sign_in_user_msg_dialog;
                } else if (requestCode == SIGN_UP_REQ) {
                    title = R.string.sign_up_button;
                    msg = R.string.sign_up_user_msg_dialog;
                }
                if (requestCode != DEL_ACC_REQ) {
                    progressDialog = new ProgressDialog(activity);
                    progressDialog.setTitle(title);
                    progressDialog.setMessage(activity.getResources().getString(msg));
                    progressDialog.setCanceledOnTouchOutside(false);
                    handler.post(() -> {
                        if (!progressDialog.isShowing())
                            progressDialog.show();
                    });
                }
            }

            @Override
            protected Void doInBackground(Void... voids) {
                if (requestCode == SIGN_IN_REQ) {
                    compositeSubs.add(myAPI.userSignIn(user.getEmail(), user.getPassword(), user.bringType().getSimpleName())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(signInState -> {   // onNext method
                                ((AuthListener) callBack).onSignIn(signInState, compositeSubs);
                            }, throwable -> {   // onError method
                                ((AuthListener) callBack).onErrorOccured(throwable);
                                handler.post(() -> {
                                    if (progressDialog.isShowing())
                                        progressDialog.dismiss();
                                });
                            }, () -> {          // onComplete method
                                handler.post(() -> {
                                    if (progressDialog.isShowing())
                                        progressDialog.dismiss();
                                });
                            }));
                } else if (requestCode == SIGN_UP_REQ) {
                    signUpUser(callBack);
                } else if (requestCode == DEL_ACC_REQ) {
                    compositeSubs.add(myAPI.deleteAccount(user.getUserId(), user.bringType().getSimpleName())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(deleteState -> {   // onNext method
                                        ((DataCallBack) callBack).onDataExtracted(deleteState, compositeSubs);
                                    }
                                    , ((DataCallBack) callBack)::onDataNotExtracted, () -> {          // onComplete method
                                        Log.d(TAG, "completed");
                                    }));
                }
                return null;
            }
        }.execute();
    }

    private void signUpUser(CallBack callBack) {
        if (user instanceof Seller) {
            Seller seller = (Seller) user;
            compositeSubs.add(myAPI.sellerSignUp(seller.getEmail(), seller.getFirstName()
                    , seller.getLastName(), seller.getSellerInfo().getComapnyName(), seller.getPassword()
                    , seller.bringType().getSimpleName(), seller.getPhoneNumber(),
                    seller.getSellerInfo().getPoBox(), seller.getSellerInfo().getFloorNum()
                    , seller.getSellerInfo().getApartmentNum(),
                    seller.getSellerInfo().getBuildingNum(), seller.getSellerInfo().getStreetNum()
                    , seller.getSellerInfo().getCreditInfo(),
                    seller.getSellerInfo().getZipCode(), seller.getSellerInfo().getTown()
                    , seller.getSellerInfo().getGovernance())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(sellerState -> {   // onNext method
                        ((AuthListener) callBack).onSignUp(sellerState, compositeSubs);
                    }, throwable -> {   // onError method
                        handler.post(() -> {
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                        });
                        ((AuthListener) callBack).onErrorOccured(throwable);
                    }, () -> {          // onComplete method
                        handler.post(() -> {
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                        });
                    }));
        } else if (user instanceof Buyer) {
            Buyer buyer = (Buyer) user;
            compositeSubs.add(myAPI.buyerSignUp(buyer.getEmail(), buyer.getFirstName()
                    , buyer.getLastName(), buyer.getPassword(), buyer.getPhoneNumber())
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(buyerState -> {   // onNext method
                        ((AuthListener) callBack).onSignUp(buyerState, compositeSubs);
                    }, throwable -> {   // onError method
                        ((AuthListener) callBack).onErrorOccured(throwable);
                        handler.post(() -> {
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                        });
                    }, () -> {          // onComplete method
                        handler.post(() -> {
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                        });
                    }));
        }
    }
}

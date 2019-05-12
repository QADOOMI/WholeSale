package database;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.mostafa.e_commerce.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import networking.RetrofitClient;
import user.structure.Buyer;
import user.structure.Seller;
import user.structure.Session;
import user.structure.User;
import wholesale.callback.CallBack;
import wholesale.callback.DataCallBack;

public final class UpdateUserData extends DatabaseAction {

    private final StorageReference storageRef;
    private int updateType;
    private ProgressDialog progressDialog;
    private Handler handler = new Handler(Looper.getMainLooper());
    private final static String TAG = "UpdateUserData";

    public UpdateUserData(Activity activity, User user, int updateType) {
        super(activity, user);
        this.updateType = updateType;
        this.storageRef = FirebaseStorage.getInstance()
                .getReferenceFromUrl("gs://e-commerce-c6abd.appspot.com/ProfileImages");
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onAccess(CallBack dataCallBack) {
        backgroundTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                progressDialog = new ProgressDialog(activity);
                progressDialog.setTitle(R.string.update_user_data_title_dialog);
                progressDialog.setMessage(activity.getResources().getString(R.string.update_user_data_msg_dialog));
                progressDialog.setCanceledOnTouchOutside(false);
                handler.post(() -> {
                    if (!progressDialog.isShowing())
                        progressDialog.show();
                });
            }

            @Override
            protected Void doInBackground(Void... classes) {
                if (user instanceof Seller) {
                    final Seller seller = (Seller) user;
                    try {
                        if (!seller.getProfileImage().equals(Session.getUser(Seller.class).getProfileImage())) {
                            storageRef.putFile(Uri.parse(user.getProfileImage()))
                                    .continueWithTask(task -> {
                                        if (task.isSuccessful()) {
                                            return storageRef.getDownloadUrl();
                                        }
                                        throw new Exception("not URL");

                                    }).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    updateSellerData(seller, task.getResult(), dataCallBack);
                                } else {
                                    Log.d(TAG, "onComplete: File not uploaded");
                                }
                            });
                            return null;
                        }
                        seller.setProfileImage(Session.getUser(Seller.class).getProfileImage());
                        updateSellerData(seller, null, dataCallBack);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (user instanceof Buyer) {
                    if (updateType == R.id.update_image_go_icon) {
                            storageRef.putFile(Uri.parse(user.getProfileImage()))
                                    .continueWithTask(task -> {
                                        if (task.isSuccessful()) {
                                            return storageRef.getDownloadUrl();
                                        }
                                        throw new Exception("not URL");

                                    }).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    compositeSubs.add(myAPI.updateUserImage("image", user.bringType().getSimpleName(), user.getUserId(), String.valueOf(task.getResult()))
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(updateState -> {   // onNext method
                                                        Log.d(TAG, "onNext: " + updateState.toString());
                                                        ((DataCallBack) dataCallBack).onDataExtracted(updateState, compositeSubs);
                                                    }
                                                    , throwable -> {   // onError method
                                                        ((DataCallBack) dataCallBack).onDataNotExtracted(throwable);
                                                        handler.post(() -> {
                                                            if (progressDialog.isShowing())
                                                                progressDialog.dismiss();
                                                        });
                                                    }
                                                    , () -> {          // onComplete method
                                                        handler.post(() -> {
                                                            if (progressDialog.isShowing())
                                                                progressDialog.dismiss();
                                                        });
                                                    }));
                                } else {
                                    Log.d(TAG, "onComplete: File not uploaded");
                                }
                            });

                    } else if (updateType == R.id.update_name_go_icon) {
                        compositeSubs.add(myAPI.updateUserName(
                                "name"
                                , user.bringType().getSimpleName()
                                , user.getUserId()
                                , user.getFirstName()
                                , user.getLastName())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(updateState -> {   // onNext method
                                            Log.d(TAG, "onNext: " + updateState.toString());
                                            ((DataCallBack) dataCallBack).onDataExtracted(updateState, compositeSubs);
                                        }
                                        , throwable -> {   // onError method
                                            ((DataCallBack) dataCallBack).onDataNotExtracted(throwable);
                                            handler.post(() -> {
                                                if (progressDialog.isShowing())
                                                    progressDialog.dismiss();
                                            });
                                        }
                                        , () -> {          // onComplete method
                                            handler.post(() -> {
                                                if (progressDialog.isShowing())
                                                    progressDialog.dismiss();
                                            });
                                        }));
                    } else if (updateType == R.id.update_password_go_icon) {
                        Log.d(TAG, "doInBackground password: " + user.getPassword());
                        compositeSubs.add(myAPI.updateUserPassword("password", user.bringType().getSimpleName(), user.getUserId(), user.getPassword())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(updateState -> {   // onNext method
                                            Log.d(TAG, "onNext: " + updateState.toString());
                                            ((DataCallBack) dataCallBack).onDataExtracted(updateState, compositeSubs);
                                        }
                                        , throwable -> {   // onError method
                                            ((DataCallBack) dataCallBack).onDataNotExtracted(throwable);
                                            handler.post(() -> {
                                                if (progressDialog.isShowing())
                                                    progressDialog.dismiss();
                                            });
                                        }
                                        , () -> {          // onComplete method
                                            handler.post(() -> {
                                                if (progressDialog.isShowing())
                                                    progressDialog.dismiss();
                                            });
                                        }));
                    } else if (updateType == R.id.update_phone_number_go_icon) {
                        compositeSubs.add(myAPI.updateUserPhoneNumber("phone", user.bringType().getSimpleName(), user.getUserId(), user.getPhoneNumber())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(updateState -> {   // onNext method
                                            Log.d(TAG, "onNext: " + updateState.toString());
                                            ((DataCallBack) dataCallBack).onDataExtracted(updateState, compositeSubs);
                                        }
                                        , throwable -> {   // onError method
                                            ((DataCallBack) dataCallBack).onDataNotExtracted(throwable);
                                            handler.post(() -> {
                                                if (progressDialog.isShowing())
                                                    progressDialog.dismiss();
                                            });
                                        }
                                        , () -> {          // onComplete method
                                            handler.post(() -> {
                                                if (progressDialog.isShowing())
                                                    progressDialog.dismiss();
                                            });
                                        }));
                    } else if (updateType == R.id.update_email_go_icon) {
                        compositeSubs.add(myAPI.updateUserEmail("email", user.bringType().getSimpleName(), user.getUserId(), user.getEmail())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(updateState -> {   // onNext method
                                            Log.d(TAG, "onNext: " + updateState.toString());
                                            ((DataCallBack) dataCallBack).onDataExtracted(updateState, compositeSubs);
                                        }
                                        , throwable -> {   // onError method
                                            ((DataCallBack) dataCallBack).onDataNotExtracted(throwable);
                                            handler.post(() -> {
                                                if (progressDialog.isShowing())
                                                    progressDialog.dismiss();
                                            });
                                        }
                                        , () -> {          // onComplete method
                                            handler.post(() -> {
                                                if (progressDialog.isShowing())
                                                    progressDialog.dismiss();
                                            });
                                        }));
                    }
                }
                return null;
            }
        }.execute();

    }

    private void updateSellerData(Seller seller, Uri task, CallBack callBack) {
        Log.e(TAG, RetrofitClient.getBaseUrl());
        compositeSubs.add(myAPI.updateSeller(
                Seller.class.getSimpleName()
                , seller.getEmail()
                , seller.getFirstName()
                , seller.getLastName()
                , seller.getSellerInfo().getComapnyName()
                , seller.getPassword()
                , seller.getPhoneNumber()
                , seller.getSellerInfo().getPoBox()
                , seller.getSellerInfo().getFloorNum()
                , seller.getSellerInfo().getApartmentNum()
                , seller.getSellerInfo().getBuildingNum()
                , seller.getSellerInfo().getStreetNum()
                , seller.getSellerInfo().getCreditInfo()
                , seller.getSellerInfo().getGovernance()
                , seller.getSellerInfo().getZipCode()
                , seller.getSellerInfo().getTown()
                , seller.getUserId()
                , task == null ? seller.getProfileImage() : task.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(updateState -> {   // onNext method
                            Log.d(TAG, "onNext: " + updateState.toString());
                            ((DataCallBack) callBack).onDataExtracted(updateState, compositeSubs);
                        }
                        , throwable -> {   // onError method
                            ((DataCallBack) callBack).onDataNotExtracted(throwable);
                            handler.post(() -> {
                                if (progressDialog.isShowing())
                                    progressDialog.dismiss();
                            });
                        }
                        , () -> {          // onComplete method
                            handler.post(() -> {
                                if (progressDialog.isShowing())
                                    progressDialog.dismiss();
                            });
                        }));
    }
}


package database;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.mostafa.e_commerce.R;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import product.structure.PriceRange;
import product.structure.Product;
import user.structure.Seller;
import user.structure.Session;
import wholesale.callback.CallBack;
import wholesale.callback.DataCallBack;

public final class SellerAction extends DatabaseAction {

    public static final int DELETE = 54;
    public static final int UPDATE = 55;
    public static final int PUBLISHED_PRODS = 56;
    public static final int FETCH_PRODUCT_RANGES = 515;
    private ProgressDialog progressDialog;
    private Product oldProduct;
    private Handler handler = new Handler(Looper.getMainLooper());
    private int requestCode;

    public SellerAction(Activity activity, Product newProduct, Product oldProduct, int requestCode) {
        super(activity, newProduct);
        this.requestCode = requestCode;
        this.oldProduct = oldProduct;
    }

    public SellerAction(Activity activity, Seller seller, int requestCode) {
        super(activity, seller);
        this.requestCode = requestCode;
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onAccess(CallBack callBack) {
        backgroundTask = new AsyncTask<Void, Void, Void>() {

            private final String TAG = SellerAction.class.getSimpleName();
            private StorageReference storeRef;
            private Seller seller;

            @Override
            protected void onPreExecute() {
                int title = 0;
                int msg = 0;
                if (requestCode == DELETE) {
                    title = R.string.seller_delete_product_title;
                    msg = R.string.seller_delete_product_msg;
                } else if (requestCode == UPDATE) {
                    title = R.string.seller_update_product_title;
                    msg = R.string.seller_update_product_msg;
                } else {
                    return;
                }

                progressDialog = new ProgressDialog(activity);
                progressDialog.setTitle(title);
                progressDialog.setMessage(activity.getResources().getString(msg));
                progressDialog.setCanceledOnTouchOutside(false);
                handler.post(() -> {
                    if (!progressDialog.isShowing())
                        progressDialog.show();
                });

                try {
                    seller = ((Seller) Session.getUser(Seller.class));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                storeRef = FirebaseStorage.getInstance()
                        .getReferenceFromUrl("gs://e-commerce-c6abd.appspot.com/Products");


            }

            @Override
            protected Void doInBackground(Void... voids) {
                if (requestCode == DELETE) {
                    // TODO: modify the api calling
                    compositeSubs.add(myAPI.deleteProduct(product.getId(), "delete", true)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(deleteState -> {   // onNext method
                                ((DataCallBack) callBack).onDataExtracted(deleteState, compositeSubs);
                            }, throwable -> {   // onError method
                                ((DataCallBack) callBack).onDataNotExtracted(throwable);
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
                } else if (requestCode == UPDATE) {
                    try {
                        if (Arrays.equals(product.getPics(), oldProduct.getPics())) {
                            updateProduct();
                        } else {
                            final int[] index = {0};
                            final Uri[] newUrl = new Uri[product.getPics().length];
                            for (Uri pic : product.getPics()) {
                                storeRef.child(FirebaseDatabase.getInstance().getReference().push().toString())
                                        .putFile(pic)
                                        .continueWithTask(task -> {
                                            if (!task.isSuccessful()) {
                                                Log.e(TAG, task.getException().getMessage(), task.getException());
                                            }
                                            return Objects.requireNonNull(task.getResult()
                                                            .getMetadata()
                                                            .getReference()
                                                    , "URL is NULL").getDownloadUrl();
                                        })
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                newUrl[index[0]] = task.getResult();
                                                index[0]++;
                                                if (index[0] == product.getPics().length - 1) {
                                                    newUrl[index[0]] = task.getResult();
                                                    try {
                                                        Log.e(TAG, product.getPriceRanges()[0].toString()
                                                                + "\n" + product.getPriceRanges()[1].toString());
                                                        Log.e(TAG, PriceRange.rangesToJSONArray(product.getPriceRanges()).toString());
                                                        product.setPics(newUrl);
                                                        updateProduct();
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                        if (progressDialog.isShowing())
                                                            progressDialog.dismiss();
                                                        Toast.makeText(activity, "Something went wrong. ", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            } else {
                                                Exception e = new Exception("Unsuccessful task");
                                                Log.e(TAG, e.getMessage(), e);
                                                if (progressDialog.isShowing()) {
                                                    new Thread(() -> {
                                                        handler.post(() -> {
                                                            progressDialog.dismiss();
                                                        });
                                                    }).start();
                                                }
                                            }
                                        });
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (progressDialog.isShowing()) {
                            new Thread(() ->
                                    handler.post(() ->
                                            progressDialog.dismiss())).start();
                        }
                    }
                } else if (requestCode == PUBLISHED_PRODS) {
                    // onError method
                    compositeSubs.add(myAPI.fetchPublishedCount(user.getUserId())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(count -> {   // onNext method
                                ((DataCallBack) callBack).onDataExtracted(count, compositeSubs);
                            }, ((DataCallBack) callBack)::onDataNotExtracted, () -> {          // onComplete method
                            }));
                } else if (requestCode == FETCH_PRODUCT_RANGES) {
                    compositeSubs.add(myAPI.fetchProductRanges(product.getId(), "ranges")
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(fetchedData -> {   // onNext method
                                        Log.d(TAG, "main page data: " + fetchedData.toString());
                                        ((DataCallBack) callBack).onDataExtracted(fetchedData, compositeSubs);
                                    }
                                    , throwable -> {   // onError method
                                        Log.e(TAG, throwable.getMessage(), throwable);
                                        ((DataCallBack) callBack).onDataNotExtracted(throwable);
                                    }, () -> {          // onComplete method
                                        Log.d(TAG, "fetch main data completed");
                                    }));
                }
                return null;
            }

            private JSONArray rangesToJson() throws JSONException {
                JSONArray ranges = new JSONArray();
                for (int i = 0; i < product.getPriceRanges().length; i++) {
                    JSONObject range = new JSONObject();
                    range.put("min", product.getPriceRanges()[i].getMinQuantity());
                    range.put("max", product.getPriceRanges()[i].getMaxQuantity());
                    range.put("price", product.getPriceRanges()[i].getPrice());
                    ranges.put(i, range);
                }
                Log.e(TAG, "rangesToJson: " + ranges.toString());
                return ranges;
            }

            private JSONArray picsToJson() throws JSONException {
                JSONArray pics = new JSONArray();
                for (int i = 0; i < product.getPics().length && i < oldProduct.getPics().length; i++) {
                    JSONObject range = new JSONObject();
                    range.put("pic", oldProduct.getPics()[i]);
                    range.put("newPic", product.getPics()[i]);
                    pics.put(i, range);
                }
                return pics;
            }

            private void updateProduct() throws JSONException {
                compositeSubs.add(myAPI.updateProduct(
                        PriceRange.getMinOrder(product.getPriceRanges())
                        , PriceRange.getMinPrice(product.getPriceRanges())
                        , product.getProductName()
                        , product.getDescription()
                        , picsToJson()
                        , rangesToJson()
                        , product.getId()
                        , "update"
                        , product.getType()
                        , false)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(updateState -> {   // onNext method
                            ((DataCallBack) callBack).onDataExtracted(updateState, compositeSubs);
                        }, throwable -> {   // onError method
                            ((DataCallBack) callBack).onDataNotExtracted(throwable);
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
        }.execute();
    }
}

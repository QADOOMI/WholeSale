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
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import product.structure.PriceRange;
import product.structure.Product;
import user.structure.Seller;
import user.structure.Session;
import wholesale.callback.CallBack;
import wholesale.callback.DataCallBack;

public final class InsertProductData extends DatabaseAction {
    private static final String TAG = InsertProductData.class.getSimpleName();
    private ProgressDialog progressDialog;
    private Handler handler = new Handler(Looper.getMainLooper());


    public InsertProductData(Activity activity, Product product) {
        super(activity, product);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onAccess(CallBack callBack) {
        backgroundTask = new AsyncTask<Void, Void, Void>() {
            private StorageReference storeRef;
            private Seller seller;
            private final ArrayList<Task<Uri>> urls = new ArrayList<>();

            @Override
            protected void onPreExecute() {
                progressDialog = new ProgressDialog(activity);
                progressDialog.setTitle(activity.getResources().getString(R.string.publishing_product_title));
                progressDialog.setMessage(activity.getResources().getString(R.string.publishing_product_msg));
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

                // upload the images to firebase
                // save it in uri list

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Log.e(TAG, e.getMessage(), e);

                }
            }

            @Override
            protected Void doInBackground(Void... voids) {
                Uri[] pics = product.getPics();
                final Uri[] newUrl = new Uri[pics.length];
                final int[] index = {0};
                for (Uri pic : pics) {
                    storeRef.child(FirebaseDatabase.getInstance().getReference().push().toString())
                            .putFile(pic)
                            .continueWithTask(task -> {
                                if (!task.isSuccessful()) {
                                    Log.e(TAG, task.getException().getMessage(), task.getException());
                                }
                                return Objects.requireNonNull(task.getResult().getMetadata().getReference(), "URL is NULL").getDownloadUrl();
                            })
                            .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            newUrl[index[0]] = task.getResult();
                            index[0]++;
                            if (index[0] == pics.length - 1) {
                                newUrl[index[0]] = task.getResult();
                                try {
                                    compositeSubs.add(myAPI.addProduct(product.getProductName()
                                            , product.getDescription()
                                            , product.getType()
                                            , product.picsToJSONArray(newUrl)
                                            , PriceRange.rangesToJSONArray(product.getPriceRanges())
                                            , seller.getSellerInfo().getComapnyName()
                                            , PriceRange.getMinOrder(product.getPriceRanges())
                                            , PriceRange.getMinPrice(product.getPriceRanges())
                                            , seller.getUserId())
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(addState -> {
                                                        ((DataCallBack) callBack).onDataExtracted(addState, compositeSubs);
                                                        handler.post(() -> {
                                                            if (progressDialog.isShowing())
                                                                progressDialog.dismiss();
                                                        });

                                                    }, throwable -> {
                                                        ((DataCallBack) callBack).onDataNotExtracted(throwable);
                                                        handler.post(() -> {
                                                            if (progressDialog.isShowing())
                                                                progressDialog.dismiss();
                                                        });
                                                    }
                                                    , () -> Log.d(TAG, "onComplete")));
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
                        }
                    });
                }
                return null;
            }
        }.execute();
    }
}

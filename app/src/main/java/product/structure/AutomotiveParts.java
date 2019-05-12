package product.structure;

import android.app.Activity;
import android.util.Log;

import org.json.JSONException;

import java.util.concurrent.ExecutionException;

import database.ActionExec;
import database.DatabaseActionExec;
import database.FetchProducts;
import database.InsertProductData;
import database.ProductReview;
import database.SellerAction;
import database.WishProduct;
import user.structure.Buyer;
import user.structure.Session;
import wholesale.callback.CallBack;
import wholesale.callback.DataCallBack;

public class AutomotiveParts extends Product {

    private String viechleType;
    private static final String TAG = AutomotiveParts.class.getSimpleName();

    public AutomotiveParts() {
    }

    @Override
    public String[] getColors() {
        return super.colors;
    }

    @Override
    public void setColors(String[] colors) {
        super.colors = colors;
    }

    @Override
    public void getAllProdData(Activity activity, DataCallBack dataCallBack, String buyerId)
            throws ExecutionException, InterruptedException {
        DatabaseActionExec actionExec = new ActionExec(new FetchProducts(activity, this, buyerId));
        actionExec.beginExecution(dataCallBack);
    }

    @Override
    public void getMainPageProducts(Activity activity, CallBack callBack)
            throws ExecutionException, InterruptedException {
        DatabaseActionExec actionExec = new ActionExec(
                new FetchProducts(activity
                        , this
                        , null
                        , FetchProducts.FETCH_MAIN_PRODUCTS));
        actionExec.beginExecution(callBack);
    }

    @Override
    public void wishListIt(Activity activity, CallBack callBack, boolean wishIt)
            throws ExecutionException, InterruptedException, JSONException {
        Buyer buyer = (Buyer) Session.getUser(Buyer.class);
        if (Session.isUserSignedIn()) {
            DatabaseActionExec actionExec = new ActionExec(new WishProduct(activity
                    , this.getId()
                    , buyer != null ? buyer.getUserId() : null
                    , wishIt)
            );
            actionExec.beginExecution(callBack);
        } else {
            Log.e(TAG, "User not signed in");
        }
    }

    @Override
    public void fetchProductDetails(Activity activity, CallBack callBack, String buyerId)
            throws ExecutionException, InterruptedException, JSONException {
        Buyer buyer = (Buyer) Session.getUser(Buyer.class);
        DatabaseActionExec actionExec = new ActionExec(new FetchProducts(activity
                , this
                , buyer != null ? buyer.getUserId() : null
                , FetchProducts.FETCH_A_PRODUCT));
        actionExec.beginExecution(callBack);
    }

    @Override
    public void fetchReviews(Activity activity, CallBack callBack)
            throws ExecutionException, InterruptedException {
        DatabaseActionExec actionExec = new ActionExec(new ProductReview(activity, this.getId()));
        actionExec.beginExecution(callBack);
    }


    @Override
    public void updateData(Activity activity, CallBack callBack, Product oldProduct, int sellerAction)
            throws ExecutionException, InterruptedException {
        DatabaseActionExec actionExec = new ActionExec(new SellerAction(activity, this, oldProduct, sellerAction));
        actionExec.beginExecution(callBack);
    }

    @Override
    public void publishProduct(Activity activity, CallBack callBack)
            throws ExecutionException, InterruptedException {
        DatabaseActionExec actionExec = new ActionExec(new InsertProductData(activity, this));
        actionExec.beginExecution(null);
    }

    String getTypeOfViechle() {
        return viechleType;
    }

    void setViechleType(String viechleType) {
        this.viechleType = viechleType;
    }

    @Override
    public String getType() {
        return this.getClass().getSimpleName();
    }
}

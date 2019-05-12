package user.structure;

import android.app.Activity;
import android.net.Uri;

import java.util.concurrent.ExecutionException;

import database.ActionExec;
import database.Authenticate;
import database.DatabaseActionExec;
import database.DoSearch;
import database.ProductReview;
import database.UpdateUserData;
import database.WishProduct;
import product.structure.Review;
import wholesale.callback.CallBack;

public class Buyer extends User {

    private static final String TAG = Buyer.class.getSimpleName();

    public Buyer() {
    }

    public Buyer(String email, String password) {
        super(email, password);
    }

    public Buyer(String userId, String firstName, String lastName, String email, Uri profileImage) {
        super(userId, firstName, lastName, email, profileImage);
    }

    public Buyer(String firstName, String lastName, String email, String phoneNumber, String password) {
        super(firstName, lastName, email, phoneNumber, password);
    }

    public Buyer(String id, String firstName, String lastName, String email, String phoneNumber, String password, String profileImage) {
        super(id, firstName, lastName, email, phoneNumber, password, profileImage);
    }


    @Override
    public void signIn(final Activity activity, CallBack callBack) throws ExecutionException, InterruptedException {
        DatabaseActionExec actionExec = new ActionExec(new Authenticate(activity, this, Authenticate.SIGN_IN_REQ));
        actionExec.beginExecution(callBack);
    }

    @Override
    public void signUp(final Activity activity, CallBack callBack) throws ExecutionException, InterruptedException {
        DatabaseActionExec actionExec = new ActionExec(new Authenticate(activity, this, Authenticate.SIGN_UP_REQ));
        actionExec.beginExecution(callBack);
    }

    @Override
    public void deleteAccount(Activity activity, CallBack callBack) throws ExecutionException, InterruptedException {
        DatabaseActionExec actionExec = new ActionExec(new Authenticate(activity, this, Authenticate.DEL_ACC_REQ));
        actionExec.beginExecution(callBack);
    }

    @Override
    public void updateUserData(Activity activity, CallBack callBack, int updateType)
            throws ExecutionException, InterruptedException {
        DatabaseActionExec actionExec = new ActionExec(new UpdateUserData(activity, this, updateType));
        actionExec.beginExecution(callBack);
    }

    public void deleteAllWished(Activity activity, CallBack callBack)
            throws ExecutionException, InterruptedException {
        DatabaseActionExec actionExec = new ActionExec(new WishProduct(activity, this.getUserId(), WishProduct.DELETE_ALL_WISHED));
        actionExec.beginExecution(callBack);
    }


    public void fetchAllWished(Activity activity, CallBack callBack)
            throws ExecutionException, InterruptedException {
        DatabaseActionExec actionExec = new ActionExec(new WishProduct(activity, this.getUserId(), WishProduct.FETCH_WISHED_PRODUCTS));
        actionExec.beginExecution(callBack);
    }

    public void reviewAProduct(Activity activity, CallBack callBack, Review review, String productId)
            throws ExecutionException, InterruptedException {
        DatabaseActionExec actionExec = new ActionExec(new ProductReview(activity, review, this.getUserId(), productId));
        actionExec.beginExecution(callBack);
    }

    public void search(Activity activity, CallBack callBack, String keyValue)
            throws ExecutionException, InterruptedException {
        DatabaseActionExec actionExec = new ActionExec(new DoSearch(activity, keyValue));
        actionExec.beginExecution(callBack);
    }

    @Override
    public Class<?> bringType() {
        return this.getClass();
    }

    @Override
    public String toString() {
        return "Buyer{" +
                super.toString() +
                "}";
    }
}

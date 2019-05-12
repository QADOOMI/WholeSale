package dialog;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.example.mostafa.e_commerce.BuyerMainActivity;
import com.example.mostafa.e_commerce.OtherActionsActivity;

import java.util.concurrent.ExecutionException;

import database.SellerAction;
import fragments.java.AddProductFragment;
import product.structure.Product;
import user.structure.Buyer;
import user.structure.Seller;
import user.structure.Session;
import user.structure.User;
import wholesale.callback.DataCallBack;

import static com.example.mostafa.e_commerce.OtherActionsActivity.CURRENT_PAGE_ID;
import static database.Authenticate.DEL_ACC_REQ;

public final class Dialog {

    private static final String TAG = Dialog.class.getSimpleName();
    public static final int SIGN_OUT = 123;
    public static final int DEL_ALL_WISHED = 114;
    public static final int DELETE_PRODUCT = 115;
    public static final int UPDATE_PRODUCT = 116;


    public static void criticalActionDialog(final Activity activity, String title, String message
            , final int requestCode, User user, DataCallBack callBack) {
        new AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    if (requestCode == SIGN_OUT) {
                        Session.signOut();
                        Intent goToDefaultPage = new Intent(activity, BuyerMainActivity.class);
                        activity.startActivity(goToDefaultPage);
                    } else if (requestCode == DEL_ALL_WISHED) {
                        try {
                            ((Buyer) user).deleteAllWished(activity, callBack);
                        } catch (ExecutionException | InterruptedException e) {
                            Log.e(TAG, "Delete All Wished: " + e.getMessage(), e);
                        }
                    } else if (requestCode == DEL_ACC_REQ) {
                        try {
                            user.deleteAccount(activity, callBack);
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    // do nothing
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public static void criticalActionDialog(final Activity activity, String title, String message
            , final int requestCode, Seller seller, final Product product, DataCallBack callBack) {
        new AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    if (requestCode == DELETE_PRODUCT) {
                        try {
                            product.updateData(activity,callBack ,null, SellerAction.UPDATE);
                        } catch (ExecutionException | InterruptedException e) {
                            Log.e(TAG, "Delete product data: " + e.getMessage(), e);
                        }
                    }else if(requestCode == UPDATE_PRODUCT){
                        Intent intent = new Intent(activity, OtherActionsActivity.class);
                        intent.putExtra(CURRENT_PAGE_ID, AddProductFragment.newInstance(product));
                        activity.startActivity(intent);
                    }
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    // do nothing
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}

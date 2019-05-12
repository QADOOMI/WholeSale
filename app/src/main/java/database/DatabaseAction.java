package database;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

import io.reactivex.disposables.CompositeDisposable;
import networking.INodeJs;
import networking.RetrofitClient;
import product.structure.Product;
import user.structure.User;
import wholesale.callback.CallBack;

// IMPLEMENTATION
public abstract class DatabaseAction {

    protected Activity activity;
    protected User user;
    protected Product product;
    protected static AsyncTask<Void, Void, Void> backgroundTask;
    protected INodeJs myAPI;
    protected CompositeDisposable compositeSubs = new CompositeDisposable();

    protected DatabaseAction(Activity activity, Product product) {
        this.activity = activity;
        this.product = product;
        myAPI = RetrofitClient.getRetrofit().create(INodeJs.class);
    }

    protected DatabaseAction(Activity activity, User user) {
        this.activity = activity;
        this.user = user;
        myAPI = RetrofitClient.getRetrofit().create(INodeJs.class);
    }

    protected DatabaseAction(Activity activity, User user, Product product) {
        this.activity = activity;
        this.user = user;
        this.product = product;
        myAPI = RetrofitClient.getRetrofit().create(INodeJs.class);
    }

    protected DatabaseAction(Activity activity) {
        this.activity = activity;
        myAPI = RetrofitClient.getRetrofit().create(INodeJs.class);
    }


    protected void toast(String text) {
        Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
    }

    protected abstract void onAccess(CallBack callBack);
}

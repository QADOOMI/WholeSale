package wholesale.callback;

import io.reactivex.disposables.CompositeDisposable;
import okhttp3.ResponseBody;

public interface AuthListener extends CallBack {
    void onSignIn(ResponseBody object, CompositeDisposable compositeDisposable);
    void onSignUp(ResponseBody object, CompositeDisposable compositeDisposable);
    void onErrorOccured(Throwable error);
}

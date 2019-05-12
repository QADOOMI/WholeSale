package wholesale.callback;

import java.io.Serializable;

import io.reactivex.disposables.CompositeDisposable;
import okhttp3.ResponseBody;

public interface DataCallBack extends CallBack, Serializable{
     void onDataExtracted(ResponseBody responseBody, CompositeDisposable compositeDisposable);
     void onDataNotExtracted(Throwable error);
}

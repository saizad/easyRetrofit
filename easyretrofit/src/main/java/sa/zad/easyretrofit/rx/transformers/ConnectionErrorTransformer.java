package sa.zad.easyretrofit.rx.transformers;

import java.net.ConnectException;
import java.net.UnknownHostException;

import androidx.annotation.Nullable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import rx.functions.Action1;

public class ConnectionErrorTransformer<T> extends BaseErrorTransformer<T, ConnectException> {

    public ConnectionErrorTransformer(@Nullable Action1<ConnectException> errorAction) {
        super(errorAction);
    }

    @Override
    public ObservableSource<T> apply(Observable<T> upstream) {
        return upstream
                .onErrorResumeNext(e -> {
                    if (!(e instanceof ConnectException || e instanceof UnknownHostException)) {
                        return Observable.error(e);
                    } else {
                        callAction(new ConnectException("Check you connection!!"));
                        return Observable.empty();
                    }
                });
    }
}

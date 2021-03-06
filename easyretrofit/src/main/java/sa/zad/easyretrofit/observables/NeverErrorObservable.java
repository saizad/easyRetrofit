package sa.zad.easyretrofit.observables;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import rx.functions.Action1;
import sa.zad.easyretrofit.ResponseException;
import sa.zad.easyretrofit.rx.operator.RetrofitResponseOperator;
import sa.zad.easyretrofit.rx.transformers.ApiErrorTransformer;
import sa.zad.easyretrofit.rx.transformers.NeverErrorTransformer;

/**
 * <p>This Observable class prevents from triggering any kind of {@link Exception}.
 * If exception is encountered {@link Observer#onComplete() onComplete}  will be called to terminate the flow.</p>
 * <br>
 * <p>In order to listen to exception call {@link NeverErrorObservable#exception(Action1) exception} for generic error
 * or call {@link NeverErrorObservable#apiException(Action1, Class) apiException} for api error.</p>
 */
public class NeverErrorObservable<T> extends Observable<T> {

  protected Observable<Response<T>> upstream;

  public NeverErrorObservable(Observable<Response<T>> upstream) {
    this.upstream = upstream
        .subscribeOn(Schedulers.io())
        .lift(new RetrofitResponseOperator<>())
        .observeOn(AndroidSchedulers.mainThread());

    /*
      Ignores any kind of exceptions
      Todo: temp fixed, there might be unknown side affects
     */

    RxJavaPlugins.setErrorHandler(throwable -> {
    });
  }

  @Override
  protected final void subscribeActual(Observer<? super T> observer) {
    upstream.map(Response::body).subscribe(observer);
    this.upstream = upstream.compose(new NeverErrorTransformer<>());
  }

  /**
   * <p>
   * unsuccessful(range [< 200 or 300 >) response will be piped into the supplied responseAction
   * </p>
   * <br><b>Note: </b>
   * <p>
   * {@link #failedResponse(Action1) failedResponse} should be called higher in the chain,
   * before {@link #apiException(Action1, Class) apiException} and {@link #exception(Action1) exception}
   * </p>
   *
   * @param responseAction callback
   * @return {@link #NeverErrorObservable(Observable)}
   */

  public final NeverErrorObservable<T> failedResponse(@NonNull Action1<Response<T>> responseAction) {
    this.upstream = upstream.doOnError(throwable -> {
      if (throwable instanceof ResponseException) {
        final ResponseException responseException = (ResponseException) throwable;
        responseAction.call((Response<T>) responseException.response());
      }
    });
    return this;
  }

  /**
   * <p>
   * Successful(range [200..300) Raw retrofit {@link Response} will be piped into the supplied responseAction
   * </p>
   * <br><b>Note: </b>
   * <p>
   * {@link #successResponse(Action1) successResponse} should be called higher in the chain,
   * before {@link #apiException(Action1, Class) apiException} and {@link #exception(Action1) exception}
   * </p>
   *
   * @param responseAction callback
   * @return {@link #NeverErrorObservable(Observable)}
   */

  public final NeverErrorObservable<T> successResponse(@NonNull Action1<Response<T>> responseAction) {
    this.upstream = upstream.doOnNext(responseAction::call);
    return this;
  }

  /**
   * <p>
   * If any {@link Throwable} is encountered, it will piped into the supplied errors action, and followed by
   * {@link Observer#onComplete() onComplete}.
   * </p>
   *
   * @param error error callback
   * @return {@link #NeverErrorObservable(Observable)}
   */
  public final NeverErrorObservable<T> exception(@Nullable Action1<Throwable> error) {
    this.upstream = upstream.compose(new NeverErrorTransformer<>(error));
    return this;
  }

  /**
   * <p>
   * If any {@link ResponseException} is encountered, it will piped into the supplied errors action, and followed by
   * {@link Observer#onComplete() onComplete}.
   * </p>
   *
   * <b>Note:</b>
   * <p>In order to listen to Api Error, call this method before
   * {@link #exception(Action1)}.</p>
   *
   * @param apiError Api Error callback
   * @param eClass   Api Error data model class
   * @param <E>      Api Error Type
   * @return {@link #NeverErrorObservable(Observable)}
   */
  public <E> NeverErrorObservable<T> apiException(Action1<E> apiError, Class<E> eClass) {
    final ApiErrorTransformer<Response<T>> transformer =
        new ApiErrorTransformer<>(e -> apiError.call(e.getErrorBody(eClass)));
    upstream = upstream.compose(transformer);
    return this;
  }

}

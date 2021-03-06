package sa.zad.easyretrofit.base;

import android.support.annotation.Nullable;

import okhttp3.HttpUrl;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import rx.functions.Action1;
import sa.zad.easyretrofit.ProgressListener;
import sa.zad.easyretrofit.call.CallEnqueueObservable;

public abstract class CallDownloadEnqueueObservable<T> extends CallEnqueueObservable<ProgressListener.Progress<T>> {

  public CallDownloadEnqueueObservable(Call<ProgressListener.Progress<T>> originalCall) {
    super(originalCall);
  }

  @Override
  protected final void success(Response<ProgressListener.Progress<T>> response) throws Exception {
      if (!response.isSuccessful()) {
        observer.onNext(Response.error(response.code(), response.errorBody()));
        return;
      }
      Response<Object> responseBodyResponse = Response.success(response.body());
      final ResponseBody responseBody = (ResponseBody) responseBodyResponse.body();
      final long startTime = System.currentTimeMillis();
      ProgressListener.Progress<T> progress = new ProgressListener.Progress<>(responseBody.contentLength(), startTime);
      final T value = responseBodyReady(responseBody, response.raw().request().url(), written -> {
        progress.setWritten(written);
        observer.onNext(Response.success(progress));
      });
      progress.setWritten(progress.size);
      progress.setValue(value);
      observer.onNext(Response.success(progress));
      observer.onComplete();
  }

  @Nullable
  protected abstract T responseBodyReady(ResponseBody responseBody, HttpUrl url, Action1<Long> writtenCallback) throws Exception;
}

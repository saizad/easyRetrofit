package sa.zad.easyretrofit;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import java.io.IOException;

import retrofit2.HttpException;


public class ResponseException extends HttpException {

  public ResponseException(final @NonNull retrofit2.Response response) {
    super(response);
  }

  @Nullable
  public <E> E getErrorBody(Class<E> error) {
    try {
      return EasyRetrofit.getInstance().gson().fromJson(response().errorBody().string(), error);
    } catch (IOException e) {
      return null;
    } catch (Exception e){
      return null;
    }
  }
}

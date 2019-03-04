package sa.zad.easyretrofit;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.math.RoundingMode;
import java.text.DecimalFormat;


public interface ProgressListener<R> {
  void onProgressStart(@NonNull Progress<R> progress);

  void onProgressUpdate(@NonNull Progress<R> progress);

  void onError(@NonNull Progress<R> progress, Exception exception);

  void onFinish(@NonNull Progress<R> progress);

  class Progress<R> {
    public long written;
    public long size;
    public @Nullable
    R value;
    private Long startTime;
    private Long latestTime;


    public Progress(long size) {
      this(size, System.currentTimeMillis());
    }

    public Progress(long size, Long startTime) {
      this.size = size;
      this.startTime = startTime;
    }

    public Long timeRemaining() {
      return totalDuration() - elapsedTime();
    }

    public Long elapsedTime(){
      return latestTime - startTime;
    }

    public Long totalDuration() {
      return (elapsedTime() * size / written);
    }

    public float getProgress() {
      DecimalFormat df = new DecimalFormat("#.##");
      df.setRoundingMode(RoundingMode.HALF_UP);
      return Float.parseFloat(df.format(100f * written / size));
    }

    public void setWritten(long written) {
      latestTime = System.currentTimeMillis();
      this.written = written;
    }

    public void setValue(@Nullable R value) {
      this.value = value;
    }

    public boolean isCompleted(){
      return written == size;
    }

    @NonNull
    @Override
    public String toString() {
      return "Size = " + size + " Written = " + written + " Progress = " + getProgress();
    }
  }
}

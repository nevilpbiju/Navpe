package com.example.navpe.Recognitions;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import java.util.List;

public class RecognitionScoreView extends View implements ResultsView {
  private static final float TEXT_SIZE_DIP = 14;
  private final Paint fgPaint;
  private final Paint bgPaint;
  private List<SimilarityClassifier.Recognition> results;

  public RecognitionScoreView(final Context context, final AttributeSet set) {
    super(context, set);

    float textSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    fgPaint = new Paint();
    fgPaint.setTextSize(textSizePx);

    bgPaint = new Paint();
    bgPaint.setColor(0xcc4285f4);
  }

  public void setResults(final List<SimilarityClassifier.Recognition> results) {
    this.results = results;
    postInvalidate();
  }

  @Override
  public void onDraw(final Canvas canvas) {
    final int x = 10;
    int y = (int) (fgPaint.getTextSize() * 1.5f);

    canvas.drawPaint(bgPaint);

    if (results != null) {
      for (final SimilarityClassifier.Recognition recognition : results) {
        canvas.drawText(recognition.getTitle() + ": " + recognition.getDistance(), x, y, fgPaint);
        y += (int) (fgPaint.getTextSize() * 1.5f);
      }
    }
  }

}

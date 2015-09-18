package com.github.channguyen.rsv;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;

import java.util.concurrent.TimeUnit;

public class RangeSliderView extends View {

  private static final String TAG = RangeSliderView.class.getSimpleName();

  private static final long RIPPLE_ANIMATION_DURATION_MS = TimeUnit.MILLISECONDS.toMillis(700);

  private static final int DEFAULT_PAINT_STROKE_WIDTH = 5;

  private static final int DEFAULT_FILLED_COLOR = Color.parseColor("#FFA500");

  private static final int DEFAULT_EMPTY_COLOR = Color.parseColor("#C3C3C3");

  private static final float DEFAULT_BAR_HEIGHT_PERCENT = 0.10f;

  private static final float DEFAULT_SLOT_RADIUS_PERCENT = 0.125f;

  private static final float DEFAULT_SLIDER_RADIUS_PERCENT = 0.25f;

  private static final int DEFAULT_RANGE_COUNT = 5;

  protected Paint paint;

  protected Paint ripplePaint;

  protected float radius;

  protected float slotRadius;

  private int currentIndex;

  private int height;

  private float currentSlidingX;

  private float currentSlidingY;

  private float selectedSlotX;

  private float selectedSlotY;

  private boolean gotSlot = false;

  private float[] slotPositions;

  private int filledColor = DEFAULT_FILLED_COLOR;

  private int emptyColor = DEFAULT_EMPTY_COLOR;

  private float barHeightPercent = DEFAULT_BAR_HEIGHT_PERCENT;

  private int rangeCount = DEFAULT_RANGE_COUNT;

  private int barHeight;

  private OnSlideListener listener;

  private float rippleRadius = 0.0f;

  private float downX;

  private float downY;

  private Path innerPath = new Path();

  private Path outerPath = new Path();

  private float slotRadiusPercent = DEFAULT_SLOT_RADIUS_PERCENT;

  private float sliderRadiusPercent = DEFAULT_SLIDER_RADIUS_PERCENT;

  public RangeSliderView(Context context) {
    this(context, null);
  }

  public RangeSliderView(Context context, AttributeSet attrs) {
    this(context, attrs, -1);
  }

  public RangeSliderView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    if (attrs != null) {
      TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RangeSliderView);
      TypedArray sa = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.layout_height});
      try {
        height = sa.getDimensionPixelSize(
          0, ViewGroup.LayoutParams.WRAP_CONTENT);
        rangeCount = a.getInt(
          R.styleable.RangeSliderView_rangeCount, DEFAULT_RANGE_COUNT);
        filledColor = a.getColor(
          R.styleable.RangeSliderView_filledColor, DEFAULT_FILLED_COLOR);
        emptyColor = a.getColor(
          R.styleable.RangeSliderView_emptyColor, DEFAULT_EMPTY_COLOR);
        barHeightPercent = a.getFloat(
          R.styleable.RangeSliderView_barHeightPercent, DEFAULT_BAR_HEIGHT_PERCENT);
        barHeightPercent = a.getFloat(
          R.styleable.RangeSliderView_barHeightPercent, DEFAULT_BAR_HEIGHT_PERCENT);
        slotRadiusPercent = a.getFloat(
          R.styleable.RangeSliderView_slotRadiusPercent, DEFAULT_SLOT_RADIUS_PERCENT);
        sliderRadiusPercent = a.getFloat(
          R.styleable.RangeSliderView_sliderRadiusPercent, DEFAULT_SLIDER_RADIUS_PERCENT);
      } finally {
        a.recycle();
        sa.recycle();
      }
    }

    setBarHeightPercent(barHeightPercent);
    setRangeCount(rangeCount);
    setSlotRadiusPercent(slotRadiusPercent);
    setSliderRadiusPercent(sliderRadiusPercent);

    barHeight = (int) (height * barHeightPercent);
    radius = height * sliderRadiusPercent;
    slotRadius = height * slotRadiusPercent;
    currentIndex = 0;

    slotPositions = new float[rangeCount];

    paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setStrokeWidth(DEFAULT_PAINT_STROKE_WIDTH);
    paint.setStyle(Paint.Style.FILL_AND_STROKE);

    ripplePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    ripplePaint.setStrokeWidth(2.0f);
    ripplePaint.setStyle(Paint.Style.FILL_AND_STROKE);

    getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
      @Override
      public boolean onPreDraw() {
        getViewTreeObserver().removeOnPreDrawListener(this);
        preComputeDrawingPosition();
        return true;
      }
    });
  }

  public int getRangeCount() {
    return rangeCount;
  }

  public void setRangeCount(int rangeCount) {
    if (rangeCount < 2) {
      throw new IllegalArgumentException("rangeCount must be >= 2");
    }
    this.rangeCount = rangeCount;
  }

  public float getBarHeightPercent() {
    return barHeightPercent;
  }

  public void setBarHeightPercent(float percent) {
    if (percent <= 0.0 || percent > 1.0) {
      throw new IllegalArgumentException("Bar height percent must be in (0, 1]");
    }
    this.barHeightPercent = percent;
  }

  public float getSlotRadiusPercent() {
    return slotRadiusPercent;
  }

  public void setSlotRadiusPercent(float percent) {
    if (percent <= 0.0 || percent > 1.0) {
      throw new IllegalArgumentException("Slot radius percent must be in (0, 1]");
    }
    this.slotRadiusPercent = percent;
  }

  public float getSliderRadiusPercent() {
    return sliderRadiusPercent;
  }

  public void setSliderRadiusPercent(float percent) {
    if (percent <= 0.0 || percent > 1.0) {
      throw new IllegalArgumentException("Slider radius percent must be in (0, 1]");
    }
    this.sliderRadiusPercent = percent;
  }

  @AnimateMethod
  public void setRadius(final float radius) {
    rippleRadius = radius;
    if (rippleRadius > 0) {
      RadialGradient radialGradient = new RadialGradient(
        downX,
        downY,
        rippleRadius * 3,
        Color.TRANSPARENT,
        Color.BLACK,
        Shader.TileMode.MIRROR
      );
      ripplePaint.setShader(radialGradient);
    }
    invalidate();
  }

  public void setOnSlideListener(OnSlideListener listener) {
    this.listener = listener;
  }

  /**
   * Perform all the calculation before drawing, should only run once
   */
  private void preComputeDrawingPosition() {
    int w = getWidthWithPadding();
    int h = getHeightWithPadding();

    /** Space between each slot */
    int spacing = w / rangeCount;

    /** Center vertical */
    int y = getPaddingTop() + h / 2;
    currentSlidingY = y;
    selectedSlotY = y;
    /**
     * Try to center it, so start by half
     * <pre>
     *
     *  Example for 4 slots
     *
     *  ____o____|____o____|____o____|____o____
     *  --space--
     *
     * </pre>
     */
    int x = getPaddingLeft() + (spacing / 2);

    /** Store the position of each slot index */
    for (int i = 0; i < rangeCount; ++i) {
      slotPositions[i] = x;
      if (i == currentIndex) {
        currentSlidingX = x;
        selectedSlotX = x;
      }
      x += spacing;
    }
  }

  public void setInitialIndex(int index) {
    if (index < 0 || index > rangeCount) {
      throw new IllegalArgumentException("Attempted to set index=" + index + " out of range [0," + rangeCount + "]");
    }
    currentIndex = index;
    currentSlidingX = selectedSlotX = slotPositions[currentIndex];
    invalidate();
  }

  public int getFilledColor() {
    return filledColor;
  }

  public void setFilledColor(int filledColor) {
    this.filledColor = filledColor;
    invalidate();
  }

  public int getEmptyColor() {
    return emptyColor;
  }

  public void setEmptyColor(int emptyColor) {
    this.emptyColor = emptyColor;
    invalidate();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
  }

  /**
   * Measures height according to the passed measure spec
   *
   * @param measureSpec int measure spec to use
   * @return int pixel size
   */
  private int measureHeight(int measureSpec) {
    int specMode = MeasureSpec.getMode(measureSpec);
    int specSize = MeasureSpec.getSize(measureSpec);
    int result;
    if (specMode == MeasureSpec.EXACTLY) {
      result = specSize;
    } else {
      result = height + getPaddingTop() + getPaddingBottom() + (2 * DEFAULT_PAINT_STROKE_WIDTH);
      if (specMode == MeasureSpec.AT_MOST) {
        result = Math.min(result, specSize);
      }
    }
    return result;
  }

  /**
   * Measures width according to the passed measure spec
   *
   * @param measureSpec int measure spec to use
   * @return int pixel size
   */
  private int measureWidth(int measureSpec) {
    int specMode = MeasureSpec.getMode(measureSpec);
    int specSize = MeasureSpec.getSize(measureSpec);
    int result;
    if (specMode == MeasureSpec.EXACTLY) {
      result = specSize;
    } else {
      result = getSuggestedMinimumWidth() + getPaddingLeft() + getPaddingRight() + (2 * DEFAULT_PAINT_STROKE_WIDTH) + (int) (2 * radius);
      if (specMode == MeasureSpec.AT_MOST) {
        result = Math.min(result, specSize);
      }
    }
    return result;
  }

  private void updateCurrentIndex() {
    float min = Float.MAX_VALUE;
    int j = 0;
    /** Find the closest to x */
    for (int i = 0; i < rangeCount; ++i) {
      float dx = Math.abs(currentSlidingX - slotPositions[i]);
      if (dx < min) {
        min = dx;
        j = i;
      }
    }
    /** This is current index of slider */
    if (j != currentIndex) {
      if (listener != null) {
        listener.onSlide(j);
      }
    }
    currentIndex = j;
    /** Correct position */
    currentSlidingX = slotPositions[j];
    selectedSlotX = currentSlidingX;
    downX = currentSlidingX;
    downY = currentSlidingY;
    animateRipple();
    invalidate();
  }

  private void animateRipple() {
    ObjectAnimator animator = ObjectAnimator.ofFloat(this, "radius", 0, radius);
    animator.setInterpolator(new AccelerateInterpolator());
    animator.setDuration(RIPPLE_ANIMATION_DURATION_MS);
    animator.start();
    animator.addListener(new Animator.AnimatorListener() {
      @Override
      public void onAnimationStart(Animator animation) {

      }

      @Override
      public void onAnimationEnd(Animator animation) {
        rippleRadius = 0;
      }

      @Override
      public void onAnimationCancel(Animator animation) {

      }

      @Override
      public void onAnimationRepeat(Animator animation) {

      }
    });
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    float y = event.getY();
    float x = event.getX();
    final int action = event.getActionMasked();
    switch (action) {
      case MotionEvent.ACTION_DOWN:
        gotSlot = isInSelectedSlot(x, y);
        downX = x;
        downY = y;
        break;

      case MotionEvent.ACTION_MOVE:
        if (gotSlot) {
          if (x >= slotPositions[0] && x <= slotPositions[rangeCount - 1]) {
            currentSlidingX = x;
            currentSlidingY = y;
            invalidate();
          }
        }
        break;

      case MotionEvent.ACTION_UP:
        if (gotSlot) {
          gotSlot = false;
          currentSlidingX = x;
          currentSlidingY = y;
          updateCurrentIndex();
        }
        break;
    }
    return true;
  }

  private boolean isInSelectedSlot(float x, float y) {
    return
      selectedSlotX - radius <= x && x <= selectedSlotX + radius &&
        selectedSlotY - radius <= y && y <= selectedSlotY + radius;
  }

  private void drawEmptySlots(Canvas canvas) {
    paint.setColor(emptyColor);
    int h = getHeightWithPadding();
    int y = getPaddingTop() + (h >> 1);
    for (int i = 0; i < rangeCount; ++i) {
      canvas.drawCircle(slotPositions[i], y, slotRadius, paint);
    }
  }

  public int getHeightWithPadding() {
    return getHeight() - getPaddingBottom() - getPaddingTop();
  }

  public int getWidthWithPadding() {
    return getWidth() - getPaddingLeft() - getPaddingRight();
  }

  private void drawFilledSlots(Canvas canvas) {
    paint.setColor(filledColor);
    int h = getHeightWithPadding();
    int y = getPaddingTop() + (h >> 1);
    for (int i = 0; i < rangeCount; ++i) {
      if (slotPositions[i] <= currentSlidingX) {
        canvas.drawCircle(slotPositions[i], y, slotRadius, paint);
      }
    }
  }

  private void drawBar(Canvas canvas, int from, int to, int color) {
    paint.setColor(color);
    int h = getHeightWithPadding();
    int half = (barHeight >> 1);
    int y = getPaddingTop() + (h >> 1);
    canvas.drawRect(from, y - half, to, y + half, paint);
  }

  private void drawRippleEffect(Canvas canvas) {
    if (rippleRadius != 0) {
      canvas.save();
      ripplePaint.setColor(Color.GRAY);
      outerPath.reset();
      outerPath.addCircle(downX, downY, rippleRadius, Path.Direction.CW);
      canvas.clipPath(outerPath);
      innerPath.reset();
      innerPath.addCircle(downX, downY, rippleRadius / 3, Path.Direction.CW);
      canvas.clipPath(innerPath, Region.Op.DIFFERENCE);
      canvas.drawCircle(downX, downY, rippleRadius, ripplePaint);
      canvas.restore();
    }
  }

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    int w = getWidthWithPadding();
    int h = getHeightWithPadding();
    int spacing = w / rangeCount;
    int border = (spacing >> 1);
    int x0 = getPaddingLeft() + border;
    int y0 = getPaddingTop() + (h >> 1);
    drawEmptySlots(canvas);
    drawFilledSlots(canvas);

    /** Draw empty bar */
    drawBar(canvas, (int) slotPositions[0], (int) slotPositions[rangeCount - 1], emptyColor);

    /** Draw filled bar */
    drawBar(canvas, x0, (int) currentSlidingX, filledColor);

    /** Draw the selected range circle */
    paint.setColor(filledColor);
    canvas.drawCircle(currentSlidingX, y0, radius, paint);
    drawRippleEffect(canvas);
  }

  /**
   * Interface to keep track sliding position
   */
  public interface OnSlideListener {

    /**
     * Notify when slider change to new index position
     *
     * @param index
     */
    void onSlide(int index);
  }
}

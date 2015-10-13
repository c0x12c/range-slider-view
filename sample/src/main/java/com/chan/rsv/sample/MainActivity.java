package com.chan.rsv.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.github.channguyen.rsv.RangeSliderView;

public class MainActivity extends AppCompatActivity {

  private RangeSliderView smallSlider;

  private RangeSliderView largeSlider;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    smallSlider = (RangeSliderView) findViewById(
      R.id.rsv_small);
    largeSlider = (RangeSliderView) findViewById(
      R.id.rsv_large);
    final RangeSliderView.OnSlideListener listener = new RangeSliderView.OnSlideListener() {
      @Override
      public void onSlide(int index) {
        Toast.makeText(
          getApplicationContext(),
          "Hi index: " + index,
          Toast.LENGTH_SHORT)
          .show();
      }
    };
    smallSlider.setOnSlideListener(listener);
    largeSlider.setOnSlideListener(listener);
  }
}
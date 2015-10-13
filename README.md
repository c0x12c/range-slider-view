# RangeSliderView
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.channguyen/rsv/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.channguyen/rsv)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-RangeSliderView-green.svg?style=flat)](https://android-arsenal.com/details/1/2511)

# Screenshots
![Main screen](/screenshots/sc.png)

# Features
- Ripple effect.
- Option to set custom colors for slider.
- Option to set custom height for slider.
- Option to set custom radius for slider.

# Usage
Add a dependency to your `build.gradle`:
```
dependencies {
    compile 'com.github.channguyen:rsv:1.0.1'
}
```
Add the `com.github.channguyen.rsv.RangeSliderView` to your layout XML file.
```XML
<LinearLayout
  xmlns:android="http://schemas.android.com/apk/res/android"
  xmlns:rsv="http://schemas.android.com/apk/res-auto"
  android:layout_width="match_parent"
  android:layout_height="match_parent"
  android:orientation="vertical"
  >

  <com.github.channguyen.rsv.RangeSliderView
    android:id="@+id/rsv_small"
    android:layout_marginTop="50dp"
    android:layout_width="match_parent"
    android:layout_height="50dp"
    android:layout_marginLeft="30dp"
    android:layout_marginRight="30dp"
    rsv:filledColor="#1A5F77"
    />


  <com.github.channguyen.rsv.RangeSliderView
    android:id="@+id/rsv_large"
    android:layout_marginTop="50dp"
    android:layout_width="match_parent"
    android:layout_height="100dp"
    android:layout_marginLeft="30dp"
    android:layout_marginRight="30dp"
    rsv:filledColor="#FF6600"
    />

</LinearLayout>
```

For more usage examples check the **sample** project.

### Changelog

**Version 1.0.1**
+ Bug fixes for layout param


**Version 1.0.0**
+ First release


# License
```
Copyright 2015 Chan Nguyen

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

# HoldTickView
An android tick view which switches state after holding.
`HoldTickView.switchingTime` defines time the view should be held
to switch its state.

![Example](resources/holdtickview-example.gif)

## Usage
1. Add this to your app/build.gradle file
```
compile 'com.koshkama:holdtickview:1.0.0'
```

2. Add the view to your layout file and customize it
```
<com.koshkama.holdtickview.HoldTickView
    android:id="@+id/holdTickView"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:checked="true"
    android:enabled="true"
    app:checkedColor="#43A047"
    app:uncheckedColor="#757575"
    app:shadowColor="#88000000"
    app:shadowRadius="4dp"
    app:tickColor="#4CAF50"
    app:tickAnimationTime="200"
    app:switchingTime="1000" />
```

3. Set a listener
```
holdTickView.onCheckedChangeListener = { isChecked: Boolean ->
    doSomething()
}
```

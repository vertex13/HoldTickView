[ ![Download](https://api.bintray.com/packages/vertex55/maven/holdtickview/images/download.svg) ](https://bintray.com/vertex55/maven/holdtickview/_latestVersion)

# HoldTickView
An android tick view which switches state after holding.
`HoldTickView.switchingTime` defines time the view should be held
to switch its state.

![Example](resources/holdtickview-example.gif)

## Usage
**1. Add gradle dependency**
- Add this to the module build.gradle file
```
dependencies {
    compile 'com.github.vertex13:holdtickview:1.0.7@aar'
}
```

- And this to the project build.gradle file
(if the file does not contain it yet)
```
buildscript {
    repositories {
        jcenter()
    }
}
```

**2. Add the view to your layout file and customize it**
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

**3. Set a listener**
- Kotlin
```
holdTickView.onCheckedChangeListener = { isChecked: Boolean ->
    doSomething()
}
```

- Java
```
holdTickView.setOnCheckedChangeListener(new Function1<Boolean, Unit>() {
    public Unit invoke(final Boolean isChecked) {
        doSomething();
        return null;
    }
});
```

**4. Set up Kotlin plugin (only for Java projects)**

If you do not use Kotlin in your project you have to add the Kotlin plugin
and the Kotlin dependency to your project. You can find detailed instructions in
[the official documentation][kotlin-plugin-reference].

## License
The MIT License (MIT)

Copyright (c) 2017 Aleksandr Pavlov

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to deal in the Software without restriction,
including without limitation the rights to use, copy, modify, merge, publish, distribute,
sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

[kotlin-plugin-reference]: https://kotlinlang.org/docs/reference/using-gradle.html
Thumbor Java Client by Square
=============================

Standalone Java client for the [Thumbor image service][1] which allows you to
build URIs in an expressive fashion using the fluent pattern.

This library is also fully compatible with the Android platform.


Examples
--------

```java
build("http://example.com/image.png")
    .resize(48, 48)
// Outputs: /unsafe/48x48/example.com/image.png

build("http://example.com/image.png")
    .crop(10, 10, 90, 90)
    .resize(40, 40)
    .smart()
// Outputs: /unsafe/10x10:90x90/smart/40x40/example.com/image.png

build("http://example.com/image.png")
    .crop(5, 5, 195, 195)
    .resize(95, 95)
    .align(BOTTOM, RIGHT)
// Outputs: /unsafe/5x5:195x195/right/bottom/95x95/example.com/image.png

build("http://example.com/background.png")
    .resize(200, 100)
    .filter(
        roundCorner(10),
        watermark(build("http://example.com/overlay1.png").resize(200, 100)),
        watermark(build("http://example.com/overlay2.png").resize(50, 50), 75, 25),
        quality(85)
    )
// Outputs: /unsafe/200x100/filters:round_corner(10,255,255,255):watermark(/unsafe/200x100/example.com/overlay1.png,0,0,0):watermark(/unsafe/50x50/example.com/overlay2.png,75,25,0):quality(85)/example.com/background.png

build("http://example.com/image.png")
    .resize(48, 48)
    .key("super secret key")
// Outputs: /ttdl3uu1vOdz7mxsjegdi6Q4iUuYq7IWPziAiW53Cff683quusS17Q-piahoiqd1/example.com/image.png
```


Building
--------

Compilation requires Maven 3.0 or newer. To compile a JAR run `mvn clean verify`
in the project root folder. The assembled file will be in the `target/`
directory.

If you are modifying the source files and the build fails due to checkstyle you
can see all of the errors in the `target/checkstyle-result.xml` file.



License
=======

    Copyright 2012 Square, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


 [1]: https://github.com/globocom/thumbor

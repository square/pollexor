Pollexor - Java Thumbor client by Square
========================================

Java client for the [Thumbor image service][1] which allows you to build URIs
in an expressive fashion using a fluent API.

This library is also fully compatible with the Android platform. :shipit:



Download
--------

Downloadable .jars can be found on the [GitHub download page][2].

You can also depend on the .jar through Maven:

    <dependency>
        <groupId>com.squareup</groupId>
        <artifactId>pollexor<artifactId>
        <version>(insert latest version)</version>
    </dependency>



Examples
--------

```java
image("http://example.com/image.png")
    .resize(48, 48)
    .toUrl()
// Produces: /unsafe/48x48/example.com/image.png

image("http://example.com/image.png")
    .crop(10, 10, 90, 90)
    .resize(40, 40)
    .smart()
    .toUrl()
// Produces: /unsafe/10x10:90x90/smart/40x40/example.com/image.png

image("http://example.com/image.png")
    .crop(5, 5, 195, 195)
    .resize(95, 95)
    .align(BOTTOM, RIGHT)
    .toUrl()
// Produces: /unsafe/5x5:195x195/right/bottom/95x95/example.com/image.png

image("http://example.com/background.png")
    .resize(200, 100)
    .filter(
        roundCorner(10),
        watermark(image("http://example.com/overlay1.png").resize(200, 100)),
        watermark(image("http://example.com/overlay2.png").resize(50, 50), 75, 25),
        quality(85)
    )
    .toUrl()
// Produces: /unsafe/200x100/filters:round_corner(10,255,255,255):watermark(/unsafe/200x100/example.com/overlay1.png,0,0,0):watermark(/unsafe/50x50/example.com/overlay2.png,75,25,0):quality(85)/example.com/background.png

image("http://example.com/image.png")
    .resize(48, 48)
    .key("super secret key")
    .toUrl()
// Produces: /hhxlPyQkIxjvGc_-u_JEVZI3jc8=/48x48/example.com/image.png

image("http://example.com/image.png")
    .resize(48, 48)
    .host("http://me.com")
    .toUrl()
// Produces: http://me.com/unsafe/48x48/example.com/image.png
```

*Note:* If you are using a version of Thumbor older than 3.0 you must call
`legacy()` to ensure the encryption used will be supported by the server.



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
 [2]: http://github.com/square/pollexor/downloads

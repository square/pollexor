Change Log
==========

Version 2.0.1 *(2014-03-18)*
----------------------------

 * Fix: Crop align and smart crop now correctly depend on resize not manual crop.


Version 2.0.0 *(2014-02-11)*
----------------------------

 * `Thumbor` object now encapsulates a thumbor host and optional encryption key
   and is the factory for building images.
 * New: `grayscale()` and `equalize()` filters.
 * New: `format()` method controls response image format.
 * New: `trim()` method removes surrounding space from image.
 * New: `resize()` now supports `ORIGINAL_SIZE` constant.
 * Fix: Remove `UnableToBuildException` in favor of built-in `IllegalArgumentException` and
   `IllegalStateException`.


Version 1.2.0 *(2013-06-19)*
----------------------------

 * Allow zero for one argument to `resize()` which will scale according to
   aspect ratio.
 * Fix: Do not mutate input image URLs. Thumbor 3.0 now supports full URLs in
   the request.


Version 1.1.2 *(2013-03-05)*
----------------------------

 * Fix: Properly place 'fit in' before requested size.


Version 1.1.1 *(2012-11-08)*
----------------------------

 * Fix: Only strip 'http' protocol from image urls.


Version 1.1.0 *(2012-07-26)*
----------------------------

 * New `frame()` filter for 9-patch images.
 * New `stripicc()` filter to remove ICC profile information.


Version 1.0.0 *(2012-07-17)*
----------------------------

 * Update to support new HMAC-SHA1 encryption present in Thumbor v3.0.
 * New `legacy()` method to revert to old encryption mechanism.


Version 0.9.0 *(2012-06-12)*
----------------------------

Initial release.

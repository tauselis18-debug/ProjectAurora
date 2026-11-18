# Project Aurora — Android starter

An original Android photo-editor starter project. It is not affiliated with Adobe and does not copy Lightroom branding or assets.

## Included in this first prototype
- Native Android app using Kotlin and Jetpack Compose
- Dark, minimal editor interface
- Android image picker
- Live preview adjustments for exposure, contrast and saturation
- Reset controls
- JPEG export to `Pictures/Aurora`

## Important current limitations
This is a UI and editing-prototype milestone, **not yet a professional RAW editor**.
- Android's generic bitmap decoder does not reliably decode camera RAW/DNG files.
- HEIC support depends on the Android device and decoder.
- Current preview adjustments are basic CPU/Compose colour-matrix operations, not a production GPU pipeline.
- No masks, curves, HSL, lens profiles, denoising, sharpening or full edit-history persistence yet.

## Planned engineering milestones
1. Integrate a dedicated RAW pipeline (LibRaw or another appropriately licensed/maintained engine), including DNG metadata and camera-profile handling.
2. Move preview processing to a GPU pipeline (OpenGL ES or Vulkan, selected after device/performance testing).
3. Add a non-destructive edit recipe model and undo/redo history.
4. Add curves, HSL, white balance, colour grading, crop/rotate, sharpening, noise reduction and selective masks.
5. Add high-quality export, colour-management tests and device-specific performance benchmarks.
6. Add a one-time purchase entitlement and Google Play billing only after the core editor is stable.

## Build
Open this folder in Android Studio (recent version), allow Gradle sync, then run the `app` configuration on an Android 8.0+ device or emulator.

The project uses Android Gradle Plugin 8.7.3, Kotlin 2.0.21 and Compose BOM 2024.12.01.

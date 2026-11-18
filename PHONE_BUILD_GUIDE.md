# Build Project Aurora from an Android phone

This project includes a GitHub Actions workflow that builds a debug APK in GitHub's cloud. No laptop or local Android Studio installation is required.

## Steps

1. Sign in to GitHub in your phone browser and create a new **public or private** repository named `ProjectAurora`.
2. In the repository, choose **Add file → Upload files**.
3. Extract `ProjectAurora_Android_Starter.zip` on your phone first, then upload the *contents* of the extracted folder, including the hidden `.github/workflows/android-apk.yml` file. If your file manager hides dot-folders, create `.github/workflows` in GitHub's web editor and add `android-apk.yml` using the contents from this project.
4. Commit the uploaded files to the `main` branch.
5. Open the repository's **Actions** tab. Select **Build Project Aurora APK** and tap **Run workflow** if it has not started automatically.
6. Wait for the workflow to finish successfully. Open the completed run and download the `ProjectAurora-debug-apk` artifact.
7. Extract the downloaded artifact ZIP, tap `app-debug.apk`, and approve Android's install prompt if asked.

## What the workflow does

- Uses GitHub-hosted Ubuntu runner
- Installs Java 17 and Android SDK platform/build tools 35
- Uses Gradle 8.9 (compatible with the project's Android Gradle Plugin 8.7.3)
- Runs `:app:assembleDebug`
- Uploads the debug APK as a downloadable workflow artifact

## Important

- This produces a **debug/test APK**, not a store-ready signed release.
- The first prototype still has only basic bitmap preview edits and JPEG export. It does not yet contain the promised full RAW/DNG processing, HEIC pipeline, GPU editing, masks, curves, HSL, denoising or complete professional feature set.
- Keep your GitHub account private and never upload signing keys, passwords, personal photos or private credentials.
- GitHub Actions availability and usage limits depend on repository visibility and GitHub account plan.

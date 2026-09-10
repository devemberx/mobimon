# MobiMon

MobiMon is an Android companion app for a parked vehicle. The first native foundation
includes two home surfaces, a shared left drawer, a replaceable dog placeholder,
appearance and display preferences, and a persistent first quest with an
exactly-once 80 XP reward.

Debug installs as `com.monsters.mobimon.demo` and uses clearly labeled simulated
vehicle data, a separate local profile, and a separate database. Start Q01 from
Quest information, wait for the next vehicle update, then acknowledge the status
card. Progress and preferences remain after restarting the app. If an unfinished
quest spans an observation restart, cancel it and start a new run before confirming.

The package rename makes MobiMon a separate Android app from earlier builds.
Existing progress and preferences are not migrated; remove the earlier app
separately if it is no longer needed.

Release reports real vehicle data as unavailable until a supported adapter is
connected. AI conversation, Q02/Q03, and a system overlay are not implemented yet.
The vehicle home is an in-app preview, with no map, media, or climate controls.
Character artwork can be replaced through `PetAvatar` in `core/core-ui`; see the
[architecture](docs/ARCHITECTURE.md#current-foundation) for extension boundaries.

## Build an APK

Set up JDK 17, Android SDK Platform 34, and Build Tools 34.0.0 using the
[development environment guide](.github/CONTRIBUTING.md#development-environment).
Run the commands below from the repository root. On Windows PowerShell, use
`.\gradlew.bat` instead of `./gradlew`.

### Debug APK

Build an APK for development and testing:

```bash
./gradlew :app:assembleDebug
```

The output is `app/build/outputs/apk/debug/app-debug.apk`. It is automatically
signed with a development key and can be installed using the steps below.

### Signed Release APK

To create an APK for distribution, open the project in Android Studio and let
Gradle sync finish:

1. Select **Build > Generate Signed Bundle / APK**.
2. Choose **APK**, then select the `app` module.
3. Select your signing keystore, or choose **Create new** to create a `.jks` file.
   Enter the keystore password, key alias, and key password.
4. Choose the **release** build variant and an output directory, then build.
5. Use the completion notification to locate the signed APK in that directory.

Keep the keystore and passwords outside Git and retain them for future updates.
Choose **Android App Bundle** instead of APK if preparing a Google Play upload;
an AAB is not directly installable with the APK installation steps below.
See Android's [app signing guide](https://developer.android.com/studio/publish/app-signing).

The command-line Release build is:

```bash
./gradlew :app:assembleRelease
```

This repository does not yet configure Release signing in Gradle. The command
therefore produces `app/build/outputs/apk/release/app-release-unsigned.apk`, which
must be signed before installation. Use the Android Studio steps above to build
a signed APK. See Android's [command-line build guide](https://developer.android.com/build/building-cmdline).

## Install an APK

The current minimum Android version is **Android 14 (API 34)**. Use a compatible
device or emulator. For supplied AAOS hardware, follow its installation and
signing requirements; a successful build alone does not verify vehicle support.

### Install over USB or on an emulator

Install **Android SDK Platform-Tools** using Android Studio's SDK Manager and add
its `platform-tools` directory to your `PATH` so that `adb` is available.

1. On a physical device, enable **Developer options > USB debugging**, connect
   it with a USB data cable, and accept the computer authorization prompt.
   Alternatively, start an Android 14 or newer emulator.
2. Check the connection:

   ```bash
   adb devices
   ```

   The target must appear with status `device`. If it shows `unauthorized`,
   unlock the device and accept the authorization prompt.
3. Install the locally built Debug APK:

   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

   For a downloaded or signed Release APK, replace the path with that APK's
   location. If multiple targets are connected, use
   `adb -s SERIAL install -r "path/to/app.apk"`, replacing `SERIAL` with the value
   shown by `adb devices`.
4. After `Success`, open **MobiMon Demo** for Debug or **MobiMon** for Release from the device's app launcher.

`-r` updates an existing installation while keeping its data when the signing
certificate matches. For connection and installation details, see the
[ADB guide](https://developer.android.com/tools/adb).

### Install directly on a device

On a device that supports APK file installation, copy or download the signed APK
to the device and open it in the file manager. If prompted, allow **Install
unknown apps** for that file manager or browser, then select **Install** and open
**MobiMon**. USB debugging is not needed for this method.
See Android's [installation guidance](https://developer.android.com/distribute/marketing-tools/alternative-distribution).

If an update fails because the signing certificate differs, use an APK signed
with the original key. Switching between Debug and Release APKs, or APKs from
different development machines, can cause this mismatch within the same application ID.
This foundation's Debug and Release variants use separate IDs and can coexist. If you choose
to uninstall the old app before installing the new one, its local data is deleted.

For contribution checks and dependency management, see [CONTRIBUTING](.github/CONTRIBUTING.md).

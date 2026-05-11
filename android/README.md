# My Words, My Way Android

Native Android implementation of the local-first app described in:

- `../ANDROID_APP_PLAN.md`
- `../ANDROID_DATA_MODEL.md`
- `../ANDROID_MOCKUPS.md`

The default model implementation is `MockModelService`, so the full product flow can run without a Gemma server:

1. weekly access gate
2. voice memo recording
3. required user-written note
4. noun suggestion review
5. saved words linked to user notes
6. notes-only export
7. local storage cleanup

Open this directory in Android Studio and sync the Gradle project. This project is configured for the installed Android 16 QPR2 SDK (`36.1`) and AGP `8.13.0`.

If Android Studio does not show your phone in the device picker, check from a terminal:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
```

The device must appear as `device`, not blank or `unauthorized`. If it is missing, reconnect USB, accept the phone's USB debugging prompt, and make sure USB debugging is enabled in Developer options.

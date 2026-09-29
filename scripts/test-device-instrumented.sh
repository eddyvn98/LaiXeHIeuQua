#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/.." && pwd)"
sdk_root="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}"
test_class="${1:-}"
test_apk="$repo_root/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"
target_package="com.eddyvn.laixehieuqua"
test_runner="$target_package.test/androidx.test.runner.AndroidJUnitRunner"

export ANDROID_HOME="$sdk_root"
export ANDROID_SDK_ROOT="$sdk_root"

adb_device() {
  if [[ -n "${ANDROID_SERIAL:-}" ]]; then
    command adb -s "$ANDROID_SERIAL" "$@"
  else
    command adb "$@"
  fi
}

cd "$repo_root"
./gradlew :app:assembleDebugAndroidTest
adb_device shell pm path "$target_package" >/dev/null
adb_device install -t -r "$test_apk"

if [[ -n "$test_class" ]]; then
  adb_device shell am instrument -w -e class "$test_class" "$test_runner"
else
  adb_device shell am instrument -w "$test_runner"
fi
adb_device shell pm path "$target_package" >/dev/null

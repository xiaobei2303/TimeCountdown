#!/bin/bash
set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
ANDROID_HOME="${ANDROID_HOME:-$PROJECT_DIR/android-sdk}"
BUILD_TOOLS="$ANDROID_HOME/build-tools/33.0.0"
PLATFORM="$ANDROID_HOME/platforms/android-33/android.jar"
APP_DIR="$PROJECT_DIR/android/app"
BUILD_DIR="$PROJECT_DIR/build"
OUTPUT_APK="$PROJECT_DIR/时间倒计时.apk"

export PATH="$BUILD_TOOLS:$ANDROID_HOME/platform-tools:$PATH"

echo "=== 清理构建目录 ==="
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/compiled" "$BUILD_DIR/gen" "$BUILD_DIR/classes" "$BUILD_DIR/dex"

echo "=== 1. 编译资源 (aapt2 compile) ==="
aapt2 compile --dir "$APP_DIR/src/main/res" -o "$BUILD_DIR/compiled/"

echo "=== 2. 链接资源 (aapt2 link) ==="
aapt2 link \
  -I "$PLATFORM" \
  --manifest "$APP_DIR/src/main/AndroidManifest.xml" \
  -A "$APP_DIR/src/main/assets" \
  -R "$BUILD_DIR/compiled/"*.flat \
  --java "$BUILD_DIR/gen" \
  -o "$BUILD_DIR/app-unsigned.apk" \
  --auto-add-overlay

echo "=== 3. 编译 Java 源码 (ECJ) ==="
java -jar "$PROJECT_DIR/ecj.jar" \
  -1.8 \
  -cp "$PLATFORM" \
  -d "$BUILD_DIR/classes" \
  "$BUILD_DIR/gen/com/countdown/app/R.java" \
  "$APP_DIR/src/main/java/com/countdown/app/MainActivity.java" \
  "$APP_DIR/src/main/java/com/countdown/app/CountdownWidget.java" \
  "$APP_DIR/src/main/java/com/countdown/app/AnniversaryWidget.java" \
  "$APP_DIR/src/main/java/com/countdown/app/SettingsActivity.java" \
  "$APP_DIR/src/main/java/com/countdown/app/WidgetConfigureActivity.java"

echo "=== 4. 转换为 DEX (d8) ==="
d8 --output "$BUILD_DIR/dex" \
  --lib "$PLATFORM" \
  "$BUILD_DIR/classes/com/countdown/app/"*.class

echo "=== 5. 将 DEX 加入 APK ==="
cd "$BUILD_DIR/dex"
zip -u "$BUILD_DIR/app-unsigned.apk" classes.dex
cd "$PROJECT_DIR"

echo "=== 6. 对齐 (zipalign) ==="
zipalign -f -p 4 "$BUILD_DIR/app-unsigned.apk" "$BUILD_DIR/app-aligned.apk"

echo "=== 7. 生成签名密钥 ==="
if [ ! -f "$BUILD_DIR/keystore.jks" ]; then
  keytool -genkeypair -v \
    -keystore "$BUILD_DIR/keystore.jks" \
    -alias countdown \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -storepass countdown123 -keypass countdown123 \
    -dname "CN=Countdown App, OU=Dev, O=Countdown, L=Beijing, ST=Beijing, C=CN"
fi

echo "=== 8. 签名 (apksigner) ==="
apksigner sign \
  --ks "$BUILD_DIR/keystore.jks" \
  --ks-key-alias countdown \
  --ks-pass pass:countdown123 \
  --key-pass pass:countdown123 \
  --out "$OUTPUT_APK" \
  "$BUILD_DIR/app-aligned.apk"

echo "=== 9. 验证签名 ==="
apksigner verify --verbose "$OUTPUT_APK"

echo ""
echo "========================================="
echo "构建成功! APK 输出: $OUTPUT_APK"
ls -lh "$OUTPUT_APK"
echo "========================================="

#!/bin/bash

# Config
SO_PATH="./app/build/intermediates/merged_native_libs/debug/mergeDebugNativeLibs/out/lib/arm64-v8a/libazura_face_lib.so"
STRIPPED_SO="libazura_face_lib_stripped.so"
OUTPUT_CPP="./app/src/main/cpp/native_integrity_generated.cpp"
TEMPLATE_CPP="./app/src/main/cpp/native_integrity_template.cpp"

# Check file exists
if [ ! -f "$SO_PATH" ]; then
  echo "❌ .so not found at $SO_PATH"
  exit 1
fi

# Strip metadata (requires 'strip' or 'llvm-strip')
cp "$SO_PATH" "$STRIPPED_SO"
strip --strip-unneeded "$STRIPPED_SO" 2>/dev/null || llvm-strip "$STRIPPED_SO"

# Get SHA256 hash
HASH=$(sha256sum "$STRIPPED_SO" | awk '{print $1}')
echo "✅ Hash: $HASH"

# XOR encode
XOR_HASH=$(python3 -c "
h = bytes.fromhex('$HASH')
x = bytes([b ^ 0xAA for b in h])
print(', '.join(f'0x{b:02x}' for b in x))
")

# Inject into template
sed "s|//__HASH_PLACEHOLDER__|const uint8_t ENCRYPTED_EXPECTED_HASH[32] = { $XOR_HASH };|" "$TEMPLATE_CPP" > "$OUTPUT_CPP"

echo "✅ Output written to: $OUTPUT_CPP"

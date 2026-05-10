YSMParser Native Libraries
==========================

This directory contains pre-built native libraries from the YSMParser project
(https://github.com/OpenYSM/YSMParser), used by LgeacyYSM to support newer
YSM model format versions (V3+).

Contents
--------

ysmparser/
├── linux-x64/       libYSMParserJNI.so + YSMParser (x86_64)
├── linux-arm64/     libYSMParserJNI.so + YSMParser (aarch64)
├── linux-loongarch64/  YSMParser (CLI only, no JNI)
├── linux-riscv64/      YSMParser (CLI only, no JNI)
├── windows-x64/     YSMParserJNI.dll + YSMParser.exe
├── windows-x86/     YSMParserJNI.dll + YSMParser.exe
└── macos-arm64/     libYSMParserJNI.dylib + YSMParser (Apple Silicon)

Source: https://github.com/OpenYSM/YSMParser
Version: 0.3.5
License: MIT (see LICENSE.md)

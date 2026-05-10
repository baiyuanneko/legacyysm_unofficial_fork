package moe.byn.minecraftmod.legacyysm.util;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

/**
 * Loads YSMParser native shared library (.so/.dll/.dylib) from inside the mod JAR.
 *
 * <p>The library is extracted to a temporary directory on first call and loaded
 * via {@link System#load}. Subsequent calls are no-ops.
 *
 * <p>Platforms that lack a JNI library (loongarch64, riscv64) are detected and
 * reported; callers should fall back to the CLI executable in those cases.
 */
public final class NativeLoader {

    private static final String NATIVE_DIR = "natives/ysmparser";
    private static final String UNSUPPORTED_MESSAGE =
            "YSMParser JNI not available for this platform. "
                    + "Falling back to CLI executable.";

    private static volatile boolean loaded = false;
    private static volatile boolean jniAvailable = false;
    private static Path extractedLib = null;

    private NativeLoader() {}

    /**
     * Load the YSMParser JNI library for the current platform.
     * Thread-safe and idempotent — safe to call multiple times.
     *
     * @return {@code true} if JNI loaded successfully, {@code false} if JNI is
     *         unavailable (caller should use CLI fallback)
     */
    public static synchronized boolean load() {
        if (loaded) {
            return jniAvailable;
        }

        PlatformInfo platform = detectPlatform();
        String libName = platform.libraryName;

        if (libName == null) {
            YesSteveModel.LOGGER.warn("YSMParser JNI: {} (os={}, arch={})",
                    UNSUPPORTED_MESSAGE, platform.osTag, platform.archTag);
            loaded = true;
            jniAvailable = false;
            return false;
        }

        String resourcePath = NATIVE_DIR + "/" + platform.folder
                + "/" + libName;

        try {
            Path tempDir = Files.createTempDirectory("ysm_native_");
            extractedLib = tempDir.resolve(libName);

            try (InputStream in = NativeLoader.class.getClassLoader()
                    .getResourceAsStream(resourcePath)) {
                if (in == null) {
                    throw new IOException("Native library not found in JAR: "
                            + resourcePath);
                }
                Files.copy(in, extractedLib, StandardCopyOption.REPLACE_EXISTING);
            }

            if (!platform.osTag.contains("win")) {
                extractedLib.toFile().setExecutable(true);
            }

            System.load(extractedLib.toAbsolutePath().toString());

            extractedLib.toFile().deleteOnExit();
            tempDir.toFile().deleteOnExit();

            YesSteveModel.LOGGER.info("YSMParser JNI loaded: {} ({}/{})",
                    libName, platform.osTag, platform.archTag);

            loaded = true;
            jniAvailable = true;
            return true;

        } catch (UnsatisfiedLinkError e) {
            throw new RuntimeException(
                    "Failed to link YSMParser native library: " + libName
                            + ". The library may be incompatible with this JVM "
                            + "or platform.", e);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to extract YSMParser native library from JAR: "
                            + resourcePath, e);
        }
    }

    public static synchronized boolean isJniAvailable() {
        if (!loaded) {
            PlatformInfo platform = detectPlatform();
            return platform.libraryName != null;
        }
        return jniAvailable;
    }

    private static PlatformInfo detectPlatform() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);

        if (arch.equals("amd64") || arch.equals("x86_64")) {
            arch = "x64";
        } else if (arch.equals("aarch64") || arch.equals("arm64")) {
            arch = "arm64";
        }

        PlatformInfo info = new PlatformInfo();
        info.osTag = os;
        info.archTag = arch;

        if (os.contains("win")) {
            if (arch.equals("x64") || arch.contains("amd64")) {
                info.folder = "windows-x64";
                info.libraryName = "YSMParserJNI.dll";
            } else if (arch.contains("x86") || arch.contains("i386")) {
                info.folder = "windows-x86";
                info.libraryName = "YSMParserJNI.dll";
            }
        } else if (os.contains("mac") || os.contains("darwin")) {
            if (arch.equals("arm64") || arch.contains("aarch64")) {
                info.folder = "macos-arm64";
                info.libraryName = "libYSMParserJNI.dylib";
            }
            // x64 macOS not bundled — no JNI
        } else if (os.contains("linux")) {
            if (arch.equals("x64") || arch.contains("amd64")) {
                info.folder = "linux-x64";
                info.libraryName = "libYSMParserJNI.so";
            } else if (arch.equals("arm64") || arch.contains("aarch64")) {
                info.folder = "linux-arm64";
                info.libraryName = "libYSMParserJNI.so";
            }
            // loongarch64, riscv64 — no JNI
        }

        return info;
    }

    private static class PlatformInfo {
        String osTag;
        String archTag;
        String folder;
        String libraryName;
    }
}

package dev.JustRed23.grandfather.utils;

import java.io.File;

public final class JarUtils {

    public static File getJarFile() {
        return new File(JarUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    }
}

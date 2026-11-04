package org.ywzj.decoration.platform;

import javax.annotation.Nullable;

public final class DecorationPlatforms {

    @Nullable
    private static volatile DecorationPlatform platform;

    private DecorationPlatforms() {}

    public static void install(DecorationPlatform implementation) {
        if (platform != null) {
            throw new IllegalStateException("Decoration platform already installed");
        }
        platform = implementation;
    }

    public static DecorationPlatform get() {
        DecorationPlatform current = platform;
        if (current == null) {
            throw new IllegalStateException("Decoration platform is not installed yet");
        }
        return current;
    }

    public static boolean isClient() {
        DecorationPlatform current = platform;
        return current != null && current.isClient();
    }

    @Nullable
    public static DecorationClientPlatform client() {
        DecorationPlatform current = platform;
        return current == null ? null : current.client();
    }
}

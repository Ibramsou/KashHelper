package fr.ibrakash.helper.platform;

import java.util.*;

public class KashPlatformService {

    private static final KashPlatform<?> defaultPlatform;
    private static final Map<KashPlatformType, KashPlatform<?>> platforms = new EnumMap<>(KashPlatformType.class);

    public static KashPlatform<?> getPlatform() {
        return defaultPlatform;
    }

    public static KashPlatform<?> getPlatform(KashPlatformType platformType) {
        return requirePlatform(platformType);
    }

    public static <P extends KashPlatform<?>> P getPlatform(KashPlatformType platformType, Class<P> platformClass) {
        KashPlatform<?> platform = requirePlatform(platformType);
        if (!platformClass.isInstance(platform)) {
            throw new IllegalStateException("Platform " + platformType + " is " + platform.getClass().getName()
                    + " but " + platformClass.getName() + " was requested.");
        }
        return platformClass.cast(platform);
    }

    private static KashPlatform<?> requirePlatform(KashPlatformType platformType) {
        return Optional.ofNullable(platforms.get(platformType)).orElseThrow(() ->
                new IllegalArgumentException("No KashPlatform found for type " + platformType + "."));
    }

    @SuppressWarnings("unchecked")
    private static ServiceLoader<KashPlatform<?>> loadPlatforms() {
        return (ServiceLoader<KashPlatform<?>>) (ServiceLoader<?>) ServiceLoader.load(KashPlatform.class, KashPlatform.class.getClassLoader());
    }

    static {
        ServiceLoader<KashPlatform<?>> loader = loadPlatforms();
        for (KashPlatform<?> platform : loader) {
            platforms.put(platform.platformType(), platform);
        }
        defaultPlatform = platforms.values().stream().min(Comparator.comparingInt(KashPlatform::priority)).orElseThrow(() ->
                new IllegalStateException("No KashPlatform implementation found."));
    }
}

package me.geek.tom.crashspotter;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class CrashSpotter implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "crash-spotter";
    public static final String MOD_NAME = "Crash Spotter";

    private static final List<Mod> mods = new ArrayList<>();

    @Override
    public void onInitialize() {
        LOGGER.info("[" + MOD_NAME + "] Initialising...");
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            // Skip minecraft and java, otherwise they will otherwise show up in the report all the time.
            if (mod.getMetadata().getId().equals("minecraft") || mod.getMetadata().getId().equals("java")) continue;
            mods.add(new Mod(mod.getMetadata().getId(), mod.getMetadata().getName()));
        }
    }

    public static StackTraceElement[] lookAtStacktrace(StackTraceElement[] stackTrace, Set<String> relatedMods, Set<String> relatedMixins) {
        List<Mod> modSearchTerms = getMods();
        List<StackTraceElement> elements = Arrays.stream(stackTrace).collect(Collectors.toList());

        int i = 0;
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().equals("     ^^^ Method injected by Mixin")) continue;

            for (Mod mod : modSearchTerms) {
                if (mod.matches(element)) {
                    relatedMods.add(String.format("\t\t- '%s' (%s)", mod.name, mod.id));
                }
            }

            try {
                Class<?> cls = Class.forName(element.getClassName());
                for (Method method : cls.getDeclaredMethods()) {
                    if (method.getName().equals(element.getMethodName())) {
                        MixinMerged mixinMetadata = method.getAnnotation(MixinMerged.class);
                        if (mixinMetadata != null) {
                            String mixinName = mixinMetadata.mixin();
                            for (Mod mod : modSearchTerms) {
                                if (mod.matches(mixinName)) {
                                    relatedMods.add(String.format("\t\t- '%s' (%s)", mod.name, mod.id));
                                    break;
                                }
                            }

                            relatedMixins.add("\t\t- " + mixinName);
                            elements.add(i + 1, new StackTraceElement("     ^^^ Method injected by a Mixin", "", mixinName, 0));
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            i++;
        }
        return elements.toArray(new StackTraceElement[0]);
    }

    private static List<Mod> getMods() {
        return mods;
    }

    private static class Mod {
        private final String id;
        private final String name;
        private final String searchId;
        private final String searchName;

        private Mod(String id, String name) {
            this.id = id;
            this.name = name;
            this.searchId = id.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
            this.searchName = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
        }

        private boolean matches(StackTraceElement element) {
            return this.matches(element.getClassName());
        }

        private boolean matches(String className) {
            String cls = className.replace(".", "").toLowerCase(Locale.ROOT);
            return cls.contains(this.searchId) || cls.contains(this.searchName);
        }
    }
}

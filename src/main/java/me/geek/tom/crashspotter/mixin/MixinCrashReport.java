package me.geek.tom.crashspotter.mixin;

import me.geek.tom.crashspotter.CrashSpotter;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static me.geek.tom.crashspotter.CrashSpotter.MOD_ID;
import static me.geek.tom.crashspotter.CrashSpotter.MOD_NAME;

@Mixin(CrashReport.class)
public abstract class MixinCrashReport {
    @Shadow @Final private List<CrashReportSection> otherSections;

    @Shadow private StackTraceElement[] stackTrace;

    @Inject(method = "addStackTrace", at = @At("HEAD"))
    private void hook_addStackTrace(CallbackInfo ci) {
        Set<String> relatedMods = new HashSet<>();
        Set<String> relatedMixins = new HashSet<>();
        this.stackTrace = CrashSpotter.lookAtStacktrace(this.stackTrace, relatedMods, relatedMixins);
        CrashReportSection section = new CrashReportSection((CrashReport) (Object) this, "Crash Spotter Report");
        section.add("Potentially related mods", () -> "\n" + String.join("\n", relatedMods));
        section.add("Potentially related mixins", () -> "\n" + String.join("\n", relatedMixins));
        if (relatedMods.contains("\t\t- '" + MOD_ID + "' (" + MOD_NAME + ")")) {
            section.add("It wasn't my fault, I promise!", "");
        }
        this.otherSections.add(section);
    }
}

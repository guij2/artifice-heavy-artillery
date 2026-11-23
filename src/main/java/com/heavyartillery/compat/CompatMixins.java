package com.heavyartillery.compat;

import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mixin plugin of {@code artifice_heavy_artillery.compat.mixins.json}: adds each compatibility mixin only when the mod it
 * targets is installed, so a missing mod costs nothing, not even a log line.
 */
public final class CompatMixins implements IMixinConfigPlugin {
    /** Mod id to the compat mixins (in {@code com.heavyartillery.mixin.compat}) that need it. */
    private static final Map<String, List<String>> BY_MOD = Map.of(
            "irons_spell_bullet", List.of("SpellBulletReloadHelperMixin")
    );

    @Override
    public List<String> getMixins() {
        LoadingModList mods = LoadingModList.get();
        // dev checks force a compat mixin on against a stand-in class: -Dartifice_heavy_artillery.compat.force=<mod id>,...
        List<String> forced = List.of(System.getProperty("artifice_heavy_artillery.compat.force", "").split(","));
        return BY_MOD.entrySet().stream()
                .filter(entry -> forced.contains(entry.getKey()) || mods != null && mods.getModFileById(entry.getKey()) != null)
                .flatMap(entry -> entry.getValue().stream())
                .toList();
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}

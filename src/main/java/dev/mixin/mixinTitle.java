package dev.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class mixinTitle {

    private static final String WORLD_NAME = "dcemi_world";
    private static final long RETRY_MS = 5000;
    private static long dcemiLastAttempt = 0L;

    @Inject(method = "init", at = @At("HEAD"))
    private void dcemiautoLoad(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) return;

        long now = System.currentTimeMillis();
        if (now - dcemiLastAttempt < RETRY_MS) return;
        dcemiLastAttempt = now;

        if (mc.getLevelSource().levelExists(WORLD_NAME)) {
            System.err.println("[DCEMI] loading world '" + WORLD_NAME + "'");
            mc.createWorldOpenFlows().loadLevel((TitleScreen) (Object) this, WORLD_NAME);
        } else {
            System.err.println("[DCEMI] creating world '" + WORLD_NAME + "' (flat, creative, peaceful, frozen)");
            LevelSettings settings = new LevelSettings(
                    WORLD_NAME,
                    GameType.CREATIVE,
                    false,
                    Difficulty.PEACEFUL,
                    true,
                    dcemi$frozenGameRules(),
                    WorldDataConfiguration.DEFAULT);
            mc.createWorldOpenFlows().createFreshLevel(
                    WORLD_NAME,
                    settings,
                    new WorldOptions(0L, false, false),
                    registryAccess -> registryAccess
                            .registryOrThrow(Registries.WORLD_PRESET)
                            .getHolderOrThrow(WorldPresets.FLAT)
                            .value()
                            .createWorldDimensions());
        }
    }

    @Unique
    private static GameRules dcemi$frozenGameRules() {
        GameRules rules = new GameRules();
        rules.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
        rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, null);
        rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, null);
        rules.getRule(GameRules.RULE_DO_PATROL_SPAWNING).set(false, null); // no pillager patrols
        rules.getRule(GameRules.RULE_DO_TRADER_SPAWNING).set(false, null); // no wandering traders
        rules.getRule(GameRules.RULE_DOINSOMNIA).set(false, null); // no phantoms
        rules.getRule(GameRules.RULE_DOFIRETICK).set(false, null);
        rules.getRule(GameRules.RULE_MOBGRIEFING).set(false, null);
        rules.getRule(GameRules.RULE_RANDOMTICKING).set(0, null);
        return rules;
    }
}
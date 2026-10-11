package ml.shadniw.mods.littlesun.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import ml.shadniw.mods.littlesun.util.DragonCoreSearch;
import ml.shadniw.mods.littlesun.util.SolarWeather;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "owmii.powah.block.solar.SolarBlockEntity")
public abstract class MixinPowahSolarBlockEntity {
    @ModifyExpressionValue(
            method = "postTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getSkyDarken()I"))
    private int littlesun$allowNightGeneration(int skyDarken) {
        MixinBlockEntityAccessor accessor = (MixinBlockEntityAccessor) this;
        if (DragonCoreSearch.hasActiveCoreNear(
                accessor.littlesun$getLevel(), accessor.littlesun$getWorldPosition())) {
            return 0;
        }
        return SolarWeather.isDaylightAndCanSeeSky(
                accessor.littlesun$getLevel(), accessor.littlesun$getWorldPosition())
                ? Math.min(skyDarken, 3)
                : skyDarken;
    }
}

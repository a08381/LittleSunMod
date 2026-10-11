package ml.shadniw.mods.littlesun.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import ml.shadniw.mods.littlesun.util.DragonCoreSearch;
import ml.shadniw.mods.littlesun.util.SolarWeather;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "mekanism.common.tile.machine.TileEntitySolarNeutronActivator")
public abstract class MixinMekanismSolarNeutronActivator {
    @ModifyExpressionValue(
            method = "canSeeSun",
            at = @At(
                    value = "INVOKE",
                    target = "Lmekanism/common/util/WorldUtils;canSeeSun(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean littlesun$allowNightOperation(boolean canSeeSun) {
        MixinBlockEntityAccessor accessor = (MixinBlockEntityAccessor) this;
        return canSeeSun || DragonCoreSearch.hasActiveCoreNear(
                accessor.littlesun$getLevel(), accessor.littlesun$getWorldPosition())
                || SolarWeather.isDaylightAndCanSeeSky(
                        accessor.littlesun$getLevel(), accessor.littlesun$getWorldPosition());
    }

    @ModifyExpressionValue(
            method = "recalculateProductionRate",
            at = @At(
                    value = "INVOKE",
                    target = "Lmekanism/common/util/WorldUtils;getSunBrightness(Lnet/minecraft/world/level/Level;F)F"))
    private float littlesun$allowNightGeneration(float brightness) {
        MixinBlockEntityAccessor accessor = (MixinBlockEntityAccessor) this;
        return DragonCoreSearch.hasActiveCoreNear(
                accessor.littlesun$getLevel(), accessor.littlesun$getWorldPosition()) ? 1.0F : brightness;
    }
}

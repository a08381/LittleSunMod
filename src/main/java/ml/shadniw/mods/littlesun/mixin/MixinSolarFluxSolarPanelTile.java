package ml.shadniw.mods.littlesun.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import ml.shadniw.mods.littlesun.util.DragonCoreSearch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "org.zeith.solarflux.block.SolarPanelTile")
public abstract class MixinSolarFluxSolarPanelTile {
    @ModifyExpressionValue(
            method = "getGeneration",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/zeith/solarflux/panels/SolarPanelInstance;computeSunIntensity(Lorg/zeith/solarflux/api/ISolarPanelTile;)F"))
    private float littlesun$allowNightGeneration(float intensity) {
        if (intensity > 0) {
            return intensity;
        }

        MixinBlockEntityAccessor accessor = (MixinBlockEntityAccessor) this;
        return DragonCoreSearch.hasActiveCoreNear(
                accessor.littlesun$getLevel(), accessor.littlesun$getWorldPosition()) ? 1.0F : intensity;
    }
}

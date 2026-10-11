package ml.shadniw.mods.littlesun.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import ml.shadniw.mods.littlesun.util.DragonCoreSearch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "mekanism.generators.common.tile.TileEntitySolarGenerator")
public abstract class MixinMekanismSolarGenerator {
    @ModifyExpressionValue(
            method = "getBrightnessMultiplier",
            at = @At(
                    value = "INVOKE",
                    target = "Lmekanism/common/util/WorldUtils;getSunBrightness(Lnet/minecraft/world/level/Level;F)F"))
    private float littlesun$allowNightGeneration(float brightness) {
        MixinBlockEntityAccessor accessor = (MixinBlockEntityAccessor) this;
        return DragonCoreSearch.hasActiveCoreNear(
                accessor.littlesun$getLevel(), accessor.littlesun$getWorldPosition()) ? 1.0F : brightness;
    }
}

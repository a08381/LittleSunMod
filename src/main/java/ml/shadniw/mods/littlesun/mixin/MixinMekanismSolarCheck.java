package ml.shadniw.mods.littlesun.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import ml.shadniw.mods.littlesun.util.DragonCoreSearch;
import ml.shadniw.mods.littlesun.util.SolarWeather;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "mekanism.generators.common.tile.TileEntitySolarGenerator$SolarCheck")
public abstract class MixinMekanismSolarCheck {
    @Shadow
    @Final
    protected BlockPos pos;

    @Shadow
    @Final
    protected Level world;

    @ModifyExpressionValue(
            method = "recheckCanSeeSun",
            at = @At(
                    value = "INVOKE",
                    target = "Lmekanism/common/util/WorldUtils;canSeeSun(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean littlesun$allowNightOperation(boolean canSeeSun) {
        return canSeeSun
                || SolarWeather.isDaylightAndCanSeeSky(world, pos)
                || DragonCoreSearch.hasActiveCoreNear(world, pos);
    }
}

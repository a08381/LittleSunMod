package ml.shadniw.mods.littlesun.mixin;

import ml.shadniw.mods.littlesun.util.DragonCoreSearch;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyCore")
public abstract class MixinDraconicEnergyCore {
    @Inject(method = "tick", at = @At("TAIL"))
    private void littlesun$refreshActiveCoreIndex(CallbackInfo callbackInfo) {
        DragonCoreSearch.refreshCoreIndex((BlockEntity) (Object) this);
    }
}

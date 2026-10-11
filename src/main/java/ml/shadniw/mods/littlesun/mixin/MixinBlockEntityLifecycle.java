package ml.shadniw.mods.littlesun.mixin;

import ml.shadniw.mods.littlesun.util.DragonCoreSearch;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public abstract class MixinBlockEntityLifecycle {
    @Inject(method = "setRemoved", at = @At("HEAD"), remap = false)
    private void littlesun$removeDragonCoreFromIndex(CallbackInfo callbackInfo) {
        DragonCoreSearch.removeCoreFromIndex((BlockEntity) (Object) this);
    }
}

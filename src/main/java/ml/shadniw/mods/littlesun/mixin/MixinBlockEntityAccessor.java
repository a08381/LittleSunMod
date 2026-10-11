package ml.shadniw.mods.littlesun.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockEntity.class)
public interface MixinBlockEntityAccessor {
    @Accessor("level")
    Level littlesun$getLevel();

    @Accessor("worldPosition")
    BlockPos littlesun$getWorldPosition();
}

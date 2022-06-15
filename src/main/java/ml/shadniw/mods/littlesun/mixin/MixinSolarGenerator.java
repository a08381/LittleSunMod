package ml.shadniw.mods.littlesun.mixin;

import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyStorageCore;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import ml.shadniw.mods.littlesun.LittleSunLoadingPlugin;
import ml.shadniw.mods.littlesun.util.FindTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(TileEntitySolarGenerator.class)
public class MixinSolarGenerator extends TileEntity {

    @Redirect(method = "onUpdate()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isDaytime()Z"))
    public boolean isDaytime(World instance) {
        List<TileEntity> escs = FindTileEntity.findUpon(world, pos, TileEnergyStorageCore.class, 64);

        LittleSunLoadingPlugin.logger.info("在方块附近发现了 " + escs.size() + " 个小太阳.");

        List<TileEnergyStorageCore> list = Arrays.stream(escs.toArray(new TileEnergyStorageCore[0]))
                .filter(esc -> esc.active.value && esc.tier.value >= 7 && esc.energy.value > 0)
                .collect(Collectors.toList());

        return !list.isEmpty() || world.isDaytime();
    }
}

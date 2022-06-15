package ml.shadniw.mods.littlesun.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FindTileEntity {


    public static List<TileEntity> findUpon(World world, BlockPos pos, Class<? extends TileEntity> clz, int range) {
        int minX = pos.getX() - range;
        int maxX = pos.getX() + range;
        int minY = pos.getY();
        int maxY = world.getHeight();
        int minZ = pos.getZ() - range;
        int maxZ = pos.getZ() + range;

        return world.loadedTileEntityList.stream().filter(
                t -> clz.isInstance(t) && t.getWorld().equals(world)
                        && t.getPos().getX() >= minX && t.getPos().getX() <= maxX
                        && t.getPos().getY() >= minY && t.getPos().getY() <= maxY
                        && t.getPos().getZ() >= minZ && t.getPos().getZ() <= maxZ
        ).collect(Collectors.toList());
    }

}

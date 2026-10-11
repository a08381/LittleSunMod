package ml.shadniw.mods.littlesun.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class SolarWeather {
    private static final long DAY_LENGTH = 24_000L;
    private static final long DAYTIME_LENGTH = 12_000L;

    private SolarWeather() {
    }

    public static boolean isDaylightAndCanSeeSky(Level level, @NotNull BlockPos position) {
        BlockPos checkedPosition = Objects.requireNonNull(position, "position");
        return level != null
                && level.dimensionType().hasSkyLight()
                && Math.floorMod(level.getDayTime(), DAY_LENGTH) < DAYTIME_LENGTH
                && level.canSeeSky(checkedPosition);
    }
}

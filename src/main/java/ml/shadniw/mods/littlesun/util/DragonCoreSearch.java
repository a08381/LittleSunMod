package ml.shadniw.mods.littlesun.util;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class DragonCoreSearch {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DRACONIC_EVOLUTION_MOD_ID = "draconicevolution";
    private static final String[] CORE_CLASS_NAMES = {
            "com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyCore",
            "com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyStorageCore"
    };
    private static final int SEARCH_RANGE = 64;
    private static final Map<ServerLevel, LevelIndex> LEVEL_INDEXES = new WeakHashMap<>();

    private static volatile CoreAccess coreAccess;
    private static volatile boolean coreAccessFailed;

    private DragonCoreSearch() {
    }

    public static void refreshCoreIndex(BlockEntity blockEntity) {
        Level level = blockEntity.getLevel();
        if (!(level instanceof ServerLevel serverLevel) || !ModList.get().isLoaded(DRACONIC_EVOLUTION_MOD_ID)) {
            return;
        }

        CoreAccess access = getCoreAccess(serverLevel);
        if (access == null || !access.coreClass.isInstance(blockEntity)) {
            return;
        }

        try {
            BlockPos position = blockEntity.getBlockPos().immutable();
            if (access.isActiveAndHighTier(blockEntity)) {
                addCore(serverLevel, position);
            } else {
                removeCore(serverLevel, position);
            }
        } catch (ReflectiveOperationException exception) {
            disableCoreAccess(exception);
        }
    }

    public static void removeCoreFromIndex(BlockEntity blockEntity) {
        Level level = blockEntity.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        CoreAccess access = coreAccess;
        if (access != null && access.coreClass.isInstance(blockEntity)) {
            removeCore(serverLevel, blockEntity.getBlockPos());
        }
    }

    public static void removeChunkFromIndex(ServerLevel level, int chunkX, int chunkZ) {
        synchronized (LEVEL_INDEXES) {
            LevelIndex index = LEVEL_INDEXES.get(level);
            if (index != null) {
                index.coresByChunk.remove(chunkKey(chunkX, chunkZ));
            }
        }
    }

    public static boolean hasActiveCoreNear(Level level, BlockPos position) {
        if (!(level instanceof ServerLevel serverLevel) || !ModList.get().isLoaded(DRACONIC_EVOLUTION_MOD_ID)) {
            return false;
        }

        CoreAccess access = getCoreAccess(serverLevel);
        if (access == null) {
            return false;
        }

        List<BlockPos> nearbyCores = getNearbyCores(serverLevel, position);
        for (BlockPos corePosition : nearbyCores) {
            if (Math.abs(corePosition.getX() - position.getX()) > SEARCH_RANGE
                    || Math.abs(corePosition.getZ() - position.getZ()) > SEARCH_RANGE) {
                continue;
            }

            LevelChunk chunk = serverLevel.getChunkSource().getChunkNow(
                    corePosition.getX() >> 4, corePosition.getZ() >> 4);
            if (chunk == null) {
                removeCore(serverLevel, corePosition);
                continue;
            }

            BlockEntity blockEntity = chunk.getBlockEntity(corePosition);
            if (blockEntity == null || !access.coreClass.isInstance(blockEntity)) {
                removeCore(serverLevel, corePosition);
                continue;
            }

            try {
                if (!access.isActiveAndHighTier(blockEntity)) {
                    removeCore(serverLevel, corePosition);
                } else if (access.hasEnergy(blockEntity)) {
                    return true;
                }
            } catch (ReflectiveOperationException exception) {
                disableCoreAccess(exception);
                return false;
            }
        }
        return false;
    }

    private static List<BlockPos> getNearbyCores(ServerLevel level, BlockPos position) {
        int minChunkX = (position.getX() - SEARCH_RANGE) >> 4;
        int maxChunkX = (position.getX() + SEARCH_RANGE) >> 4;
        int minChunkZ = (position.getZ() - SEARCH_RANGE) >> 4;
        int maxChunkZ = (position.getZ() + SEARCH_RANGE) >> 4;
        List<BlockPos> nearbyCores = new ArrayList<>();

        synchronized (LEVEL_INDEXES) {
            LevelIndex index = LEVEL_INDEXES.get(level);
            if (index == null) {
                return nearbyCores;
            }
            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                    Set<BlockPos> positions = index.coresByChunk.get(chunkKey(chunkX, chunkZ));
                    if (positions != null) {
                        nearbyCores.addAll(positions);
                    }
                }
            }
        }
        return nearbyCores;
    }

    private static void addCore(ServerLevel level, BlockPos position) {
        synchronized (LEVEL_INDEXES) {
            LevelIndex index = LEVEL_INDEXES.computeIfAbsent(level, ignored -> new LevelIndex());
            index.coresByChunk
                    .computeIfAbsent(chunkKey(position.getX() >> 4, position.getZ() >> 4), ignored -> new HashSet<>())
                    .add(position.immutable());
        }
    }

    private static void removeCore(ServerLevel level, BlockPos position) {
        synchronized (LEVEL_INDEXES) {
            LevelIndex index = LEVEL_INDEXES.get(level);
            if (index == null) {
                return;
            }
            long key = chunkKey(position.getX() >> 4, position.getZ() >> 4);
            Set<BlockPos> positions = index.coresByChunk.get(key);
            if (positions != null) {
                positions.remove(position);
                if (positions.isEmpty()) {
                    index.coresByChunk.remove(key);
                }
            }
            if (index.coresByChunk.isEmpty()) {
                LEVEL_INDEXES.remove(level);
            }
        }
    }

    private static long chunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xffffffffL);
    }

    private static CoreAccess getCoreAccess(ServerLevel level) {
        CoreAccess current = coreAccess;
        if (current != null || coreAccessFailed) {
            return current;
        }

        synchronized (DragonCoreSearch.class) {
            if (coreAccess != null || coreAccessFailed) {
                return coreAccess;
            }
            try {
                ClassLoader classLoader = level.getClass().getClassLoader();
                for (String coreClassName : CORE_CLASS_NAMES) {
                    try {
                        Class<?> coreClass = Class.forName(coreClassName, false, classLoader);
                        coreAccess = CoreAccess.create(coreClass);
                        return coreAccess;
                    } catch (ClassNotFoundException ignored) {
                        // The legacy and current Draconic Evolution versions use different core class names.
                    }
                }
                throw new ClassNotFoundException(String.join(", ", CORE_CLASS_NAMES));
            } catch (ReflectiveOperationException | LinkageError exception) {
                disableCoreAccess(exception);
                return null;
            }
        }
    }

    private static void disableCoreAccess(Throwable exception) {
        synchronized (DragonCoreSearch.class) {
            if (!coreAccessFailed) {
                coreAccessFailed = true;
                coreAccess = null;
                synchronized (LEVEL_INDEXES) {
                    LEVEL_INDEXES.clear();
                }
                LOGGER.error("Could not read Draconic Evolution energy core state; disabling LittleSun core support.",
                        exception);
            }
        }
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                Field field = current.getDeclaredField(name);
                if (!field.trySetAccessible()) {
                    throw new NoSuchFieldException("Field is inaccessible: " + current.getName() + "." + name);
                }
                return field;
            } catch (NoSuchFieldException exception) {
                if (current.getSuperclass() == null) {
                    throw exception;
                }
            }
        }
        throw new NoSuchFieldException(type.getName() + "." + name);
    }

    private static Field findOptionalField(Class<?> type, String name) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                Field field = current.getDeclaredField(name);
                return field.trySetAccessible() ? field : null;
            } catch (NoSuchFieldException exception) {
                if (current.getSuperclass() == null) {
                    return null;
                }
            }
        }
        return null;
    }

    private static java.lang.reflect.Method findMethod(Class<?> type, String... names) {
        for (String name : names) {
            for (Class<?> current = type; current != null; current = current.getSuperclass()) {
                try {
                    java.lang.reflect.Method method = current.getDeclaredMethod(name);
                    if (method.trySetAccessible()) {
                        return method;
                    }
                } catch (NoSuchMethodException exception) {
                    if (current.getSuperclass() == null) {
                        break;
                    }
                }
            }
        }
        return null;
    }

    private static final class LevelIndex {
        private final Map<Long, Set<BlockPos>> coresByChunk = new HashMap<>();
    }

    private static final class CoreAccess {
        private final Class<?> coreClass;
        private final ValueReader activeReader;
        private final ValueReader tierReader;
        private final ValueReader energyReader;

        private CoreAccess(Class<?> coreClass, Field activeField, Field tierField, Field energyField)
                throws NoSuchFieldException {
            this.coreClass = coreClass;
            this.activeReader = ValueReader.forField(activeField, "get");
            this.tierReader = ValueReader.forField(tierField, "get");
            this.energyReader = ValueReader.forField(energyField, "getOPStored", "get");
        }

        private static CoreAccess create(Class<?> coreClass) throws NoSuchFieldException {
            return new CoreAccess(
                    coreClass,
                    findField(coreClass, "active"),
                    findField(coreClass, "tier"),
                    findField(coreClass, "energy"));
        }

        private boolean isActiveAndHighTier(BlockEntity blockEntity) throws ReflectiveOperationException {
            Object active = activeReader.read(blockEntity);
            Object tier = tierReader.read(blockEntity);
            return Boolean.TRUE.equals(active) && tier instanceof Number tierValue && tierValue.intValue() >= 7;
        }

        private boolean hasEnergy(BlockEntity blockEntity) throws ReflectiveOperationException {
            Object energy = energyReader.read(blockEntity);
            return energy instanceof Number energyValue && energyValue.doubleValue() > 0;
        }
    }

    private static final class ValueReader {
        private final Field ownerField;
        private final Field valueField;
        private final java.lang.reflect.Method valueMethod;

        private ValueReader(Field ownerField, Field valueField, java.lang.reflect.Method valueMethod) {
            this.ownerField = ownerField;
            this.valueField = valueField;
            this.valueMethod = valueMethod;
        }

        private static ValueReader forField(Field field, String... methods) throws NoSuchFieldException {
            java.lang.reflect.Method getter = findMethod(field.getType(), methods);
            if (getter != null) {
                return new ValueReader(field, null, getter);
            }
            Field nestedValue = findOptionalField(field.getType(), "value");
            if (nestedValue != null) {
                return new ValueReader(field, nestedValue, null);
            }
            throw new NoSuchFieldException("No readable value accessor for " + field);
        }

        private Object read(Object owner) throws ReflectiveOperationException {
            Object valueOwner = ownerField.get(owner);
            return valueField != null ? valueField.get(valueOwner) : valueMethod.invoke(valueOwner);
        }
    }
}

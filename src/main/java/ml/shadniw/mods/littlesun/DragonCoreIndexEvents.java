package ml.shadniw.mods.littlesun;

import ml.shadniw.mods.littlesun.util.DragonCoreSearch;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

@EventBusSubscriber(modid = LittleSun.MOD_ID)
public final class DragonCoreIndexEvents {
    private DragonCoreIndexEvents() {
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            DragonCoreSearch.removeChunkFromIndex(
                    serverLevel, event.getChunk().getPos().x, event.getChunk().getPos().z);
        }
    }
}

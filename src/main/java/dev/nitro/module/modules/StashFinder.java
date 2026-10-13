package dev.nitro.module.modules;

import dev.nitro.module.Category;
import dev.nitro.module.Module;
import dev.nitro.setting.BooleanSetting;
import dev.nitro.setting.NumberSetting;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/** Scans loaded chunks for clusters of storage blocks, reports them and logs to .minecraft/nitro/stashes.txt */
public class StashFinder extends Module {
    private final NumberSetting minContainers = add(new NumberSetting("Min containers", 10, 3, 100, 1));
    private final BooleanSetting log = add(new BooleanSetting("Log to file", true));
    private final Set<Long> reported = new HashSet<>();
    private int tick;

    public StashFinder() { super("StashFinder", "Finds chunks with lots of chests/shulkers/barrels.", Category.WORLD); }

    @Override
    public void onEnable() { reported.clear(); tick = 0; }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (tick++ % 40 != 0) return;

        int r = mc.options.getViewDistance().getValue();
        int pcx = mc.player.getChunkPos().x, pcz = mc.player.getChunkPos().z;

        for (int cx = pcx - r; cx <= pcx + r; cx++) {
            for (int cz = pcz - r; cz <= pcz + r; cz++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(cx, cz);
                if (chunk == null) continue;

                int count = 0;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (be instanceof ChestBlockEntity || be instanceof ShulkerBoxBlockEntity || be instanceof BarrelBlockEntity) count++;
                }
                if (count < minContainers.get()) continue;
                if (!reported.add(ChunkPos.toLong(cx, cz))) continue;

                String msg = "§b[NITRO]§r Stash: " + count + " containers near X " + (cx * 16 + 8) + " Z " + (cz * 16 + 8);
                mc.player.sendMessage(Text.literal(msg), false);
                if (log.get()) write(count, cx * 16 + 8, cz * 16 + 8);
            }
        }
    }

    private void write(int count, int x, int z) {
        try {
            Path file = FabricLoader.getInstance().getGameDir().resolve("nitro").resolve("stashes.txt");
            Files.createDirectories(file.getParent());
            String server = mc.getCurrentServerEntry() != null ? mc.getCurrentServerEntry().address : "singleplayer";
            String line = LocalDateTime.now() + " | " + server + " | " + mc.world.getRegistryKey().getValue()
                    + " | X " + x + " Z " + z + " | " + count + " containers\n";
            Files.writeString(file, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {}
    }
}

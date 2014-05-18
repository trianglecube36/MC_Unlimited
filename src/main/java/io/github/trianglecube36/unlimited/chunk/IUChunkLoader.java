package io.github.trianglecube36.unlimited.chunk;

import java.io.IOException;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;

public interface IUChunkLoader {
    /**
     * Loads the specified(XZ) chunk into the specified world.
     */
    UChunk32 loadChunk(World world, int x, int y, int z) throws IOException;
    UChunk2D loadChunk2D(World world, int x, int z) throws IOException;

    void saveChunk(World world, UChunk32 chunk) throws MinecraftException, IOException;
    void saveChunk2D(World world, UChunk2D chunk) throws MinecraftException, IOException;

    /**
     * Save extra data associated with this Chunk not normally saved during autosave, only during chunk unload.
     * Currently unused.
     */
    void saveExtraChunkData(World world, UChunk32 chunk);

    /**
     * Called every World.tick()
     */
    void chunkTick();

    /**
     * Save extra data not associated with any Chunk.  Not saved during autosave, only during world unload.  Currently
     * unused.
     */
    void saveExtraData();
}

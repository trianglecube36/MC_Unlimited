package io.github.trianglecube36.unlimited.chunk;

import java.util.List;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;

public interface IUChunkProvider {
    boolean chunkExists(int x, int y, int z);
    boolean chunk2DExists(int x, int z);

    UChunk32 provideChunk(int x, int y, int z);
    UChunk2D provideChunk2D(int x, int z);

    UChunk32 loadChunk(int x, int y, int z);
    UChunk2D loadChunk2D(int x, int z);
    
    /**
     * Populates chunk with ores etc etc
     */
    void populate(IUChunkProvider provider, int x, int y, int z);

    /**
     * Two modes of operation: if passed true, save all Chunks in one go.  If passed false, save up to two chunks.
     * Return true if all chunks have been saved.
     */
    boolean saveChunks(boolean doall, IProgressUpdate progress);

    /**
     * Unloads chunks that are marked to be unloaded. This is not guaranteed to unload every such chunk.
     */
    boolean unloadQueuedChunks();

    /**
     * Returns if the IChunkProvider supports saving.
     */
    boolean canSave();

    /**
     * Converts the instance data to a readable string.
     */
    String makeString();

    /**
     * Returns a list of creatures of the specified type that can spawn at the given location.
     */
    List getPossibleCreatures(EnumCreatureType var1, int x, int y, int z);

    // fined structure?... used for eye of ender
    ChunkPosition func_147416_a(World world, String str, int x, int y, int z);

    int getLoadedChunkCount();
    int getLoadedChunk2DCount();

    void recreateStructures(int x, int y, int z);

    /**
     * Save extra data not associated with any Chunk.  Not saved during autosave, only during world unload.  Currently
     * unimplemented.
     */
    void saveExtraData();
}

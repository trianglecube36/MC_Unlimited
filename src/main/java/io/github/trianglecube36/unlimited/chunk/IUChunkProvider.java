package io.github.trianglecube36.unlimited.chunk;

import java.util.List;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;

public interface IUChunkProvider {
    /**
     * Checks to see if a chunk exists at x, y
     */
    boolean chunkExists(int x, int y, int z);

    /**
     * Will return back a chunk, if it doesn't exist and its not a MP client it will generates all the blocks for the
     * specified chunk from the map seed and chunk seed
     */
    UChunk32 provideChunk(int x, int y, int z);

    /**
     * loads or generates the chunk at the chunk location specified
     */
    UChunk32 loadChunk(int x, int y, int z);

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

    ChunkPosition func_147416_a(World world, String str, int x, int y, int z);

    int getLoadedChunkCount();

    void recreateStructures(int x, int y, int z);

    /**
     * Save extra data not associated with any Chunk.  Not saved during autosave, only during world unload.  Currently
     * unimplemented.
     */
    void saveExtraData();
}

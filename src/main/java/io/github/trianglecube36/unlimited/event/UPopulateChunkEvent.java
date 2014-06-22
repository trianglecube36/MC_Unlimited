package io.github.trianglecube36.unlimited.event;

import io.github.trianglecube36.unlimited.chunk.IUChunkProvider;

import java.util.Random;

import net.minecraft.world.World;

public class UPopulateChunkEvent extends UChunkProviderEvent
{
    public final World world;
    public final Random rand;
    public final int chunkX;
    public final int chunkY;
    public final int chunkZ;
    public final boolean hasVillageGenerated;
    
    public UPopulateChunkEvent(IUChunkProvider chunkProvider, World world, Random rand, int chunkX, int chunkY, int chunkZ, boolean hasVillageGenerated)
    {
        super(chunkProvider);
        this.world = world;
        this.rand = rand;
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;
        this.hasVillageGenerated = hasVillageGenerated;
    }
    
    public static class Pre extends UPopulateChunkEvent
    {
        public Pre(IUChunkProvider chunkProvider, World world, Random rand, int chunkX, int chunkY, int chunkZ, boolean hasVillageGenerated)
        {
            super(chunkProvider, world, rand, chunkX, chunkY, chunkZ, hasVillageGenerated);
        }
    }
    
    public static class Post extends UPopulateChunkEvent
    {
        public Post(IUChunkProvider chunkProvider, World world, Random rand, int chunkX, int chunkY, int chunkZ, boolean hasVillageGenerated)
        {
            super(chunkProvider, world, rand, chunkX, chunkY, chunkZ, hasVillageGenerated);
        }
    }
    
    /**
     * This event is fired when a chunk is populated with a terrain feature.
     * 
     * You can set the result to DENY to prevent the default generation
     * of a terrain feature.
     */
    @HasResult
    public static class Populate extends UPopulateChunkEvent
    {
        /** Use CUSTOM to filter custom event types
         */
        public static enum EventType { DUNGEON, FIRE, GLOWSTONE, ICE, LAKE, LAVA, NETHER_LAVA, ANIMALS, CUSTOM }
        
        public final EventType type;

        public Populate(IUChunkProvider chunkProvider, World world, Random rand, int chunkX, int chunkY, int chunkZ, boolean hasVillageGenerated, EventType type)
        {
            super(chunkProvider, world, rand, chunkX, chunkY, chunkZ, hasVillageGenerated);
            this.type = type;
        }
    }
}
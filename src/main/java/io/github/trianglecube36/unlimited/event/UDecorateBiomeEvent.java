package io.github.trianglecube36.unlimited.event;

import java.util.Random;

import cpw.mods.fml.common.eventhandler.Event;

import net.minecraft.world.World;

public class UDecorateBiomeEvent extends Event
{
    public final World world;
    public final Random rand;
    /**
     * was chunkX - forge miss named it... IT IS BLOCK NOT CHUNK COORDINANTS!
     */
    public final int blockX;
    /**
     * would have been chunkY - forge miss named x and z... IT IS BLOCK NOT CHUNK COORDINANTS!
     */
    public final int blockY;
    /**
     * was chunkZ - forge miss named it... IT IS BLOCK NOT CHUNK COORDINANTS!
     */
    public final int blockZ;
    
    public UDecorateBiomeEvent(World world, Random rand, int worldX, int worldY, int worldZ)
    {
        this.world = world;
        this.rand = rand;
        this.blockX = worldX;
        this.blockY = worldY;
        this.blockZ = worldZ;
    }
    
    public static class Pre extends UDecorateBiomeEvent
    {
        public Pre(World world, Random rand, int worldX, int worldY, int worldZ)
        {
            super(world, rand, worldX, worldY, worldZ);
        }
    }
    
    public static class Post extends UDecorateBiomeEvent
    {
        public Post(World world, Random rand, int worldX, int worldY, int worldZ)
        {
            super(world, rand, worldX, worldY, worldZ);
        }
    }

    /**
     * This event is fired when a chunk is decorated with a biome feature.
     * 
     * You can set the result to DENY to prevent the default biome decoration.
     */
    @HasResult
    public static class Decorate extends UDecorateBiomeEvent
    {
        /** Use CUSTOM to filter custom event types
         */
        public static enum EventType { BIG_SHROOM, CACTUS, CLAY, DEAD_BUSH, LILYPAD, FLOWERS, GRASS, LAKE, PUMPKIN, REED, SAND, SAND_PASS2, SHROOM, TREE, CUSTOM }
        
        public final EventType type;
        
        public Decorate(World world, Random rand, int worldX, int worldY, int worldZ, EventType type)
        {
            super(world, rand, worldX, worldY, worldZ);
            this.type = type;
        }
    }
}
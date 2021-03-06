package io.github.trianglecube36.unlimited.event;

import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import cpw.mods.fml.common.eventhandler.Event;

public class UOreGenEvent extends Event
{
    public final World world;
    public final Random rand;
    public final int worldX;
    public final int worldY;
    public final int worldZ;
    
    public UOreGenEvent(World world, Random rand, int worldX, int worldY, int worldZ)
    {
        this.world = world;
        this.rand = rand;
        this.worldX = worldX;
        this.worldY = worldY;
        this.worldZ = worldZ;
    }
    
    public static class Pre extends UOreGenEvent
    {
        public Pre(World world, Random rand, int worldX, int worldY, int worldZ)
        {
            super(world, rand, worldX, worldY, worldZ);
        }
    }
    
    public static class Post extends UOreGenEvent
    {
        public Post(World world, Random rand, int worldX, int worldY, int worldZ)
        {
            super(world, rand, worldX, worldY, worldZ);
        }
    }
    
    /**
     * This event is fired when an ore is generated in a chunk.
     * 
     * You can set the result to DENY to prevent the default ore generation.
     */
    @HasResult
    public static class GenerateMinable extends UOreGenEvent
    {
        public static enum EventType { COAL, DIAMOND, DIRT, GOLD, GRAVEL, IRON, LAPIS, REDSTONE, QUARTZ, CUSTOM }
        
        public final EventType type;
        public final WorldGenerator generator;
        
        public GenerateMinable(World world, Random rand, WorldGenerator generator, int worldX, int worldY, int worldZ, EventType type)
        {
            super(world, rand, worldX, worldY, worldZ);
            this.generator = generator;
            this.type = type;
        }
    }
}
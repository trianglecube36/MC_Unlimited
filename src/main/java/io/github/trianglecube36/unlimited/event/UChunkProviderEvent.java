package io.github.trianglecube36.unlimited.event;

import io.github.trianglecube36.unlimited.chunk.IUChunkProvider;
import net.minecraft.block.Block;
import net.minecraft.world.biome.BiomeGenBase;
import cpw.mods.fml.common.eventhandler.Event;

public class UChunkProviderEvent extends Event
{

    public final IUChunkProvider chunkProvider;
    
    public UChunkProviderEvent(IUChunkProvider chunkProvider)
    {
        this.chunkProvider = chunkProvider;
    }
    
    /**
     * This event is fired when a chunks blocks are replaced by a biomes top and
     * filler blocks.
     * 
     * You can set the result to DENY to prevent the default replacement.
     */
    @HasResult
    public static class ReplaceBiomeBlocks extends UChunkProviderEvent 
    {
        public final int chunkX;
        public final int chunkY;
        public final int chunkZ;
        public final Block[] blockArray;
        public final byte[] metaArray;
        public final BiomeGenBase[] biomeArray;
        
        public ReplaceBiomeBlocks(IUChunkProvider chunkProvider, int chunkX, int chunkY, int chunkZ, Block[] blockArray, BiomeGenBase[] biomeArray)
        {
            super(chunkProvider);
            this.chunkX = chunkX;
            this.chunkY = chunkY;
            this.chunkZ = chunkZ;
            this.blockArray = blockArray;
            this.biomeArray = biomeArray;
            metaArray = new byte[256];
        }
        
        public ReplaceBiomeBlocks(IUChunkProvider chunkProvider, int chunkX, int chunkY, int chunkZ, Block[] blockArray, byte[] metaArray, BiomeGenBase[] biomeArray)
        {
            super(chunkProvider);
            this.chunkX = chunkX;
            this.chunkY = chunkY;
            this.chunkZ = chunkZ;
            this.blockArray = blockArray;
            this.biomeArray = biomeArray;
            this.metaArray = metaArray;
        }
       
    }
    
    /**
     * This event is fired before a chunks terrain noise field is initialized.
     * 
     * You can set the result to DENY to substitute your own noise field.
     */
    @HasResult
    public static class InitNoiseField extends UChunkProviderEvent 
    {
        public double[] noisefield;
        public final int posX;
        public final int posY;
        public final int posZ;
        public final int sizeX;
        public final int sizeY;
        public final int sizeZ;
        
        public InitNoiseField(IUChunkProvider chunkProvider, double[] noisefield, int posX, int posY, int posZ, int sizeX, int sizeY, int sizeZ)
        {
            super(chunkProvider);
            this.noisefield = noisefield;
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
        }
    }
}
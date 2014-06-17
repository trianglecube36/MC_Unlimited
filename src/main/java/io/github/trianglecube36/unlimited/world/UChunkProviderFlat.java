package io.github.trianglecube36.unlimited.world;

import io.github.trianglecube36.unlimited.chunk.IUChunkProvider;
import io.github.trianglecube36.unlimited.chunk.UChunk2D;
import io.github.trianglecube36.unlimited.chunk.UChunk32;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.FlatGeneratorInfo;
import net.minecraft.world.gen.FlatLayerInfo;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.MapGenVillage;

public class UChunkProviderFlat implements IUChunkProvider
{
    private World worldObj;
    private Random random;
    private final Block[] cachedBlockIDs = new Block[256];
    private final byte[] cachedBlockMetadata = new byte[256];
    private final FlatGeneratorInfo flatWorldGenInfo;
    private final List structureGenerators = new ArrayList();
    private final boolean hasDecoration;
    private final boolean hasDungeons;
    private WorldGenLakes waterLakeGenerator;
    private WorldGenLakes lavaLakeGenerator;

    public UChunkProviderFlat(World par1World, long par2, boolean par4, String par5Str)
    {
        this.worldObj = par1World;
        this.random = new Random(par2);
        this.flatWorldGenInfo = FlatGeneratorInfo.createFlatGeneratorFromString(par5Str);

        if (par4)
        {
            Map map = this.flatWorldGenInfo.getWorldFeatures();

            if (map.containsKey("village"))
            {
                Map map1 = (Map)map.get("village");

                if (!map1.containsKey("size"))
                {
                    map1.put("size", "1");
                }

                this.structureGenerators.add(new MapGenVillage(map1));
            }

            if (map.containsKey("biome_1"))
            {
                this.structureGenerators.add(new MapGenScatteredFeature((Map)map.get("biome_1")));
            }

            if (map.containsKey("mineshaft"))
            {
                this.structureGenerators.add(new MapGenMineshaft((Map)map.get("mineshaft")));
            }

            if (map.containsKey("stronghold"))
            {
                this.structureGenerators.add(new MapGenStronghold((Map)map.get("stronghold")));
            }
        }

        this.hasDecoration = this.flatWorldGenInfo.getWorldFeatures().containsKey("decoration");

        if (this.flatWorldGenInfo.getWorldFeatures().containsKey("lake"))
        {
            this.waterLakeGenerator = new WorldGenLakes(Blocks.water);
        }

        if (this.flatWorldGenInfo.getWorldFeatures().containsKey("lava_lake"))
        {
            this.lavaLakeGenerator = new WorldGenLakes(Blocks.lava);
        }

        this.hasDungeons = this.flatWorldGenInfo.getWorldFeatures().containsKey("dungeon");
        Iterator iterator = this.flatWorldGenInfo.getFlatLayers().iterator();

        while (iterator.hasNext())
        {
            FlatLayerInfo flatlayerinfo = (FlatLayerInfo)iterator.next();

            for (int j = flatlayerinfo.getMinY(); j < flatlayerinfo.getMinY() + flatlayerinfo.getLayerCount(); ++j)
            {
                this.cachedBlockIDs[j] = flatlayerinfo.func_151536_b();
                this.cachedBlockMetadata[j] = (byte)flatlayerinfo.getFillBlockMeta();
            }
        }
    }

    /**
     * loads or generates the chunk at the chunk location specified
     */
    public UChunk32 loadChunk(int x, int y, int z)
    {
        return this.provideChunk(x, y, z);
    }

    /**
     * Will return back a chunk, if it doesn't exist and its not a MP client it will generates all the blocks for the
     * specified chunk from the map seed and chunk seed
     */
    public UChunk32 provideChunk(int x, int y, int z)
    {
        UChunk32 chunk = new UChunk32(this.worldObj, x, y, z);
        int l;

        if(y >= 0 && y < 8){ // 8 << 5 = 256
        	for (int iy = y << 5; iy < this.cachedBlockIDs.length && iy < y << 5 + 32; ++iy)
        	{
        		Block block = this.cachedBlockIDs[iy];
            
        		if (block != null)
        		{
        			l = (iy >> 2) & 4; // get 16th's bit in to 4th's bit pos (get bit 5 in to bit 3)
        			ExtendedBlockStorage[] blockArrays = chunk.getBlockStorageArray();
        			ExtendedBlockStorage blockArray1 = blockArrays[l];
        			ExtendedBlockStorage blockArray2 = blockArrays[l + 1];
        			ExtendedBlockStorage blockArray3 = blockArrays[l + 2];
        			ExtendedBlockStorage blockArray4 = blockArrays[l + 3];

        			if (blockArray1 == null)
        			{
        				blockArray1 = new ExtendedBlockStorage(l, !this.worldObj.provider.hasNoSky);
        				blockArrays[l] = blockArray1;
        			}
        			if (blockArray2 == null)
        			{
        				blockArray2 = new ExtendedBlockStorage(l + 1, !this.worldObj.provider.hasNoSky);
        				blockArrays[l + 1] = blockArray2;
        			}
        			if (blockArray3 == null)
        			{
        				blockArray3 = new ExtendedBlockStorage(l + 2, !this.worldObj.provider.hasNoSky);
        				blockArrays[l + 2] = blockArray3;
        			}
        			if (blockArray4 == null)
        			{
        				blockArray4 = new ExtendedBlockStorage(l + 3, !this.worldObj.provider.hasNoSky);
        				blockArrays[l + 3] = blockArray4;
        			}

        			for (int ix = 0; ix < 16; ++ix)
        			{
        				for (int iz = 0; iz < 16; ++iz)
        				{
        					blockArray1.func_150818_a(ix, iy & 15, iz, block);
        					blockArray1.setExtBlockMetadata(ix, iy & 15, iz, this.cachedBlockMetadata[iy]);
        					blockArray1.func_150818_a(ix, iy & 15, iz, block);
        					blockArray1.setExtBlockMetadata(ix, iy & 15, iz, this.cachedBlockMetadata[iy]);
        					blockArray1.func_150818_a(ix, iy & 15, iz, block);
        					blockArray1.setExtBlockMetadata(ix, iy & 15, iz, this.cachedBlockMetadata[iy]);
        					blockArray1.func_150818_a(ix, iy & 15, iz, block);
        					blockArray1.setExtBlockMetadata(ix, iy & 15, iz, this.cachedBlockMetadata[iy]);
        				}
        			}
        		}
        	}
        }
        
        Iterator iterator = this.structureGenerators.iterator();

        while (iterator.hasNext())
        {
            MapGenStructure mapgenstructure = (MapGenStructure)iterator.next();
            //mapgenstructure.func_151539_a(this, this.worldObj, x, y, z, (Block[])null); //TODO: re-add this
        }

        UChunk2D c2D = worldObj.get2DChunk(x, z);
        worldObj.le.populateLight(chunk, c2D);
        return chunk;
    }
    
    public boolean chunkExists(int x, int y, int z)
    {
        return true;
    }

    /**
     * Populates chunk with ores etc etc
     */
    public void populate(IUChunkProvider par1IChunkProvider, int x, int y, int z)
    {
        int k = x * 16;
        int l = z * 16;
        BiomeGenBase biomegenbase = this.worldObj.getBiomeGenForCoords(k + 16, l + 16);
        boolean flag = false;
        this.random.setSeed(this.worldObj.getSeed());
        long i1 = this.random.nextLong() / 2L * 2L + 1L;
        long j1 = this.random.nextLong() / 2L * 2L + 1L;
        this.random.setSeed((long)x * i1 + (long)z * j1 ^ this.worldObj.getSeed());
        Iterator iterator = this.structureGenerators.iterator();

        while (iterator.hasNext())
        {
            MapGenStructure mapgenstructure = (MapGenStructure)iterator.next();
            boolean flag1 = mapgenstructure.generateStructuresInChunk(this.worldObj, this.random, x, z);

            if (mapgenstructure instanceof MapGenVillage)
            {
                flag |= flag1;
            }
        }

        int l1;
        int i2;
        int j2;

        if (this.waterLakeGenerator != null && !flag && this.random.nextInt(4) == 0)
        {
            l1 = k + this.random.nextInt(16) + 8;
            i2 = this.random.nextInt(256);
            j2 = l + this.random.nextInt(16) + 8;
            this.waterLakeGenerator.generate(this.worldObj, this.random, l1, i2, j2);
        }

        if (this.lavaLakeGenerator != null && !flag && this.random.nextInt(8) == 0)
        {
            l1 = k + this.random.nextInt(16) + 8;
            i2 = this.random.nextInt(this.random.nextInt(248) + 8);
            j2 = l + this.random.nextInt(16) + 8;

            if (i2 < 63 || this.random.nextInt(10) == 0)
            {
                this.lavaLakeGenerator.generate(this.worldObj, this.random, l1, i2, j2);
            }
        }

        if (this.hasDungeons)
        {
            for (l1 = 0; l1 < 8; ++l1)
            {
                i2 = k + this.random.nextInt(16) + 8;
                j2 = this.random.nextInt(256);
                int k1 = l + this.random.nextInt(16) + 8;
                (new WorldGenDungeons()).generate(this.worldObj, this.random, i2, j2, k1);
            }
        }

        if (this.hasDecoration)
        {
            biomegenbase.decorate(this.worldObj, this.random, k, l);
        }
    }

    /**
     * Two modes of operation: if passed true, save all Chunks in one go.  If passed false, save up to two chunks.
     * Return true if all chunks have been saved.
     */
    public boolean saveChunks(boolean par1, IProgressUpdate par2IProgressUpdate)
    {
        return true;
    }

    /**
     * Save extra data not associated with any Chunk.  Not saved during autosave, only during world unload.  Currently
     * unimplemented.
     */
    public void saveExtraData() {}

    /**
     * Unloads chunks that are marked to be unloaded. This is not guaranteed to unload every such chunk.
     */
    public boolean unloadQueuedChunks()
    {
        return false;
    }

    /**
     * Returns if the IChunkProvider supports saving.
     */
    public boolean canSave()
    {
        return true;
    }

    /**
     * Converts the instance data to a readable string.
     */
    public String makeString()
    {
        return "FlatLevelSource";
    }

    /**
     * Returns a list of creatures of the specified type that can spawn at the given location.
     */
    public List getPossibleCreatures(EnumCreatureType par1EnumCreatureType, int par2, int par3, int par4)
    {
        BiomeGenBase biomegenbase = this.worldObj.getBiomeGenForCoords(par2, par4);
        return biomegenbase.getSpawnableList(par1EnumCreatureType);
    }

    public ChunkPosition func_147416_a(World p_147416_1_, String p_147416_2_, int p_147416_3_, int p_147416_4_, int p_147416_5_)
    {
        if ("Stronghold".equals(p_147416_2_))
        {
            Iterator iterator = this.structureGenerators.iterator();

            while (iterator.hasNext())
            {
                MapGenStructure mapgenstructure = (MapGenStructure)iterator.next();

                if (mapgenstructure instanceof MapGenStronghold)
                {
                    return mapgenstructure.func_151545_a(p_147416_1_, p_147416_3_, p_147416_4_, p_147416_5_);
                }
            }
        }

        return null;
    }

    public int getLoadedChunkCount()
    {
        return 0;
    }
    
    public int getLoadedChunk2DCount()
    {
        return 0;
    }

    public void recreateStructures(int x, int y, int z)
    {
        Iterator iterator = this.structureGenerators.iterator();

        while (iterator.hasNext())
        {
            MapGenStructure mapgenstructure = (MapGenStructure)iterator.next();
            //mapgenstructure.func_151539_a(this, this.worldObj, x, z, (Block[])null); //TODO: re-add this
        }
    }

	@Override
	public boolean chunk2DExists(int x, int z) {
		return true;
	}

	private BiomeGenBase[] biomesForGeneration;
	@Override
	public UChunk2D provideChunk2D(int cx, int cz) {
		UChunk2D c2D = new UChunk2D(worldObj, cx, cz);
    	byte[] abyte = c2D.blockBiomeArray;
    	this.biomesForGeneration = this.worldObj.getWorldChunkManager().loadBlockGeneratorData(this.biomesForGeneration, cx << 5, cz << 5, 32, 32);
    	
    	for (int i = 0; i < abyte.length; ++i)
        {
            abyte[i] = (byte)this.biomesForGeneration[i].biomeID;
        }
    	return c2D;
	}

	@Override
	public UChunk2D loadChunk2D(int x, int z) {
		return this.provideChunk2D(x, z);
	}
}
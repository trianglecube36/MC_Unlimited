package io.github.trianglecube36.unlimited.world;

import io.github.trianglecube36.unlimited.UTempGenerator;
import io.github.trianglecube36.unlimited.chunk.IUChunkProvider;
import io.github.trianglecube36.unlimited.chunk.UChunk2D;
import io.github.trianglecube36.unlimited.chunk.UChunk32;
import io.github.trianglecube36.unlimited.event.UChunkProviderEvent;
import io.github.trianglecube36.unlimited.event.UPopulateChunkEvent;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.NoiseGenerator;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraftforge.common.*;
import cpw.mods.fml.common.eventhandler.Event.*;
import net.minecraftforge.event.terraingen.*;

public class UChunkProviderEnd implements IUChunkProvider
{
    private Random endRNG;
    private NoiseGeneratorOctaves noiseGen1;
    private NoiseGeneratorOctaves noiseGen2;
    private NoiseGeneratorOctaves noiseGen3;
    public NoiseGeneratorOctaves noiseGen4;
    public NoiseGeneratorOctaves noiseGen5;
    private World endWorld;
    private double[] densities;
    /** The biomes that are used to generate the chunk */
    private BiomeGenBase[] biomesForGeneration;
    double[] noiseData1;
    double[] noiseData2;
    double[] noiseData3;
    double[] noiseData4;
    double[] noiseData5;
    int[][] field_73203_h = new int[32][32];

    public UChunkProviderEnd(World par1World, long par2)
    {
        this.endWorld = par1World;
        this.endRNG = new Random(par2);
        this.noiseGen1 = new NoiseGeneratorOctaves(this.endRNG, 16);
        this.noiseGen2 = new NoiseGeneratorOctaves(this.endRNG, 16);
        this.noiseGen3 = new NoiseGeneratorOctaves(this.endRNG, 8);
        this.noiseGen4 = new NoiseGeneratorOctaves(this.endRNG, 10);
        this.noiseGen5 = new NoiseGeneratorOctaves(this.endRNG, 16);

        NoiseGenerator[] noiseGens = {noiseGen1, noiseGen2, noiseGen3, noiseGen4, noiseGen5};
        noiseGens = TerrainGen.getModdedNoiseGenerators(par1World, this.endRNG, noiseGens);
        this.noiseGen1 = (NoiseGeneratorOctaves)noiseGens[0];
        this.noiseGen2 = (NoiseGeneratorOctaves)noiseGens[1];
        this.noiseGen3 = (NoiseGeneratorOctaves)noiseGens[2];
        this.noiseGen4 = (NoiseGeneratorOctaves)noiseGens[3];
        this.noiseGen5 = (NoiseGeneratorOctaves)noiseGens[4];
    }

    public void generateTarrain(int cx, int cz, Block[] blockArray, BiomeGenBase[] biomeArray)
    {
        byte b0 = 2;
        int k = b0 + 1;
        byte b1 = 33;
        int l = b0 + 1;
        this.densities = this.initializeNoiseField(this.densities, cx * b0, 0, cz * b0, k, b1, l);

        for (int i1 = 0; i1 < b0; ++i1)
        {
            for (int j1 = 0; j1 < b0; ++j1)
            {
                for (int k1 = 0; k1 < 32; ++k1)
                {
                    double d0 = 0.25D;
                    double d1 = this.densities[((i1 + 0) * l + j1 + 0) * b1 + k1 + 0];
                    double d2 = this.densities[((i1 + 0) * l + j1 + 1) * b1 + k1 + 0];
                    double d3 = this.densities[((i1 + 1) * l + j1 + 0) * b1 + k1 + 0];
                    double d4 = this.densities[((i1 + 1) * l + j1 + 1) * b1 + k1 + 0];
                    double d5 = (this.densities[((i1 + 0) * l + j1 + 0) * b1 + k1 + 1] - d1) * d0;
                    double d6 = (this.densities[((i1 + 0) * l + j1 + 1) * b1 + k1 + 1] - d2) * d0;
                    double d7 = (this.densities[((i1 + 1) * l + j1 + 0) * b1 + k1 + 1] - d3) * d0;
                    double d8 = (this.densities[((i1 + 1) * l + j1 + 1) * b1 + k1 + 1] - d4) * d0;

                    for (int l1 = 0; l1 < 4; ++l1)
                    {
                        double d9 = 0.125D;
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * d9;
                        double d13 = (d4 - d2) * d9;

                        for (int i2 = 0; i2 < 8; ++i2)
                        {
                            int j2 = i2 + i1 * 8 << 11 | 0 + j1 * 8 << 7 | k1 * 4 + l1;
                            short short1 = 128;
                            double d14 = 0.125D;
                            double d15 = d10;
                            double d16 = (d11 - d10) * d14;

                            for (int k2 = 0; k2 < 8; ++k2)
                            {
                                Block block = null;

                                if (d15 > 0.0D)
                                {
                                    block = Blocks.end_stone;
                                }

                                blockArray[j2] = block;
                                j2 += short1;
                                d15 += d16;
                            }

                            d10 += d12;
                            d11 += d13;
                        }

                        d1 += d5;
                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                    }
                }
            }
        }
    }

    public void replaceBlocksForBiome(int cx, int cy, int cz, Block[] blockArray, BiomeGenBase[] biomes)
    {
        UChunkProviderEvent.ReplaceBiomeBlocks event = new UChunkProviderEvent.ReplaceBiomeBlocks(this, cx, cy, cz, blockArray, biomes);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() == Result.DENY) return;

        for (int x = 0; x < 32; ++x)
        {
            for (int z = 0; z < 32; ++z)
            {
                byte b0 = 1;
                int i1 = -1;
                Block block = Blocks.end_stone;
                Block block1 = Blocks.end_stone;

                for (int y = 31; y >= 0; --y)
                {
                    int index = (z << 10) | (x << 5) | y;
                    Block block2 = blockArray[index];

                    if (block2 != null && block2.getMaterial() != Material.air)
                    {
                        if (block2 == Blocks.stone)
                        {
                            if (i1 == -1)
                            {
                                if (b0 <= 0)
                                {
                                    block = null;
                                    block1 = Blocks.end_stone;
                                }

                                i1 = b0;

                                if (y >= 0)
                                {
                                    blockArray[index] = block;
                                }
                                else
                                {
                                    blockArray[index] = block1;
                                }
                            }
                            else if (i1 > 0)
                            {
                                --i1;
                                blockArray[index] = block1;
                            }
                        }
                    }
                    else
                    {
                        i1 = -1;
                    }
                }
            }
        }
    }

    /**
     * loads or generates the chunk at the chunk location specified
     */
    public UChunk32 loadChunk(int cx, int cy, int cz)
    {
        return this.provideChunk(cx, cy, cz);
    }

    /**
     * Will return back a chunk, if it doesn't exist and its not a MP client it will generates all the blocks for the
     * specified chunk from the map seed and chunk seed
     */
    public UChunk32 provideChunk(int cx, int cy, int cz)
    {
        this.endRNG.setSeed((long)cx * 341873128712L + (long)cy * 740231021407L + (long)cz * 132897987541L);
        Block[] ablock = new Block[32768];
        //this.biomesForGeneration = this.endWorld.getWorldChunkManager().loadBlockGeneratorData(this.biomesForGeneration, cx * 16, cz * 16, 16, 16);
        UChunk2D c2D = this.endWorld.get2DChunk(cx, cz);
        c2D.getBiomeGenArray(this.biomesForGeneration);
        //this.generateTarrain(cx, cy, cz, ablock, this.biomesForGeneration); //TODO: re-add
        UTempGenerator.generateTarrain(cx, cy, cz, ablock, this.biomesForGeneration);
        this.replaceBlocksForBiome(cx, cy, cz, ablock, this.biomesForGeneration);
        UChunk32 chunk = new UChunk32(this.endWorld, ablock, cx, cy, cz);
        /*byte[] abyte = chunk.getBiomeArray();

        for (int k = 0; k < abyte.length; ++k)
        {
            abyte[k] = (byte)this.biomesForGeneration[k].biomeID;
        }*/

        endWorld.le.populateLight(chunk, c2D);
        return chunk;
    }
    
    public UChunk2D loadChunk2D(int cx, int cz){
    	return provideChunk2D(cx, cz);
    }
    
    public UChunk2D provideChunk2D(int cx, int cz){
    	UChunk2D c2D = new UChunk2D(endWorld, cx, cz);
    	byte[] abyte = c2D.blockBiomeArray;
    	this.biomesForGeneration = this.endWorld.getWorldChunkManager().loadBlockGeneratorData(this.biomesForGeneration, cx << 5, cz << 5, 32, 32);
    	
    	for (int i = 0; i < abyte.length; ++i)
        {
            abyte[i] = (byte)this.biomesForGeneration[i].biomeID;
        }
    	return c2D;
    }

    /**
     * generates a subset of the level's terrain data. Takes 7 arguments: the [empty] noise array, the position, and the
     * size.
     */
    private double[] initializeNoiseField(double[] par1ArrayOfDouble, int octX, int octY, int octZ, int u3_1, int u33, int u3_2)
    {
        UChunkProviderEvent.InitNoiseField event = new UChunkProviderEvent.InitNoiseField(this, par1ArrayOfDouble, octX, octY, octZ, u3_1, u33, u3_2);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() == Result.DENY) return event.noisefield;

        if (par1ArrayOfDouble == null)
        {
            par1ArrayOfDouble = new double[u3_1 * u33 * u3_2];
        }

        double d0 = 684.412D;
        double d1 = 684.412D;
        this.noiseData4 = this.noiseGen4.generateNoiseOctaves(this.noiseData4, octX, octZ, u3_1, u3_2, 1.121D, 1.121D, 0.5D);
        this.noiseData5 = this.noiseGen5.generateNoiseOctaves(this.noiseData5, octX, octZ, u3_1, u3_2, 200.0D, 200.0D, 0.5D);
        d0 *= 2.0D;
        this.noiseData1 = this.noiseGen3.generateNoiseOctaves(this.noiseData1, octX, octY, octZ, u3_1, u33, u3_2, d0 / 80.0D, d1 / 160.0D, d0 / 80.0D);
        this.noiseData2 = this.noiseGen1.generateNoiseOctaves(this.noiseData2, octX, octY, octZ, u3_1, u33, u3_2, d0, d1, d0);
        this.noiseData3 = this.noiseGen2.generateNoiseOctaves(this.noiseData3, octX, octY, octZ, u3_1, u33, u3_2, d0, d1, d0);
        int k1 = 0;
        int l1 = 0;

        for (int i2 = 0; i2 < u3_1; ++i2)
        {
            for (int j2 = 0; j2 < u3_2; ++j2)
            {
                double d2 = (this.noiseData4[l1] + 256.0D) / 512.0D;

                if (d2 > 1.0D)
                {
                    d2 = 1.0D;
                }

                double d3 = this.noiseData5[l1] / 8000.0D;

                if (d3 < 0.0D)
                {
                    d3 = -d3 * 0.3D;
                }

                d3 = d3 * 3.0D - 2.0D;
                float f = (float)(i2 + octX - 0) / 1.0F;
                float f1 = (float)(j2 + octZ - 0) / 1.0F;
                float f2 = 100.0F - MathHelper.sqrt_float(f * f + f1 * f1) * 8.0F;

                if (f2 > 80.0F)
                {
                    f2 = 80.0F;
                }

                if (f2 < -100.0F)
                {
                    f2 = -100.0F;
                }

                if (d3 > 1.0D)
                {
                    d3 = 1.0D;
                }

                d3 /= 8.0D;
                d3 = 0.0D;

                if (d2 < 0.0D)
                {
                    d2 = 0.0D;
                }

                d2 += 0.5D;
                d3 = d3 * (double)u33 / 16.0D;
                ++l1;
                double d4 = (double)u33 / 2.0D;

                for (int k2 = 0; k2 < u33; ++k2)
                {
                    double d5 = 0.0D;
                    double d6 = ((double)k2 - d4) * 8.0D / d2;

                    if (d6 < 0.0D)
                    {
                        d6 *= -1.0D;
                    }

                    double d7 = this.noiseData2[k1] / 512.0D;
                    double d8 = this.noiseData3[k1] / 512.0D;
                    double d9 = (this.noiseData1[k1] / 10.0D + 1.0D) / 2.0D;

                    if (d9 < 0.0D)
                    {
                        d5 = d7;
                    }
                    else if (d9 > 1.0D)
                    {
                        d5 = d8;
                    }
                    else
                    {
                        d5 = d7 + (d8 - d7) * d9;
                    }

                    d5 -= 8.0D;
                    d5 += (double)f2;
                    byte b0 = 2;
                    double d10;

                    if (k2 > u33 / 2 - b0)
                    {
                        d10 = (double)((float)(k2 - (u33 / 2 - b0)) / 64.0F);

                        if (d10 < 0.0D)
                        {
                            d10 = 0.0D;
                        }

                        if (d10 > 1.0D)
                        {
                            d10 = 1.0D;
                        }

                        d5 = d5 * (1.0D - d10) + -3000.0D * d10;
                    }

                    b0 = 8;

                    if (k2 < b0)
                    {
                        d10 = (double)((float)(b0 - k2) / ((float)b0 - 1.0F));
                        d5 = d5 * (1.0D - d10) + -30.0D * d10;
                    }

                    par1ArrayOfDouble[k1] = d5;
                    ++k1;
                }
            }
        }

        return par1ArrayOfDouble;
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
        BlockFalling.fallInstantly = true;

        MinecraftForge.EVENT_BUS.post(new UPopulateChunkEvent.Pre(par1IChunkProvider, endWorld, endWorld.rand, x, y, z, false));

        int bx = x * 32;
        int by = y * 32;
        int bz = z * 32;
        BiomeGenBase biomegenbase = this.endWorld.getBiomeGenForCoords(bx + 32, bz + 32);
        biomegenbase.decorate(this.endWorld, this.endWorld.rand, bx, by, bz);

        MinecraftForge.EVENT_BUS.post(new UPopulateChunkEvent.Post(par1IChunkProvider, endWorld, endWorld.rand, x, y, z, false));

        BlockFalling.fallInstantly = false;
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
        return "RandomLevelSource";
    }

    /**
     * Returns a list of creatures of the specified type that can spawn at the given location.
     */
    public List getPossibleCreatures(EnumCreatureType par1EnumCreatureType, int par2, int par3, int par4)
    {
        BiomeGenBase biomegenbase = this.endWorld.getBiomeGenForCoords(par2, par4);
        return biomegenbase.getSpawnableList(par1EnumCreatureType);
    }

    public ChunkPosition func_147416_a(World p_147416_1_, String p_147416_2_, int p_147416_3_, int p_147416_4_, int p_147416_5_)
    {
        return null;
    }

    public int getLoadedChunkCount()
    {
        return 0;
    }

    public void recreateStructures(int x, int y, int z) {}

	@Override
	public boolean chunk2DExists(int x, int z) {
		return true;
	}

	@Override
	public int getLoadedChunk2DCount() {
		return 0;
	}
}
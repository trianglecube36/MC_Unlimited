package io.github.trianglecube36.unlimited.world;

import io.github.trianglecube36.unlimited.chunk.IUChunkProvider;
import io.github.trianglecube36.unlimited.chunk.UChunk2D;
import io.github.trianglecube36.unlimited.chunk.UChunk32;
import io.github.trianglecube36.unlimited.event.UChunkProviderEvent;
import io.github.trianglecube36.unlimited.event.UDecorateBiomeEvent;
import io.github.trianglecube36.unlimited.event.UPopulateChunkEvent;
import io.github.trianglecube36.unlimited.event.UTerrainGen;
import static io.github.trianglecube36.unlimited.event.UOreGenEvent.GenerateMinable.EventType.*;
import static io.github.trianglecube36.unlimited.event.UPopulateChunkEvent.Populate.EventType.*;
import static io.github.trianglecube36.unlimited.event.UDecorateBiomeEvent.Decorate.EventType.*;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCavesHell;
import net.minecraft.world.gen.NoiseGenerator;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.feature.WorldGenFire;
import net.minecraft.world.gen.feature.WorldGenFlowers;
import net.minecraft.world.gen.feature.WorldGenGlowStone1;
import net.minecraft.world.gen.feature.WorldGenGlowStone2;
import net.minecraft.world.gen.feature.WorldGenHellLava;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.structure.MapGenNetherBridge;
import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.*;
import net.minecraftforge.common.*;
import cpw.mods.fml.common.eventhandler.Event.*;
import net.minecraftforge.event.terraingen.*;

public class UChunkProviderHell implements IUChunkProvider
{
    private Random hellRNG;
    /** A NoiseGeneratorOctaves used in generating nether terrain */
    private NoiseGeneratorOctaves netherNoiseGen1;
    private NoiseGeneratorOctaves netherNoiseGen2;
    private NoiseGeneratorOctaves netherNoiseGen3;
    /** Determines whether slowsand or gravel can be generated at a location */
    private NoiseGeneratorOctaves slowsandGravelNoiseGen;
    /**
     * Determines whether something other than nettherack can be generated at a location
     */
    private NoiseGeneratorOctaves netherrackExculsivityNoiseGen;
    public NoiseGeneratorOctaves netherNoiseGen6;
    public NoiseGeneratorOctaves netherNoiseGen7;
    /** Is the world that the nether is getting generated. */
    private World worldObj;
    private double[] noiseField;
    public MapGenNetherBridge genNetherBridge = new MapGenNetherBridge();
    /**
     * Holds the noise used to determine whether slowsand can be generated at a location
     */
    private double[] slowsandNoise = new double[256];
    private double[] gravelNoise = new double[256];
    /**
     * Holds the noise used to determine whether something other than netherrack can be generated at a location
     */
    private double[] netherrackExclusivityNoise = new double[256];
    private MapGenBase netherCaveGenerator = new MapGenCavesHell();
    double[] noiseData1;
    double[] noiseData2;
    double[] noiseData3;
    double[] noiseData4;
    double[] noiseData5;

    {
        genNetherBridge = (MapGenNetherBridge) TerrainGen.getModdedMapGen(genNetherBridge, NETHER_BRIDGE);
        netherCaveGenerator = TerrainGen.getModdedMapGen(netherCaveGenerator, NETHER_CAVE);
    }

    public UChunkProviderHell(World par1World, long par2)
    {
        this.worldObj = par1World;
        this.hellRNG = new Random(par2);
        this.netherNoiseGen1 = new NoiseGeneratorOctaves(this.hellRNG, 16);
        this.netherNoiseGen2 = new NoiseGeneratorOctaves(this.hellRNG, 16);
        this.netherNoiseGen3 = new NoiseGeneratorOctaves(this.hellRNG, 8);
        this.slowsandGravelNoiseGen = new NoiseGeneratorOctaves(this.hellRNG, 4);
        this.netherrackExculsivityNoiseGen = new NoiseGeneratorOctaves(this.hellRNG, 4);
        this.netherNoiseGen6 = new NoiseGeneratorOctaves(this.hellRNG, 10);
        this.netherNoiseGen7 = new NoiseGeneratorOctaves(this.hellRNG, 16);

        NoiseGenerator[] noiseGens = {netherNoiseGen1, netherNoiseGen2, netherNoiseGen3, slowsandGravelNoiseGen, netherrackExculsivityNoiseGen, netherNoiseGen6, netherNoiseGen7};
        noiseGens = TerrainGen.getModdedNoiseGenerators(par1World, this.hellRNG, noiseGens);
        this.netherNoiseGen1 = (NoiseGeneratorOctaves)noiseGens[0];
        this.netherNoiseGen2 = (NoiseGeneratorOctaves)noiseGens[1];
        this.netherNoiseGen3 = (NoiseGeneratorOctaves)noiseGens[2];
        this.slowsandGravelNoiseGen = (NoiseGeneratorOctaves)noiseGens[3];
        this.netherrackExculsivityNoiseGen = (NoiseGeneratorOctaves)noiseGens[4];
        this.netherNoiseGen6 = (NoiseGeneratorOctaves)noiseGens[5];
        this.netherNoiseGen7 = (NoiseGeneratorOctaves)noiseGens[6];
    }

    public void generateTarrain(int x, int y, int z, Block[] blockArray)
    {
        byte b0 = 4;
        byte b1 = 32;
        int k = b0 + 1;
        byte b2 = 17;
        int l = b0 + 1;
        this.noiseField = this.initializeNoiseField(this.noiseField, x * b0, 0, z * b0, k, b2, l);

        for (int i1 = 0; i1 < b0; ++i1)
        {
            for (int j1 = 0; j1 < b0; ++j1)
            {
                for (int k1 = 0; k1 < 16; ++k1)
                {
                    double d0 = 0.125D;
                    double d1 = this.noiseField[((i1 + 0) * l + j1 + 0) * b2 + k1 + 0];
                    double d2 = this.noiseField[((i1 + 0) * l + j1 + 1) * b2 + k1 + 0];
                    double d3 = this.noiseField[((i1 + 1) * l + j1 + 0) * b2 + k1 + 0];
                    double d4 = this.noiseField[((i1 + 1) * l + j1 + 1) * b2 + k1 + 0];
                    double d5 = (this.noiseField[((i1 + 0) * l + j1 + 0) * b2 + k1 + 1] - d1) * d0;
                    double d6 = (this.noiseField[((i1 + 0) * l + j1 + 1) * b2 + k1 + 1] - d2) * d0;
                    double d7 = (this.noiseField[((i1 + 1) * l + j1 + 0) * b2 + k1 + 1] - d3) * d0;
                    double d8 = (this.noiseField[((i1 + 1) * l + j1 + 1) * b2 + k1 + 1] - d4) * d0;

                    for (int l1 = 0; l1 < 8; ++l1)
                    {
                        double d9 = 0.25D;
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * d9;
                        double d13 = (d4 - d2) * d9;

                        for (int i2 = 0; i2 < 4; ++i2)
                        {
                            int j2 = i2 + i1 * 4 << 11 | 0 + j1 * 4 << 7 | k1 * 8 + l1;
                            short short1 = 128;
                            double d14 = 0.25D;
                            double d15 = d10;
                            double d16 = (d11 - d10) * d14;

                            for (int k2 = 0; k2 < 4; ++k2)
                            {
                                Block block = null;

                                if (k1 * 8 + l1 < b1)
                                {
                                    block = Blocks.lava;
                                }

                                if (d15 > 0.0D)
                                {
                                    block = Blocks.netherrack;
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

    public void replaceBlocksForBiome(int x, int y, int z, Block[] blockArray)
    {
        UChunkProviderEvent.ReplaceBiomeBlocks event = new UChunkProviderEvent.ReplaceBiomeBlocks(this, x, y, z, blockArray, null);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() == Result.DENY) return;

        byte b0 = 64;
        double d0 = 0.03125D;
        this.slowsandNoise = this.slowsandGravelNoiseGen.generateNoiseOctaves(this.slowsandNoise, x * 16, z * 16, 0, 16, 16, 1, d0, d0, 1.0D);
        this.gravelNoise = this.slowsandGravelNoiseGen.generateNoiseOctaves(this.gravelNoise, x * 16, 109, z * 16, 16, 1, 16, d0, 1.0D, d0);
        this.netherrackExclusivityNoise = this.netherrackExculsivityNoiseGen.generateNoiseOctaves(this.netherrackExclusivityNoise, x * 16, z * 16, 0, 16, 16, 1, d0 * 2.0D, d0 * 2.0D, d0 * 2.0D);

        for (int k = 0; k < 16; ++k)
        {
            for (int l = 0; l < 16; ++l)
            {
                boolean flag = this.slowsandNoise[k + l * 16] + this.hellRNG.nextDouble() * 0.2D > 0.0D;
                boolean flag1 = this.gravelNoise[k + l * 16] + this.hellRNG.nextDouble() * 0.2D > 0.0D;
                int i1 = (int)(this.netherrackExclusivityNoise[k + l * 16] / 3.0D + 3.0D + this.hellRNG.nextDouble() * 0.25D);
                int j1 = -1;
                Block block = Blocks.netherrack;
                Block block1 = Blocks.netherrack;

                for (int k1 = 127; k1 >= 0; --k1)
                {
                    int l1 = (l * 16 + k) * 128 + k1;

                    if (k1 < 127 - this.hellRNG.nextInt(5) && k1 > 0 + this.hellRNG.nextInt(5))
                    {
                        Block block2 = blockArray[l1];

                        if (block2 != null && block2.getMaterial() != Material.air)
                        {
                            if (block2 == Blocks.netherrack)
                            {
                                if (j1 == -1)
                                {
                                    if (i1 <= 0)
                                    {
                                        block = null;
                                        block1 = Blocks.netherrack;
                                    }
                                    else if (k1 >= b0 - 4 && k1 <= b0 + 1)
                                    {
                                        block = Blocks.netherrack;
                                        block1 = Blocks.netherrack;

                                        if (flag1)
                                        {
                                            block = Blocks.gravel;
                                            block1 = Blocks.netherrack;
                                        }

                                        if (flag)
                                        {
                                            block = Blocks.soul_sand;
                                            block1 = Blocks.soul_sand;
                                        }
                                    }

                                    if (k1 < b0 && (block == null || block.getMaterial() == Material.air))
                                    {
                                        block = Blocks.lava;
                                    }

                                    j1 = i1;

                                    if (k1 >= b0 - 1)
                                    {
                                        blockArray[l1] = block;
                                    }
                                    else
                                    {
                                        blockArray[l1] = block1;
                                    }
                                }
                                else if (j1 > 0)
                                {
                                    --j1;
                                    blockArray[l1] = block1;
                                }
                            }
                        }
                        else
                        {
                            j1 = -1;
                        }
                    }
                    else
                    {
                        blockArray[l1] = Blocks.bedrock;
                    }
                }
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
    
    public UChunk2D loadChunk2D(int x, int z)
    {
        return this.provideChunk2D(x, z);
    }
    
    private BiomeGenBase[] biomesForGeneration;
    
    public UChunk2D provideChunk2D(int cx, int cz){
    	UChunk2D c2D = new UChunk2D(worldObj, cx, cz);
    	byte[] abyte = c2D.blockBiomeArray;
    	this.biomesForGeneration = this.worldObj.getWorldChunkManager().loadBlockGeneratorData(this.biomesForGeneration, cx << 5, cz << 5, 32, 32);
    	
    	for (int i = 0; i < abyte.length; ++i)
        {
            abyte[i] = (byte)this.biomesForGeneration[i].biomeID;
        }
    	return c2D;
    }

    /**
     * Will return back a chunk, if it doesn't exist and its not a MP client it will generates all the blocks for the
     * specified chunk from the map seed and chunk seed
     */
    public UChunk32 provideChunk(int cx, int cy, int cz)
    {
        this.hellRNG.setSeed((long)cx * 341873128712L + (long)cy * 740231021407L + (long)cz * 132897987541L);
        Block[] ablock = new Block[32768];
        this.generateTarrain(cx, cy, cz, ablock);
        this.replaceBlocksForBiome(cx, cy, cz, ablock);
        this.netherCaveGenerator.func_151539_a(this, this.worldObj, cx, cy, cz, ablock);
        this.genNetherBridge.func_151539_a(this, this.worldObj, cx, cy, cz, ablock);
        UChunk32 chunk = new UChunk32(this.worldObj, ablock, cx, cy, cz);

        //chunk.resetRelightChecks();
        return chunk;
    }

    /**
     * generates a subset of the level's terrain data. Takes 7 arguments: the [empty] noise array, the position, and the
     * size.
     */
    private double[] initializeNoiseField(double[] par1ArrayOfDouble, int par2, int par3, int par4, int par5, int par6, int par7)
    {
        UChunkProviderEvent.InitNoiseField event = new UChunkProviderEvent.InitNoiseField(this, par1ArrayOfDouble, par2, par3, par4, par5, par6, par7);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() == Result.DENY) return event.noisefield;

        if (par1ArrayOfDouble == null)
        {
            par1ArrayOfDouble = new double[par5 * par6 * par7];
        }

        double d0 = 684.412D;
        double d1 = 2053.236D;
        this.noiseData4 = this.netherNoiseGen6.generateNoiseOctaves(this.noiseData4, par2, par3, par4, par5, 1, par7, 1.0D, 0.0D, 1.0D);
        this.noiseData5 = this.netherNoiseGen7.generateNoiseOctaves(this.noiseData5, par2, par3, par4, par5, 1, par7, 100.0D, 0.0D, 100.0D);
        this.noiseData1 = this.netherNoiseGen3.generateNoiseOctaves(this.noiseData1, par2, par3, par4, par5, par6, par7, d0 / 80.0D, d1 / 60.0D, d0 / 80.0D);
        this.noiseData2 = this.netherNoiseGen1.generateNoiseOctaves(this.noiseData2, par2, par3, par4, par5, par6, par7, d0, d1, d0);
        this.noiseData3 = this.netherNoiseGen2.generateNoiseOctaves(this.noiseData3, par2, par3, par4, par5, par6, par7, d0, d1, d0);
        int k1 = 0;
        int l1 = 0;
        double[] adouble1 = new double[par6];
        int i2;

        for (i2 = 0; i2 < par6; ++i2)
        {
            adouble1[i2] = Math.cos((double)i2 * Math.PI * 6.0D / (double)par6) * 2.0D;
            double d2 = (double)i2;

            if (i2 > par6 / 2)
            {
                d2 = (double)(par6 - 1 - i2);
            }

            if (d2 < 4.0D)
            {
                d2 = 4.0D - d2;
                adouble1[i2] -= d2 * d2 * d2 * 10.0D;
            }
        }

        for (i2 = 0; i2 < par5; ++i2)
        {
            for (int k2 = 0; k2 < par7; ++k2)
            {
                double d3 = (this.noiseData4[l1] + 256.0D) / 512.0D;

                if (d3 > 1.0D)
                {
                    d3 = 1.0D;
                }

                double d4 = 0.0D;
                double d5 = this.noiseData5[l1] / 8000.0D;

                if (d5 < 0.0D)
                {
                    d5 = -d5;
                }

                d5 = d5 * 3.0D - 3.0D;

                if (d5 < 0.0D)
                {
                    d5 /= 2.0D;

                    if (d5 < -1.0D)
                    {
                        d5 = -1.0D;
                    }

                    d5 /= 1.4D;
                    d5 /= 2.0D;
                    d3 = 0.0D;
                }
                else
                {
                    if (d5 > 1.0D)
                    {
                        d5 = 1.0D;
                    }

                    d5 /= 6.0D;
                }

                d3 += 0.5D;
                d5 = d5 * (double)par6 / 16.0D;
                ++l1;

                for (int j2 = 0; j2 < par6; ++j2)
                {
                    double d6 = 0.0D;
                    double d7 = adouble1[j2];
                    double d8 = this.noiseData2[k1] / 512.0D;
                    double d9 = this.noiseData3[k1] / 512.0D;
                    double d10 = (this.noiseData1[k1] / 10.0D + 1.0D) / 2.0D;

                    if (d10 < 0.0D)
                    {
                        d6 = d8;
                    }
                    else if (d10 > 1.0D)
                    {
                        d6 = d9;
                    }
                    else
                    {
                        d6 = d8 + (d9 - d8) * d10;
                    }

                    d6 -= d7;
                    double d11;

                    if (j2 > par6 - 4)
                    {
                        d11 = (double)((float)(j2 - (par6 - 4)) / 3.0F);
                        d6 = d6 * (1.0D - d11) + -10.0D * d11;
                    }

                    if ((double)j2 < d4)
                    {
                        d11 = (d4 - (double)j2) / 4.0D;

                        if (d11 < 0.0D)
                        {
                            d11 = 0.0D;
                        }

                        if (d11 > 1.0D)
                        {
                            d11 = 1.0D;
                        }

                        d6 = d6 * (1.0D - d11) + -10.0D * d11;
                    }

                    par1ArrayOfDouble[k1] = d6;
                    ++k1;
                }
            }
        }

        return par1ArrayOfDouble;
    }

    /**
     * Checks to see if a chunk exists at x, y
     */
    public boolean chunkExists(int x, int y, int z)
    {
        return true;
    }
    
    public boolean chunk2DExists(int x, int z)
    {
        return true;
    }

    /**
     * Populates chunk with ores etc etc
     */
    public void populate(IUChunkProvider par1IChunkProvider, int cx, int cy, int cz)
    {
        BlockFalling.fallInstantly = true;

        MinecraftForge.EVENT_BUS.post(new UPopulateChunkEvent.Pre(par1IChunkProvider, worldObj, hellRNG, cx, cy, cz, false));

        int bx = cx * 32;
        int by = cy * 32;
        int bz = cz * 32;
        this.genNetherBridge.generateStructuresInChunk(this.worldObj, this.hellRNG, cx, cy, cz);
        int i1;
        int j1;
        int k1;
        int l1;

        boolean doGen = UTerrainGen.populate(par1IChunkProvider, worldObj, hellRNG, cx, cy, cz, false, NETHER_LAVA);
        for (i1 = 0; doGen && i1 < 8; ++i1)
        {
            j1 = bx + this.hellRNG.nextInt(32) + 16;
            k1 = by + this.hellRNG.nextInt(32) + 16;
            l1 = bz + this.hellRNG.nextInt(32) + 16;
            (new WorldGenHellLava(Blocks.flowing_lava, false)).generate(this.worldObj, this.hellRNG, j1, k1, l1);
        }

        i1 = this.hellRNG.nextInt(this.hellRNG.nextInt(10) + 1) + 1; //was i1 = this.hellRNG.nextInt(this.hellRNG.nextInt(10) + 1) + 1;
        int i2;

        doGen = UTerrainGen.populate(par1IChunkProvider, worldObj, hellRNG, cx, cy, cz, false, FIRE);
        for (j1 = 0; doGen && j1 < i1; ++j1)
        {
            k1 = bx + this.hellRNG.nextInt(32) + 16;
            l1 = by + this.hellRNG.nextInt(32) + 16;
            i2 = bz + this.hellRNG.nextInt(32) + 16;
            (new WorldGenFire()).generate(this.worldObj, this.hellRNG, k1, l1, i2);
        }

        i1 = this.hellRNG.nextInt(this.hellRNG.nextInt(10) + 1);
        
        doGen = UTerrainGen.populate(par1IChunkProvider, worldObj, hellRNG, cx, cy, cz, false, GLOWSTONE);
        for (j1 = 0; doGen && j1 < i1; ++j1)
        {
            k1 = bx + this.hellRNG.nextInt(32) + 16;
            l1 = by + this.hellRNG.nextInt(32) + 16;
            i2 = bz + this.hellRNG.nextInt(32) + 16;
            (new WorldGenGlowStone1()).generate(this.worldObj, this.hellRNG, k1, l1, i2);
        }

        for (j1 = 0; doGen && j1 < 10; ++j1)
        {
            k1 = bx + this.hellRNG.nextInt(32) + 16;
            l1 = by + this.hellRNG.nextInt(32) + 16;
            i2 = bz + this.hellRNG.nextInt(32) + 16;
            (new WorldGenGlowStone2()).generate(this.worldObj, this.hellRNG, k1, l1, i2);
        }

        MinecraftForge.EVENT_BUS.post(new UDecorateBiomeEvent.Pre(worldObj, hellRNG, bx, by, bz));

        doGen = UTerrainGen.decorate(worldObj, hellRNG, bx, by, bz, SHROOM);
        if (doGen && this.hellRNG.nextBoolean()) //was doGen && this.hellRNG.nextInt(1) == 0
        {
            j1 = bx + this.hellRNG.nextInt(32) + 16;
            k1 = by + this.hellRNG.nextInt(32) + 16;
            l1 = bz + this.hellRNG.nextInt(32) + 16;
            (new WorldGenFlowers(Blocks.brown_mushroom)).generate(this.worldObj, this.hellRNG, j1, k1, l1);
        }

        if (doGen && this.hellRNG.nextBoolean()) //was doGen && this.hellRNG.nextInt(1) == 0
        {
            j1 = bx + this.hellRNG.nextInt(32) + 16;
            k1 = by + this.hellRNG.nextInt(32) + 16;
            l1 = bz + this.hellRNG.nextInt(32) + 16;
            (new WorldGenFlowers(Blocks.red_mushroom)).generate(this.worldObj, this.hellRNG, j1, k1, l1);
        }

        WorldGenMinable worldgenminable = new WorldGenMinable(Blocks.quartz_ore, 13, Blocks.netherrack);
        int j2;

        doGen = UTerrainGen.generateOre(worldObj, hellRNG, worldgenminable, bx, by, bz, QUARTZ);
        for (k1 = 0; doGen && k1 < 16; ++k1)
        {
            l1 = bx + this.hellRNG.nextInt(32) + 8;
            i2 = by + this.hellRNG.nextInt(32) + 16;
            j2 = bz + this.hellRNG.nextInt(32) + 8;
            worldgenminable.generate(this.worldObj, this.hellRNG, l1, i2, j2); //mojang? y you do dis? NOTE: +8 OFFSET ON X AND Z IS APPLYED AT WorldGenMinable
        }

        /*for (k1 = 0; k1 < 16; ++k1)
        {
            l1 = bx + this.hellRNG.nextInt(16);
            i2 = this.hellRNG.nextInt(108) + 10;
            j2 = bz + this.hellRNG.nextInt(16);
            (new WorldGenHellLava(Blocks.flowing_lava, true)).generate(this.worldObj, this.hellRNG, l1, i2, j2);
        }*/ //TODO: check this!

        MinecraftForge.EVENT_BUS.post(new UDecorateBiomeEvent.Post(worldObj, hellRNG, bx, by, bz));
        MinecraftForge.EVENT_BUS.post(new UPopulateChunkEvent.Post(par1IChunkProvider, worldObj, hellRNG, cx, cy, cz, false));

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
        return "HellRandomLevelSource";
    }

    /**
     * Returns a list of creatures of the specified type that can spawn at the given location.
     */
    public List getPossibleCreatures(EnumCreatureType mobtype, int par2, int par3, int par4)
    {
        if (mobtype == EnumCreatureType.monster)
        {
            if (this.genNetherBridge.hasStructureAt(par2, par3, par4))
            {
                return this.genNetherBridge.getSpawnList();
            }

            if (this.genNetherBridge.func_142038_b(par2, par3, par4) && this.worldObj.getBlock(par2, par3 - 1, par4) == Blocks.nether_brick)
            {
                return this.genNetherBridge.getSpawnList();
            }
        }

        BiomeGenBase biomegenbase = this.worldObj.getBiomeGenForCoords(par2, par4);
        return biomegenbase.getSpawnableList(mobtype);
    }

    public ChunkPosition func_147416_a(World p_147416_1_, String p_147416_2_, int p_147416_3_, int p_147416_4_, int p_147416_5_)
    {
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
        this.genNetherBridge.func_151539_a(this, this.worldObj, x, y, z, (Block[])null);
    }
}
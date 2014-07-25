package io.github.trianglecube36.unlimited.world;

import io.github.trianglecube36.unlimited.chunk.IUChunkProvider;
import io.github.trianglecube36.unlimited.chunk.UChunk2D;
import io.github.trianglecube36.unlimited.chunk.UChunk32;
import io.github.trianglecube36.unlimited.event.UChunkProviderEvent;
import io.github.trianglecube36.unlimited.event.UPopulateChunkEvent;
import io.github.trianglecube36.unlimited.event.UTerrainGen;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraft.world.gen.MapGenRavine;
import net.minecraft.world.gen.NoiseGenerator;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;
//import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.*;
import static io.github.trianglecube36.unlimited.event.UPopulateChunkEvent.Populate.EventType.*;
import net.minecraftforge.common.*;
import cpw.mods.fml.common.eventhandler.Event.*;
import net.minecraftforge.event.terraingen.*;

public class UChunkProviderGenerate implements IUChunkProvider
{
    /** RNG. */
    private Random rand;
    private NoiseGeneratorOctaves field_147431_j;
    private NoiseGeneratorOctaves field_147432_k;
    private NoiseGeneratorOctaves field_147429_l;
    private NoiseGeneratorPerlin field_147430_m;
    /** A NoiseGeneratorOctaves used in generating terrain */
    public NoiseGeneratorOctaves noiseGen5;
    /** A NoiseGeneratorOctaves used in generating terrain */
    public NoiseGeneratorOctaves noiseGen6;
    public NoiseGeneratorOctaves mobSpawnerNoise;
    /** Reference to the World object. */
    private World worldObj;
    /** are map structures going to be generated (e.g. strongholds) */
    private final boolean mapFeaturesEnabled;
    private WorldType field_147435_p;
    private final double[] field_147434_q;
    private final float[] parabolicField;
    private double[] stoneNoise = new double[256];
    private MapGenBase caveGenerator = new MapGenCaves();
    /** Holds Stronghold Generator */
    private MapGenStronghold strongholdGenerator = new MapGenStronghold();
    /** Holds Village Generator */
    private MapGenVillage villageGenerator = new MapGenVillage();
    /** Holds Mineshaft Generator */
    private MapGenMineshaft mineshaftGenerator = new MapGenMineshaft();
    private MapGenScatteredFeature scatteredFeatureGenerator = new MapGenScatteredFeature();
    /** Holds ravine generator */
    private MapGenBase ravineGenerator = new MapGenRavine();
    /** The biomes that are used to generate the chunk */
    private BiomeGenBase[] biomesForGeneration;
    double[] field_147427_d;
    double[] field_147428_e;
    double[] field_147425_f;
    double[] field_147426_g;
    int[][] field_73219_j = new int[32][32];

    {
        caveGenerator = TerrainGen.getModdedMapGen(caveGenerator, CAVE);
        strongholdGenerator = (MapGenStronghold) TerrainGen.getModdedMapGen(strongholdGenerator, STRONGHOLD);
        villageGenerator = (MapGenVillage) TerrainGen.getModdedMapGen(villageGenerator, VILLAGE);
        mineshaftGenerator = (MapGenMineshaft) TerrainGen.getModdedMapGen(mineshaftGenerator, MINESHAFT);
        scatteredFeatureGenerator = (MapGenScatteredFeature) TerrainGen.getModdedMapGen(scatteredFeatureGenerator, SCATTERED_FEATURE);
        ravineGenerator = TerrainGen.getModdedMapGen(ravineGenerator, RAVINE);
    }    

    public UChunkProviderGenerate(World par1World, long par2, boolean par4)
    {
        this.worldObj = par1World;
        this.mapFeaturesEnabled = par4;
        this.field_147435_p = par1World.getWorldInfo().getTerrainType();
        this.rand = new Random(par2);
        this.field_147431_j = new NoiseGeneratorOctaves(this.rand, 16);
        this.field_147432_k = new NoiseGeneratorOctaves(this.rand, 16);
        this.field_147429_l = new NoiseGeneratorOctaves(this.rand, 8);
        this.field_147430_m = new NoiseGeneratorPerlin(this.rand, 4);
        this.noiseGen5 = new NoiseGeneratorOctaves(this.rand, 10);
        this.noiseGen6 = new NoiseGeneratorOctaves(this.rand, 16);
        this.mobSpawnerNoise = new NoiseGeneratorOctaves(this.rand, 8);
        this.field_147434_q = new double[825];
        this.parabolicField = new float[25];

        for (int j = -2; j <= 2; ++j)
        {
            for (int k = -2; k <= 2; ++k)
            {
                float f = 10.0F / MathHelper.sqrt_float((float)(j * j + k * k) + 0.2F);
                this.parabolicField[j + 2 + (k + 2) * 5] = f;
            }
        }

        NoiseGenerator[] noiseGens = {field_147431_j, field_147432_k, field_147429_l, field_147430_m, noiseGen5, noiseGen6, mobSpawnerNoise};
        noiseGens = TerrainGen.getModdedNoiseGenerators(par1World, this.rand, noiseGens);
        this.field_147431_j = (NoiseGeneratorOctaves)noiseGens[0];
        this.field_147432_k = (NoiseGeneratorOctaves)noiseGens[1];
        this.field_147429_l = (NoiseGeneratorOctaves)noiseGens[2];
        this.field_147430_m = (NoiseGeneratorPerlin)noiseGens[3];
        this.noiseGen5 = (NoiseGeneratorOctaves)noiseGens[4];
        this.noiseGen6 = (NoiseGeneratorOctaves)noiseGens[5];
        this.mobSpawnerNoise = (NoiseGeneratorOctaves)noiseGens[6];
    }

    public void generateTarrain(int chunkX, int chunkY, int chunkZ, Block[] blocks)
    {
    	byte airHieght = 63;
        this.biomesForGeneration = this.worldObj.getWorldChunkManager().getBiomesForGeneration(this.biomesForGeneration, chunkX * 8 - 4, chunkZ * 8 - 4, 10, 10); // was 1/2 of now
        this.likeInitializeNoiseField(chunkX * 8, chunkY * 8, chunkZ * 8); // was 1/2 of now

        for (int xI4 = 0; xI4 < 8; ++xI4) // was 4
        {
            int nMinX_index = xI4 * 5;
            int nMaxX_index = (xI4 + 1) * 5;

            for (int zI4 = 0; zI4 < 8; ++zI4) // was 4
            {
                int nMinXMinZ_index = (nMinX_index + zI4) * 5; // was 33
                int nMinXMaxZ_index = (nMinX_index + zI4 + 1) * 5; // was 33
                int nMaxXMinZ_index = (nMaxX_index + zI4) * 5; // was 33
                int nMaxXMaxZ_index = (nMaxX_index + zI4 + 1) * 5; // was 33

                for (int yI8 = 0; yI8 < 4; ++yI8)// was 32
                {
                    double d0 = 0.125D;
                    double nMinXMinZ = this.field_147434_q[nMinXMinZ_index + yI8];
                    double nMinXMaxZ = this.field_147434_q[nMinXMaxZ_index + yI8];
                    double nMaxXMinZ = this.field_147434_q[nMaxXMinZ_index + yI8];
                    double nMaxXMaxZ = this.field_147434_q[nMaxXMaxZ_index + yI8];
                    double nDifMinXMinZ = (this.field_147434_q[nMinXMinZ_index + yI8 + 1] - nMinXMinZ) * d0;
                    double nDifMinXMaxZ = (this.field_147434_q[nMinXMaxZ_index + yI8 + 1] - nMinXMaxZ) * d0;
                    double nDifMaxXMinZ = (this.field_147434_q[nMaxXMinZ_index + yI8 + 1] - nMaxXMinZ) * d0;
                    double nDifMaxXMaxZ = (this.field_147434_q[nMaxXMaxZ_index + yI8 + 1] - nMaxXMaxZ) * d0;

                    for (int yI1 = 0; yI1 < 8; ++yI1)
                    {
                        double d9 = 0.25D;
                        double nMinXMinZ_copy = nMinXMinZ;
                        double nMinXMaxZ_copy = nMinXMaxZ;
                        double nXDifMinZ = (nMaxXMinZ - nMinXMinZ) * d9;
                        double nXDifMaxZ = (nMaxXMaxZ - nMinXMaxZ) * d9;

                        for (int xI1 = 0; xI1 < 4; ++xI1)
                        {
                            int blockIndex = xI1 + xI4 * 4 << 10 | 0 + zI4 * 4 << 5 | yI8 * 8 + yI1;
                            short zIncValue = 32; // was 256
                            blockIndex -= zIncValue;
                            double d14 = 0.25D;
                            double d16 = (nMinXMaxZ_copy - nMinXMinZ_copy) * d14;
                            double d15 = nMinXMinZ_copy - d16;

                            for (int zI1 = 0; zI1 < 4; ++zI1)
                            {
                                if ((d15 += d16) > 0.0D)
                                {
                                    blocks[blockIndex += zIncValue] = Blocks.stone;
                                }
                                else if (yI8 * 8 + yI1 < airHieght)
                                {
                                    blocks[blockIndex += zIncValue] = Blocks.water;
                                }
                                else
                                {
                                    blocks[blockIndex += zIncValue] = null;
                                }
                            }

                            nMinXMinZ_copy += nXDifMinZ;
                            nMinXMaxZ_copy += nXDifMaxZ;
                        }

                        nMinXMinZ += nDifMinXMinZ;
                        nMinXMaxZ += nDifMinXMaxZ;
                        nMaxXMinZ += nDifMaxXMinZ;
                        nMaxXMaxZ += nDifMaxXMaxZ;
                    }
                }
            }
        }
    }

    public void replaceBlocksForBiome(int x, int y, int z, Block[] blocks, byte[] datas, BiomeGenBase[] biomes)
    {
        UChunkProviderEvent.ReplaceBiomeBlocks event = new UChunkProviderEvent.ReplaceBiomeBlocks(this, x, y, z, blocks, datas, biomes);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() == Result.DENY) return;

        double d0 = 0.03125D;
        this.stoneNoise = this.field_147430_m.func_151599_a(this.stoneNoise, (double)(x * 32), (double)(z * 32), 32, 32, d0 * 2.0D, d0 * 2.0D, 1.0D);
        // was this.stoneNoise = this.field_147430_m.func_151599_a(this.stoneNoise, (double)(x * 16), (double)(z * 16), 16, 16, d0 * 2.0D, d0 * 2.0D, 1.0D);

        for (int ix = 0; ix < 32; ++ix) // was 16
        {
            for (int iz = 0; iz < 32; ++iz) // was 16
            {
                BiomeGenBase biomegenbase = biomes[iz + ix * 32]; // was 16
                biomegenbase.genTerrainBlocks(this.worldObj, this.rand, blocks, datas, x * 32 + ix, z * 32 + iz, this.stoneNoise[iz + ix * 32]);
                // was biomegenbase.genTerrainBlocks(this.worldObj, this.rand, blocks, datas, x * 16 + ix, z * 16 + iz, this.stoneNoise[iz + ix * 16]);
            }
        }
    }

    public UChunk2D loadChunk2D(int x, int z)
    {
        return this.provideChunk2D(x, z);
    }
    
    /**
     * loads or generates the chunk at the chunk location specified
     */
    public UChunk32 loadChunk(int x, int y, int z)
    {
        return this.provideChunk(x, y, z);
    }
    
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
    public UChunk32 provideChunk(int x, int y, int z)
    {
        this.rand.setSeed((long)x * 341873128712L + (long)y * 740231021407L + (long)z * 132897987541L);
        Block[] ablock = new Block[65536];
        byte[] abyte = new byte[65536];
        this.generateTarrain(x, y, z, ablock);
        UChunk2D c2D = this.worldObj.get2DChunk(x, z);
        this.biomesForGeneration = c2D.getBiomeGenArray(biomesForGeneration);
        this.replaceBlocksForBiome(x, y, z, ablock, abyte, this.biomesForGeneration);
        this.caveGenerator.func_151539_a(this, this.worldObj, x, y, z, ablock);
        this.ravineGenerator.func_151539_a(this, this.worldObj, x, y, z, ablock);

        if (this.mapFeaturesEnabled)
        {
            this.mineshaftGenerator.func_151539_a(this, this.worldObj, x, y, z, ablock);
            this.villageGenerator.func_151539_a(this, this.worldObj, x, y, z, ablock);
            this.strongholdGenerator.func_151539_a(this, this.worldObj, x, y, z, ablock);
            this.scatteredFeatureGenerator.func_151539_a(this, this.worldObj, x, y, z, ablock);
        }

        UChunk32 chunk = new UChunk32(this.worldObj, ablock, abyte, x, y, z);

        //chunk.generateSkylightMap();
        return chunk;
    }

    private void likeInitializeNoiseField(int x4, int y4, int z4)
    {
        this.field_147426_g = this.noiseGen6.generateNoiseOctaves(this.field_147426_g, x4, z4, 10, 10, 200.0D, 200.0D, 0.5D);
        this.field_147427_d = this.field_147429_l.generateNoiseOctaves(this.field_147427_d, x4, y4, z4, 10, 5, 10, 8.555150000000001D, 4.277575000000001D, 8.555150000000001D);
        this.field_147428_e = this.field_147431_j.generateNoiseOctaves(this.field_147428_e, x4, y4, z4, 10, 5, 10, 684.412D, 684.412D, 684.412D);
        this.field_147425_f = this.field_147432_k.generateNoiseOctaves(this.field_147425_f, x4, y4, z4, 10, 5, 10, 684.412D, 684.412D, 684.412D);
        int l = 0;
        int i1 = 0;

        for (int j1 = 0; j1 < 10; ++j1)
        {
            for (int k1 = 0; k1 < 10; ++k1)
            {
                float f = 0.0F;
                float f1 = 0.0F;
                float f2 = 0.0F;
                byte b0 = 2;
                BiomeGenBase biomegenbase = this.biomesForGeneration[j1 + 2 + (k1 + 2) * 20]; // was 10

                for (int l1 = -b0; l1 <= b0; ++l1)
                {
                    for (int i2 = -b0; i2 <= b0; ++i2)
                    {
                        BiomeGenBase biomegenbase1 = this.biomesForGeneration[j1 + l1 + 2 + (k1 + i2 + 2) * 20]; // was 10
                        float f3 = biomegenbase1.rootHeight;
                        float f4 = biomegenbase1.heightVariation;

                        if (this.field_147435_p == WorldType.AMPLIFIED && f3 > 0.0F)
                        {
                            f3 = 1.0F + f3 * 2.0F;
                            f4 = 1.0F + f4 * 4.0F;
                        }

                        float f5 = this.parabolicField[l1 + 2 + (i2 + 2) * 5] / (f3 + 2.0F);

                        if (biomegenbase1.rootHeight > biomegenbase.rootHeight)
                        {
                            f5 /= 2.0F;
                        }

                        f += f4 * f5;
                        f1 += f3 * f5;
                        f2 += f5;
                    }
                }

                f /= f2;
                f1 /= f2;
                f = f * 0.9F + 0.1F;
                f1 = (f1 * 4.0F - 1.0F) / 8.0F;
                double d12 = this.field_147426_g[i1] / 8000.0D;

                if (d12 < 0.0D)
                {
                    d12 = -d12 * 0.3D;
                }

                d12 = d12 * 3.0D - 2.0D;

                if (d12 < 0.0D)
                {
                    d12 /= 2.0D;

                    if (d12 < -1.0D)
                    {
                        d12 = -1.0D;
                    }

                    d12 /= 1.4D;
                    d12 /= 2.0D;
                }
                else
                {
                    if (d12 > 1.0D)
                    {
                        d12 = 1.0D;
                    }

                    d12 /= 8.0D;
                }

                ++i1;
                double d13 = (double)f1;
                double d14 = (double)f;
                d13 += d12 * 0.2D;
                d13 = d13 * 8.5D / 8.0D;
                double d5 = 8.5D + d13 * 4.0D;

                for (int j2 = 0; j2 < 5; ++j2)
                {
                    double d6 = ((double)j2 - d5) * 12.0D * 128.0D / 256.0D / d14;

                    if (d6 < 0.0D)
                    {
                        d6 *= 4.0D;
                    }

                    double d7 = this.field_147428_e[l] / 512.0D;
                    double d8 = this.field_147425_f[l] / 512.0D;
                    double d9 = (this.field_147427_d[l] / 10.0D + 1.0D) / 2.0D;
                    double d10 = MathHelper.denormalizeClamp(d7, d8, d9) - d6;

                    if (j2 > 29)
                    {
                        double d11 = (double)((float)(j2 - 29) / 3.0F);
                        d10 = d10 * (1.0D - d11) + -10.0D * d11;
                    }

                    this.field_147434_q[l] = d10;
                    ++l;
                }
            }
        }
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
    public void populate(IUChunkProvider par1IChunkProvider, int x, int y, int z)
    {
        BlockFalling.fallInstantly = true;
        int blockX = x * 32;
        int blockY = y * 32;
        int blockZ = z * 32;
        BiomeGenBase biomegenbase = this.worldObj.getBiomeGenForCoords(blockX + 32, blockZ + 32); // was 16 , 16 ... center of populate area
        this.rand.setSeed(this.worldObj.getSeed());
        long randX = this.rand.nextLong() / 2L * 2L + 1L;
        long randY = this.rand.nextLong() / 2L * 2L + 1L;
        long randZ = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed((long)x * randX + (long)z * randZ ^ this.worldObj.getSeed());
        boolean flag = false;

        MinecraftForge.EVENT_BUS.post(new UPopulateChunkEvent.Pre(par1IChunkProvider, worldObj, rand, x, y, z, flag));

        if (this.mapFeaturesEnabled)
        {
            this.mineshaftGenerator       .generateStructuresInChunk(this.worldObj, this.rand, x, y, z);
            flag = this.villageGenerator  .generateStructuresInChunk(this.worldObj, this.rand, x, y, z);
            this.strongholdGenerator      .generateStructuresInChunk(this.worldObj, this.rand, x, y, z);
            this.scatteredFeatureGenerator.generateStructuresInChunk(this.worldObj, this.rand, x, y, z);
        }

        int gx;
        int gy;
        int gz;

        if (biomegenbase != BiomeGenBase.desert && biomegenbase != BiomeGenBase.desertHills && !flag && this.rand.nextInt(4) == 0 //TODO: new value
            && UTerrainGen.populate(par1IChunkProvider, worldObj, rand, x, y, z, flag, LAKE))
        {
            gx = blockX + this.rand.nextInt(32) + 16;
            gy = blockY + this.rand.nextInt(32) + 16;
            gz = blockZ + this.rand.nextInt(32) + 16;
            (new WorldGenLakes(Blocks.water)).generate(this.worldObj, this.rand, gx, gy, gz);
        }

        if (UTerrainGen.populate(par1IChunkProvider, worldObj, rand, x, y, z, flag, LAVA) && !flag && this.rand.nextInt(8) == 0) //TODO: new value
        {
        	gx = blockX + this.rand.nextInt(32) + 16;
            gy = blockY + this.rand.nextInt(32) + 16;
            gz = blockZ + this.rand.nextInt(32) + 16;

            if (gy < 63 || this.rand.nextInt(10) == 0)
            {
                (new WorldGenLakes(Blocks.lava)).generate(this.worldObj, this.rand, gx, gy, gz);
            }
        }

        boolean doGen = UTerrainGen.populate(par1IChunkProvider, worldObj, rand, x, y, z, flag, DUNGEON);
        for (gx = 0; doGen && gx < 8; ++gx) //TODO: new value
        {
        	gx = blockX + this.rand.nextInt(32) + 16;
            gy = blockY + this.rand.nextInt(32) + 16;
            gz = blockZ + this.rand.nextInt(32) + 16;
            (new WorldGenDungeons()).generate(this.worldObj, this.rand, gx, gy, gz);
        }

        biomegenbase.decorate(this.worldObj, this.rand, blockX, blockY, blockZ);
        //TODO: create 2D gen system
        /*if (UTerrainGen.populate(par1IChunkProvider, worldObj, rand, x, y, z, flag, ANIMALS))
        {
        	SpawnerAnimals.performWorldGenSpawning(this.worldObj, biomegenbase, blockX + 8, blockZ + 8, 16, 16, this.rand);
        }*/
        blockX += 16;
        blockY += 16;
        blockZ += 16;

        //TODO: create 2D gen system
        /*doGen = TerrainGen.populate(par1IChunkProvider, worldObj, rand, x, y, z, flag, ICE);
        for (gx = 0; doGen && gx < 16; ++gx)
        {
            for (gz = 0; gz < 16; ++gy)
            {
                gy = this.worldObj.getPrecipitationHeight(blockX + gx, blockZ + gz);

                if (this.worldObj.isBlockFreezable(gx + blockX, gy - 1, gz + blockZ))
                {
                    this.worldObj.setBlock(gx + blockX, gy - 1, gz + blockZ, Blocks.ice, 0, 2);
                }

                if (this.worldObj.func_147478_e(gx + blockX, gy, gz + blockZ, true))
                {
                    this.worldObj.setBlock(gx + blockX, gy, gz + blockZ, Blocks.snow_layer, 0, 2);
                }
            }
        }*/

        MinecraftForge.EVENT_BUS.post(new UPopulateChunkEvent.Post(par1IChunkProvider, worldObj, rand, x, y, z, flag));

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
        BiomeGenBase biomegenbase = this.worldObj.getBiomeGenForCoords(par2, par4);
        return par1EnumCreatureType == EnumCreatureType.monster && this.scatteredFeatureGenerator.func_143030_a(par2, par3, par4) ? this.scatteredFeatureGenerator.getScatteredFeatureSpawnList() : biomegenbase.getSpawnableList(par1EnumCreatureType);
    }

    public ChunkPosition func_147416_a(World p_147416_1_, String p_147416_2_, int p_147416_3_, int p_147416_4_, int p_147416_5_)
    {
        return "Stronghold".equals(p_147416_2_) && this.strongholdGenerator != null ? this.strongholdGenerator.func_151545_a(p_147416_1_, p_147416_3_, p_147416_4_, p_147416_5_) : null;
    }

    public int getLoadedChunkCount()
    {
        return 0;
    }
    
    public int getLoadedChunk2DCount() {
		return 0;
	}

    public void recreateStructures(int x, int y, int z)
    {
        if (this.mapFeaturesEnabled)
        {
            this.mineshaftGenerator.func_151539_a(this, this.worldObj, x, y, z, (Block[])null);
            this.villageGenerator.func_151539_a(this, this.worldObj, x, y, z, (Block[])null);
            this.strongholdGenerator.func_151539_a(this, this.worldObj, x, y, z, (Block[])null);
            this.scatteredFeatureGenerator.func_151539_a(this, this.worldObj, x, y, z, (Block[])null);
        }
    }
}
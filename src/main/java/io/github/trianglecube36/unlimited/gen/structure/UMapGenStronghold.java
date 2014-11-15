package io.github.trianglecube36.unlimited.gen.structure;

import io.github.trianglecube36.unlimited.chunk.UChunkCoordIntPair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeManager;

public class UMapGenStronghold extends UMapGenStructure
{
    public final List field_151546_e;
    /**
     * is spawned false and set true once the defined BiomeGenBases were compared with the present ones
     */
    private boolean ranBiomeCheck;
    private UChunkCoordIntPair[] structureCoords;
    private double field_82671_h;
    private int field_82672_i;

    public UMapGenStronghold()
    {
        this.structureCoords = new UChunkCoordIntPair[3];
        this.field_82671_h = 32.0D;
        this.field_82672_i = 3;
        this.field_151546_e = new ArrayList();
        BiomeGenBase[] abiomegenbase = BiomeGenBase.getBiomeGenArray();
        int i = abiomegenbase.length;

        for (int j = 0; j < i; ++j)
        {
            BiomeGenBase biomegenbase = abiomegenbase[j];

            if (biomegenbase != null && biomegenbase.rootHeight > 0.0F && !BiomeManager.strongHoldBiomesBlackList.contains(biomegenbase))
            {
                this.field_151546_e.add(biomegenbase);
            }
        }
        for (BiomeGenBase biome : BiomeManager.strongHoldBiomes)
        {
            if (!this.field_151546_e.contains(biome))
            {
                this.field_151546_e.add(biome);
            }
        }
    }

    public UMapGenStronghold(Map p_i2068_1_)
    {
        this();
        Iterator iterator = p_i2068_1_.entrySet().iterator();

        while (iterator.hasNext())
        {
            Entry entry = (Entry)iterator.next();

            if (((String)entry.getKey()).equals("distance"))
            {
                this.field_82671_h = MathHelper.parseDoubleWithDefaultAndMax((String)entry.getValue(), this.field_82671_h, 1.0D);
            }
            else if (((String)entry.getKey()).equals("count"))
            {
                this.structureCoords = new UChunkCoordIntPair[MathHelper.parseIntWithDefaultAndMax((String)entry.getValue(), this.structureCoords.length, 1)];
            }
            else if (((String)entry.getKey()).equals("spread"))
            {
                this.field_82672_i = MathHelper.parseIntWithDefaultAndMax((String)entry.getValue(), this.field_82672_i, 1);
            }
        }
    }

    public String func_143025_a()
    {
        return "Stronghold";
    }

    protected boolean canSpawnStructureAtCoords(int cX, int cY, int cZ)
    {
    	if(cY != 2){
    		return false;
    	}
        if (!this.ranBiomeCheck)
        {
            Random random = new Random();
            random.setSeed(this.worldObj.getSeed());
            double d0 = random.nextDouble() * Math.PI * 2.0D;
            int l = 1;

            for (int i1 = 0; i1 < this.structureCoords.length; ++i1)
            {
                double d1 = (1.25D * (double)l + random.nextDouble()) * this.field_82671_h * (double)l;
                int j1 = (int)Math.round(Math.cos(d0) * d1);
                int k1 = (int)Math.round(Math.sin(d0) * d1);
                ChunkPosition chunkposition = this.worldObj.getWorldChunkManager().findBiomePosition((j1 << 4) + 8, (k1 << 4) + 8, 112, this.field_151546_e, random);

                if (chunkposition != null)
                {
                    j1 = chunkposition.chunkPosX >> 5;
                    k1 = chunkposition.chunkPosZ >> 5;
                }

                this.structureCoords[i1] = new UChunkCoordIntPair(j1, 2, k1); //TODO: check
                d0 += (Math.PI * 2D) * (double)l / (double)this.field_82672_i;

                if (i1 == this.field_82672_i)
                {
                    l += 2 + random.nextInt(5);
                    this.field_82672_i += 1 + random.nextInt(2);
                }
            }

            this.ranBiomeCheck = true;
        }

        UChunkCoordIntPair[] achunkcoordintpair = this.structureCoords;
        int l1 = achunkcoordintpair.length;

        for (int k = 0; k < l1; ++k)
        {
            UChunkCoordIntPair chunkcoordintpair = achunkcoordintpair[k];

            if (cX == chunkcoordintpair.chunkXPos && cY == chunkcoordintpair.chunkYPos && cZ == chunkcoordintpair.chunkZPos)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a list of other locations at which the structure generation has been run, or null if not relevant to this
     * structure generator.
     */
    protected List getCoordList()
    {
        ArrayList arraylist = new ArrayList();
        UChunkCoordIntPair[] achunkcoordintpair = this.structureCoords;
        int i = achunkcoordintpair.length;

        for (int j = 0; j < i; ++j)
        {
            UChunkCoordIntPair chunkcoordintpair = achunkcoordintpair[j];

            if (chunkcoordintpair != null)
            {
                arraylist.add(chunkcoordintpair.func_151349_a(64));
            }
        }

        return arraylist;
    }

    protected UStructureStart getStructureStart(int cX, int cY, int cZ)
    {
        UMapGenStronghold.Start start;

        for (start = new UMapGenStronghold.Start(this.worldObj, this.rand, cX, cY, cZ); start.getComponents().isEmpty() || ((UStructureStrongholdPieces.Stairs2)start.getComponents().get(0)).strongholdPortalRoom == null; start = new UMapGenStronghold.Start(this.worldObj, this.rand, cX, cY, cZ))
        {
            ;
        }

        return start;
    }

    public static class Start extends UStructureStart
        {
            public Start() {}

            public Start(World world, Random rand, int cX, int cY, int cZ)
            {
                super(cX, cY, cZ);
                UStructureStrongholdPieces.prepareStructurePieces();
                UStructureStrongholdPieces.Stairs2 stairs2 = new UStructureStrongholdPieces.Stairs2(0, rand, (cX << 5) + 2, (cY << 5) + 2, (cZ << 5) + 2);
                this.components.add(stairs2);
                stairs2.buildComponent(stairs2, this.components, rand);
                List list = stairs2.field_75026_c;

                while (!list.isEmpty())
                {
                    int k = rand.nextInt(list.size());
                    UStructureComponent structurecomponent = (UStructureComponent)list.remove(k);
                    structurecomponent.buildComponent(stairs2, this.components, rand);
                }

                this.updateBoundingBox();
                this.markAvailableHeight(world, rand, 10);
            }
        }
}
package io.github.trianglecube36.unlimited.gen.structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class UMapGenScatteredFeature extends UMapGenStructure
{
    private static List biomelist = Arrays.asList(new BiomeGenBase[] {BiomeGenBase.desert, BiomeGenBase.desertHills, BiomeGenBase.jungle, BiomeGenBase.jungleHills, BiomeGenBase.swampland});
    /** contains possible spawns for scattered features */
    private List scatteredFeatureSpawnList;
    /** the maximum distance between scattered features */
    private int maxDistanceBetweenScatteredFeatures;
    /** the minimum distance between scattered features */
    private int minDistanceBetweenScatteredFeatures;

    public UMapGenScatteredFeature()
    {
        this.scatteredFeatureSpawnList = new ArrayList();
        this.maxDistanceBetweenScatteredFeatures = 16; // was 32
        this.minDistanceBetweenScatteredFeatures = 3; // was 8
        this.scatteredFeatureSpawnList.add(new BiomeGenBase.SpawnListEntry(EntityWitch.class, 1, 1, 1));
    }

    public UMapGenScatteredFeature(Map p_i2061_1_)
    {
        this();
        Iterator iterator = p_i2061_1_.entrySet().iterator();

        while (iterator.hasNext())
        {
            Entry entry = (Entry)iterator.next();

            if (((String)entry.getKey()).equals("distance"))
            {
                this.maxDistanceBetweenScatteredFeatures = MathHelper.parseIntWithDefaultAndMax((String)entry.getValue(), this.maxDistanceBetweenScatteredFeatures, this.minDistanceBetweenScatteredFeatures + 1);
            }
        }
    }

    public String func_143025_a()
    {
        return "Temple";
    }

    protected boolean canSpawnStructureAtCoords(int cX, int cY, int cZ)
    {
    	if(cY != 2){
    		return false;
    	}
        int cX1 = cX;
        int cZ1 = cZ;

        if (cX < 0)
        {
            cX -= this.maxDistanceBetweenScatteredFeatures - 1;
        }

        if (cZ < 0)
        {
            cZ -= this.maxDistanceBetweenScatteredFeatures - 1;
        }

        int i1 = cX / this.maxDistanceBetweenScatteredFeatures;
        int j1 = cZ / this.maxDistanceBetweenScatteredFeatures;
        Random random = this.worldObj.setRandomSeed(i1, j1, 14357617);
        i1 *= this.maxDistanceBetweenScatteredFeatures;
        j1 *= this.maxDistanceBetweenScatteredFeatures;
        i1 += random.nextInt(this.maxDistanceBetweenScatteredFeatures - this.minDistanceBetweenScatteredFeatures);
        j1 += random.nextInt(this.maxDistanceBetweenScatteredFeatures - this.minDistanceBetweenScatteredFeatures);

        if (cX1 == i1 && cZ1 == j1)
        {
            BiomeGenBase biomegenbase = this.worldObj.getWorldChunkManager().getBiomeGenAt(cX1 * 32 + 16, cZ1 * 32 + 16);
            Iterator iterator = biomelist.iterator();

            while (iterator.hasNext())
            {
                BiomeGenBase biomegenbase1 = (BiomeGenBase)iterator.next();

                if (biomegenbase == biomegenbase1)
                {
                    return true;
                }
            }
        }

        return false;
    }

    protected UStructureStart getStructureStart(int cX, int cY, int cZ)
    {
        return new UMapGenScatteredFeature.Start(this.worldObj, this.rand, cX, cY, cZ);
    }

    public boolean func_143030_a(int bX, int bY, int bZ)
    {
        UStructureStart structurestart = this.func_143028_c(bX, bY, bZ);

        if (structurestart != null && structurestart instanceof UMapGenScatteredFeature.Start && !structurestart.components.isEmpty())
        {
            UStructureComponent structurecomponent = (UStructureComponent)structurestart.components.getFirst();
            return structurecomponent instanceof UComponentScatteredFeaturePieces.SwampHut;
        }
        else
        {
            return false;
        }
    }

    /**
     * returns possible spawns for scattered features
     */
    public List getScatteredFeatureSpawnList()
    {
        return this.scatteredFeatureSpawnList;
    }

    public static class Start extends UStructureStart
        {
            public Start() {}

            public Start(World world, Random rand, int cX, int cY, int cZ)
            {
                super(cX, cY, cZ);
                BiomeGenBase biomegenbase = world.getBiomeGenForCoords(cX * 32 + 16, cZ * 32 + 16);

                if (biomegenbase != BiomeGenBase.jungle && biomegenbase != BiomeGenBase.jungleHills)
                {
                    if (biomegenbase == BiomeGenBase.swampland)
                    {
                        UComponentScatteredFeaturePieces.SwampHut swamphut = new UComponentScatteredFeaturePieces.SwampHut(rand, cX * 32, cY * 32, cZ * 32);
                        this.components.add(swamphut);
                    }
                    else
                    {
                        UComponentScatteredFeaturePieces.DesertPyramid desertpyramid = new UComponentScatteredFeaturePieces.DesertPyramid(rand, cX * 32, cY * 32, cZ * 32);
                        this.components.add(desertpyramid);
                    }
                }
                else
                {
                    UComponentScatteredFeaturePieces.JunglePyramid junglepyramid = new UComponentScatteredFeaturePieces.JunglePyramid(rand, cX * 32, cY * 32, cZ * 32);
                    this.components.add(junglepyramid);
                }

                this.updateBoundingBox();
            }
        }
}
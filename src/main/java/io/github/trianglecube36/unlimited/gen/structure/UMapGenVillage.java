package io.github.trianglecube36.unlimited.gen.structure;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class UMapGenVillage extends UMapGenStructure
{
    /** A list of all the biomes villages can spawn in. */
    public static List villageSpawnBiomes = Arrays.asList(new BiomeGenBase[] {BiomeGenBase.plains, BiomeGenBase.desert, BiomeGenBase.savanna});
    /** World terrain type, 0 for normal, 1 for flat map */
    private int terrainType;
    private int field_82665_g;
    private int field_82666_h;

    public UMapGenVillage()
    {
        this.field_82665_g = 16; //was 32
        this.field_82666_h = 4;  //was 8
    }

    public UMapGenVillage(Map p_i2093_1_)
    {
        this();
        Iterator iterator = p_i2093_1_.entrySet().iterator();

        while (iterator.hasNext())
        {
            Entry entry = (Entry)iterator.next();

            if (((String)entry.getKey()).equals("size"))
            {
                this.terrainType = MathHelper.parseIntWithDefaultAndMax((String)entry.getValue(), this.terrainType, 0);
            }
            else if (((String)entry.getKey()).equals("distance"))
            {
                this.field_82665_g = MathHelper.parseIntWithDefaultAndMax((String)entry.getValue(), this.field_82665_g, this.field_82666_h + 1);
            }
        }
    }

    public String func_143025_a()
    {
        return "Village";
    }

    protected boolean canSpawnStructureAtCoords(int cX, int cY, int cZ)
    {
    	if(cY != 2){ //TODO: check
    		return false;
    	}
        int k = cX;
        int l = cZ;

        if (cX < 0)
        {
            cX -= this.field_82665_g - 1;
        }

        if (cZ < 0)
        {
            cZ -= this.field_82665_g - 1;
        }

        int i1 = cX / this.field_82665_g;
        int j1 = cZ / this.field_82665_g;
        Random random = this.worldObj.setRandomSeed(i1, j1, 10387312);
        i1 *= this.field_82665_g;
        j1 *= this.field_82665_g;
        i1 += random.nextInt(this.field_82665_g - this.field_82666_h);
        j1 += random.nextInt(this.field_82665_g - this.field_82666_h);

        if (k == i1 && l == j1)
        {
            boolean flag = this.worldObj.getWorldChunkManager().areBiomesViable(k * 32 + 16, l * 32 + 16, 0, villageSpawnBiomes);

            if (flag)
            {
                return true;
            }
        }

        return false;
    }

    protected UStructureStart getStructureStart(int cX, int cY, int cZ)
    {
        return new UMapGenVillage.Start(this.worldObj, this.rand, cX, cY, cZ, this.terrainType);
    }

    public static class Start extends UStructureStart
        {
            /** well ... thats what it does */
            private boolean hasMoreThanTwoComponents;

            public Start() {}

            public Start(World world, Random rand, int cX, int cY, int cZ, int type)
            {
                super(cX, cY, cZ);
                List list = UStructureVillagePieces.getStructureVillageWeightedPieceList(rand, type);
                UStructureVillagePieces.Start start = new UStructureVillagePieces.Start(world.getWorldChunkManager(), 0, rand, (cX << 5) + 2, (cY << 5) + 2, (cZ << 5) + 2, list, type);
                this.components.add(start);
                start.buildComponent(start, this.components, rand);
                List list1 = start.field_74930_j;
                List list2 = start.field_74932_i;
                int l;

                while (!list1.isEmpty() || !list2.isEmpty())
                {
                    UStructureComponent structurecomponent;

                    if (list1.isEmpty())
                    {
                        l = rand.nextInt(list2.size());
                        structurecomponent = (UStructureComponent)list2.remove(l);
                        structurecomponent.buildComponent(start, this.components, rand);
                    }
                    else
                    {
                        l = rand.nextInt(list1.size());
                        structurecomponent = (UStructureComponent)list1.remove(l);
                        structurecomponent.buildComponent(start, this.components, rand);
                    }
                }

                this.updateBoundingBox();
                l = 0;
                Iterator iterator = this.components.iterator();

                while (iterator.hasNext())
                {
                    UStructureComponent structurecomponent1 = (UStructureComponent)iterator.next();

                    if (!(structurecomponent1 instanceof UStructureVillagePieces.Road))
                    {
                        ++l;
                    }
                }

                this.hasMoreThanTwoComponents = l > 2;
            }

            /**
             * currently only defined for Villages, returns true if Village has more than 2 non-road components
             */
            public boolean isSizeableStructure()
            {
                return this.hasMoreThanTwoComponents;
            }

            public void func_143022_a(NBTTagCompound p_143022_1_)
            {
                super.func_143022_a(p_143022_1_);
                p_143022_1_.setBoolean("Valid", this.hasMoreThanTwoComponents);
            }

            public void func_143017_b(NBTTagCompound p_143017_1_)
            {
                super.func_143017_b(p_143017_1_);
                this.hasMoreThanTwoComponents = p_143017_1_.getBoolean("Valid");
            }
        }
}
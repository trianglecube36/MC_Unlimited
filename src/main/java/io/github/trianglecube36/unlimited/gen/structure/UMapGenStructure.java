package io.github.trianglecube36.unlimited.gen.structure;

import io.github.trianglecube36.unlimited.gen.UMapGenBase;
import io.github.trianglecube36.unlimited.util.HashMap3D;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;

import net.minecraft.block.Block;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ReportedException;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.MapGenStructureData;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;

public abstract class UMapGenStructure extends UMapGenBase
{
    private MapGenStructureData field_143029_e;
    /**
     * TODO: THIS IS WHY OLD WORLDS LAG...
     * if you are a server admin that owned a server for a long time with 1 world,
     * you may have needed to reset the world as players would generate so much land that
     * the server would get more and more lag...
     * "why?! only the chunks around players and the spawn chunks are loaded!"
     * 
     * This HashMap is one of the examples of the HOLE world being loaded even when not needed!
     * My goal is to also try to fix this O(amount of world generated)
     */
    protected HashMap3D structureMap = new HashMap3D(); // <---- smash this

    public abstract String func_143025_a();

    protected final void func_151538_a(World world, final int checkX, final int checkY, final int checkZ, int cX, int cY, int cZ, Block[] blockArray)
    {
        this.func_143027_a(world);

        if (!this.structureMap.containsItem(checkX, checkY, checkZ))
        {
            this.rand.nextInt();

            try
            {
                if (this.canSpawnStructureAtCoords(checkX, checkY, checkZ))
                {
                    StructureStart structurestart = this.getStructureStart(checkX, checkY, checkZ);
                    this.structureMap.add(checkX, checkY, checkZ, structurestart);
                    this.func_143026_a(checkX, checkY, checkZ, structurestart);
                }
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception preparing structure feature");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Feature being prepared");
                crashreportcategory.addCrashSectionCallable("Is feature chunk", new Callable()
                {
                    public String call()
                    {
                        return UMapGenStructure.this.canSpawnStructureAtCoords(checkX, checkY, checkZ) ? "True" : "False";
                    }
                });
                crashreportcategory.addCrashSection("UChunk32 location", String.format("%d,%d", new Object[] {Integer.valueOf(checkX), Integer.valueOf(checkY), Integer.valueOf(checkZ)}));
                crashreportcategory.addCrashSectionCallable("Chunk pos hash (obsolete)", new Callable()
                {
                    public String call()
                    {
                        return String.valueOf(ChunkCoordIntPair.chunkXZ2Int(checkX, checkZ));
                    }
                });
                crashreportcategory.addCrashSectionCallable("Structure type", new Callable()
                {
                    public String call()
                    {
                        return UMapGenStructure.this.getClass().getCanonicalName();
                    }
                });
                throw new ReportedException(crashreport);
            }
        }
    }

    /**
     * Generates structures in specified chunk next to existing structures. Does *not* generate StructureStarts.
     */
    public boolean generateStructuresInChunk(World world, Random rand, int cX, int cY, int cZ)
    {
        this.func_143027_a(world);
        int mX = (cX << 5) + 16;
        int mY = (cY << 5) + 16;
        int mZ = (cZ << 5) + 16;
        boolean flag = false;
        Iterator iterator = this.structureMap.iterator();

        while (iterator.hasNext())
        {
            StructureStart structurestart = (StructureStart)iterator.next();

            if (structurestart.isSizeableStructure() && structurestart.getBoundingBox().intersectsWith(mX, mY, mZ, mX + 31, mY + 31, mZ + 31))
            {
                structurestart.generateStructure(world, rand, new StructureBoundingBox(mX, mY, mZ, mX + 31, mY + 31, mZ + 31));
                flag = true;
                this.func_143026_a(structurestart.func_143019_e(), structurestart.func_143018_f(), structurestart);
            }
        }

        return flag;
    }

    /**
     * Returns true if the structure generator has generated a structure located at the given position tuple.
     */
    public boolean hasStructureAt(int bX, int bY, int bZ)
    {
        this.func_143027_a(this.worldObj);
        return this.func_143028_c(bX, bY, bZ) != null;
    }

    protected StructureStart func_143028_c(int bX, int bY, int bZ)
    {
        Iterator iterator = this.structureMap.iterator();

        while (iterator.hasNext())
        {
            StructureStart structurestart = (StructureStart)iterator.next();

            if (structurestart.isSizeableStructure() && structurestart.getBoundingBox().intersectsWith(bX, bZ, bX, bZ))
            {
                Iterator iterator1 = structurestart.getComponents().iterator();

                while (iterator1.hasNext())
                {
                    StructureComponent structurecomponent = (StructureComponent)iterator1.next();

                    if (structurecomponent.getBoundingBox().isVecInside(bX, bY, bZ))
                    {
                        return structurestart;
                    }
                }
            }
        }

        return null;
    }

    public boolean func_142038_b(int p_142038_1_, int p_142038_2_, int p_142038_3_)
    {
        this.func_143027_a(this.worldObj);
        Iterator iterator = this.structureMap.values().iterator();
        StructureStart structurestart;

        do
        {
            if (!iterator.hasNext())
            {
                return false;
            }

            structurestart = (StructureStart)iterator.next();
        }
        while (!structurestart.isSizeableStructure());

        return structurestart.getBoundingBox().intersectsWith(p_142038_1_, p_142038_3_, p_142038_1_, p_142038_3_);
    }

    public ChunkPosition func_151545_a(World p_151545_1_, int p_151545_2_, int p_151545_3_, int p_151545_4_)
    {
        this.worldObj = p_151545_1_;
        this.func_143027_a(p_151545_1_);
        this.rand.setSeed(p_151545_1_.getSeed());
        long l = this.rand.nextLong();
        long i1 = this.rand.nextLong();
        long j1 = (long)(p_151545_2_ >> 4) * l;
        long k1 = (long)(p_151545_4_ >> 4) * i1;
        this.rand.setSeed(j1 ^ k1 ^ p_151545_1_.getSeed());
        this.func_151538_a(p_151545_1_, p_151545_2_ >> 4, p_151545_4_ >> 4, 0, 0, (Block[])null);
        double d0 = Double.MAX_VALUE;
        ChunkPosition chunkposition = null;
        Iterator iterator = this.structureMap.values().iterator();
        ChunkPosition chunkposition1;
        int l1;
        int i2;
        int j2;
        double d1;

        while (iterator.hasNext())
        {
            StructureStart structurestart = (StructureStart)iterator.next();

            if (structurestart.isSizeableStructure())
            {
                StructureComponent structurecomponent = (StructureComponent)structurestart.getComponents().get(0);
                chunkposition1 = structurecomponent.func_151553_a();
                l1 = chunkposition1.chunkPosX - p_151545_2_;
                i2 = chunkposition1.chunkPosY - p_151545_3_;
                j2 = chunkposition1.chunkPosZ - p_151545_4_;
                d1 = (double)(l1 * l1 + i2 * i2 + j2 * j2);

                if (d1 < d0)
                {
                    d0 = d1;
                    chunkposition = chunkposition1;
                }
            }
        }

        if (chunkposition != null)
        {
            return chunkposition;
        }
        else
        {
            List list = this.getCoordList();

            if (list != null)
            {
                ChunkPosition chunkposition2 = null;
                Iterator iterator1 = list.iterator();

                while (iterator1.hasNext())
                {
                    chunkposition1 = (ChunkPosition)iterator1.next();
                    l1 = chunkposition1.chunkPosX - p_151545_2_;
                    i2 = chunkposition1.chunkPosY - p_151545_3_;
                    j2 = chunkposition1.chunkPosZ - p_151545_4_;
                    d1 = (double)(l1 * l1 + i2 * i2 + j2 * j2);

                    if (d1 < d0)
                    {
                        d0 = d1;
                        chunkposition2 = chunkposition1;
                    }
                }

                return chunkposition2;
            }
            else
            {
                return null;
            }
        }
    }

    /**
     * Returns a list of other locations at which the structure generation has been run, or null if not relevant to this
     * structure generator.
     */
    protected List getCoordList()
    {
        return null;
    }

    private void func_143027_a(World p_143027_1_)
    {
        if (this.field_143029_e == null)
        {
            this.field_143029_e = (MapGenStructureData)p_143027_1_.perWorldStorage.loadData(MapGenStructureData.class, this.func_143025_a());

            if (this.field_143029_e == null)
            {
                this.field_143029_e = new MapGenStructureData(this.func_143025_a());
                p_143027_1_.perWorldStorage.setData(this.func_143025_a(), this.field_143029_e);
            }
            else
            {
                NBTTagCompound nbttagcompound = this.field_143029_e.func_143041_a();
                Iterator iterator = nbttagcompound.func_150296_c().iterator();

                while (iterator.hasNext())
                {
                    String s = (String)iterator.next();
                    NBTBase nbtbase = nbttagcompound.getTag(s);

                    if (nbtbase.getId() == 10)
                    {
                        NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbtbase;

                        if (nbttagcompound1.hasKey("ChunkX") && nbttagcompound1.hasKey("ChunkZ"))
                        {
                            int i = nbttagcompound1.getInteger("ChunkX");
                            int j = nbttagcompound1.getInteger("ChunkZ");
                            StructureStart structurestart = MapGenStructureIO.func_143035_a(nbttagcompound1, p_143027_1_);

                            if (structurestart != null)
                            {
                                this.structureMap.put(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(i, j)), structurestart);
                            }
                        }
                    }
                }
            }
        }
    }

    private void func_143026_a(int p_143026_1_, int p_143026_2_, StructureStart p_143026_3_)
    {
        this.field_143029_e.func_143043_a(p_143026_3_.func_143021_a(p_143026_1_, p_143026_2_), p_143026_1_, p_143026_2_);
        this.field_143029_e.markDirty();
    }

    protected abstract boolean canSpawnStructureAtCoords(int p_75047_1_, int p_75047_2_);

    protected abstract StructureStart getStructureStart(int p_75049_1_, int p_75049_2_);
}
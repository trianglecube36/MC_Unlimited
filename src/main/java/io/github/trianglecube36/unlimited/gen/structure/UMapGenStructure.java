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

public abstract class UMapGenStructure extends UMapGenBase
{
    private UMapGenStructureData field_143029_e;
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
                    UStructureStart structurestart = this.getStructureStart(checkX, checkY, checkZ);
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
            UStructureStart structurestart = (UStructureStart)iterator.next();

            if (structurestart.isSizeableStructure() && structurestart.getBoundingBox().intersectsWith(mX, mY, mZ, mX + 31, mY + 31, mZ + 31))
            {
                structurestart.generateStructure(world, rand, new UStructureBoundingBox(mX, mY, mZ, mX + 31, mY + 31, mZ + 31));
                flag = true;
                this.func_143026_a(structurestart.func_143019_e(), structurestart.getChunkY(), structurestart.func_143018_f(), structurestart);
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

    protected UStructureStart func_143028_c(int bX, int bY, int bZ)
    {
        Iterator iterator = this.structureMap.iterator();

        while (iterator.hasNext())
        {
            UStructureStart structurestart = (UStructureStart)iterator.next();

            if (structurestart.isSizeableStructure() && structurestart.getBoundingBox().intersectsWith(bX, bY, bZ, bX, bY, bZ))
            {
                Iterator iterator1 = structurestart.getComponents().iterator();

                while (iterator1.hasNext())
                {
                    UStructureComponent structurecomponent = (UStructureComponent)iterator1.next();

                    if (structurecomponent.getBoundingBox().isVecInside(bX, bY, bZ))
                    {
                        return structurestart;
                    }
                }
            }
        }

        return null;
    }

    public boolean func_142038_b(int bX, int bY, int bZ)
    {
        this.func_143027_a(this.worldObj);
        Iterator iterator = this.structureMap.iterator();
        UStructureStart structurestart;

        do
        {
            if (!iterator.hasNext())
            {
                return false;
            }

            structurestart = (UStructureStart)iterator.next();
        }
        while (!structurestart.isSizeableStructure());

        return structurestart.getBoundingBox().intersectsWith(bX, bY, bZ, bX, bY, bZ);
    }

    public ChunkPosition func_151545_a(World world, int bX, int bY, int bZ)
    {
        this.worldObj = world;
        this.func_143027_a(world);
        this.rand.setSeed(world.getSeed());
        long rl1 = this.rand.nextLong();
        long rl2 = this.rand.nextLong();
        long rl3 = this.rand.nextLong();
        long rlX = (long)(bX >> 5) * rl1;
        long rlY = (long)(bY >> 5) * rl2;
        long rlZ = (long)(bZ >> 5) * rl3;
        this.rand.setSeed(rlX ^ rlY ^ rlZ ^ world.getSeed());
        this.func_151538_a(world, bX >> 5, bY >> 5, bZ >> 5, 0, 0, 0, (Block[])null);
        double d0 = Double.MAX_VALUE;
        ChunkPosition chunkposition = null;
        Iterator iterator = this.structureMap.iterator();
        ChunkPosition chunkposition1;
        int l1;
        int i2;
        int j2;
        double d1;

        while (iterator.hasNext())
        {
            UStructureStart structurestart = (UStructureStart)iterator.next();

            if (structurestart.isSizeableStructure())
            {
                UStructureComponent structurecomponent = (UStructureComponent)structurestart.getComponents().get(0);
                chunkposition1 = structurecomponent.func_151553_a();
                l1 = chunkposition1.chunkPosX - bX;
                i2 = chunkposition1.chunkPosY - bY;
                j2 = chunkposition1.chunkPosZ - bZ;
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
                    l1 = chunkposition1.chunkPosX - bX;
                    i2 = chunkposition1.chunkPosY - bY;
                    j2 = chunkposition1.chunkPosZ - bZ;
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

    private void func_143027_a(World world)
    {
        if (this.field_143029_e == null)
        {
            this.field_143029_e = (UMapGenStructureData)world.perWorldStorage.loadData(UMapGenStructureData.class, this.func_143025_a());

            if (this.field_143029_e == null)
            {
                this.field_143029_e = new UMapGenStructureData(this.func_143025_a());
                world.perWorldStorage.setData(this.func_143025_a(), this.field_143029_e);
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

                        if (nbttagcompound1.hasKey("ChunkX") && nbttagcompound1.hasKey("ChunkY") && nbttagcompound1.hasKey("ChunkZ"))
                        {
                            int cX = nbttagcompound1.getInteger("ChunkX");
                            int cY = nbttagcompound1.getInteger("ChunkY");
                            int cZ = nbttagcompound1.getInteger("ChunkZ");
                            UStructureStart structurestart = UMapGenStructureIO.func_143035_a(nbttagcompound1, world);

                            if (structurestart != null)
                            {
                                this.structureMap.add(cX, cY, cZ, structurestart);
                            }
                        }
                    }
                }
            }
        }
    }

    private void func_143026_a(int cX, int cY, int cZ, UStructureStart ss)
    {
        this.field_143029_e.func_143043_a(ss.func_143021_a(cX, cY, cZ), cX, cY, cZ);
        this.field_143029_e.markDirty();
    }

    protected abstract boolean canSpawnStructureAtCoords(int cX, int cY, int cZ);

    protected abstract UStructureStart getStructureStart(int cX, int cY, int cZ);
}
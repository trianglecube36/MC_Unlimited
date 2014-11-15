package io.github.trianglecube36.unlimited.gen.structure;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

public abstract class UStructureStart
{
    /** List of all StructureComponents that are part of this structure */
    protected LinkedList components = new LinkedList();
    protected UStructureBoundingBox boundingBox;
    private int blockX;
    private int blockY;
    private int blockZ;

    public UStructureStart() {}

    public UStructureStart(int bX, int bY, int bZ)
    {
        this.blockX = bX;
        this.blockY = bY;
        this.blockZ = bZ;
    }

    public UStructureBoundingBox getBoundingBox()
    {
        return this.boundingBox;
    }

    public LinkedList getComponents()
    {
        return this.components;
    }

    /**
     * Keeps iterating Structure Pieces and spawning them until the checks tell it to stop
     */
    public void generateStructure(World world, Random rand, UStructureBoundingBox bbox)
    {
        Iterator iterator = this.components.iterator();

        while (iterator.hasNext())
        {
            UStructureComponent structurecomponent = (UStructureComponent)iterator.next();

            if (structurecomponent.getBoundingBox().intersectsWith(bbox) && !structurecomponent.addComponentParts(world, rand, bbox))
            {
                iterator.remove();
            }
        }
    }

    /**
     * Calculates total bounding box based on components' bounding boxes and saves it to boundingBox
     */
    protected void updateBoundingBox()
    {
        this.boundingBox = UStructureBoundingBox.getNewBoundingBox();
        Iterator iterator = this.components.iterator();

        while (iterator.hasNext())
        {
            UStructureComponent structurecomponent = (UStructureComponent)iterator.next();
            this.boundingBox.expandTo(structurecomponent.getBoundingBox());
        }
    }

    public NBTTagCompound func_143021_a(int cX, int cY, int cZ) //TODO: left off here
    {
        if (UMapGenStructureIO.func_143033_a(this) == null) // This is just a more friendly error instead of the 'Null String' below
        {
            throw new RuntimeException("StructureStart \"" + this.getClass().getName() + "\" missing ID Mapping, Modder see MapGenStructureIO");
        }
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setString("id", UMapGenStructureIO.func_143033_a(this));
        nbttagcompound.setInteger("ChunkX", cX);
        nbttagcompound.setInteger("ChunkZ", cZ);
        nbttagcompound.setTag("BB", this.boundingBox.func_151535_h());
        NBTTagList nbttaglist = new NBTTagList();
        Iterator iterator = this.components.iterator();

        while (iterator.hasNext())
        {
            UStructureComponent structurecomponent = (UStructureComponent)iterator.next();
            nbttaglist.appendTag(structurecomponent.func_143010_b());
        }

        nbttagcompound.setTag("Children", nbttaglist);
        this.func_143022_a(nbttagcompound);
        return nbttagcompound;
    }

    public void func_143022_a(NBTTagCompound p_143022_1_) {}

    public void func_143020_a(World p_143020_1_, NBTTagCompound p_143020_2_)
    {
        this.blockX = p_143020_2_.getInteger("ChunkX");
        this.blockZ = p_143020_2_.getInteger("ChunkZ");

        if (p_143020_2_.hasKey("BB"))
        {
            this.boundingBox = new UStructureBoundingBox(p_143020_2_.getIntArray("BB"));
        }

        NBTTagList nbttaglist = p_143020_2_.getTagList("Children", 10);

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            this.components.add(UMapGenStructureIO.func_143032_b(nbttaglist.getCompoundTagAt(i), p_143020_1_));
        }

        this.func_143017_b(p_143020_2_);
    }

    public void func_143017_b(NBTTagCompound p_143017_1_) {}

    /**
     * offsets the structure Bounding Boxes up to a certain height, typically 63 - 10
     */
    protected void markAvailableHeight(World p_75067_1_, Random p_75067_2_, int p_75067_3_)
    {
        int j = 63 - p_75067_3_;
        int k = this.boundingBox.getYSize() + 1;

        if (k < j)
        {
            k += p_75067_2_.nextInt(j - k);
        }

        int l = k - this.boundingBox.maxY;
        this.boundingBox.offset(0, l, 0);
        Iterator iterator = this.components.iterator();

        while (iterator.hasNext())
        {
            UStructureComponent structurecomponent = (UStructureComponent)iterator.next();
            structurecomponent.getBoundingBox().offset(0, l, 0);
        }
    }

    protected void setRandomHeight(World p_75070_1_, Random p_75070_2_, int p_75070_3_, int p_75070_4_)
    {
        int k = p_75070_4_ - p_75070_3_ + 1 - this.boundingBox.getYSize();
        boolean flag = true;
        int i1;

        if (k > 1)
        {
            i1 = p_75070_3_ + p_75070_2_.nextInt(k);
        }
        else
        {
            i1 = p_75070_3_;
        }

        int l = i1 - this.boundingBox.minY;
        this.boundingBox.offset(0, l, 0);
        Iterator iterator = this.components.iterator();

        while (iterator.hasNext())
        {
            UStructureComponent structurecomponent = (UStructureComponent)iterator.next();
            structurecomponent.getBoundingBox().offset(0, l, 0);
        }
    }

    /**
     * currently only defined for Villages, returns true if Village has more than 2 non-road components
     */
    public boolean isSizeableStructure()
    {
        return true;
    }

    public int func_143019_e()
    {
        return this.blockX;
    }

    public int func_143018_f()
    {
        return this.blockZ;
    }
}
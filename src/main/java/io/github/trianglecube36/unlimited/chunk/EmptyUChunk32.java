package io.github.trianglecube36.unlimited.chunk;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EmptyUChunk32 extends UChunk32 {
    public EmptyUChunk32(World par1World, int x, int y, int z)
    {
        super(par1World, z, y, z);
    }

    /**
     * Checks whether the chunk is at the X/Z location specified
     */
    public boolean isAtLocation(int x, int y, int z)
    {
        return x == this.xPosition && y == this.yPosition && z == this.zPosition;
    }

    public Block getBlock(int x, int y, int z)
    {
        return Blocks.air;
    }

    public int func_150808_b(int x, int y, int z)
    {
        return 255;
    }

    public boolean setBlockAndMetadata(int p_150807_1_, int p_150807_2_, int p_150807_3_, Block p_150807_4_, int p_150807_5_)
    {
        return true;
    }

    /**
     * Return the metadata corresponding to the given coordinates inside a chunk.
     */
    public int getBlockMetadata(int x, int y, int z)
    {
        return 0;
    }

    /**
     * Set the metadata of a block in the chunk
     */
    public boolean setBlockMetadata(int x, int y, int z, int data)
    {
        return false;
    }

    /**
     * Gets the amount of light saved in this block (doesn't adjust for daylight)
     */
    public int getSavedLightValue(EnumSkyBlock par1EnumSkyBlock, int x, int y, int z)
    {
        return 0;
    }

    /**
     * Sets the light value at the coordinate. If enumskyblock is set to sky it sets it in the skylightmap and if its a
     * block then into the blocklightmap. Args enumSkyBlock, x, y, z, lightValue
     */
    public void setLightValue(EnumSkyBlock par1EnumSkyBlock, int x, int y, int z, int value) {}

    /**
     * Gets the amount of light on a block taking into account sunlight
     */
    public int getBlockLightValue(int x, int y, int z, int somenumber)
    {
        return 0;
    }

    /**
     * Adds an entity to the chunk. Args: entity
     */
    public void addEntity(Entity par1Entity) {}

    /**
     * removes entity using its y chunk coordinate as its index
     */
    public void removeEntity(Entity par1Entity) {}

    public TileEntity createTileEntity(int x, int y, int z)
    {
        return null;
    }

    public void func_150813_a(TileEntity p_150813_1_) {}

    public void func_150812_a(int x, int y, int z, TileEntity p_150812_4_) {}

    public void removeTileEntity(int x, int y, int z) {}

    /**
     * Called when this Chunk is loaded by the ChunkProvider
     */
    public void onChunkLoad() {}

    /**
     * Called when this Chunk is unloaded by the ChunkProvider
     */
    public void onChunkUnload() {}

    /**
     * Sets the isModified flag for this Chunk
     */
    public void setChunkModified() {}

    /**
     * Fills the given list of all entities that intersect within the given bounding box that aren't the passed entity
     * Args: entity, aabb, listToFill
     */
    public void getEntitiesWithinAABBForEntity(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB, List par3List, IEntitySelector par4IEntitySelector) {}

    /**
     * Gets all entities that can be assigned to the specified class. Args: entityClass, aabb, listToFill
     */
    public void getEntitiesOfTypeWithinAAAB(Class par1Class, AxisAlignedBB par2AxisAlignedBB, List par3List, IEntitySelector par4IEntitySelector) {}

    /**
     * Returns true if this Chunk needs to be saved
     */
    public boolean needsSaving(boolean par1)
    {
        return false;
    }

    public Random getRandomWithSeed(long par1)
    {
    	return new Random(this.worldObj.getSeed() + (long)(this.xPosition * this.xPosition * 4987142) + (long)(this.xPosition * 5947611) + (long)(this.yPosition * this.yPosition * 345005437) + (long)(this.yPosition * 20934547) + (long)(this.zPosition * this.zPosition * 4392871L) + (long)(this.zPosition * 389711) ^ par1);
    }

    public boolean isEmpty()
    {
        return true;
    }

    /**
     * Returns whether the ExtendedBlockStorages containing levels (in blocks) from arg 1 to arg 2 are fully empty
     * (true) or not (false).
     */
    public boolean getAreLevelsEmpty(int par1, int par2)
    {
        return true;
    }
}

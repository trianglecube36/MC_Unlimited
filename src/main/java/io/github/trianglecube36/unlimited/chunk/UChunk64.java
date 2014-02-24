package io.github.trianglecube36.unlimited.chunk;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.github.trianglecube36.unlimited.event.UChunkEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.command.IEntitySelector;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.world.ChunkEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UChunk64
{
    private static final Logger field_150817_t = LogManager.getLogger();
    /**
     * Determines if the chunk is lit or not at a light value greater than 0.
     */
    public static boolean isLit;
    /**
     * INDEXING BIT ORDER: yyzzxx
     */
    private ExtendedBlockStorage[] storageArrays; // now 4x4x4 array

    /**
     * Which columns need their skylightMaps updated.
     */
    public boolean[] updateSkylightColumns; //TODO: keep or delete?
    /**
     * Whether or not this Chunk is currently loaded into the World
     */
    public boolean isChunkLoaded;
    /**
     * Reference to the World object.
     */
    public World worldObj;

    public final int xPosition;
    public final int yPosition;
    public final int zPosition;

    private boolean isGapLightingUpdated;
    public Map chunkTileEntityMap;

    public List entityLists;
    /**
     * Boolean value indicating if the terrain is populated.
     */
    public boolean isTerrainPopulated;
    
    //was field_150814_l
    public boolean isLightPopulated;
    public boolean field_150815_m;
    /**
     * Set to true if the chunk has been modified and needs to be updated internally.
     */
    public boolean isModified;
    /**
     * Whether this Chunk has any Entities and thus requires saving on every tick
     */
    public boolean hasEntities;
    /**
     * The time according to World.worldTime when this chunk was last saved
     */
    public long lastSaveTime;
    /**
     * Updates to this chunk will not be sent to clients if this is false. This field is set to true the first time the
     * chunk is sent to a client, and never set to false.
     */
    public boolean sendUpdates;
    /**
     * Lowest value in the heightmap.
     */
    public int heightMapMinimum;
    /**
     * the cumulative number of ticks players have been in this chunk
     */
    public long inhabitedTime;
    /**
     * Contains the current round-robin relight check index, and is implied as the relight check location as well.
     */
    private int queuedLightChecks;

    public UChunk64(World par1World, int x, int y, int z)
    {
        this.storageArrays = new ExtendedBlockStorage[64];
        this.updateSkylightColumns = new boolean[256];
        this.chunkTileEntityMap = new HashMap();
        this.queuedLightChecks = 4096;
        this.worldObj = par1World;
        this.xPosition = x;
        this.yPosition = y;
        this.zPosition = z;

        this.entityLists = new ArrayList();
    }

    public UChunk64(World w, Block[] blockarray, int x, int y, int z)
    { //TODO: fix this and or remove
        this(w, x, y, z);
        int k = blockarray.length / 256;
        boolean flag = !w.provider.hasNoSky;

        for (int l = 0; l < 16; ++l)
        {
            for (int i1 = 0; i1 < 16; ++i1)
            {
                for (int j1 = 0; j1 < k; ++j1)
                {
                    Block block = blockarray[l << 11 | i1 << 7 | j1];

                    if (block != null && block.getMaterial() != Material.air)
                    {
                        int k1 = j1 >> 4;

                        if (this.storageArrays[k1] == null)
                        {
                            this.storageArrays[k1] = new ExtendedBlockStorage(k1 << 4, flag);
                        }

                        this.storageArrays[k1].func_150818_a(l, j1 & 15, i1, block);
                    }
                }
            }
        }
    }

    public UChunk64(World w, Block[] blockarray, byte[] p_i45447_3_, int x, int y, int z)
    { //TODO: fix this
        this(w, x, y, z);
        int k = blockarray.length / 256;
        boolean flag = !w.provider.hasNoSky;

        for (int l = 0; l < 16; ++l)
        {
            for (int i1 = 0; i1 < 16; ++i1)
            {
                for (int j1 = 0; j1 < k; ++j1)
                {
                    int k1 = l * k * 16 | i1 * k | j1;
                    Block block = blockarray[k1];

                    if (block != null && block != Blocks.air)
                    {
                        int l1 = j1 >> 4;

                        if (this.storageArrays[l1] == null)
                        {
                            this.storageArrays[l1] = new ExtendedBlockStorage(l1 << 4, flag);
                        }

                        this.storageArrays[l1].func_150818_a(l, j1 & 15, i1, block);
                        this.storageArrays[l1].setExtBlockMetadata(l, j1 & 15, i1, p_i45447_3_[k1]);
                    }
                }
            }
        }
    }

    /**
     * Checks whether the chunk is at the X/Z location specified
     */
    public boolean isAtLocation(int x, int y, int z)
    {
        return x == this.xPosition && y == this.yPosition && z == this.zPosition;
    }

    /**
     * Returns the ExtendedBlockStorage array for this Chunk.
     */
    public ExtendedBlockStorage[] getBlockStorageArray()
    {
        return this.storageArrays;
    }

    /**
     * Propagates a given sky-visible block's light value downward and upward to neighboring blocks as necessary.
     */
    private void propagateSkylightOcclusion(int par1, int par2)
    {
        this.updateSkylightColumns[par1 + par2 * 16] = true;
        this.isGapLightingUpdated = true;
    }

    /**
     * Checks the height of a block next to a sky-visible block and schedules a lighting update as necessary.
     */
    private void checkSkylightNeighborHeight(int par1, int par2, int par3)
    {
        int l = this.worldObj.getHeightValue(par1, par2);

        if (l > par3)
        {
            this.updateSkylightNeighborHeight(par1, par2, par3, l + 1);
        }
        else if (l < par3)
        {
            this.updateSkylightNeighborHeight(par1, par2, l, par3 + 1);
        }
    }

    private void updateSkylightNeighborHeight(int par1, int par2, int par3, int par4)
    {
        if (par4 > par3 && this.worldObj.doChunksNearChunkExist(par1, 0, par2, 16))
        {
            for (int i1 = par3; i1 < par4; ++i1)
            {
                this.worldObj.updateLightByType(EnumSkyBlock.Sky, par1, i1, par2);
            }

            this.isModified = true;
        }
    }

    public int func_150808_b(int p_150808_1_, int p_150808_2_, int p_150808_3_)
    { //TODO: should not use world q
        int x = (xPosition << 4) + p_150808_1_;
        int z = (zPosition << 4) + p_150808_3_;
        return this.getBlock(p_150808_1_, p_150808_2_, p_150808_3_).getLightOpacity(worldObj, x, p_150808_2_, p_150808_3_);
    }

    /**
     * func_150810_a
     */
    public Block getBlock(final int x, final int y, final int z)
    {
        Block block = Blocks.air;
        ExtendedBlockStorage extendedblockstorage = this.storageArrays[(y >> 4) << 4 | (z >> 4) << 2 | x >> 4];

        if (extendedblockstorage != null)
        {
            try
            {
                block = extendedblockstorage.getBlockByExtId(x & 15, y & 15, z & 15);
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Getting block");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being got");
                crashreportcategory.addCrashSectionCallable("Location", new Callable()
                {
                    public String call()
                    {
                        return CrashReportCategory.getLocationInfo(x, y, z);
                    }
                });
                throw new ReportedException(crashreport);
            }
        }

        return block;
    }

    /**
     * Return the metadata corresponding to the given coordinates inside a chunk.
     */
    public int getBlockMetadata(int x, int y, int z)
    {
        ExtendedBlockStorage extendedblockstorage = this.storageArrays[(y >> 4) << 4 | (z >> 4) << 2 | x >> 4];
        return extendedblockstorage != null ? extendedblockstorage.getExtBlockMetadata(x & 15, y & 15, z & 15) : 0;
    }

    public boolean setBlockAndMetadata(int x, int y, int z, Block block, int data)
    {//TODO: fix light updating
        int i1 = z << 4 | x;

        /*if (y >= this.precipitationHeightMap[i1] - 1)
        {
            this.precipitationHeightMap[i1] = -999;
        }

        int j1 = this.heightMap[i1];*/
        Block block1 = this.getBlock(x, y, z);
        int k1 = this.getBlockMetadata(x, y, z);

        if (block1 == block && k1 == data)
        {
            return false;
        }
        else
        {
        	int index = (y >> 4) << 4 | (z >> 4) << 2 | x >> 4;
            ExtendedBlockStorage extendedblockstorage = this.storageArrays[index];
            //boolean flag = false;

            if (extendedblockstorage == null)
            {
                if (block == Blocks.air)
                {
                    return false;
                }

                extendedblockstorage = this.storageArrays[index] = new ExtendedBlockStorage(y >> 4 << 4, !this.worldObj.provider.hasNoSky);
                //flag = y >= j1;
            }

            int l1 = this.xPosition * 64 + x;
            int wy = this.yPosition * 64 + x;
            int i2 = this.zPosition * 64 + z;

            if (!this.worldObj.isRemote)
            {
                block1.onBlockPreDestroy(this.worldObj, l1, wy, i2, k1);
            }

            extendedblockstorage.func_150818_a(x & 15, y & 15, z & 15, block);
            extendedblockstorage.setExtBlockMetadata(x & 15, y & 15, z & 15, data); // Move this above to prevent other mods/tile entites from creating invalid ones for the wrong metadata

            if (!this.worldObj.isRemote)
            {
                block1.breakBlock(this.worldObj, l1, wy, i2, block1, k1);
            }
            else if (block1.hasTileEntity(k1))
            {
                TileEntity te = this.getTileEntityUnsafe(x & 0x40, y & 0x40, z & 0x40); //note: dont know why check > 64
                if (te != null && te.shouldRefresh(block1, block, k1, data, worldObj, l1, wy, i2))
                {
                    this.worldObj.removeTileEntity(l1, wy, i2);
                }
            }

            if (extendedblockstorage.getBlockByExtId(x & 15, y & 15, z & 15) != block)
            {
                return false;
            }
            else
            {
                /*if (flag)
                {
                    this.generateSkylightMap();
                }
                else
                {
                    int j2 = block.func_149717_k();
                    int k2 = block1.func_149717_k();

                    if (j2 > 0)
                    {
                        if (y >= j1)
                        {
                            this.relightBlock(x, y + 1, z);
                        }
                    }
                    else if (y == j1 - 1)
                    {
                        this.relightBlock(x, y, z);
                    }

                    if (j2 != k2 && (j2 < k2 || this.getSavedLightValue(EnumSkyBlock.Sky, x, y, z) > 0 || this.getSavedLightValue(EnumSkyBlock.Block, x, y, z) > 0))
                    {
                        this.propagateSkylightOcclusion(x, z);
                    }
                }*/

                TileEntity tileentity;

                if (!this.worldObj.isRemote)
                {
                    block.onBlockAdded(this.worldObj, l1, wy, i2);
                }

                if (block.hasTileEntity(data))
                {
                    tileentity = this.createTileEntity(x, y, z);

                    if (tileentity != null)
                    {
                        tileentity.updateContainingBlockInfo();
                        tileentity.blockMetadata = data;
                    }
                }

                this.isModified = true;
                return true;
            }
        }
    }

    /**
     * Set the metadata of a block in the chunk
     */
    public boolean setBlockMetadata(int x, int y, int z, int data)
    {
        ExtendedBlockStorage blockarray = this.storageArrays[(y >> 4) << 4 | (z >> 4) << 2 | x >> 4];

        if (blockarray == null)
        {
            return false;
        }
        else
        {
            int i1 = blockarray.getExtBlockMetadata(x & 15, y & 15, z & 15);

            if (i1 == data)
            {
                return false;
            }
            else
            {
                this.isModified = true;
                blockarray.setExtBlockMetadata(x & 15, y & 15, z & 15, data);

                if (blockarray.getBlockByExtId(x & 15, y & 15, z & 15).hasTileEntity(data))
                {
                    TileEntity tileentity = this.createTileEntity(x, y, z);

                    if (tileentity != null)
                    {
                        tileentity.updateContainingBlockInfo();
                        tileentity.blockMetadata = data;
                    }
                }

                return true;
            }
        }
    }

    /**
     * Gets the amount of light saved in this block (doesn't adjust for daylight)
     */
    public int getSavedLightValue(EnumSkyBlock par1EnumSkyBlock, int x, int y, int z)
    {//TODO: new Lighting System?
        //ExtendedBlockStorage extendedblockstorage = this.storageArrays[(y >> 4) << 4 | (z >> 4) << 2 | x >> 4];
        //return extendedblockstorage == null ? (this.canBlockSeeTheSky(x, y, z) ? par1EnumSkyBlock.defaultLightValue : 0) : (par1EnumSkyBlock == EnumSkyBlock.Sky ? (this.worldObj.provider.hasNoSky ? 0 : extendedblockstorage.getExtSkylightValue(x & 15, y & 15, z & 15)) : (par1EnumSkyBlock == EnumSkyBlock.Block ? extendedblockstorage.getExtBlocklightValue(x & 15, y & 15, z & 15) : par1EnumSkyBlock.defaultLightValue));
        return 0;
    }

    /**
     * Sets the light value at the coordinate. If enumskyblock is set to sky it sets it in the skylightmap and if its a
     * block then into the blocklightmap. Args enumSkyBlock, x, y, z, lightValue
     */
    public void setLightValue(EnumSkyBlock par1EnumSkyBlock, int x, int y, int z, int value)
    {
        ExtendedBlockStorage extendedblockstorage = this.storageArrays[(y >> 4) << 4 | (z >> 4) << 2 | x >> 4];

        if (extendedblockstorage == null)
        {
            extendedblockstorage = this.storageArrays[(y >> 4) << 4 | (z >> 4) << 2 | x >> 4] = new ExtendedBlockStorage(y >> 4 << 4, !this.worldObj.provider.hasNoSky);
            //this.generateSkylightMap();
            //TODO: new lighting system
        } 

        this.isModified = true;

        if (par1EnumSkyBlock == EnumSkyBlock.Sky)
        {
            if (!this.worldObj.provider.hasNoSky)
            {
                extendedblockstorage.setExtSkylightValue(x & 15, y & 15, z & 15, value);
            }
        }
        else if (par1EnumSkyBlock == EnumSkyBlock.Block)
        {
            extendedblockstorage.setExtBlocklightValue(x & 15, y & 15, z & 15, value);
        }
    }

    /**
     * Gets the amount of light on a block taking into account sunlight
     */
    public int getBlockLightValue(int x, int y, int z, int somenumber)
    {
        ExtendedBlockStorage extendedblockstorage = this.storageArrays[(y >> 4) << 4 | (z >> 4) << 2 | x >> 4];

        if (extendedblockstorage == null)
        {
            return !this.worldObj.provider.hasNoSky && somenumber < EnumSkyBlock.Sky.defaultLightValue ? EnumSkyBlock.Sky.defaultLightValue - somenumber : 0;
        }
        else
        {
            int i1 = this.worldObj.provider.hasNoSky ? 0 : extendedblockstorage.getExtSkylightValue(x & 15, y & 15, z & 15);

            if (i1 > 0)
            {
                isLit = true;
            }

            i1 -= somenumber;
            int j1 = extendedblockstorage.getExtBlocklightValue(x & 15, y & 15, z & 15);

            if (j1 > i1)
            {
                i1 = j1;
            }

            return i1;
        }
    }

    /**
     * Adds an entity to the chunk. Args: entity
     */
    public void addEntity(Entity par1Entity)
    {
        this.hasEntities = true;
        int i = MathHelper.floor_double(par1Entity.posX / 64.0D);
        int ey = MathHelper.floor_double(par1Entity.posY / 64.0D);
        int j = MathHelper.floor_double(par1Entity.posZ / 64.0D);

        if (i != this.xPosition || ey != this.yPosition || j != this.zPosition)
        {
            field_150817_t.error("Wrong location! " + par1Entity);
            Thread.dumpStack();
        }

        //MinecraftForge.EVENT_BUS.post(new EntityEvent.EnteringChunk(par1Entity, this.xPosition, this.zPosition, par1Entity.chunkCoordX, par1Entity.chunkCoordZ));
        par1Entity.addedToChunk = true;
        //TODO: old... use for emulation
        //par1Entity.chunkCoordX = this.xPosition;
        //par1Entity.chunkCoordY = k;
        //par1Entity.chunkCoordZ = this.zPosition;
        par1Entity.UchunkCoordX = this.xPosition;
        par1Entity.UchunkCoordY = this.yPosition;
        par1Entity.UchunkCoordZ = this.zPosition;
        this.entityLists.add(par1Entity);
    }

    /**
     * removes entity using its y chunk coordinate as its index
     */
    public void removeEntity(Entity par1Entity)
    {
        this.entityLists.remove(par1Entity);
    }

    // was func_150806_e
    public TileEntity createTileEntity(int x, int y, int z)
    {
        ChunkPosition chunkposition = new ChunkPosition(x, y, z);
        TileEntity tileentity = (TileEntity)this.chunkTileEntityMap.get(chunkposition);

        if (tileentity != null && tileentity.isInvalid())
        {
            chunkTileEntityMap.remove(chunkposition);
            tileentity = null;
        }

        if (tileentity == null)
        {
            Block block = this.getBlock(x, y, z);
            int meta = this.getBlockMetadata(x, y, z);

            if (!block.hasTileEntity(meta))
            {
                return null;
            }

            tileentity = block.createTileEntity(worldObj, meta);
            this.worldObj.setTileEntity(this.xPosition * 64 + x, this.yPosition * 64 + y, this.zPosition * 64 + z, tileentity);
        }

        return tileentity;
    }

    /**
     * chunk & world add tileentity
     */
    public void func_150813_a(TileEntity p_150813_1_)
    {
        int i = p_150813_1_.xCoord - this.xPosition * 64;
        int j = p_150813_1_.yCoord - this.yPosition * 64;
        int k = p_150813_1_.zCoord - this.zPosition * 64;
        this.func_150812_a(i, j, k, p_150813_1_);

        if (this.isChunkLoaded)
        {
            this.worldObj.addTileEntity(p_150813_1_);
        }
    }

    /**
     * chunk add tileentity
     */
    public void func_150812_a(int x, int y, int z, TileEntity p_150812_4_)
    {
        ChunkPosition chunkposition = new ChunkPosition(x, y, z);
        p_150812_4_.setWorldObj(this.worldObj);
        p_150812_4_.xCoord = this.xPosition * 64 + x;
        p_150812_4_.yCoord = this.yPosition * 64 + y;
        p_150812_4_.zCoord = this.zPosition * 64 + z;

        int metadata = getBlockMetadata(x, y, z);
        if (this.getBlock(x, y, z).hasTileEntity(metadata))
        {
            if (this.chunkTileEntityMap.containsKey(chunkposition))
            {
                ((TileEntity)this.chunkTileEntityMap.get(chunkposition)).invalidate();
            }

            p_150812_4_.validate();
            this.chunkTileEntityMap.put(chunkposition, p_150812_4_);
        }
    }

    public void removeTileEntity(int p_150805_1_, int p_150805_2_, int p_150805_3_)
    {
        ChunkPosition chunkposition = new ChunkPosition(p_150805_1_, p_150805_2_, p_150805_3_);

        if (this.isChunkLoaded)
        {
            TileEntity tileentity = (TileEntity)this.chunkTileEntityMap.remove(chunkposition);

            if (tileentity != null)
            {
                tileentity.invalidate();
            }
        }
    }

    /**
     * Called when this Chunk is loaded by the ChunkProvider
     */
    public void onChunkLoad()
    {
        this.isChunkLoaded = true;
        this.worldObj.func_147448_a(this.chunkTileEntityMap.values());

        Iterator iterator = this.entityLists.iterator();

        while (iterator.hasNext())
        {
            Entity entity = (Entity)iterator.next();
            entity.onChunkLoad();
        }

        this.worldObj.addLoadedEntities(this.entityLists);
        MinecraftForge.EVENT_BUS.post(new UChunkEvent.Load(this));
    }

    /**
     * Called when this Chunk is unloaded by the ChunkProvider
     */
    public void onChunkUnload()
    {
        this.isChunkLoaded = false;
        Iterator iterator = this.chunkTileEntityMap.values().iterator();

        while (iterator.hasNext())
        {
            TileEntity tileentity = (TileEntity)iterator.next();
            this.worldObj.func_147457_a(tileentity);
        }

        this.worldObj.unloadEntities(this.entityLists);
        MinecraftForge.EVENT_BUS.post(new UChunkEvent.Unload(this));
    }

    /**
     * Sets the isModified flag for this Chunk
     */
    public void setChunkModified()
    {
        this.isModified = true;
    }

    /**
     * Fills the given list of all entities that intersect within the given bounding box that aren't the passed entity
     * Args: entity, aabb, listToFill
     */
    public void getEntitiesWithinAABBForEntity(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB, List par3List, IEntitySelector par4IEntitySelector)
    {
        List list1 = this.entityLists;

        for (int l = 0; l < list1.size(); ++l)
        {
            Entity entity1 = (Entity)list1.get(l);

            if (entity1 != par1Entity && entity1.boundingBox.intersectsWith(par2AxisAlignedBB) && (par4IEntitySelector == null || par4IEntitySelector.isEntityApplicable(entity1)))
            {
                par3List.add(entity1);
                Entity[] aentity = entity1.getParts();

                if (aentity != null)
                {
                    for (int i1 = 0; i1 < aentity.length; ++i1)
                    {
                        entity1 = aentity[i1];

                        if (entity1 != par1Entity && entity1.boundingBox.intersectsWith(par2AxisAlignedBB) && (par4IEntitySelector == null || par4IEntitySelector.isEntityApplicable(entity1)))
                        {
                            par3List.add(entity1);
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets all entities that can be assigned to the specified class. Args: entityClass, aabb, listToFill
     */
    public void getEntitiesOfTypeWithinAAAB(Class par1Class, AxisAlignedBB par2AxisAlignedBB, List par3List, IEntitySelector par4IEntitySelector)
    {
        List list1 = this.entityLists;

        for (int l = 0; l < list1.size(); ++l)
        {
            Entity entity = (Entity)list1.get(l);

            if (par1Class.isAssignableFrom(entity.getClass()) && entity.boundingBox.intersectsWith(par2AxisAlignedBB) && (par4IEntitySelector == null || par4IEntitySelector.isEntityApplicable(entity)))
            {
                par3List.add(entity);
            }
        }
    }

    /**
     * Returns true if this Chunk needs to be saved
     */
    public boolean needsSaving(boolean par1)
    {
        if (par1)
        {
            if (this.hasEntities && this.worldObj.getTotalWorldTime() != this.lastSaveTime || this.isModified)
            {
                return true;
            }
        }
        else if (this.hasEntities && this.worldObj.getTotalWorldTime() >= this.lastSaveTime + 600L)
        {
            return true;
        }

        return this.isModified;
    }

    public Random getRandomWithSeed(long par1)
    {
        return new Random(this.worldObj.getSeed() + (long)(this.xPosition * this.xPosition * 4987142) + (long)(this.xPosition * 5947611) + (long)(this.yPosition * this.yPosition * 345005437) + (long)(this.yPosition * 20934547) + (long)(this.zPosition * this.zPosition * 4392871L) + (long)(this.zPosition * 389711) ^ par1);
    }

    public boolean isEmpty()
    {
        return false;
    }

    public void populateChunk(IUChunkProvider cp1, IUChunkProvider cp2, int par3, int par4, int par5)
    {//TODO: fined a better way!
        if (!this.isTerrainPopulated && cp1.chunkExists(par3 + 1, par4 + 1) && cp1.chunkExists(par3, par4 + 1) && cp1.chunkExists(par3 + 1, par4))
        {
            cp1.populate(cp2, par3, par4);
        }

        if (cp1.chunkExists(par3 - 1, par4) && !cp1.provideChunk(par3 - 1, par4).isTerrainPopulated && cp1.chunkExists(par3 - 1, par4 + 1) && cp1.chunkExists(par3, par4 + 1) && cp1.chunkExists(par3 - 1, par4 + 1))
        {
            cp1.populate(cp2, par3 - 1, par4);
        }

        if (cp1.chunkExists(par3, par4 - 1) && !cp1.provideChunk(par3, par4 - 1).isTerrainPopulated && cp1.chunkExists(par3 + 1, par4 - 1) && cp1.chunkExists(par3 + 1, par4 - 1) && cp1.chunkExists(par3 + 1, par4))
        {
            cp1.populate(cp2, par3, par4 - 1);
        }

        if (cp1.chunkExists(par3 - 1, par4 - 1) && !cp1.provideChunk(par3 - 1, par4 - 1).isTerrainPopulated && cp1.chunkExists(par3, par4 - 1) && cp1.chunkExists(par3 - 1, par4))
        {
            cp1.populate(cp2, par3 - 1, par4 - 1);
        }
    }

    public boolean func_150802_k()
    {
        return this.field_150815_m && this.isTerrainPopulated && this.isLightPopulated;
    }

    /**
     * Gets a ChunkCoordIntPair representing the Chunk's position.
     */
    public UChunkCoordIntPair getChunkCoordIntPair()
    {
        return new UChunkCoordIntPair(this.xPosition, this.yPosition, this.zPosition);
    }

    /**
     * Returns whether the ExtendedBlockStorages containing levels (in blocks) from arg 1 to arg 2 are fully empty
     * (true) or not (false).
     */
    public boolean getAreLevelsEmpty(int minY, int maxY)
    {
        if (minY < 0)
        {
            minY = 0;
        }

        if (maxY >= 64)
        {
            maxY = 63;
        }
        
        minY = minY >> 4;
        maxY = maxY >> 4;
        
        int i = maxY << 4;
        for (int k = minY << 4; k <= i; k++)
        {
        	ExtendedBlockStorage extendedblockstorage = this.storageArrays[k];

        	if (extendedblockstorage != null && !extendedblockstorage.isEmpty())
        	{
        		return false;
        	}
        }

        return true;
    }

    public void setStorageArrays(ExtendedBlockStorage[] par1ArrayOfExtendedBlockStorage)
    {
        this.storageArrays = par1ArrayOfExtendedBlockStorage;
    }

    /**
     * Initialise this chunk with new binary data
     */
    @SideOnly(Side.CLIENT)
    public void fillChunk(byte[] par1ArrayOfByte, int par2, int par3, boolean par4)
    {
        Iterator iterator = chunkTileEntityMap.values().iterator();
        while(iterator.hasNext())
        {
            TileEntity tileEntity = (TileEntity)iterator.next();
            tileEntity.updateContainingBlockInfo();
            tileEntity.getBlockMetadata();
            tileEntity.getBlockType();
        }

        int k = 0;
        boolean flag1 = !this.worldObj.provider.hasNoSky;
        int l;

        for (l = 0; l < this.storageArrays.length; ++l)
        {
            if ((par2 & 1 << l) != 0)
            {
                if (this.storageArrays[l] == null)
                {
                    this.storageArrays[l] = new ExtendedBlockStorage(l << 4, flag1);
                }

                byte[] abyte1 = this.storageArrays[l].getBlockLSBArray();
                System.arraycopy(par1ArrayOfByte, k, abyte1, 0, abyte1.length);
                k += abyte1.length;
            }
            else if (par4 && this.storageArrays[l] != null)
            {
                this.storageArrays[l] = null;
            }
        }

        NibbleArray nibblearray;

        for (l = 0; l < this.storageArrays.length; ++l)
        {
            if ((par2 & 1 << l) != 0 && this.storageArrays[l] != null)
            {
                nibblearray = this.storageArrays[l].getMetadataArray();
                System.arraycopy(par1ArrayOfByte, k, nibblearray.data, 0, nibblearray.data.length);
                k += nibblearray.data.length;
            }
        }

        for (l = 0; l < this.storageArrays.length; ++l)
        {
            if ((par2 & 1 << l) != 0 && this.storageArrays[l] != null)
            {
                nibblearray = this.storageArrays[l].getBlocklightArray();
                System.arraycopy(par1ArrayOfByte, k, nibblearray.data, 0, nibblearray.data.length);
                k += nibblearray.data.length;
            }
        }

        if (flag1)
        {
            for (l = 0; l < this.storageArrays.length; ++l)
            {
                if ((par2 & 1 << l) != 0 && this.storageArrays[l] != null)
                {
                    nibblearray = this.storageArrays[l].getSkylightArray();
                    System.arraycopy(par1ArrayOfByte, k, nibblearray.data, 0, nibblearray.data.length);
                    k += nibblearray.data.length;
                }
            }
        }

        for (l = 0; l < this.storageArrays.length; ++l)
        {
            if ((par3 & 1 << l) != 0)
            {
                if (this.storageArrays[l] == null)
                {
                    k += 2048;
                }
                else
                {
                    nibblearray = this.storageArrays[l].getBlockMSBArray();

                    if (nibblearray == null)
                    {
                        nibblearray = this.storageArrays[l].createBlockMSBArray();
                    }

                    System.arraycopy(par1ArrayOfByte, k, nibblearray.data, 0, nibblearray.data.length);
                    k += nibblearray.data.length;
                }
            }
            else if (par4 && this.storageArrays[l] != null && this.storageArrays[l].getBlockMSBArray() != null)
            {
                this.storageArrays[l].clearMSBArray();
            }
        }

        //TODO: move biomes to 2dchunk
        /*if (par4)
        {
            System.arraycopy(par1ArrayOfByte, k, this.blockBiomeArray, 0, this.blockBiomeArray.length);
            int i1 = k + this.blockBiomeArray.length;
        }*/

        for (l = 0; l < this.storageArrays.length; ++l)
        {
            if (this.storageArrays[l] != null && (par2 & 1 << l) != 0)
            {
                this.storageArrays[l].removeInvalidBlocks();
            }
        }

        this.isLightPopulated = true;
        this.isTerrainPopulated = true;
        //this.generateHeightMap();
        List<TileEntity> invalidList = new ArrayList<TileEntity>();
        iterator = this.chunkTileEntityMap.values().iterator();

        while (iterator.hasNext())
        {
            TileEntity tileentity = (TileEntity)iterator.next();
            int x = tileentity.xCoord & 63;
            int y = tileentity.yCoord & 63;
            int z = tileentity.zCoord & 63;
            Block block = tileentity.getBlockType();
            if (block != getBlock(x, y, z) || tileentity.blockMetadata != this.getBlockMetadata(x, y, z))
            {
                invalidList.add(tileentity);
            }
            tileentity.updateContainingBlockInfo();
        }

        for (TileEntity te : invalidList)
        {
            te.invalidate();
        }
    }

    //TODO: move to UChunk2D
    /*public BiomeGenBase getBiomeGenForWorldCoords(int par1, int par2, WorldChunkManager par3WorldChunkManager)
    {
        int k = this.blockBiomeArray[par2 << 4 | par1] & 255;

        if (k == 255)
        {
            BiomeGenBase biomegenbase = par3WorldChunkManager.getBiomeGenAt((this.xPosition << 4) + par1, (this.zPosition << 4) + par2);
            k = biomegenbase.biomeID;
            this.blockBiomeArray[par2 << 4 | par1] = (byte)(k & 255);
        }
        BiomeGenBase bgb = BiomeGenBase.func_150568_d(k);
        return bgb == null ? BiomeGenBase.plains : bgb;
    }*/

    /**
     * Returns an array containing a 16x16 mapping on the X/Z of block positions in this Chunk to biome IDs.
     */
    //TODO: move to UChunk2D
    /*public byte[] getBiomeArray()
    {
        return this.blockBiomeArray;
    }*/

    //TODO: move to UChunk2D
    /*public void setBiomeArray(byte[] par1ArrayOfByte)
    {
        this.blockBiomeArray = par1ArrayOfByte;
    }*/

    /**
     * Resets the relight check index to 0 for this Chunk.
     */
    public void resetRelightChecks()
    {
        this.queuedLightChecks = 0;
    }

    /**
     * Retrieves the tile entity, WITHOUT creating it.
     * Good for checking if it exists.
     * 
     * @param x
     * @param y
     * @param z
     * @return The tile entity at the specified location, if it exists and is valid.
     */
    public TileEntity getTileEntityUnsafe(int x, int y, int z)
    {
        ChunkPosition chunkposition = new ChunkPosition(x, y, z);
        TileEntity tileentity = (TileEntity)this.chunkTileEntityMap.get(chunkposition);

        if (tileentity != null && tileentity.isInvalid())
        {
            chunkTileEntityMap.remove(chunkposition);
            tileentity = null;
        }

        return tileentity;
    }

    /**
     * Removes the tile entity at the specified position, only if it's
     * marked as invalid.
     * 
     * @param x
     * @param y
     * @param z
     */
    public void removeInvalidTileEntity(int x, int y, int z)
    {
        ChunkPosition position = new ChunkPosition(x, y, z);
        if (isChunkLoaded)
        {
            TileEntity entity = (TileEntity)chunkTileEntityMap.get(position);
            if (entity != null && entity.isInvalid())
            {
                chunkTileEntityMap.remove(position);
            }
        }
    }
}
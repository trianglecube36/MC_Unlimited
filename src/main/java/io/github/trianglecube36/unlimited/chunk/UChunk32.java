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

public class UChunk32
{
    private static final Logger field_150817_t = LogManager.getLogger();
    public static boolean isLit;
    /**
     * INDEXING BIT ORDER: yzx
     */
    private ExtendedBlockStorage[] storageArrays; // now 2x2x2 array

    public boolean isChunkLoaded;
    public World worldObj;

    public final int xPosition;
    public final int yPosition;
    public final int zPosition;

    private boolean isGapLightingUpdated;
    public Map chunkTileEntityMap;

    public List entityLists;
    public boolean isTerrainPopulated;
    
    //was field_150814_l
    public boolean isLightPopulated;
    public boolean field_150815_m;
    public boolean isModified;
    public boolean hasEntities;
    public long lastSaveTime;
    /**
     * Updates to this chunk will not be sent to clients if this is false. This field is set to true the first time the
     * chunk is sent to a client, and never set to false.
     */
    public boolean sendUpdates;
    public long inhabitedTime;

    public UChunk32(World par1World, int x, int y, int z)
    {
        this.storageArrays = new ExtendedBlockStorage[8];
        this.chunkTileEntityMap = new HashMap();
        this.worldObj = par1World;
        this.xPosition = x;
        this.yPosition = y;
        this.zPosition = z;

        this.entityLists = new ArrayList();
    }

    public UChunk32(World w, Block[] blockarray, int x, int y, int z)
    {
    	this(w, x, y, z);
    	boolean flag = !w.provider.hasNoSky;

        for (int bx = 0; bx < 32; ++bx)
        {
            for (int bz = 0; bz < 32; ++bz)
            {
                for (int by = 0; by < 32; ++by)
                {
                    int bIndex = (bx << 10) | (bz << 5) | by;
                    Block block = blockarray[bIndex];

                    if (block != null && block != Blocks.air)
                    {
                        int index = (bz >> 4) << 2 | (by >> 4) << 1 | bx >> 4;

                        if (this.storageArrays[index] == null)
                        {
                            this.storageArrays[index] = new ExtendedBlockStorage(0, flag);
                        }

                        this.storageArrays[index].func_150818_a(bx & 15, by & 15, bz & 15, block);
                    }
                }
            }
        }
    }

    public UChunk32(World w, Block[] blockarray, byte[] dataarray, int x, int y, int z)
    {
        this(w, x, y, z);
        boolean flag = !w.provider.hasNoSky;

        for (int bx = 0; bx < 32; ++bx)
        {
            for (int bz = 0; bz < 32; ++bz)
            {
                for (int by = 0; by < 32; ++by)
                {
                    int bIndex = (bx << 10) | (bz << 5) | by;
                    Block block = blockarray[bIndex];

                    if (block != null && block != Blocks.air)
                    {
                        int index = (bz >> 4) << 2 | (by >> 4) << 1 | bx >> 4;

                        if (this.storageArrays[index] == null)
                        {
                            this.storageArrays[index] = new ExtendedBlockStorage(0, flag);
                        }

                        this.storageArrays[index].func_150818_a(bx & 15, by & 15, bz & 15, block);
                        this.storageArrays[index].setExtBlockMetadata(bx & 15, by & 15, bz & 15, dataarray[bIndex]);
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

    public int func_150808_b(int px, int py, int pz)
    {
        int x = (xPosition << 5) + px;
        int y = (yPosition << 5) + py;
        int z = (zPosition << 5) + pz;
        return this.getBlock(px, py, pz).getLightOpacity(worldObj, x, y, z);
    }

    public Block getBlock(final int x, final int y, final int z)
    {
        Block block = Blocks.air;
        ExtendedBlockStorage extendedblockstorage = this.storageArrays[(z >> 4) << 2 | (y >> 4) << 1 | x >> 4];

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
        ExtendedBlockStorage extendedblockstorage = this.storageArrays[(z >> 4) << 2 | (y >> 4) << 1 | x >> 4];
        return extendedblockstorage != null ? extendedblockstorage.getExtBlockMetadata(x & 15, y & 15, z & 15) : 0;
    }

    public boolean setBlockAndMetadata(int x, int y, int z, Block block, int data)
    {//TODO: fix stuff
     //TODO: link to new lighting system
        int i1 = z << 4 | x;

        /*if (y >= this.precipitationHeightMap[i1] - 1)
        {
            this.precipitationHeightMap[i1] = -999;
        }

        int j1 = this.heightMap[i1];*/
        Block block1 = this.getBlock(x, y, z);
        int data1 = this.getBlockMetadata(x, y, z);

        if (block1 == block && data1 == data)
        {
            return false;
        }
        else
        {
        	int index = (z >> 4) << 2 | (y >> 4) << 1 | x >> 4;
            ExtendedBlockStorage extendedblockstorage = this.storageArrays[index];
            //boolean flag = false;

            if (extendedblockstorage == null)
            {
                if (block == Blocks.air)
                {
                    return false;
                }

                extendedblockstorage = this.storageArrays[index] = new ExtendedBlockStorage(0, !this.worldObj.provider.hasNoSky);
                //flag = y >= j1;
            }

            int wx = (this.xPosition << 5) + x;
            int wy = (this.yPosition << 5) + x;
            int wz = (this.zPosition << 5) + z;

            if (!this.worldObj.isRemote)
            {
                block1.onBlockPreDestroy(this.worldObj, wx, wy, wz, data1);
            }

            extendedblockstorage.func_150818_a(x & 15, y & 15, z & 15, block);
            extendedblockstorage.setExtBlockMetadata(x & 15, y & 15, z & 15, data); // Move this above to prevent other mods/tile entites from creating invalid ones for the wrong metadata

            if (!this.worldObj.isRemote)
            {
                block1.breakBlock(this.worldObj, wx, wy, wz, block1, data1);
            }
            else if (block1.hasTileEntity(data1))
            {
                TileEntity te = this.getTileEntityUnsafe(x & 31, y & 31, z & 31); //note: dont know why check > 64
                if (te != null && te.shouldRefresh(block1, block, data1, data, worldObj, wx, wy, wz))
                {
                    this.worldObj.removeTileEntity(wx, wy, wz);
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
                    block.onBlockAdded(this.worldObj, wx, wy, wz);
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
        ExtendedBlockStorage blockarray = this.storageArrays[(z >> 4) << 2 | (y >> 4) << 1 | x >> 4];

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
    public int getSavedLightValue(EnumSkyBlock type, int x, int y, int z)
    {
        ExtendedBlockStorage storage = this.storageArrays[(z >> 4) << 2 | (y >> 4) << 1 | x >> 4];
        if(storage == null){
        	if(type == EnumSkyBlock.Sky){
        		return worldObj.canBlockSeeTheSky(x, y, z) ? type.defaultLightValue : 0;
        	}else{
        		return 0;
        	}
        }else{
        	if(type == EnumSkyBlock.Block){
        		return this.worldObj.provider.hasNoSky ? 0 : storage.getExtSkylightValue(x & 15, y & 15, z & 15);
        	}else{
        		return type == EnumSkyBlock.Block ? storage.getExtBlocklightValue(x & 15, y & 15, z & 15) : type.defaultLightValue;
        	}
        }
        //was
        //return storage == null ? (worldObj.canBlockSeeTheSky(x, y, z) ? type.defaultLightValue : 0) : (type == EnumSkyBlock.Sky ? (this.worldObj.provider.hasNoSky ? 0 : storage.getExtSkylightValue(x & 15, y & 15, z & 15)) : (type == EnumSkyBlock.Block ? storage.getExtBlocklightValue(x & 15, y & 15, z & 15) : type.defaultLightValue));
    }

    /**
     * Sets the light value at the coordinate. If enumskyblock is set to sky it sets it in the skylightmap and if its a
     * block then into the blocklightmap. Args enumSkyBlock, x, y, z, lightValue
     */
    public void setLightValue(EnumSkyBlock par1EnumSkyBlock, int x, int y, int z, int value)
    {
        ExtendedBlockStorage extendedblockstorage = this.storageArrays[(z >> 4) << 2 | (y >> 4) << 1 | x >> 4];

        if (extendedblockstorage == null)
        {
            extendedblockstorage = this.storageArrays[(z >> 4) << 2 | (y >> 4) << 1 | x >> 4] = new ExtendedBlockStorage(0, !this.worldObj.provider.hasNoSky);
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
        ExtendedBlockStorage extendedblockstorage = this.storageArrays[(z >> 4) << 2 | (y >> 4) << 1 | x >> 4];

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
        int ex = MathHelper.floor_double(par1Entity.posX / 32.0D);
        int ey = MathHelper.floor_double(par1Entity.posY / 32.0D);
        int ez = MathHelper.floor_double(par1Entity.posZ / 32.0D);

        if (ex != this.xPosition || ey != this.yPosition || ez != this.zPosition)
        {
            field_150817_t.error("Wrong location! " + par1Entity);
            Thread.dumpStack();
        }
        //TODO: do somthing?
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
            this.worldObj.setTileEntity((this.xPosition << 5) + x, (this.yPosition << 5) + y, (this.zPosition << 5) + z, tileentity);
        }

        return tileentity;
    }

    /**
     * chunk & world add tileentity
     */
    public void func_150813_a(TileEntity tilee)
    {
        int i = tilee.xCoord - (this.xPosition << 5);
        int j = tilee.yCoord - (this.yPosition << 5);
        int k = tilee.zCoord - (this.zPosition << 5);
        this.func_150812_a(i, j, k, tilee);

        if (this.isChunkLoaded)
        {
            this.worldObj.addTileEntity(tilee);
        }
    }

    /**
     * chunk add tileentity
     */
    public void func_150812_a(int x, int y, int z, TileEntity tilee)
    {
        ChunkPosition chunkposition = new ChunkPosition(x, y, z);
        tilee.setWorldObj(this.worldObj);
        tilee.xCoord = (this.xPosition << 5) + x;
        tilee.yCoord = (this.yPosition << 5) + y;
        tilee.zCoord = (this.zPosition << 5) + z;

        int metadata = getBlockMetadata(x, y, z);
        if (this.getBlock(x, y, z).hasTileEntity(metadata))
        {
            if (this.chunkTileEntityMap.containsKey(chunkposition))
            {
                ((TileEntity)this.chunkTileEntityMap.get(chunkposition)).invalidate();
            }

            tilee.validate();
            this.chunkTileEntityMap.put(chunkposition, tilee);
        }
    }

    public void removeTileEntity(int x, int y, int z)
    {
        ChunkPosition chunkposition = new ChunkPosition(x, y, z);

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
    {//TODO: this does not work well
        if (minY < 0)
        {
            minY = 0;
        }

        if (maxY >= 32)
        {
            maxY = 31;
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

    public void setStorageArrays(ExtendedBlockStorage[] array)
    {
        this.storageArrays = array;
    }

    /**
     * Initialise this chunk with new binary data
     */
    @SideOnly(Side.CLIENT)
    public void fillChunk(byte[] par1ArrayOfByte, int hasstorageBits, int hasmsbBits, boolean par4)
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
            if ((hasstorageBits & 1 << l) != 0)
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
            if ((hasstorageBits & 1 << l) != 0 && this.storageArrays[l] != null)
            {
                nibblearray = this.storageArrays[l].getMetadataArray();
                System.arraycopy(par1ArrayOfByte, k, nibblearray.data, 0, nibblearray.data.length);
                k += nibblearray.data.length;
            }
        }

        for (l = 0; l < this.storageArrays.length; ++l)
        {
            if ((hasstorageBits & 1 << l) != 0 && this.storageArrays[l] != null)
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
                if ((hasstorageBits & 1 << l) != 0 && this.storageArrays[l] != null)
                {
                    nibblearray = this.storageArrays[l].getSkylightArray();
                    System.arraycopy(par1ArrayOfByte, k, nibblearray.data, 0, nibblearray.data.length);
                    k += nibblearray.data.length;
                }
            }
        }

        for (l = 0; l < this.storageArrays.length; ++l)
        {
            if ((hasmsbBits & 1 << l) != 0)
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
            if (this.storageArrays[l] != null && (hasstorageBits & 1 << l) != 0)
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
            int x = tileentity.xCoord & 31;
            int y = tileentity.yCoord & 31;
            int z = tileentity.zCoord & 31;
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
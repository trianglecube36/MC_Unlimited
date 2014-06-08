package io.github.trianglecube36.unlimited.chunk;

import io.github.trianglecube36.unlimited.event.UChunk2DDataEvent;
import io.github.trianglecube36.unlimited.event.UChunkDataEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.chunk.storage.RegionFileCache;
import net.minecraft.world.storage.IThreadedFileIO;
import net.minecraft.world.storage.ThreadedFileIOBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkDataEvent;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLLog;

public class ChunkIO implements IThreadedFileIO, IUChunkLoader {
    private static final Logger logger = LogManager.getLogger();
    private List chunksToRemove = new ArrayList();
    private List chunk2DsToRemove = new ArrayList();
    private Set pendingUChunk32sCoordinates = new HashSet();
    private Set pendingUChunk2DsCoordinates = new HashSet();
    private Object syncLockObject = new Object();
    /**
     * Save directory for chunks using the cool new epic save format
     */
    public final File chunkSaveLocation;

    public ChunkIO(File par1File)
    {
        this.chunkSaveLocation = par1File;
    }

    /**
     * Loads the specified(XZ) chunk into the specified world.
     */
    public UChunk32 loadChunk(World world, int x, int y, int z) throws IOException
    {
        NBTTagCompound nbttagcompound = null;
        UChunkCoordIntPair location = new UChunkCoordIntPair(x, y ,z);
        
        synchronized (this.syncLockObject)
        {
            if (this.pendingUChunk32sCoordinates.contains(location))
            {
                for (int k = 0; k < this.chunksToRemove.size(); ++k)
                {
                    if (((ChunkIO.PendingChunk)this.chunksToRemove.get(k)).chunkCoordinate.equals(location))
                    {
                        nbttagcompound = ((ChunkIO.PendingChunk)this.chunksToRemove.get(k)).nbtTags;
                        break;
                    }
                }
            }
        }

        if (nbttagcompound == null)
        {
            DataInputStream datainputstream = URegionFileCache.getChunkInputStream(this.chunkSaveLocation, x, y, z);

            if (datainputstream == null)
            {
                return null;
            }

            nbttagcompound = CompressedStreamTools.read(datainputstream);
        }

        return this.checkedReadChunkFromNBT(world, x, y, z, nbttagcompound);
    }
    
    public UChunk2D loadChunk2D(World world, int x, int z) throws IOException
    {
        NBTTagCompound nbttagcompound = null;
        UChunkCoordIntPair location = new UChunkCoordIntPair(x, 0 ,z);
        
        synchronized (this.syncLockObject)
        {
            if (this.pendingUChunk2DsCoordinates.contains(location))
            {
                for (int k = 0; k < this.chunk2DsToRemove.size(); ++k)
                {
                    if (((ChunkIO.PendingChunk)this.chunk2DsToRemove.get(k)).chunkCoordinate.equals(location))
                    {
                        nbttagcompound = ((ChunkIO.PendingChunk)this.chunk2DsToRemove.get(k)).nbtTags;
                        break;
                    }
                }
            }
        }

        if (nbttagcompound == null)
        {
            DataInputStream datainputstream = URegionFileCache.getChunk2DInputStream(this.chunkSaveLocation, x, z);

            if (datainputstream == null)
            {
                return null;
            }

            nbttagcompound = CompressedStreamTools.read(datainputstream);
        }

        return this.checkedReadChunk2DFromNBT(world, x, z, nbttagcompound);
    }

    /**
     * Wraps readChunkFromNBT. Checks the coordinates and several NBT tags.
     */
    protected UChunk32 checkedReadChunkFromNBT(World world, int x, int y, int z, NBTTagCompound tag)
    {
        if (!tag.hasKey("Level", 10))
        {
            logger.error("Chunk file at " + x + "," + y + "," + z + " is missing level data, skipping");
            return null;
        }
        else if (!tag.getCompoundTag("Level").hasKey("Sections", 9))
        {
            logger.error("Chunk file at " + x + "," + y + "," + z + " is missing block data, skipping");
            return null;
        }
        else
        {
            UChunk32 chunk = this.readChunkFromNBT(world, tag.getCompoundTag("Level"));

            if (!chunk.isAtLocation(x, y, z))
            {
                logger.error("Chunk file at " + x + "," + y + "," + z + " is in the wrong location; relocating. (Expected " + x + "," + y + "," + z + ", got " + chunk.xPosition + ", " + chunk.yPosition + ", " + chunk.zPosition + ")");
                tag.setInteger("xPos", x);
                tag.setInteger("yPos", y);
                tag.setInteger("zPos", z);
                chunk = this.readChunkFromNBT(world, tag.getCompoundTag("Level"));
            }

            MinecraftForge.EVENT_BUS.post(new UChunkDataEvent.Load(chunk, tag));
            return chunk;
        }
    }
    
    protected UChunk2D checkedReadChunk2DFromNBT(World world, int x, int z, NBTTagCompound tag)
    {
        if (!tag.hasKey("Level", 10))
        {
            logger.error("Chunk2D file at " + x + "," + z + " is missing level data, skipping");
            return null;
        }
        else if (!tag.getCompoundTag("Level").hasKey("Sections", 9))
        {
            logger.error("Chunk2D file at " + x + "," + z + " is missing block data, skipping");
            return null;
        }
        else
        {
            UChunk2D chunk = this.readChunk2DFromNBT(world, tag.getCompoundTag("Level"));

            if (!chunk.isAtLocation(x, z))
            {
                logger.error("Chunk2D file at " + x + "," + z + " is in the wrong location; relocating. (Expected " + x + "," + z + ", got " + chunk.xPosition +  ", " + chunk.zPosition + ")");
                tag.setInteger("xPos", x);
                tag.setInteger("zPos", z);
                chunk = this.readChunk2DFromNBT(world, tag.getCompoundTag("Level"));
            }

            MinecraftForge.EVENT_BUS.post(new UChunk2DDataEvent.Load(chunk, tag));
            return chunk;
        }
    }

    public void saveChunk(World world, UChunk32 chunk) throws MinecraftException, IOException
    {
        world.checkSessionLock();

        try
        {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound.setTag("Level", nbttagcompound1);
            this.writeChunkToNBT(chunk, world, nbttagcompound1);
            MinecraftForge.EVENT_BUS.post(new UChunkDataEvent.Save(chunk, nbttagcompound));
            this.addChunkToPending(chunk.getChunkCoordIntPair(), nbttagcompound);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }
    
    public void saveChunk2D(World world, UChunk2D chunk) throws MinecraftException, IOException
    {
        world.checkSessionLock();

        try
        {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound.setTag("Level", nbttagcompound1);
            this.writeChunk2DToNBT(chunk, world, nbttagcompound1);
            MinecraftForge.EVENT_BUS.post(new UChunk2DDataEvent.Save(chunk, nbttagcompound));
            this.addChunkToPending(new UChunkCoordIntPair(chunk.xPosition, 0, chunk.zPosition), nbttagcompound);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    protected void addChunkToPending(UChunkCoordIntPair location, NBTTagCompound tag)
    {
        synchronized (this.syncLockObject)
        {
            if (this.pendingUChunk32sCoordinates.contains(location))
            {
                for (int i = 0; i < this.chunksToRemove.size(); ++i)
                {
                    if (((ChunkIO.PendingChunk)this.chunksToRemove.get(i)).chunkCoordinate.equals(location))
                    {
                        this.chunksToRemove.set(i, new ChunkIO.PendingChunk(location, tag));
                        return;
                    }
                }
            }

            this.chunksToRemove.add(new ChunkIO.PendingChunk(location, tag));
            this.pendingUChunk32sCoordinates.add(location);
            ThreadedFileIOBase.threadedIOInstance.queueIO(this);
        }
    }
    
    protected void addChunk2DToPending(UChunkCoordIntPair location, NBTTagCompound tag)
    {
        synchronized (this.syncLockObject)
        {
            if (this.pendingUChunk2DsCoordinates.contains(location))
            {
                for (int i = 0; i < this.chunk2DsToRemove.size(); ++i)
                {
                    if (((ChunkIO.PendingChunk)this.chunk2DsToRemove.get(i)).chunkCoordinate.equals(location))
                    {
                        this.chunk2DsToRemove.set(i, new ChunkIO.PendingChunk(location, tag));
                        return;
                    }
                }
            }

            this.chunk2DsToRemove.add(new ChunkIO.PendingChunk(location, tag));
            this.pendingUChunk2DsCoordinates.add(location);
            ThreadedFileIOBase.threadedIOInstance.queueIO(this);
        }
    }

    /**
     * Returns a boolean stating if the write was unsuccessful.
     */
    public boolean writeNextIO()
    {
        ChunkIO.PendingChunk pendingchunk = null;
        boolean is2d = false;
        Object object = this.syncLockObject; // looks like this does nothing

        synchronized (this.syncLockObject)
        {
            if (this.chunksToRemove.isEmpty())
            {
            	is2d = true;
            	if(this.chunk2DsToRemove.isEmpty()){
            		return false;
            	}else{
            		pendingchunk = (ChunkIO.PendingChunk)this.chunk2DsToRemove.remove(0);
            		this.pendingUChunk2DsCoordinates.remove(pendingchunk.chunkCoordinate);
            	}
            }else{
            	pendingchunk = (ChunkIO.PendingChunk)this.chunksToRemove.remove(0);
            	this.pendingUChunk32sCoordinates.remove(pendingchunk.chunkCoordinate);
            }
        }

        if (pendingchunk != null)
        {
            try
            {
            	if(is2d){
            		this.writeChunk2DNBTTags(pendingchunk);
            	}else{
            		this.writeChunkNBTTags(pendingchunk);
            	}
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }

        return true;
    }

    private void writeChunkNBTTags(ChunkIO.PendingChunk pending) throws IOException
    {
        DataOutputStream dataoutputstream = URegionFileCache.getChunkOutputStream(this.chunkSaveLocation, pending.chunkCoordinate.chunkXPos, pending.chunkCoordinate.chunkYPos, pending.chunkCoordinate.chunkZPos);
        CompressedStreamTools.write(pending.nbtTags, dataoutputstream);
        dataoutputstream.close();
    }
    
    private void writeChunk2DNBTTags(ChunkIO.PendingChunk pending) throws IOException
    {
        DataOutputStream dataoutputstream = URegionFileCache.getChunk2DOutputStream(this.chunkSaveLocation, pending.chunkCoordinate.chunkXPos, pending.chunkCoordinate.chunkZPos);
        CompressedStreamTools.write(pending.nbtTags, dataoutputstream);
        dataoutputstream.close();
    }

    /**
     * Save extra data associated with this Chunk not normally saved during autosave, only during chunk unload.
     * Currently unused.
     */
    public void saveExtraChunkData(World world, UChunk32 chunk) {}

    /**
     * Called every World.tick()
     */
    public void chunkTick() {}

    /**
     * Save extra data not associated with any Chunk.  Not saved during autosave, only during world unload.  Currently
     * unused.
     */
    public void saveExtraData()
    {
        while (this.writeNextIO())
        {
            ;
        }
    }

    /**
     * Writes the Chunk passed as an argument to the NBTTagCompound also passed, using the World argument to retrieve
     * the Chunk's last update time.
     */
    private void writeChunkToNBT(UChunk32 chunk, World world, NBTTagCompound tag)
    {
        tag.setByte("V", (byte)1); //TODO: what is this used for?
        tag.setInteger("xPos", chunk.xPosition);
        tag.setInteger("yPos", chunk.yPosition);
        tag.setInteger("zPos", chunk.zPosition);
        tag.setLong("LastUpdate", world.getTotalWorldTime());
        tag.setBoolean("TerrainPopulated", chunk.isTerrainPopulated);
        tag.setBoolean("LightPopulated", chunk.isLightPopulated);
        tag.setLong("InhabitedTime", chunk.inhabitedTime);
        ExtendedBlockStorage[] blockarrays = chunk.getBlockStorageArray();
        NBTTagList sectionsListNBT = new NBTTagList();
        boolean flag = !world.provider.hasNoSky;
        int i = blockarrays.length;
        NBTTagCompound nbttagcompound1;

        for (int j = 0; j < i; ++j)
        {
            ExtendedBlockStorage extendedblockstorage = blockarrays[j];

            if (extendedblockstorage != null)
            {
                nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Y", (byte) j); //no longer used as y
                nbttagcompound1.setByteArray("Blocks", extendedblockstorage.getBlockLSBArray());

                if (extendedblockstorage.getBlockMSBArray() != null)
                {
                    nbttagcompound1.setByteArray("Add", extendedblockstorage.getBlockMSBArray().data);
                }

                nbttagcompound1.setByteArray("Data", extendedblockstorage.getMetadataArray().data);
                nbttagcompound1.setByteArray("BlockLight", extendedblockstorage.getBlocklightArray().data);

                if (flag)
                {
                    nbttagcompound1.setByteArray("SkyLight", extendedblockstorage.getSkylightArray().data);
                }
                else
                {
                    nbttagcompound1.setByteArray("SkyLight", new byte[extendedblockstorage.getBlocklightArray().data.length]);
                }

                sectionsListNBT.appendTag(nbttagcompound1);
            }
        }

        tag.setTag("Sections", sectionsListNBT);
        chunk.hasEntities = false;
        NBTTagList nbttaglist2 = new NBTTagList();
        Iterator iterator1 = chunk.entityLists.iterator();

        while (iterator1.hasNext())
        {
            Entity entity = (Entity)iterator1.next();
            nbttagcompound1 = new NBTTagCompound();
            try
            {
                if (entity.writeToNBTOptional(nbttagcompound1))
                {
                    chunk.hasEntities = true;
                    nbttaglist2.appendTag(nbttagcompound1);
                }
            }
            catch (Exception e)
            {
                FMLLog.log(Level.ERROR, e,
                    "An Entity type %s has thrown an exception trying to write state. It will not persist. Report this to the mod author",
                    entity.getClass().getName());
            }
        }

        tag.setTag("Entities", nbttaglist2);
        NBTTagList nbttaglist3 = new NBTTagList();
        iterator1 = chunk.chunkTileEntityMap.values().iterator();

        while (iterator1.hasNext())
        {
            TileEntity te = (TileEntity)iterator1.next();
            nbttagcompound1 = new NBTTagCompound();
            try {
            te.writeToNBT(nbttagcompound1);
            nbttaglist3.appendTag(nbttagcompound1);
            }
            catch (Exception e)
            {
                FMLLog.log(Level.ERROR, e,
                        "A TileEntity type %s has throw an exception trying to write state. It will not persist. Report this to the mod author",
                        te.getClass().getName());
            }
        }

        tag.setTag("TileEntities", nbttaglist3);
        List list = world.getPendingBlockUpdates(chunk, false);

        if (list != null)
        {
            long k = world.getTotalWorldTime();
            NBTTagList nbttaglist1 = new NBTTagList();
            Iterator iterator = list.iterator();

            while (iterator.hasNext())
            {
                NextTickListEntry nextticklistentry = (NextTickListEntry)iterator.next();
                NBTTagCompound nbttagcompound2 = new NBTTagCompound();
                nbttagcompound2.setInteger("i", Block.getIdFromBlock(nextticklistentry.func_151351_a()));
                nbttagcompound2.setInteger("x", nextticklistentry.xCoord);
                nbttagcompound2.setInteger("y", nextticklistentry.yCoord);
                nbttagcompound2.setInteger("z", nextticklistentry.zCoord);
                nbttagcompound2.setInteger("t", (int)(nextticklistentry.scheduledTime - k));
                nbttagcompound2.setInteger("p", nextticklistentry.priority);
                nbttaglist1.appendTag(nbttagcompound2);
            }

            tag.setTag("TileTicks", nbttaglist1);
        }
    }
    
    private UChunk32 readChunkFromNBT(World par1World, NBTTagCompound tag)
    {
        int x = tag.getInteger("xPos");
        int y = tag.getInteger("yPos");
        int z = tag.getInteger("zPos");
        UChunk32 chunk = new UChunk32(par1World, x, y ,z);
        chunk.isTerrainPopulated = tag.getBoolean("TerrainPopulated");
        chunk.isLightPopulated = tag.getBoolean("LightPopulated");
        chunk.inhabitedTime = tag.getLong("InhabitedTime");
        NBTTagList secTags = tag.getTagList("Sections", 10);
        byte b0 = 64;
        ExtendedBlockStorage[] aextendedblockstorage = new ExtendedBlockStorage[b0];
        boolean flag = !par1World.provider.hasNoSky;

        for (int k = 0; k < secTags.tagCount(); ++k)
        {
            NBTTagCompound nbttagcompound1 = secTags.getCompoundTagAt(k);
            byte index = nbttagcompound1.getByte("Y");
            
            byte[] blocks = nbttagcompound1.getByteArray("Blocks");
            NibbleArray data = new NibbleArray(nbttagcompound1.getByteArray("Data"), 4);
            NibbleArray blockLight = new NibbleArray(nbttagcompound1.getByteArray("BlockLight"), 4);
            NibbleArray skyLight;
            if (flag)
            {
                skyLight = new NibbleArray(nbttagcompound1.getByteArray("SkyLight"), 4);
            }
            ExtendedBlockStorage extendedblockstorage = new ExtendedBlockStorage(0, flag);
            if (nbttagcompound1.hasKey("Add", 7))
            {
                extendedblockstorage.setBlockMSBArray(new NibbleArray(nbttagcompound1.getByteArray("Add"), 4));
            }
            extendedblockstorage.removeInvalidBlocks();
            aextendedblockstorage[index] = extendedblockstorage;
        }

        chunk.setStorageArrays(aextendedblockstorage);

        NBTTagList nbttaglist1 = tag.getTagList("Entities", 10);

        if (nbttaglist1 != null)
        {
            for (int l = 0; l < nbttaglist1.tagCount(); ++l)
            {
                NBTTagCompound nbttagcompound3 = nbttaglist1.getCompoundTagAt(l);
                Entity entity2 = EntityList.createEntityFromNBT(nbttagcompound3, par1World);
                chunk.hasEntities = true;

                if (entity2 != null)
                {
                    chunk.addEntity(entity2);
                    Entity entity = entity2;

                    for (NBTTagCompound nbttagcompound2 = nbttagcompound3; nbttagcompound2.hasKey("Riding", 10); nbttagcompound2 = nbttagcompound2.getCompoundTag("Riding"))
                    {
                        Entity entity1 = EntityList.createEntityFromNBT(nbttagcompound2.getCompoundTag("Riding"), par1World);

                        if (entity1 != null)
                        {
                            chunk.addEntity(entity1);
                            entity.mountEntity(entity1);
                        }

                        entity = entity1;
                    }
                }
            }
        }

        NBTTagList nbttaglist2 = tag.getTagList("TileEntities", 10);

        if (nbttaglist2 != null)
        {
            for (int i1 = 0; i1 < nbttaglist2.tagCount(); ++i1)
            {
                NBTTagCompound nbttagcompound4 = nbttaglist2.getCompoundTagAt(i1);
                TileEntity tileentity = TileEntity.createAndLoadEntity(nbttagcompound4);

                if (tileentity != null)
                {
                    chunk.func_150813_a(tileentity);
                }
            }
        }

        if (tag.hasKey("TileTicks", 9))
        {
            NBTTagList nbttaglist3 = tag.getTagList("TileTicks", 10);

            if (nbttaglist3 != null)
            {
                for (int j1 = 0; j1 < nbttaglist3.tagCount(); ++j1)
                {
                    NBTTagCompound nbttagcompound5 = nbttaglist3.getCompoundTagAt(j1);
                    par1World.func_147446_b(nbttagcompound5.getInteger("x"), nbttagcompound5.getInteger("y"), nbttagcompound5.getInteger("z"), Block.getBlockById(nbttagcompound5.getInteger("i")), nbttagcompound5.getInteger("t"), nbttagcompound5.getInteger("p"));
                }
            }
        }

        return chunk;
    }
    
    private void writeChunk2DToNBT(UChunk2D chunk, World world, NBTTagCompound tag)
    {
        tag.setByte("V", (byte)1); //TODO: what is this used for?
        tag.setInteger("xPos", chunk.xPosition);
        tag.setInteger("yPos", chunk.zPosition);
        tag.setInteger("zPos", chunk.zPosition);
        tag.setLong("LastUpdate", world.getTotalWorldTime());
        tag.setByteArray("PrecipitationMap", chunk.pHeightMap.save());
        tag.setByteArray("HeightMap", chunk.heightMap.save());
        tag.setByteArray("SolidMap", chunk.solidMap.save());
        tag.setByteArray("Biomes", chunk.blockBiomeArray);
    }

    private UChunk2D readChunk2DFromNBT(World world, NBTTagCompound tag)
    {
        int x = tag.getInteger("xPos");
        int z = tag.getInteger("zPos");
        UChunk2D chunk = new UChunk2D(world, x, z);

        chunk.pHeightMap.loadData(tag.getByteArray("PrecipitationMap"));
        chunk.heightMap.loadData(tag.getByteArray("HeightMap"));
        chunk.solidMap.loadData(tag.getByteArray("SolidMap"));
        chunk.blockBiomeArray = tag.getByteArray("Biomes");
        tag.setByteArray("Biomes", chunk.blockBiomeArray);
        return chunk;
    }
    
    static class PendingChunk
    {
        public final UChunkCoordIntPair chunkCoordinate;
        public final NBTTagCompound nbtTags;

        public PendingChunk(UChunkCoordIntPair location, NBTTagCompound par2NBTTagCompound)
        {
            this.chunkCoordinate = location;
            this.nbtTags = par2NBTTagCompound;
        }
    }
}
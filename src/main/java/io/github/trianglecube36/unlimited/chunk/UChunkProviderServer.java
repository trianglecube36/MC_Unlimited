package io.github.trianglecube36.unlimited.chunk;

import io.github.trianglecube36.unlimited.util.HashMap3D;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.LongHashMap;
import net.minecraft.util.ReportedException;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.registry.GameRegistry;

public class UChunkProviderServer implements IUChunkProvider
{
    private static final Logger field_147417_b = LogManager.getLogger();
    /**
     * used by unload100OldestChunks to iterate the loadedChunkHashMap for unload (underlying assumption, first in,
     * first out)
     */
    private Set chunksToUnload = new HashSet();
    private UChunk64 defaultEmptyChunk;
    private IUChunkProvider currentChunkProvider;
    public IUChunkLoader currentChunkLoader;
    /**
     * if this is false, the defaultEmptyChunk will be returned by the provider
     */
    public boolean loadChunkOnProvideRequest = true;
    private HashMap3D loadedChunkHashMap = new HashMap3D();
    private List loadedChunks = new ArrayList();
    private WorldServer worldObj;

    public UChunkProviderServer(WorldServer par1WorldServer, IUChunkLoader par2IChunkLoader, IUChunkProvider par3IChunkProvider)
    {
        this.defaultEmptyChunk = new EmptyUChunk64(par1WorldServer, 0, 0, 0);
        this.worldObj = par1WorldServer;
        this.currentChunkLoader = par2IChunkLoader;
        this.currentChunkProvider = par3IChunkProvider;
    }

    /**
     * Checks to see if a chunk exists at x, y
     */
    public boolean chunkExists(int x, int y, int z)
    {
        return this.loadedChunkHashMap.containsItem(x, y, z);
    }

    /**
     * marks chunk for unload by "unload100OldestChunks"  if there is no spawn point, or if the center of the chunk is
     * outside 200 blocks (x or z) of the spawn
     */
    public void unloadChunksIfNotNearSpawn(int x, int y, int z)
    {
        if (this.worldObj.provider.canRespawnHere() && DimensionManager.shouldLoadSpawn(this.worldObj.provider.dimensionId))
        {
            ChunkCoordinates chunkcoordinates = this.worldObj.getSpawnPoint();
            int k = x * 64 + 32 - chunkcoordinates.posX;
            int m = y * 64 + 32 - chunkcoordinates.posY;
            int l = z * 64 + 32 - chunkcoordinates.posZ;
            short short1 = 128;

            if (k < -short1 || k > short1 || m < -short1 || m > short1 || l < -short1 || l > short1)
            {
                this.chunksToUnload.add(new ChunkCoordinates(x, y, z));
            }
        }
        else
        {
            this.chunksToUnload.add(new ChunkCoordinates(x, y, z));
        }
    }

    /**
     * marks all chunks for unload, ignoring those near the spawn
     */
    public void unloadAllChunks()
    {
        Iterator iterator = this.loadedChunks.iterator();

        while (iterator.hasNext())
        {
            UChunk64 chunk = (UChunk64)iterator.next();
            this.unloadChunksIfNotNearSpawn(chunk.xPosition, chunk.yPosition, chunk.zPosition);
        }
    }

    /**
     * loads or generates the chunk at the chunk location specified
     */
    public UChunk64 loadChunk(int x, int y, int z)
    {
    	ChunkCoordinates k = new ChunkCoordinates(x, y, z);
        this.chunksToUnload.remove(k);
        UChunk64 chunk = (UChunk64)this.loadedChunkHashMap.getValueByKey(x, y, z);

        if (chunk == null)
        {
        	//TODO: add emulation for old type chunk
        	/*
            chunk = ForgeChunkManager.fetchDormantChunk(k, this.worldObj);
            if (chunk == null)
            {
            */
            chunk = this.safeLoadChunk(x, y, z);
            /*
        	}
        	*/

            if (chunk == null)
            {
                if (this.currentChunkProvider == null)
                {
                    chunk = this.defaultEmptyChunk;
                }
                else
                {
                    try
                    {
                        chunk = this.currentChunkProvider.provideChunk(x, y, z);
                    }
                    catch (Throwable throwable)
                    {
                        CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception generating new chunk");
                        CrashReportCategory crashreportcategory = crashreport.makeCategory("Chunk to be generated");
                        crashreportcategory.addCrashSection("Location", String.format("%d,%d", new Object[] {Integer.valueOf(x), Integer.valueOf(y)}));
                        crashreportcategory.addCrashSection("Position hash", k);
                        crashreportcategory.addCrashSection("Generator", this.currentChunkProvider.makeString());
                        throw new ReportedException(crashreport);
                    }
                }
            }

            this.loadedChunkHashMap.add(x, y, z, chunk);
            this.loadedChunks.add(chunk);
            chunk.onChunkLoad();
            chunk.populateChunk(this, this, x, y, z);
        }

        return chunk;
    }

    /**
     * Will return back a chunk, if it doesn't exist and its not a MP client it will generates all the blocks for the
     * specified chunk from the map seed and chunk seed
     */
    public UChunk64 provideChunk(int x, int y, int z)
    {
        UChunk64 chunk = (UChunk64)this.loadedChunkHashMap.getValueByKey(x, y, z);
        return chunk == null ? (!this.worldObj.findingSpawnPoint && !this.loadChunkOnProvideRequest ? this.defaultEmptyChunk : this.loadChunk(x, y, z)) : chunk;
    }

    /**
     * used by loadChunk, but catches any exceptions if the load fails.
     */
    private UChunk64 safeLoadChunk(int x, int y, int z)
    {
        if (this.currentChunkLoader == null)
        {
            return null;
        }
        else
        {
            try
            {
                UChunk64 chunk = this.currentChunkLoader.loadChunk(this.worldObj, x, y, z);

                if (chunk != null)
                {
                    chunk.lastSaveTime = this.worldObj.getTotalWorldTime();

                    if (this.currentChunkProvider != null)
                    {
                        this.currentChunkProvider.recreateStructures(x, y, z);
                    }
                }

                return chunk;
            }
            catch (Exception exception)
            {
                field_147417_b.error("Couldn\'t load chunk", exception);
                return null;
            }
        }
    }

    /**
     * used by saveChunks, but catches any exceptions if the save fails.
     */
    private void safeSaveExtraChunkData(UChunk64 par1Chunk)
    {
        if (this.currentChunkLoader != null)
        {
            try
            {
                this.currentChunkLoader.saveExtraChunkData(this.worldObj, par1Chunk);
            }
            catch (Exception exception)
            {
                field_147417_b.error("Couldn\'t save entities", exception);
            }
        }
    }

    /**
     * used by saveChunks, but catches any exceptions if the save fails.
     */
    private void safeSaveChunk(UChunk64 par1Chunk)
    {
        if (this.currentChunkLoader != null)
        {
            try
            {
                par1Chunk.lastSaveTime = this.worldObj.getTotalWorldTime();
                this.currentChunkLoader.saveChunk(this.worldObj, par1Chunk);
            }
            catch (IOException ioexception)
            {
                field_147417_b.error("Couldn\'t save chunk", ioexception);
            }
            catch (MinecraftException minecraftexception)
            {
                field_147417_b.error("Couldn\'t save chunk; already in use by another instance of Minecraft?", minecraftexception);
            }
        }
    }

    /**
     * Populates chunk with ores etc etc
     */
    public void populate(IUChunkProvider par1IChunkProvider, int x, int y, int z)
    {
        UChunk64 chunk = this.provideChunk(x, y, z);

        if (!chunk.isTerrainPopulated)
        {
        	//TODO: new lighting system... some lighting thing
        	/*
            chunk.func_150809_p();
            */

            if (this.currentChunkProvider != null)
            {
                this.currentChunkProvider.populate(par1IChunkProvider, x, y, z);
                /*
                GameRegistry.generateWorld(x, y, worldObj, currentChunkProvider, par1IChunkProvider);
                TODO: fix for emulation
                */
                chunk.setChunkModified();
            }
        }
    }

    /**
     * Two modes of operation: if passed true, save all Chunks in one go.  If passed false, save up to two chunks.
     * Return true if all chunks have been saved.
     */
    public boolean saveChunks(boolean par1, IProgressUpdate par2IProgressUpdate)
    {
        int i = 0;

        for (int j = 0; j < this.loadedChunks.size(); ++j)
        {
            UChunk64 chunk = (UChunk64)this.loadedChunks.get(j);

            if (par1)
            {
                this.safeSaveExtraChunkData(chunk);
            }

            if (chunk.needsSaving(par1))
            {
                this.safeSaveChunk(chunk);
                chunk.isModified = false;
                ++i;

                if (i == 6 && !par1) //was i == 24
                {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Save extra data not associated with any Chunk.  Not saved during autosave, only during world unload.  Currently
     * unimplemented.
     */
    public void saveExtraData()
    {
        if (this.currentChunkLoader != null)
        {
            this.currentChunkLoader.saveExtraData();
        }
    }

    /**
     * Unloads chunks that are marked to be unloaded. This is not guaranteed to unload every such chunk.
     */
    public boolean unloadQueuedChunks()
    {
        if (!this.worldObj.canNotSave)
        {
            for (UChunkCoordIntPair forced : this.worldObj.getPersistentChunks().keySet())
            {
                this.chunksToUnload.remove(new ChunkCoordinates(forced.chunkXPos, forced.chunkYPos, forced.chunkZPos));
            }

            for (int i = 0; i < 100; ++i)
            {
                if (!this.chunksToUnload.isEmpty())
                {
                	ChunkCoordinates loc = (ChunkCoordinates)this.chunksToUnload.iterator().next();
                    UChunk64 chunk = (UChunk64)this.loadedChunkHashMap.getValueByKey(loc.posX, loc.posY, loc.posZ);
                    chunk.onChunkUnload();
                    this.safeSaveChunk(chunk);
                    this.safeSaveExtraChunkData(chunk);
                    this.chunksToUnload.remove(loc);
                    this.loadedChunkHashMap.remove(loc.posX, loc.posY, loc.posZ);
                    this.loadedChunks.remove(chunk);
                    /*
                    TODO: dont know how I am going to get this to work :\
                    ForgeChunkManager.putDormantChunk(ChunkCoordIntPair.chunkXZ2Int(chunk.xPosition, chunk.zPosition), chunk);
                    */
                    if(loadedChunks.size() == 0 && ForgeChunkManager.getPersistentChunksFor(this.worldObj).size() == 0 && !DimensionManager.shouldLoadSpawn(this.worldObj.provider.dimensionId)){
                        DimensionManager.unloadWorld(this.worldObj.provider.dimensionId);
                        return currentChunkProvider.unloadQueuedChunks();
                    }
                }
            }

            if (this.currentChunkLoader != null)
            {
                this.currentChunkLoader.chunkTick();
            }
        }

        return this.currentChunkProvider.unloadQueuedChunks();
    }

    /**
     * Returns if the IChunkProvider supports saving.
     */
    public boolean canSave()
    {
        return !this.worldObj.canNotSave;
    }

    /**
     * Converts the instance data to a readable string.
     */
    public String makeString()
    {
        return "ServerChunkCache: " + this.loadedChunkHashMap.getNumHashElements() + " Drop: " + this.chunksToUnload.size();
    }

    /**
     * Returns a list of creatures of the specified type that can spawn at the given location.
     */
    public List getPossibleCreatures(EnumCreatureType par1EnumCreatureType, int par2, int par3, int par4)
    {
        return this.currentChunkProvider.getPossibleCreatures(par1EnumCreatureType, par2, par3, par4);
    }

    public ChunkPosition func_147416_a(World p_147416_1_, String p_147416_2_, int p_147416_3_, int p_147416_4_, int p_147416_5_)
    {
        return this.currentChunkProvider.func_147416_a(p_147416_1_, p_147416_2_, p_147416_3_, p_147416_4_, p_147416_5_);
    }

    public int getLoadedChunkCount()
    {
        return this.loadedChunkHashMap.getNumHashElements();
    }

    public void recreateStructures(int x, int y, int z) {}
}

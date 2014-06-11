package io.github.trianglecube36.unlimited;

import io.github.trianglecube36.unlimited.chunk.ChunkIO;
import io.github.trianglecube36.unlimited.chunk.IUChunkLoader;
import io.github.trianglecube36.unlimited.chunk.URegionFileCache;

import java.io.File;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.ThreadedFileIOBase;
import net.minecraft.world.storage.WorldInfo;

public class USaveHandler extends SaveHandler implements IUSaveHandler
{
    public USaveHandler(File par1File, String par2Str, boolean par3)
    {
        super(par1File, par2Str, par3);
    }

    /**
     * Returns the chunk loader with the provided world provider
     */
    public IChunkLoader getChunkLoader(WorldProvider par1WorldProvider)
    {
    	throw new RuntimeException("Unlimited: error: a mod tryed to create new instance of old chunk loader");
    }
    
    public IUChunkLoader getUChunkLoader(WorldProvider wp) {
		File file1 = this.getWorldDirectory();
        File file2;

        if (wp.getSaveFolder() != null)
        {
            file2 = new File(file1, wp.getSaveFolder());
            file2.mkdirs();
            return new ChunkIO(file2);
        }
        else
        {
            return new ChunkIO(file1);
        }
	}

    /**
     * Saves the given World Info with the given NBTTagCompound as the Player.
     */
    public void saveWorldInfoWithPlayer(WorldInfo par1WorldInfo, NBTTagCompound par2NBTTagCompound)
    {
        par1WorldInfo.setSaveVersion(-1); // note!!!! new version number... was 19133 (anvil)
        super.saveWorldInfoWithPlayer(par1WorldInfo, par2NBTTagCompound);
    }

    /**
     * Called to flush all changes to disk, waiting for them to complete.
     */
    public void flush()
    {
        try
        {
            ThreadedFileIOBase.threadedIOInstance.waitForFinish();
        }
        catch (InterruptedException interruptedexception)
        {
            interruptedexception.printStackTrace();
        }

        URegionFileCache.clearRegionFileReferences();
    }
}
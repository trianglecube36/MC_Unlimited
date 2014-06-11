package io.github.trianglecube36.unlimited;

import io.github.trianglecube36.unlimited.chunk.IUChunkLoader;

import java.io.File;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

public interface IUSaveHandler extends ISaveHandler
{
    /**
     * Loads and returns the world info
     */
    WorldInfo loadWorldInfo();

    /**
     * Checks the session lock to prevent save collisions
     */
    void checkSessionLock() throws MinecraftException;

    /**
     * Returns the chunk loader with the provided world provider
     */
    IChunkLoader getChunkLoader(WorldProvider var1);
    
    IUChunkLoader getUChunkLoader(WorldProvider var1);

    /**
     * Saves the given World Info with the given NBTTagCompound as the Player.
     */
    void saveWorldInfoWithPlayer(WorldInfo var1, NBTTagCompound var2);

    /**
     * Saves the passed in world info.
     */
    void saveWorldInfo(WorldInfo var1);

    /**
     * returns null if no saveHandler is relevent (eg. SMP)
     */
    IPlayerFileData getSaveHandler();

    /**
     * Called to flush all changes to disk, waiting for them to complete.
     */
    void flush();

    /**
     * Gets the File object corresponding to the base directory of this world.
     */
    File getWorldDirectory();

    /**
     * Gets the file location of the given map
     */
    File getMapFileFromName(String var1);

    /**
     * Returns the name of the directory where world information is saved.
     */
    String getWorldDirectoryName();
}
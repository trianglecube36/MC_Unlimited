package io.github.trianglecube36.unlimited.event;

import io.github.trianglecube36.unlimited.chunk.UChunk2D;
import io.github.trianglecube36.unlimited.chunk.UChunk32;
import net.minecraft.nbt.NBTTagCompound;

public class UChunk2DDataEvent extends UChunk2DEvent
{
    private final NBTTagCompound data;

    public UChunk2DDataEvent(UChunk2D chunk, NBTTagCompound data)
    {
        super(chunk);
        this.data = data;
    }
    
    public NBTTagCompound getData()
    {
        return data;
    }
    
    public static class Load extends UChunk2DDataEvent
    {
        public Load(UChunk2D chunk, NBTTagCompound data)
        {
            super(chunk, data);
        }
    }

    public static class Save extends UChunk2DDataEvent
    {
        public Save(UChunk2D chunk, NBTTagCompound data)
        {
            super(chunk, data);
        }
    }
}
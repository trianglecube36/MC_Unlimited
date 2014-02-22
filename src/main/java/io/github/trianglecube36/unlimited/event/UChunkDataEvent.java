package io.github.trianglecube36.unlimited.event;

import io.github.trianglecube36.unlimited.chunk.UChunk64;
import net.minecraft.nbt.NBTTagCompound;

public class UChunkDataEvent extends UChunkEvent
{
    private final NBTTagCompound data;

    public UChunkDataEvent(UChunk64 chunk, NBTTagCompound data)
    {
        super(chunk);
        this.data = data;
    }
    
    public NBTTagCompound getData()
    {
        return data;
    }
    
    public static class Load extends UChunkDataEvent
    {
        public Load(UChunk64 chunk, NBTTagCompound data)
        {
            super(chunk, data);
        }
    }

    public static class Save extends UChunkDataEvent
    {
        public Save(UChunk64 chunk, NBTTagCompound data)
        {
            super(chunk, data);
        }
    }
}
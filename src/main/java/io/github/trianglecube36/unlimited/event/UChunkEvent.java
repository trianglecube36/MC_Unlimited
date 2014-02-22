package io.github.trianglecube36.unlimited.event;

import io.github.trianglecube36.unlimited.chunk.UChunk64;
import net.minecraftforge.event.world.WorldEvent;

public class UChunkEvent extends WorldEvent
{
    private final UChunk64 chunk;

	public UChunkEvent(UChunk64 chunk)
    {
        super(chunk.worldObj);
        this.chunk = chunk;
    }
    
    public UChunk64 getChunk()
    {
        return chunk;
    }
    
    public static class Load extends UChunkEvent
    {
        public Load(UChunk64 chunk)
        {
            super(chunk);
        }
    }

    public static class Unload extends UChunkEvent
    {
        public Unload(UChunk64 chunk)
        {
            super(chunk);
        }
    }
}
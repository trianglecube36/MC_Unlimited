package io.github.trianglecube36.unlimited.event;

import io.github.trianglecube36.unlimited.chunk.UChunk32;
import net.minecraftforge.event.world.WorldEvent;

public class UChunkEvent extends WorldEvent
{
    private final UChunk32 chunk;

	public UChunkEvent(UChunk32 chunk)
    {
        super(chunk.worldObj);
        this.chunk = chunk;
    }
    
    public UChunk32 getChunk()
    {
        return chunk;
    }
    
    public static class Load extends UChunkEvent
    {
        public Load(UChunk32 chunk)
        {
            super(chunk);
        }
    }

    public static class Unload extends UChunkEvent
    {
        public Unload(UChunk32 chunk)
        {
            super(chunk);
        }
    }
}
package io.github.trianglecube36.unlimited.event;

import io.github.trianglecube36.unlimited.chunk.UChunk2D;
import net.minecraftforge.event.world.WorldEvent;

public class UChunk2DEvent extends WorldEvent
{
    private final UChunk2D chunk;

	public UChunk2DEvent(UChunk2D chunk)
    {
        super(chunk.worldObj);
        this.chunk = chunk;
    }
    
    public UChunk2D getChunk()
    {
        return chunk;
    }
    
    public static class Load extends UChunk2DEvent
    {
        public Load(UChunk2D chunk)
        {
            super(chunk);
        }
    }

    public static class Unload extends UChunk2DEvent
    {
        public Unload(UChunk2D chunk)
        {
            super(chunk);
        }
    }
}
package io.github.trianglecube36.unlimited.chunk;

import net.minecraft.world.ChunkPosition;

public class UChunkCoordIntPair
{
    /**
     * The X position of this Chunk Coordinate Pair
     */
    public final int chunkXPos;
    public final int chunkYPos;
    /**
     * The Z position of this Chunk Coordinate Pair
     */
    public final int chunkZPos;
    

    public UChunkCoordIntPair(int x, int y, int z)
    {
        this.chunkXPos = x;
        this.chunkYPos = y;
        this.chunkZPos = z;
    }

    /**
     * converts a chunk coordinate pair to an integer (suitable for hashing)
     */
    public static long chunkXZ2Int(int par0, int par1)
    {//TODO: fix this
        return (long)par0 & 4294967295L | ((long)par1 & 4294967295L) << 32;
    }

    /**
     * left it here for emulation
     */
    public int hashCode()
    {
        long i = chunkXZ2Int(this.chunkXPos, this.chunkZPos);
        int j = (int)i;
        int k = (int)(i >> 32);
        return j ^ k;
    }
    
    public int[] chunkXYZInt96()
    {
        return new int[]{this.chunkXPos, this.chunkYPos, this.chunkZPos};
    }

    public boolean equals(Object par1Obj)
    {
        UChunkCoordIntPair cc = (UChunkCoordIntPair)par1Obj;
        return cc.chunkXPos == this.chunkXPos && cc.chunkYPos == this.chunkYPos && cc.chunkZPos == this.chunkZPos;
    }

    public int getCenterXPos()
    {
        return (this.chunkXPos << 5) + 16;
    }
    
    public int getCenterYPos()
    {
        return (this.chunkYPos << 5) + 16;
    }

    public int getCenterZPosition()
    {
        return (this.chunkZPos << 5) + 16;
    }

    public ChunkPosition func_151349_a(int p_151349_1_)
    {
        return new ChunkPosition(this.getCenterXPos(), p_151349_1_, this.getCenterZPosition());
    }

    public String toString()
    {
        return "[" + this.chunkXPos + ", " + this.chunkYPos + ", " + this.chunkZPos + "]";
    }
}
package io.github.trianglecube36.unlimited.chunk;

import net.minecraft.world.World;

public class UChunk2D {
	public World worldObj;
	
	private byte[] blockBiomeArray;
	
	public StackArray pHeightMap;
    public StackArray heightMap;
    public int heightMapMin;
    
    public final int xPosition;
    public final int zPosition;
    
    public UChunk2D(int x, int z){
    	xPosition = x;
    	zPosition = z;
    	heightMap = new StackArray();
    	pHeightMap = new StackArray();
    }
}

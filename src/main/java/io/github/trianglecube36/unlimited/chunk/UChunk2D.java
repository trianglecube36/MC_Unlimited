package io.github.trianglecube36.unlimited.chunk;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.World;

public class UChunk2D {
	public World worldObj;
	
	private byte[] blockBiomeArray;
	
	public StackArray pHeightMap;
    public StackArray heightMap;
    public int heightMapMin;
    
    public final int xPosition;
    public final int zPosition;
    
    public List columnChunks;
    
    public UChunk2D(int x, int z){
    	xPosition = x;
    	zPosition = z;
    	heightMap = new StackArray();
    	pHeightMap = new StackArray();
    	columnChunks = new ArrayList();
    }
    
    public boolean shouldUnload(){
    	return columnChunks.isEmpty();
    }
    
    public void chunkLoad(UChunk64 chunk){
    	int overblock = (chunk.yPosition + 1) << 6;
    	int ix;
    	int iy;
    	int iz;
    	for(ix = 0;ix < 64;ix++){
    		for(iz = 0;iz < 64;iz++){
    			if(heightMap.get(ix, iz) < overblock){
    				iy = 63;
    				while(iy >= 0){
    					if(!canBeTop(chunk, ix, iy, iz)){
    						continue;
    					}
    					heightMap.set(ix, iz, iy + (chunk.yPosition << 6));
    					break;
    				}
    			}
    		}
    	}
    }
    
    /**
     * x, y, z are relative to the UChunk64
     */
    public boolean canBeTop(UChunk64 chunk, int x, int y, int z){
    	return chunk.getBlock(x, y, z).getLightOpacity(worldObj, x + (chunk.xPosition << 6), y + (chunk.yPosition << 6), z + (chunk.zPosition << 6)) != 0;
    }
}

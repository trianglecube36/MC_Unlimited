package io.github.trianglecube36.unlimited.chunk;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;

public class UChunk2D {
	public World worldObj;
	
	public byte[] blockBiomeArray;
	
	public StackArray pHeightMap;
    public StackArray heightMap;
    public StackArray solidMap;
    
    public final int xPosition;
    public final int zPosition;
    
    public List columnChunks;

	public boolean isModified;
    
    public UChunk2D(World w, int x, int z){
    	worldObj = w;
    	xPosition = x;
    	zPosition = z;
    	heightMap = new StackArray();
    	pHeightMap = new StackArray();
    	solidMap = new StackArray();
    	columnChunks = new ArrayList();
    }
    
    public boolean shouldUnload(){
    	return columnChunks.isEmpty();
    }
    
    public void chunkLoad(UChunk32 chunk){
    	int overblock = (chunk.yPosition + 1) << 5;
    	int ix;
    	int iy;
    	int iz;
    	for(ix = 0;ix < 32;ix++){
    		for(iz = 0;iz < 32;iz++){
    			if(heightMap.get(ix, iz) < overblock){
    				iy = 31;
    				while(iy >= 0){
    					if(!canBeTop(chunk, ix, iy, iz)){
    						iy--;
    						continue;
    					}
    					heightMap.set(ix, iz, iy + (chunk.yPosition << 5));
    					break;
    				}
    			}
    		}
    	}
    }
    
    /**
     * x, y, z are relative to the UChunk64
     */
    public boolean canBeTop(UChunk32 chunk, int x, int y, int z){
    	return chunk.getBlock(x, y, z).getLightOpacity(worldObj, x + (chunk.xPosition << 5), y + (chunk.yPosition << 5), z + (chunk.zPosition << 5)) != 0;
    }
    
    public int getTopSolidBlock(int x, int z){
    	//TODO: this is not like hieght map or presip map... it is needed for genoration...
    	//DONT WANT TO FINED TREES UNDER GROUND... data MUST sort of come form genorator!
    	return 64;
    }

    public BiomeGenBase getBiomeGenForWorldCoords(int x, int z, WorldChunkManager cm)
    {
        int k = this.blockBiomeArray[z << 5 | x] & 255;

        if (k == 255)
        {
            BiomeGenBase biomegenbase = cm.getBiomeGenAt((this.xPosition << 5) + x, (this.zPosition << 5) + z);
            k = biomegenbase.biomeID;
            this.blockBiomeArray[z << 5 | x] = (byte)(k & 255);
        }
        BiomeGenBase bio = BiomeGenBase.getBiome(k);
        return bio == null ? BiomeGenBase.plains : bio;
    }
    
    public BiomeGenBase[] getBiomeGenArray(BiomeGenBase[] array){
    	int k;
    	BiomeGenBase bio;
    	for(int i = 0; i < 1024;i++){
    		k = this.blockBiomeArray[i] & 255;
    		bio = BiomeGenBase.getBiome(k);
    		array[i] = bio == null ? BiomeGenBase.plains : bio;
    	}
    	
    	return array;
    }

	public boolean canBlockSeeTheSky(int x, int y, int z)
    {
        return y >= this.heightMap.get(x, z);
    }

	public int getHeightValue(int x, int z) {
		return this.heightMap.get(x, z);
	}

	public int getPrecipitationHeight(int x, int z) {
		return this.pHeightMap.get(x, z);
	}
	
	public boolean needsSaving(boolean force){
		return true;
	}

	public boolean isAtLocation(int x, int z) {
		return x == this.xPosition && z == this.zPosition;
	}
}

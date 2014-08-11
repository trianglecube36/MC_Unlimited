package io.github.trianglecube36.unlimited.gen;

import io.github.trianglecube36.unlimited.chunk.IUChunkProvider;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.world.World;

public class UMapGenBase
{
    /** The number of chunks to gen-check in any given direction. */
    protected int range = 6; //was 8 - now 6 for 1.5x bigger possible structures >:) 
    /** The RNG used by the MapGen classes. */
    protected Random rand = new Random();
    /** This world object. */
    protected World worldObj;

    /** chunk generating */
    public void func_151539_a(IUChunkProvider chunkpro, World world, int x, int y, int z, Block[] blockArray)
    {
        int checkA = this.range;
        this.worldObj = world;
        this.rand.setSeed(world.getSeed());
        long xr = this.rand.nextLong();
        long yr = this.rand.nextLong();
        long zr = this.rand.nextLong();

        for (int ix = x - checkA; ix <= x + checkA; ++ix)
        {
        	for (int iy = y - checkA; iy <= y + checkA; ++iy)
        	{
        		for (int iz = z - checkA; iz <= z + checkA; ++iz)
        		{
        			long xxr = (long)ix * xr;
        			long yyr = (long)iy * yr;
        			long zzr = (long)iz * zr;
        			this.rand.setSeed(xxr ^ yyr ^ zzr ^ world.getSeed());
        			this.func_151538_a(world, ix, iy, iz, x, y, z, blockArray);
        		}
            }
        }
    }
    /** chunk generating apply random ness crazy stuff idk */
    protected void func_151538_a(World world, int checkX, int checkY, int checkZ, int cX, int cY, int cZ, Block[] blockArray) {}
}
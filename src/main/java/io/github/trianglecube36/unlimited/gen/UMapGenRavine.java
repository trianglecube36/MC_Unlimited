package io.github.trianglecube36.unlimited.gen;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.MapGenBase;

public class UMapGenRavine extends UMapGenBase
{
    private float[] field_75046_d = new float[1024];

    protected void func_151540_a(long rlong, int cX, int cY, int cZ, Block[] blockArray, double checkX, double checkY, double checkZ, float upar1, float upar2, float upar3, int upar4, int upar5, double upar6)
    {
        Random random = new Random(rlong);
        double mX = (double)(cX * 32 + 16);
        double mY = (double)(cZ * 32 + 16);
        double mZ = (double)(cZ * 32 + 16);
        float f3 = 0.0F;
        float f4 = 0.0F;

        if (upar5 <= 0)
        {
            int j1 = this.range * 32 - 32;
            upar5 = j1 - random.nextInt(j1 / 4);
        }

        boolean flag1 = false;

        if (upar4 == -1)
        {
            upar4 = upar5 / 2;
            flag1 = true;
        }

        float f5 = 1.0F;

        for (int k1 = 0; k1 < 256; ++k1)
        {
            if (k1 == 0 || random.nextInt(3) == 0)
            {
                f5 = 1.0F + random.nextFloat() * random.nextFloat() * 1.0F;
            }

            this.field_75046_d[k1] = f5 * f5;
        }

        for (; upar4 < upar5; ++upar4)
        {
            double d12 = 1.5D + (double)(MathHelper.sin((float)upar4 * (float)Math.PI / (float)upar5) * upar1 * 1.0F);
            double d6 = d12 * upar6;
            d12 *= (double)random.nextFloat() * 0.25D + 0.75D;
            d6 *= (double)random.nextFloat() * 0.25D + 0.75D;
            float f6 = MathHelper.cos(upar3);
            float f7 = MathHelper.sin(upar3);
            checkX += (double)(MathHelper.cos(upar2) * f6);
            checkY += (double)f7;
            checkZ += (double)(MathHelper.sin(upar2) * f6);
            upar3 *= 0.7F;
            upar3 += f4 * 0.05F;
            upar2 += f3 * 0.05F;
            f4 *= 0.8F;
            f3 *= 0.5F;
            f4 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            f3 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;

            if (flag1 || random.nextInt(4) != 0)
            {
                double d7 = checkX - mX;
                double d8 = checkZ - mZ;
                double d9 = (double)(upar5 - upar4);
                double d10 = (double)(upar1 + 2.0F + 16.0F);

                if (d7 * d7 + d8 * d8 - d9 * d9 > d10 * d10)
                {
                    return;
                }

                if (checkX >= mX - 32.0D - d12 * 2.0D && checkY >= mY - 32.0D - d12 * 2.0D && checkZ >= mZ - 32.0D - d12 * 2.0D && checkX <= mX + 32.0D + d12 * 2.0D && checkY <= mY + 32.0D + d12 * 2.0D && checkZ <= mZ + 32.0D + d12 * 2.0D)
                {
                    int minx = MathHelper.floor_double(checkX - d12) - cX * 32 - 1;
                    int maxx = MathHelper.floor_double(checkX + d12) - cX * 32 + 1;
                    int miny = MathHelper.floor_double(checkY - d6 ) - cY * 32 - 1;
                    int maxy = MathHelper.floor_double(checkY + d6 ) - cY * 32 + 1;
                    int minz = MathHelper.floor_double(checkZ - d12) - cZ * 32 - 1;
                    int maxz = MathHelper.floor_double(checkZ + d12) - cZ * 32 + 1;

                    if (minx < 0)
                    {
                        minx = 0;
                    }

                    if (maxx > 32) // was 16
                    {
                        maxx = 32; // was 16
                    }

                    if (miny < 0) // was 1
                    {
                        miny = 0; // was 1
                    }

                    if (maxy > 32) // was 248
                    {
                        maxy = 32; // was 248
                    }

                    if (minz < 0)
                    {
                        minz = 0;
                    }

                    if (maxz > 32) // was 16
                    {
                        maxz = 32; // was 16
                    }

                    boolean flag2 = false;
                    int ix;
                    int j3;

                    for (ix = minx; !flag2 && ix < maxx; ++ix)
                    {
                        for (int iz = minz; !flag2 && iz < maxz; ++iz)
                        {
                            for (int iy = maxy + 1; !flag2 && iy >= miny - 1; --iy)
                            {
                            	j3 = (ix << 10) + (iz << 5) + iy;

                                Block block = blockArray[j3];

                                if (isOceanBlock(blockArray, j3, ix, iy, iz, cX, cY, cZ))
                                {
                                    flag2 = true;
                                }

                                if (iy != miny - 1 && ix != minx && ix != maxx - 1 && iz != minz && iz != maxz - 1)
                                {
                                    iy = miny;
                                } 
                            }
                        }
                    }

                    if (!flag2)
                    {
                        for (ix = minx; ix < maxx; ++ix)
                        {
                            double disX = ((double)(ix + cX * 32) + 0.5D - checkX) / d12;

                            for (j3 = minz; j3 < maxz; ++j3)
                            {
                                double disZ = ((double)(j3 + cZ * 32) + 0.5D - checkZ) / d12;
                                int k3 = (ix << 10) + (j3 << 5) + maxy;
                                boolean flag = false;

                                if (disX * disX + disZ * disZ < 1.0D)
                                {
                                    for (int l3 = maxy - 1; l3 >= miny; --l3)
                                    {
                                        double disY = ((double)(l3 + cY * 32) + 0.5D - checkY) / d6;

                                        if ((disX * disX + disZ * disZ) * (double)this.field_75046_d[l3] + disY * disY / 6.0D < 1.0D)
                                        {
                                            Block block1 = blockArray[k3];

                                            if (isTopBlock(blockArray, k3, ix, l3, j3, cX, cY, cZ))
                                            {
                                                flag = true;
                                            }

                                            digBlock(blockArray, k3, ix, l3, j3, cX, cY, cZ, flag);
                                        }

                                        --k3;
                                    }
                                }
                            }
                        }

                        if (flag1)
                        {
                            break;
                        }
                    }
                }
            }
        }
    }

    protected void func_151538_a(World world, int checkX, int checkY, int checkZ, int locX, int locY, int locZ, Block[] blockArray)
    {
    	if(checkY < 0 || checkY >= 2){
    		return;
    	}
        if (this.rand.nextInt(25) == 0) //was 50
        {
            double d0 = (double)(checkX * 32 + this.rand.nextInt(32));
        	double d1 = (double)(this.rand.nextInt(this.rand.nextInt(40) + 8) + 20);
            double d2 = (double)(checkZ * 32 + this.rand.nextInt(32));
            byte b0 = 1;

            for (int i1 = 0; i1 < b0; ++i1)
            {
                float f = this.rand.nextFloat() * (float)Math.PI * 2.0F;
                float f1 = (this.rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float f2 = (this.rand.nextFloat() * 2.0F + this.rand.nextFloat()) * 2.0F;
                this.func_151540_a(this.rand.nextLong(), locX, locY, locZ, blockArray, d0, d1, d2, f2, f, f1, 0, 0, 3.0D);
            }
        }
    }

    protected boolean isOceanBlock(Block[] data, int index, int x, int y, int z, int chunkX, int chunkY, int chunkZ)
    {
        return data[index] == Blocks.water || data[index] == Blocks.flowing_water;
    }

    //Exception biomes to make sure we generate like vanilla
    private boolean isExceptionBiome(BiomeGenBase biome)
    {
        if (biome == BiomeGenBase.mushroomIsland) return true;
        if (biome == BiomeGenBase.beach) return true;
        if (biome == BiomeGenBase.desert) return true;
        return false;
    }

    //Determine if the block at the specified location is the top block for the biome, we take into account
    //Vanilla bugs to make sure that we generate the map the same way vanilla does.
    private boolean isTopBlock(Block[] data, int index, int x, int y, int z, int chunkX, int chunkY, int chunkZ)
    {
        BiomeGenBase biome = worldObj.getBiomeGenForCoords(x + chunkX * 16, z + chunkZ * 16);
        return (isExceptionBiome(biome) ? data[index] == Blocks.grass : data[index] == biome.topBlock);
    }

    /**
     * Digs out the current block, default implementation removes stone, filler, and top block
     * Sets the block to lava if y is less then 10, and air other wise.
     * If setting to air, it also checks to see if we've broken the surface and if so 
     * tries to make the floor the biome's top block
     * 
     * @param data Block data array
     * @param index Pre-calculated index into block data
     * @param x local X position
     * @param y local Y position
     * @param z local Z position
     * @param chunkX Chunk X position
     * @param chunkZ Chunk Y position
     * @param foundTop True if we've encountered the biome's top block. Ideally if we've broken the surface.
     */
    protected void digBlock(Block[] data, int index, int x, int y, int z, int chunkX, int chunkY, int chunkZ, boolean foundTop)
    {
        BiomeGenBase biome = worldObj.getBiomeGenForCoords(x + chunkX * 32, z + chunkZ * 32);
        Block top    = (isExceptionBiome(biome) ? Blocks.grass : biome.topBlock);
        Block filler = (isExceptionBiome(biome) ? Blocks.dirt  : biome.fillerBlock);
        Block block  = data[index];

        if (block == Blocks.stone || block == filler || block == top)
        {
            if (y < 10)
            {
                data[index] = Blocks.flowing_lava;
            }
            else
            {
                data[index] = null;

                if (foundTop && data[index - 1] == filler)
                {
                    data[index - 1] = top;
                }
            }
        }
    }
}
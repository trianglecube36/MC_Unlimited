package io.github.trianglecube36.unlimited.gen;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.MapGenBase;

public class UMapGenCaves extends UMapGenBase
{

    protected void func_151542_a(long rlong, int cX, int cY, int cZ, Block[] blockArray, double checkX, double checkY, double checkZ)
    {
        this.func_151541_a(rlong, cX, cY, cZ, blockArray, checkX, checkY, checkZ, 1.0F + this.rand.nextFloat() * 6.0F, 0.0F, 0.0F, -1, -1, 0.5D);
    }

    protected void func_151541_a(long rlong, int cX, int cY, int cZ, Block[] blockArray, double checkX, double checkY, double checkZ, float upar1, float upar2, float upar3, int upar4, int upar5, double upar6)
    {
        double mX = (double)(cX * 32 + 16);
        double mY = (double)(cY * 32 + 16);
        double mZ = (double)(cZ * 32 + 16);
        float f3 = 0.0F;
        float f4 = 0.0F;
        Random random = new Random(rlong);

        if (upar5 <= 0)
        {
            int j1 = this.range * 32 - 32;
            upar5 = j1 - random.nextInt(j1 / 4);
        }

        boolean flag2 = false;

        if (upar4 == -1)
        {
            upar4 = upar5 / 2;
            flag2 = true;
        }

        int k1 = random.nextInt(upar5 / 2) + upar5 / 4;

        for (boolean flag = random.nextInt(6) == 0; upar4 < upar5; ++upar4)
        {
            double d6 = 1.5D + (double)(MathHelper.sin((float)upar4 * (float)Math.PI / (float)upar5) * upar1 * 1.0F);
            double d7 = d6 * upar6;
            float f5 = MathHelper.cos(upar3);
            float f6 = MathHelper.sin(upar3);
            checkX += (double)(MathHelper.cos(upar2) * f5);
            checkY += (double)f6;
            checkZ += (double)(MathHelper.sin(upar2) * f5);

            if (flag)
            {
                upar3 *= 0.92F;
            }
            else
            {
                upar3 *= 0.7F;
            }

            upar3 += f4 * 0.1F;
            upar2 += f3 * 0.1F;
            f4 *= 0.9F;
            f3 *= 0.75F;
            f4 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            f3 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;

            if (!flag2 && upar4 == k1 && upar1 > 1.0F && upar5 > 0)
            {
                this.func_151541_a(random.nextLong(), cX, cY, cZ, blockArray, checkX, checkY, checkZ, random.nextFloat() * 0.5F + 0.5F, upar2 - ((float)Math.PI / 2F), upar3 / 3.0F, upar4, upar5, 1.0D);
                this.func_151541_a(random.nextLong(), cX, cY, cZ, blockArray, checkX, checkY, checkZ, random.nextFloat() * 0.5F + 0.5F, upar2 + ((float)Math.PI / 2F), upar3 / 3.0F, upar4, upar5, 1.0D);
                return;
            }

            if (flag2 || random.nextInt(4) != 0)
            {
                double incX = checkX - mX;
                double incY = checkY - mY;
                double incZ = checkZ - mZ;
                double d10 = (double)(upar5 - upar4);
                double d11 = (double)(upar1 + 2.0F + 32.0F); // was (double)(upar1 + 2.0F + 16.0F)

                if (incX * incX + incY * incY + incZ * incZ - d10 * d10 > d11 * d11)
                {
                    return;
                }

                if (checkX >= mX - 32.0D - d6 * 2.0D && checkY >= mY - 32.0D - d6 * 2.0D && checkZ >= mZ - 32.0D - d6 * 2.0D && checkX <= mX + 32.0D + d6 * 2.0D && checkY <= mY + 32.0D + d6 * 2.0D && checkZ <= mZ + 32.0D + d6 * 2.0D) //was 16
                {
                    int minx = MathHelper.floor_double(checkX - d6) - cX * 32 - 1; // was 16
                    int maxx = MathHelper.floor_double(checkX + d6) - cX * 32 + 1; //
                    int miny = MathHelper.floor_double(checkY - d7) - cY * 32 - 1; //
                    int maxy = MathHelper.floor_double(checkY + d7) - cY * 32 + 1; //
                    int minz = MathHelper.floor_double(checkZ - d6) - cZ * 32 - 1; //
                    int maxz = MathHelper.floor_double(checkZ + d6) - cZ * 32 + 1; //

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

                    boolean flag3 = false;
                    int ix;
                    int j3;

                    for (ix = minx; !flag3 && ix < maxx; ++ix)
                    {
                        for (int iz = minz; !flag3 && iz < maxz; ++iz)
                        {
                            for (int iy = maxy + 1; !flag3 && iy >= miny - 1; --iy)
                            {
                                j3 = (ix << 10) + (iz << 5) + iy;

                                Block block = blockArray[j3];

                                if (isOceanBlock(blockArray, j3, ix, iy, iz, cX, cY, cZ))
                                {
                                    flag3 = true;
                                }

                                if (iy != miny - 1 && ix != minx && ix != maxx - 1 && iz != minz && iz != maxz - 1)
                                {
                                    iy = miny;
                                }
                            }
                        }
                    }

                    if (!flag3)
                    {
                        for (ix = minx; ix < maxx; ++ix)
                        {
                            double disX = ((double)(ix + cX * 32) + 0.5D - checkX) / d6;

                            for (j3 = minz; j3 < maxz; ++j3)
                            {
                                double disZ = ((double)(j3 + cZ * 32) + 0.5D - checkZ) / d6;
                                int k3 = (ix << 10) + (j3 << 5) + maxy;
                                boolean flag1 = false;

                                if (disX * disX + disZ * disZ < 1.0D) //TODO: check to see that we dont need Y here
                                {
                                    for (int l3 = maxy - 1; l3 >= miny; --l3)
                                    {
                                        double disY = ((double)(l3 + cY * 32) + 0.5D - checkY) / d7;

                                        if (disY > -0.7D && disX * disX + disY * disY + disZ * disZ < 1.0D)
                                        {
                                            Block block1 = blockArray[k3];

                                            if (isTopBlock(blockArray, k3, ix, l3, j3, cX, cY, cZ))
                                            {
                                                flag1 = true;
                                            }
                                            digBlock(blockArray, k3, ix, l3, j3, cX, cY, cZ, flag1);
                                        }

                                        --k3;
                                    }
                                }
                            }
                        }

                        if (flag2)
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
        int i1 = this.rand.nextInt(this.rand.nextInt(this.rand.nextInt(15) + 1) + 1);

        if (this.rand.nextInt(7) != 0)
        {
            i1 = 0;
        }

        for (int j1 = 0; j1 < i1; ++j1)
        {
            double d0 = (double)(checkX * 32 + this.rand.nextInt(32));
            double d1 = (double)(checkY * 32 + this.rand.nextInt(32));
            double d2 = (double)(checkZ * 32 + this.rand.nextInt(32));
            int k1 = 1;

            if (this.rand.nextInt(4) == 0)
            {
                this.func_151542_a(this.rand.nextLong(), locX, locY, locZ, blockArray, d0, d1, d2);
                k1 += this.rand.nextInt(4);
            }

            for (int l1 = 0; l1 < k1; ++l1)
            {
                float f = this.rand.nextFloat() * (float)Math.PI * 2.0F;
                float f1 = (this.rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float f2 = this.rand.nextFloat() * 2.0F + this.rand.nextFloat();

                if (this.rand.nextInt(10) == 0)
                {
                    f2 *= this.rand.nextFloat() * this.rand.nextFloat() * 3.0F + 1.0F;
                }

                this.func_151541_a(this.rand.nextLong(), locX, locY, locZ, blockArray, d0, d1, d2, f2, f, f1, 0, 0, 1.0D);
            }
        }
    }

    protected boolean isOceanBlock(Block[] data, int index, int x, int y, int z, int chunkX, int chunkY, int chunkZ)
    {
        return data[index] == Blocks.flowing_water || data[index] == Blocks.water;
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
        BiomeGenBase biome = worldObj.getBiomeGenForCoords(x + chunkX * 32, z + chunkZ * 32);
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
            if (((chunkY << 5) + y) < -500) //was 10
            {
                data[index] = Blocks.lava;
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
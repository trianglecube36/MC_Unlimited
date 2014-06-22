package io.github.trianglecube36.unlimited.light;

import io.github.trianglecube36.unlimited.chunk.UChunk2D;
import io.github.trianglecube36.unlimited.chunk.UChunk32;

import net.minecraft.block.Block;
import net.minecraft.util.Facing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

//               1
//              121
//             12321
//            1234321
//           123454321
//          12345654321
//         1234567654321
//        123456787654321
//       12345678987654321
//      123456789A987654321
//     123456789ABA987654321
//    123456789ABCBA987654321
//   123456789ABCDCBA987654321
//  123456789ABCDEDCBA987654321
// 123456789ABCDEFEDCBA987654321
//  123456789ABCDEDCBA987654321
//   123456789ABCDCBA987654321
//    123456789ABCBA987654321
//     123456789ABA987654321
//      123456789A987654321
//       12345678987654321
//        123456787654321
//         1234567654321
//          12345654321
//           123454321
//            1234321
//             12321
//              121
//               1
public class LightingEngine {
	public World world;
	
	/**
     * is a temporary list of blocks and light values used when updating light levels. Holds up to 32x32x32 blocks (the
     * maximum influence of a light source.) Every element is a packed bit value: 0000000000LLLLzzzzzzyyyyyyxxxxxx. The
     * 4-bit L is a light level used when darkening blocks. 6-bit numbers x, y and z represent the block's offset from
     * the original block, plus 32 (i.e. value of 31 would mean a -1 offset
     */
	private int[] lightUpdateBlockList;
	
	public LightingEngine(World w){
		world = w;
		lightUpdateBlockList = new int[32768];
	}
	
	public void populateLight(UChunk32 chunk, UChunk2D c2D){
		
	}
	
	public void rePopulateLight(UChunk32 chunk, UChunk2D c2D){
		if(!world.provider.hasNoSky){
			int topblock = (chunk.yPosition << 5) + 31;
			int skyvalue;
			int hieght;
			int skyDefault = EnumSkyBlock.Sky.defaultLightValue;
			for(int x = 0;x < 32;x++){
				for(int z = 0;z < 32;z++){
					skyvalue = chunk.getSavedLightValue(EnumSkyBlock.Sky, x, 31, z);
					hieght = c2D.heightMap.get(x, z);
					if(hieght < topblock && skyvalue < skyDefault){
						skyColomnGained(chunk, x, z, hieght);
					}else if(hieght > topblock && skyvalue == skyDefault){ //note: not hieght >= topblock
						skyColomnLost(chunk, x, z, hieght);
					}
				}
			}
		}
	}
	
	public void skyColomnLost(UChunk32 chunk, int x, int z, int newh){
		int offy = (chunk.yPosition << 5);
		int base = offy + 30; // note: one less as 31 is checked true
		while(chunk.getSavedLightValue(EnumSkyBlock.Sky, x, base & 5, z) == 15 && base != offy){
			base--;
		}
		int offx = (chunk.xPosition << 5) + x;
		int offz = (chunk.zPosition << 5) + z;
		for(int i = 31 + (chunk.yPosition << 5);i >= base;i--){
			updateLightByType(EnumSkyBlock.Sky, offx, i, offz);
		}
	}
	
	public void skyColomnGained(UChunk32 chunk, int x, int z, int newh){
		int offx = (chunk.xPosition << 5) + x;
		int offz = (chunk.zPosition << 5) + z;
		for(int i = (chunk.yPosition << 5) + 31;i > newh;i--){
			lightWave(offx, i, offz, 15, EnumSkyBlock.Sky);
		}
	}
	
	public void lightWave(int x, int y, int z, int compV, EnumSkyBlock type){
		int saveV = world.getSavedLightValue(type, x, y, z);
        //int compV = this.computeLightValue(x, y, z, type);
		if(compV > saveV)
        {
			world.theProfiler.startSection("lightWave");
            int riAt = 0;
            int riStacking = 0;
            int rdata;
            int rx;
            int ry;
            int rz;
            int value1;
            int value2;
            int disX;
            int disZ;
            int disY;

            this.lightUpdateBlockList[riStacking++] = 133152;

            while (riAt < riStacking)
            {
                rdata = this.lightUpdateBlockList[riAt++];
                rx = (rdata & 63) - 32 + x;
                ry = (rdata >> 6 & 63) - 32 + y;
                rz = (rdata >> 12 & 63) - 32 + z;
                value1 = world.getSavedLightValue(type, rx, ry, rz);
                value2 = this.computeLightValue(rx, ry, rz, type);

                if (value2 != value1)
                {
                    world.setLightValue(type, rx, ry, rz, value2);

                    if (value2 > value1)
                    {
                        disX = Math.abs(rx - x);
                        disY = Math.abs(ry - y);
                        disZ = Math.abs(rz - z);
                        boolean flag = riStacking < this.lightUpdateBlockList.length - 6;

                        if (disX + disY + disZ < 17 && flag)
                        {
                            if (world.getSavedLightValue(type, rx - 1, ry, rz) < value2)
                            {
                                this.lightUpdateBlockList[riStacking++] = rx - 1 - x + 32 + (ry - y + 32 << 6) + (rz - z + 32 << 12);
                            }

                            if (world.getSavedLightValue(type, rx + 1, ry, rz) < value2)
                            {
                                this.lightUpdateBlockList[riStacking++] = rx + 1 - x + 32 + (ry - y + 32 << 6) + (rz - z + 32 << 12);
                            }

                            if (world.getSavedLightValue(type, rx, ry - 1, rz) < value2)
                            {
                                this.lightUpdateBlockList[riStacking++] = rx - x + 32 + (ry - 1 - y + 32 << 6) + (rz - z + 32 << 12);
                            }

                            if (world.getSavedLightValue(type, rx, ry + 1, rz) < value2)
                            {
                                this.lightUpdateBlockList[riStacking++] = rx - x + 32 + (ry + 1 - y + 32 << 6) + (rz - z + 32 << 12);
                            }

                            if (world.getSavedLightValue(type, rx, ry, rz - 1) < value2)
                            {
                                this.lightUpdateBlockList[riStacking++] = rx - x + 32 + (ry - y + 32 << 6) + (rz - 1 - z + 32 << 12);
                            }

                            if (world.getSavedLightValue(type, rx, ry, rz + 1) < value2)
                            {
                                this.lightUpdateBlockList[riStacking++] = rx - x + 32 + (ry - y + 32 << 6) + (rz + 1 - z + 32 << 12);
                            }
                        }
                    }
                }
            }

            world.theProfiler.endSection();
        }
	}
	
    public boolean updateLightByType(EnumSkyBlock type, int x, int y, int z)
    {
        if (!world.doChunksNearChunkExist(x, y, z, 17))
        {
            return false;
        }
        else
        {
            int riAt = 0;
            int riStacking = 0;
            world.theProfiler.startSection("getBrightness");
            int saveV = world.getSavedLightValue(type, x, y, z);
            int compV = this.computeLightValue(x, y, z, type);
            int rdata;
            int rx;
            int ry;
            int rz;
            int value1;
            int value2;
            int disX;
            int disZ;
            int disY;

            if (compV > saveV)
            {
                this.lightUpdateBlockList[riStacking++] = 133152;
            }
            else if (compV < saveV)
            {
                this.lightUpdateBlockList[riStacking++] = 133152 | saveV << 18;

                while (riAt < riStacking)
                {
                    rdata = this.lightUpdateBlockList[riAt++];
                    rx = (rdata & 63) - 32 + x;
                    ry = (rdata >> 6 & 63) - 32 + y;
                    rz = (rdata >> 12 & 63) - 32 + z;
                    value1 = rdata >> 18 & 15;
                    value2 = world.getSavedLightValue(type, rx, ry, rz);

                    if (value2 == value1)
                    {
                        world.setLightValue(type, rx, ry, rz, 0);

                        if (value1 > 0)
                        {
                            disX = MathHelper.abs_int(rx - x);
                            disY = MathHelper.abs_int(ry - y);
                            disZ = MathHelper.abs_int(rz - z);

                            if (disX + disY + disZ < 17)
                            {
                                for (int i = 0; i < 6; ++i)
                                {
                                    int sideX = rx + Facing.offsetsXForSide[i];
                                    int sideY = ry + Facing.offsetsYForSide[i];
                                    int sideZ = rz + Facing.offsetsZForSide[i];
                                    int sub = Math.max(1, world.getBlock(sideX, sideY, sideZ).getLightOpacity(world, sideX, sideY, sideZ));
                                    value2 = world.getSavedLightValue(type, sideX, sideY, sideZ);

                                    if (value2 == value1 - sub && riStacking < this.lightUpdateBlockList.length)
                                    {
                                        this.lightUpdateBlockList[riStacking++] = sideX - x + 32 | sideY - y + 32 << 6 | sideZ - z + 32 << 12 | value1 - sub << 18;
                                    }
                                }
                            }
                        }
                    }
                }

                riAt = 0;
            }

            world.theProfiler.endSection();
            world.theProfiler.startSection("checkedPosition < toCheckCount");

            while (riAt < riStacking)
            {
                rdata = this.lightUpdateBlockList[riAt++];
                rx = (rdata & 63) - 32 + x;
                ry = (rdata >> 6 & 63) - 32 + y;
                rz = (rdata >> 12 & 63) - 32 + z;
                value1 = world.getSavedLightValue(type, rx, ry, rz);
                value2 = this.computeLightValue(rx, ry, rz, type);

                if (value2 != value1)
                {
                    world.setLightValue(type, rx, ry, rz, value2);

                    if (value2 > value1)
                    {
                        disX = Math.abs(rx - x);
                        disY = Math.abs(ry - y);
                        disZ = Math.abs(rz - z);
                        boolean flag = riStacking < this.lightUpdateBlockList.length - 6;

                        if (disX + disY + disZ < 17 && flag)
                        {
                            if (world.getSavedLightValue(type, rx - 1, ry, rz) < value2)
                            {
                                this.lightUpdateBlockList[riStacking++] = rx - 1 - x + 32 + (ry - y + 32 << 6) + (rz - z + 32 << 12);
                            }

                            if (world.getSavedLightValue(type, rx + 1, ry, rz) < value2)
                            {
                                this.lightUpdateBlockList[riStacking++] = rx + 1 - x + 32 + (ry - y + 32 << 6) + (rz - z + 32 << 12);
                            }

                            if (world.getSavedLightValue(type, rx, ry - 1, rz) < value2)
                            {
                                this.lightUpdateBlockList[riStacking++] = rx - x + 32 + (ry - 1 - y + 32 << 6) + (rz - z + 32 << 12);
                            }

                            if (world.getSavedLightValue(type, rx, ry + 1, rz) < value2)
                            {
                                this.lightUpdateBlockList[riStacking++] = rx - x + 32 + (ry + 1 - y + 32 << 6) + (rz - z + 32 << 12);
                            }

                            if (world.getSavedLightValue(type, rx, ry, rz - 1) < value2)
                            {
                                this.lightUpdateBlockList[riStacking++] = rx - x + 32 + (ry - y + 32 << 6) + (rz - 1 - z + 32 << 12);
                            }

                            if (world.getSavedLightValue(type, rx, ry, rz + 1) < value2)
                            {
                                this.lightUpdateBlockList[riStacking++] = rx - x + 32 + (ry - y + 32 << 6) + (rz + 1 - z + 32 << 12);
                            }
                        }
                    }
                }
            }

            world.theProfiler.endSection();
            return true;
        }
    }
    
    private int computeLightValue(int x, int y, int z, EnumSkyBlock type)
    {
        if (type == EnumSkyBlock.Sky && world.canBlockSeeTheSky(x, y, z))
        {
            return 15;
        }
        else
        {
            Block block = world.getBlock(x, y, z);
            int blockLight = block.getLightValue(world, x, y, z);
            int dlv = type == EnumSkyBlock.Sky ? 0 : blockLight; //Default Light Value
            int opacity = block.getLightOpacity(world, x, y, z);

            if (opacity >= 15 && blockLight > 0)
            {
                opacity = 1;
            }

            if (opacity < 1)
            {
                opacity = 1;
            }

            if (opacity >= 15)
            {
                return 0;
            }
            else if (dlv >= 14)
            {
                return dlv;
            }
            else
            {
            	int rx;
            	int ry;
            	int rz;
            	int saveV;
                for (int side = 0; side < 6; ++side)
                {
                    rx = x + Facing.offsetsXForSide[side];
                    ry = y + Facing.offsetsYForSide[side];
                    rz = z + Facing.offsetsZForSide[side];
                    saveV = world.getSavedLightValue(type, rx, ry, rz) - opacity;

                    if (saveV > dlv)
                    {
                        dlv = saveV;
                    }

                    if (dlv >= 14)
                    {
                        return dlv;
                    }
                }

                return dlv;
            }
        }
    }
}

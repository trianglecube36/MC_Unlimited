package io.github.trianglecube36.unlimited;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeGenBase;

public class UTempGenerator {
	public static void generateTarrain(int cx, int cy, int cz, Block[] blockArray, BiomeGenBase[] biomeArray)
    {
		int index;
		int x;
		int z;
		int y;
		for(x = 0;x < 32;x++){
			for(z = 0;z < 32;z++){
				int h = (int) (Math.sin(((cx << 5) | x) / 50.0) * 40.0 + 40.0);
				for(y = 31; y >= 0; --y){
					if(((cy << 5) | y) <= h){
						index = (z << 10) | (z << 5) | y;
						blockArray[index] = Blocks.glowstone;
					}
				}
			}
		}
    }
}

package io.github.trianglecube36.unlimited.gen.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class UMapGenNetherBridge extends UMapGenStructure
{
    private List spawnList = new ArrayList();

    public UMapGenNetherBridge()
    {
        this.spawnList.add(new BiomeGenBase.SpawnListEntry(EntityBlaze.class, 10, 2, 3));
        this.spawnList.add(new BiomeGenBase.SpawnListEntry(EntityPigZombie.class, 5, 4, 4));
        this.spawnList.add(new BiomeGenBase.SpawnListEntry(EntitySkeleton.class, 10, 4, 4));
        this.spawnList.add(new BiomeGenBase.SpawnListEntry(EntityMagmaCube.class, 3, 4, 4));
    }

    public String func_143025_a()
    {
        return "Fortress";
    }

    public List getSpawnList()
    {
        return this.spawnList;
    }

    protected boolean canSpawnStructureAtCoords(int cX, int cY, int cZ)
    {
    	if(cY == 2){ // sould make it at the level thay should gen at
    		int k = cX >> 3; //was 4
    		int l = cZ >> 3; //was 4
        	this.rand.setSeed((long)(k ^ l << 4) ^ this.worldObj.getSeed());
        	this.rand.nextInt();
        	return this.rand.nextInt(3) != 0 ? false : (cX != (k << 3) + 2 + this.rand.nextInt(4) ? false : cZ == (l << 3) + 2 + this.rand.nextInt(4));
    	}
    	return false;
    }

    protected UStructureStart getStructureStart(int cX, int cY, int cZ)
    {
        return new UMapGenNetherBridge.Start(this.worldObj, this.rand, cX, cY, cZ);
    }

    public static class Start extends UStructureStart
        {
            public Start() {}

            public Start(World world, Random r, int cX, int cY, int cZ)
            {
                super(cX, cY, cZ);
                UStructureNetherBridgePieces.Start start = new UStructureNetherBridgePieces.Start(r, (cX << 5) + 2, (cY << 5) + 2, (cZ << 5) + 2); //TODO: y the + 2?! //was Start(r, (cX << 4) + 2, (cZ << 4) + 2);
                this.components.add(start);
                start.buildComponent(start, this.components, r);
                ArrayList arraylist = start.field_74967_d;

                while (!arraylist.isEmpty())
                {
                    int k = r.nextInt(arraylist.size());
                    UStructureComponent structurecomponent = (UStructureComponent)arraylist.remove(k);
                    structurecomponent.buildComponent(start, this.components, r);
                }

                this.updateBoundingBox();
                this.setRandomHeight(world, r, 48, 70);
            }
        }
}
package io.github.trianglecube36.unlimited.event;

import io.github.trianglecube36.unlimited.chunk.IUChunkProvider;
import io.github.trianglecube36.unlimited.event.UDecorateBiomeEvent.Decorate;
import io.github.trianglecube36.unlimited.event.UOreGenEvent.GenerateMinable;
import io.github.trianglecube36.unlimited.event.UPopulateChunkEvent.Populate;
import io.github.trianglecube36.unlimited.gen.UMapGenBase;

import java.util.Random;

import cpw.mods.fml.common.eventhandler.Event.Result;
import net.minecraft.world.World;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.InitMapGenEvent;

public abstract class UTerrainGen
{
	public static UMapGenBase getModdedMapGen(UMapGenBase original, UInitMapGenEvent.EventType type)
    {
        UInitMapGenEvent event = new UInitMapGenEvent(type, original);
        MinecraftForge.TERRAIN_GEN_BUS.post(event);
        return event.newGen;
    }
	
    public static boolean populate(IUChunkProvider chunkProvider, World world, Random rand, int chunkX, int chunkY, int chunkZ, boolean hasVillageGenerated, Populate.EventType type)
    {
        UPopulateChunkEvent.Populate event = new UPopulateChunkEvent.Populate(chunkProvider, world, rand, chunkX, chunkY, chunkZ, hasVillageGenerated, type);
        MinecraftForge.TERRAIN_GEN_BUS.post(event);
        return event.getResult() != Result.DENY;
    }
    
    public static boolean decorate(World world, Random rand, int chunkX, int chunkY, int chunkZ, Decorate.EventType type)
    {
        Decorate event = new Decorate(world, rand, chunkX, chunkY, chunkZ, type);
        MinecraftForge.TERRAIN_GEN_BUS.post(event);
        return event.getResult() != Result.DENY;
    }
    
    public static boolean generateOre(World world, Random rand, WorldGenerator generator, int worldX, int worldY, int worldZ, GenerateMinable.EventType type)
    {
        GenerateMinable event = new GenerateMinable(world, rand, generator, worldX, worldY, worldZ, type);
        MinecraftForge.ORE_GEN_BUS.post(event);
        return event.getResult() != Result.DENY;
    }
}
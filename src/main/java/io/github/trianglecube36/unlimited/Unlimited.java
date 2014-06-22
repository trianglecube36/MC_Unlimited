package io.github.trianglecube36.unlimited;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;

@Mod(modid = Unlimited.MODID, version = Unlimited.VERSION)
public class Unlimited
{
    public static final String MODID = "unlimited";
    public static final String VERSION = "1.7.2-0.1.0";
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	System.out.println("SOME RANDOM TEXT WHEN LOADING!");
    }
}

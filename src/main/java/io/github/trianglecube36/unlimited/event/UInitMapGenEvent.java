package io.github.trianglecube36.unlimited.event;

import io.github.trianglecube36.unlimited.gen.UMapGenBase;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.world.gen.MapGenBase;

public class UInitMapGenEvent extends Event
{
    /** Use CUSTOM to filter custom event types
     */
    public static enum EventType { CAVE, MINESHAFT, NETHER_BRIDGE, NETHER_CAVE, RAVINE, SCATTERED_FEATURE, STRONGHOLD, VILLAGE, CUSTOM }
    
    public final EventType type;
    public final UMapGenBase originalGen;
    public UMapGenBase newGen;
    
    UInitMapGenEvent(EventType type, UMapGenBase original)
    {
        this.type = type;
        this.originalGen = original;
        this.newGen = original;
    }
}
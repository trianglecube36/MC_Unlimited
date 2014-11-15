package io.github.trianglecube36.unlimited.gen.structure;

import java.util.Random;

import net.minecraft.world.World;

public class UStructureMineshaftStart extends UStructureStart
{
    private static final String __OBFID = "CL_00000450";

    public UStructureMineshaftStart() {}

    public UStructureMineshaftStart(World world, Random rand, int cX, int cY, int cZ)
    {
        super(cX, cZ);
        UStructureMineshaftPieces.Room room = new UStructureMineshaftPieces.Room(0, rand, (cX << 5) + 2, (cY << 5) + 2, (cZ << 5) + 2);
        this.components.add(room);
        room.buildComponent(room, this.components, rand);
        this.updateBoundingBox();
        this.markAvailableHeight(world, rand, 10);
    }
}
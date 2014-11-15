package io.github.trianglecube36.unlimited.gen.structure;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.util.MathHelper;

public class UMapGenMineshaft extends UMapGenStructure
{
    private double field_82673_e = 0.004D;

    public UMapGenMineshaft() {}

    public String func_143025_a()
    {
        return "Mineshaft";
    }

    /**
     * support for flat world
     * */
    public UMapGenMineshaft(Map p_i2034_1_)
    {
        Iterator iterator = p_i2034_1_.entrySet().iterator();

        while (iterator.hasNext())
        {
            Entry entry = (Entry)iterator.next();

            if (((String)entry.getKey()).equals("chance"))
            {
                this.field_82673_e = MathHelper.parseDoubleWithDefault((String)entry.getValue(), this.field_82673_e);
            }
        }
    }

    protected boolean canSpawnStructureAtCoords(int cX, int cY, int cZ)
    {
        return this.rand.nextDouble() < this.field_82673_e/* && this.rand.nextInt(80) < Math.max(Math.abs(cX), Math.abs(cZ))*/;
    }

    protected UStructureStart getStructureStart(int cX, int cY, int cZ)
    {
        return new UStructureMineshaftStart(this.worldObj, this.rand, cX, cY, cZ);
    }
}
package io.github.trianglecube36.unlimited.util;

public class HashUnit3D
{
    public final int keyX, keyY, keyZ;
    
    public Object value;

    HashUnit3D nextEntry;
    final int hash;

    HashUnit3D(int x, int y, int z, int hs, Object obj, HashUnit3D next)
    {
        this.value = obj;
        this.nextEntry = next;
        this.keyX = x;
        this.keyY = y;
        this.keyZ = z;
        this.hash = hs;
    }

    public final boolean equals(Object par1Obj)
    {
        if (!(par1Obj instanceof HashUnit3D))
        {
            return false;
        }
        else
        {
            HashUnit3D unit96 = (HashUnit3D)par1Obj;
            if (keyX == unit96.keyX && keyY == unit96.keyY && keyZ == unit96.keyZ)
            {
                Object object1 = this.value;
                Object object2 = unit96.value;

                if (object1 == object2 || object1 != null && object1.equals(object2))
                {
                    return true;
                }
            }

            return false;
        }
    }

    public final String toString()
    {
        return this.keyX + ", " + this.keyY + ", " + this.keyZ + " = " + this.value;
    }
}
package io.github.trianglecube36.unlimited.util;

public class HashUnit2D
{
    public final int keyX, keyZ;
    
    public Object value;

    HashUnit2D nextEntry;
    final int hash;

    HashUnit2D(int x, int z, int hs, Object obj, HashUnit2D next)
    {
        this.value = obj;
        this.nextEntry = next;
        this.keyX = x;
        this.keyZ = z;
        this.hash = hs;
    }

    public final boolean equals(Object par1Obj)
    {
        if (!(par1Obj instanceof HashUnit2D))
        {
            return false;
        }
        else
        {
            HashUnit2D unit64 = (HashUnit2D)par1Obj;
            if (keyX == unit64.keyX && keyZ == unit64.keyZ)
            {
                Object object1 = this.value;
                Object object2 = unit64.value;

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
        return this.keyX + ", " + this.keyZ + " = " + this.value;
    }
}
package io.github.trianglecube36.unlimited.util;

public class HashMap2D
{
    /** the array of all elements in the hash */
    private transient HashUnit2D[] hashArray = new HashUnit2D[16];

    /** the number of elements in the hash array */
    private transient int numHashElements;

    private int capacity = 12;

    /**
     * percent of the hasharray that can be used without hash colliding probably
     */
    private final float percentUseable = 0.75F;

    /** count of times elements have been added/removed */
    private transient volatile int modCount;

    /**
     * returns the hashed key given the original key
     */
    private static int getHashedKey(int i, int j)
    {
        return hash((int)((i * 32407889) ^ (j * 70849))); //some random numbers...
    }

    /**
     * the hash function
     */
    private static int hash(int par0)
    {
        par0 ^= par0 >>> 20 ^ par0 >>> 12;
        return par0 ^ par0 >>> 7 ^ par0 >>> 4;
    }

    /**
     * gets the index in the hash given the array length and the hashed key
     */
    private static int getHashIndex(int par0, int par1)
    {
        return par0 & par1 - 1;
    }

    public int getNumHashElements()
    {
        return this.numHashElements;
    }

    /**
     * get the value from the map given the key
     */
    public Object getValueByKey(int i, int j)
    {
        int H = getHashedKey(i, j);

        for (HashUnit2D unit64 = this.hashArray[getHashIndex(H, this.hashArray.length)]; unit64 != null; unit64 = unit64.nextEntry)
        {
            if (unit64.keyX == i && unit64.keyZ == j)
            {
                return unit64.value;
            }
        }

        return null;
    }

    public boolean containsItem(int i, int j)
    {
        return this.getEntry(i, j) != null;
    }

    final HashUnit2D getEntry(int i, int j)
    {
        int H = getHashedKey(i, j);

        for (HashUnit2D unit64 = this.hashArray[getHashIndex(H, this.hashArray.length)]; unit64 != null; unit64 = unit64.nextEntry)
        {
            if (unit64.keyX == i && unit64.keyZ == j)
            {
                return unit64;
            }
        }

        return null;
    }

    /**
     * Add a key-value pair.
     */
    public void add(int i, int j, Object par3Obj)
    {
        int H = getHashedKey(i, j);
        int I = getHashIndex(H, this.hashArray.length);

        for (HashUnit2D unit64 = this.hashArray[I]; unit64 != null; unit64 = unit64.nextEntry)
        {
            if (unit64.keyX == i && unit64.keyZ == j)
            {
                unit64.value = par3Obj;
                return;
            }
        }

        ++this.modCount;
        this.createKey(i, j, H, par3Obj, I);
    }

    /**
     * resizes the table
     */
    private void resizeTable(int par1)
    {
    	HashUnit2D[] alonghashmapentry = this.hashArray;
        int j = alonghashmapentry.length;

        if (j == 1073741824)
        {
            this.capacity = Integer.MAX_VALUE;
        }
        else
        {
        	HashUnit2D[] alonghashmapentry1 = new HashUnit2D[par1];
            this.copyHashTableTo(alonghashmapentry1);
            this.hashArray = alonghashmapentry1;
            this.capacity = (int)((float)par1 * this.percentUseable);
        }
    }

    /**
     * copies the hash table to the specified array
     */
    private void copyHashTableTo(HashUnit2D[] par1)
    {
    	HashUnit2D[] aunit64 = this.hashArray;
        int i = par1.length;

        for (int j = 0; j < aunit64.length; ++j)
        {
        	HashUnit2D unit64 = aunit64[j];

            if (unit64 != null)
            {
            	aunit64[j] = null;
                HashUnit2D unit641;

                do
                {
                	unit641 = unit64.nextEntry;
                    int k = getHashIndex(unit64.hash, i);
                    unit64.nextEntry = par1[k];
                    par1[k] = unit64;
                    unit64 = unit641;
                }
                while (unit641 != null);
            }
        }
    }

    /**
     * calls the removeKey method and returns removed object
     */
    public Object remove(int i, int j)
    {
    	HashUnit2D unit64 = this.removeKey(i, j);
        return unit64 == null ? null : unit64.value;
    }

    /**
     * removes the key from the hash linked list
     */
    final HashUnit2D removeKey(int i, int j)
    {
        int H = getHashedKey(i, j);
        int I = getHashIndex(H, this.hashArray.length);
        HashUnit2D unit64 = this.hashArray[I];
        HashUnit2D unit641;
        HashUnit2D unit642;

        for (unit641 = unit64; unit641 != null; unit641 = unit642)
        {
            unit642 = unit641.nextEntry;

            if (unit641.keyX == i && unit641.keyZ == j)
            {
                ++this.modCount;
                --this.numHashElements;

                if (unit64 == unit641)
                {
                    this.hashArray[I] = unit642;
                }
                else
                {
                    unit64.nextEntry = unit642;
                }

                return unit641;
            }

            unit64 = unit641;
        }

        return unit641;
    }

    /**
     * creates the key in the hash table
     */
    private void createKey(int i, int j, int hs, Object par4Obj, int par5)
    {
    	HashUnit2D unit64 = this.hashArray[par5];
        this.hashArray[par5] = new HashUnit2D(i, j, hs, par4Obj, unit64);

        if (this.numHashElements++ >= this.capacity)
        {
            this.resizeTable(2 * this.hashArray.length);
        }
    }

    /**
     * public method to get the hashed key(hashCode)
     */
    static int getHashCode(int i, int j)
    {
        return getHashedKey(i, j);
    }
}

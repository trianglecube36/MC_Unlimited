package io.github.trianglecube36.unlimited.util;

import java.util.Iterator;

public class HashMap3D
{
    /** the array of all elements in the hash */
    private transient HashUnit3D[] hashArray = new HashUnit3D[16];

    /** the number of elements in the hash array */
    private transient int numHashElements;

    private int capacity = 12;

    /**
     * percent of the hasharray that can be used without hash colliding probably
     */
    private final float percentUseable = 0.75F;

    /** count of times elements have been added/removed */
    @SuppressWarnings("unused")
	private transient volatile int modCount;

    /**
     * returns the hashed key given the original key
     */
    private static int getHashedKey(int i, int j, int k)
    {
        return hash((int)((i * 32407889) ^ (j * 70849) ^ (k * 59444325))); //some random numbers...
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
    public Object getValueByKey(int i, int j, int k)
    {
        int H = getHashedKey(i, j, k);

        for (HashUnit3D unit96 = this.hashArray[getHashIndex(H, this.hashArray.length)]; unit96 != null; unit96 = unit96.nextEntry)
        {
            if (unit96.keyX == i && unit96.keyY == j && unit96.keyZ == k)
            {
                return unit96.value;
            }
        }

        return null;
    }

    public boolean containsItem(int i, int j, int k)
    {
        return this.getEntry(i, j, k) != null;
    }

    final HashUnit3D getEntry(int i, int j, int k)
    {
        int H = getHashedKey(i, j, k);

        for (HashUnit3D unit96 = this.hashArray[getHashIndex(H, this.hashArray.length)]; unit96 != null; unit96 = unit96.nextEntry)
        {
            if (unit96.keyX == i && unit96.keyY == j && unit96.keyZ == k)
            {
                return unit96;
            }
        }

        return null;
    }

    /**
     * Add a key-value pair.
     */
    public void add(int i, int j, int k, Object par3Obj)
    {
        int H = getHashedKey(i, j, k);
        int I = getHashIndex(H, this.hashArray.length);

        for (HashUnit3D unit96 = this.hashArray[I]; unit96 != null; unit96 = unit96.nextEntry)
        {
            if (unit96.keyX == i && unit96.keyY == j && unit96.keyZ == k)
            {
                unit96.value = par3Obj;
                return;
            }
        }

        ++this.modCount;
        this.createKey(i, j, k, H, par3Obj, I);
    }

    /**
     * resizes the table
     */
    private void resizeTable(int par1)
    {
    	HashUnit3D[] alonghashmapentry = this.hashArray;
        int j = alonghashmapentry.length;

        if (j == 1073741824)
        {
            this.capacity = Integer.MAX_VALUE;
        }
        else
        {
        	HashUnit3D[] alonghashmapentry1 = new HashUnit3D[par1];
            this.copyHashTableTo(alonghashmapentry1);
            this.hashArray = alonghashmapentry1;
            this.capacity = (int)((float)par1 * this.percentUseable);
        }
    }

    /**
     * copies the hash table to the specified array
     */
    private void copyHashTableTo(HashUnit3D[] par1ArrayOfLongHashMapEntry)
    {
    	HashUnit3D[] aunit96 = this.hashArray;
        int i = par1ArrayOfLongHashMapEntry.length;

        for (int j = 0; j < aunit96.length; ++j)
        {
        	HashUnit3D unit96 = aunit96[j];

            if (unit96 != null)
            {
            	aunit96[j] = null;
                HashUnit3D unit961;

                do
                {
                	unit961 = unit96.nextEntry;
                    int k = getHashIndex(unit96.hash, i);
                    unit96.nextEntry = par1ArrayOfLongHashMapEntry[k];
                    par1ArrayOfLongHashMapEntry[k] = unit96;
                    unit96 = unit961;
                }
                while (unit961 != null);
            }
        }
    }

    /**
     * calls the removeKey method and returns removed object
     */
    public Object remove(int i, int j, int k)
    {
    	HashUnit3D hashmapentry = this.removeKey(i, j, k);
        return hashmapentry == null ? null : hashmapentry.value;
    }

    /**
     * removes the key from the hash linked list
     */
    final HashUnit3D removeKey(int i, int j, int k)
    {
        int H = getHashedKey(i, j, k);
        int I = getHashIndex(H, this.hashArray.length);
        HashUnit3D unit96 = this.hashArray[I];
        HashUnit3D unit961;
        HashUnit3D unit962;

        for (unit961 = unit96; unit961 != null; unit961 = unit962)
        {
            unit962 = unit961.nextEntry;

            if (unit961.keyX == i && unit961.keyY == j && unit961.keyZ == k)
            {
                ++this.modCount;
                --this.numHashElements;

                if (unit96 == unit961)
                {
                    this.hashArray[I] = unit962;
                }
                else
                {
                    unit96.nextEntry = unit962;
                }

                return unit961;
            }

            unit96 = unit961;
        }

        return unit961;
    }

    /**
     * creates the key in the hash table
     */
    private void createKey(int i, int j, int k, int hs, Object par4Obj, int par5)
    {
    	HashUnit3D longhashmapentry = this.hashArray[par5];
        this.hashArray[par5] = new HashUnit3D(i, j, k, hs, par4Obj, longhashmapentry);

        if (this.numHashElements++ >= this.capacity)
        {
            this.resizeTable(2 * this.hashArray.length);
        }
    }

    /**
     * public method to get the hashed key(hashCode)
     */
    static int getHashCode(int i, int j, int k)
    {
        return getHashedKey(i, j, k);
    }
    
    public Iterator iterator(){
    	return new Map3DIterator();
    }
    
    public class Map3DIterator implements Iterator{
    	private int index;
    	private HashUnit3D next;
    	
    	private Map3DIterator(){
    		move();
    	}
    	
    	private void move(){
    		if(next != null && next.nextEntry != null){
    			next = next.nextEntry;
    			return;
    		}
    		index++;
    		for(;index < HashMap3D.this.hashArray.length;index++){
    			if(HashMap3D.this.hashArray[index] != null){
    				next = HashMap3D.this.hashArray[index];
    				return;
    			}
    		}
    		next = null;
    	}
    	
		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public Object next() {
			HashUnit3D r = next;
			move();
			return r;
		}

		@Override
		public void remove() {
			
		}
    }
}

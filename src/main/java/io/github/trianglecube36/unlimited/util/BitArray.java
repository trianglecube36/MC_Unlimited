package io.github.trianglecube36.unlimited.util;

public class BitArray
{
    public long words[];

    private static int wordIndex(int i)
    {
        return i >> 6;
    }

    public BitArray(long al[])
    {
        words = al;
    }

    public void flip(int i)
    {
        int j = wordIndex(i);
        words[j] ^= 1L << i;
    }

    public void set(int i)
    {
        if(i < 0)
        {
            throw new IndexOutOfBoundsException((new StringBuilder()).append("bitIndex < 0: ").append(i).toString());
        } else
        {
            int j = wordIndex(i);
            words[j] |= 1L << i;
            return;
        }
    }

    public void clear(int i)
    {
        int j = wordIndex(i);
        words[j] &= ~(1L << i);
        return;
    }

    public void clear()
    {
    	int i = words.length;
        while(i > 0) 
        {
            words[--i] = 0L;
        }
    }

    public boolean get(int i)
    {
        int j = wordIndex(i);
        return (words[j] & 1L << i) != 0L;
    }
}

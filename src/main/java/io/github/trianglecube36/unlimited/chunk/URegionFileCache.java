package io.github.trianglecube36.unlimited.chunk;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class URegionFileCache
{
    /**
     * A map containing Files as keys and RegionFiles as values
     */
    private static final Map regionsByFilename = new HashMap();
    private static final Map region2dsByFilename = new HashMap();

    public static synchronized URegionFile createOrLoadRegionFile(File par0File, int x, int y, int z)
    {
        File file2 = new File(par0File, "region");
        File file3 = new File(file2, "r." + (x >> 4) + "." + (y >> 4) + "." + (z >> 4) + ".mcu");
        URegionFile regionfile = (URegionFile)regionsByFilename.get(file3);

        if (regionfile != null)
        {
            return regionfile;
        }
        else
        {
            if (!file2.exists())
            {
                file2.mkdirs();
            }

            if (regionsByFilename.size() >= 256) //TODO: split with region2d
            {
                clearRegionFileReferences();
            }

            URegionFile regionfile1 = new URegionFile(file3);
            regionsByFilename.put(file3, regionfile1);
            return regionfile1;
        }
    }
    
    public static synchronized URegion2DFile createOrLoadRegion2DFile(File par0File, int x, int z)
    {
        File file2 = new File(par0File, "region2d");
        File file3 = new File(file2, "r." + (x >> 4) + "." + (z >> 4) + ".mcu");
        URegion2DFile region2dfile = (URegion2DFile)region2dsByFilename.get(file3);

        if (region2dfile != null)
        {
            return region2dfile;
        }
        else
        {
            if (!file2.exists())
            {
                file2.mkdirs();
            }

            if (regionsByFilename.size() >= 256) //TODO: split with region
            {
                clearRegion2DFileReferences();
            }

            URegion2DFile region2dfile1 = new URegion2DFile(file3);
            region2dsByFilename.put(file3, region2dfile1);
            return region2dfile1;
        }
    }

    /**
     * Saves the current Chunk Map Cache
     */
    public static synchronized void clearRegionFileReferences()
    {
        Iterator iterator = regionsByFilename.values().iterator();

        while (iterator.hasNext())
        {
            URegionFile regionfile = (URegionFile)iterator.next();

            try
            {
                if (regionfile != null)
                {
                    regionfile.close();
                }
            }
            catch (IOException ioexception)
            {
                ioexception.printStackTrace();
            }
        }

        regionsByFilename.clear();
    }
    
    /**
     * Saves the current Chunk Map Cache
     */
    public static synchronized void clearRegion2DFileReferences()
    {
        Iterator iterator = region2dsByFilename.values().iterator();

        while (iterator.hasNext())
        {
            URegion2DFile regionfile = (URegion2DFile)iterator.next();

            try
            {
                if (regionfile != null)
                {
                    regionfile.close();
                }
            }
            catch (IOException ioexception)
            {
                ioexception.printStackTrace();
            }
        }

        region2dsByFilename.clear();
    }

    /**
     * Returns an input stream for the specified chunk. Args: worldDir, chunkX, chunkZ
     */
    public static DataInputStream getChunkInputStream(File par0File, int x, int y, int z)
    {
        URegionFile regionfile = createOrLoadRegionFile(par0File, x, y, z);
        return regionfile.getChunkDataInputStream(x & 15, y & 15, z & 15);
    }
    
    public static DataInputStream getChunk2DInputStream(File par0File, int x, int z)
    {
        URegion2DFile regionfile = createOrLoadRegion2DFile(par0File, x, z);
        return regionfile.getChunk2DDataInputStream(x & 15, z & 15);
    }

    /**
     * Returns an output stream for the specified chunk. Args: worldDir, chunkX, chunkZ
     */
    public static DataOutputStream getChunkOutputStream(File par0File, int x, int y, int z)
    {
        URegionFile regionfile = createOrLoadRegionFile(par0File, x, y, z);
        return regionfile.getChunkDataOutputStream(x & 15, y & 15, z & 15);
    }
    
    public static DataOutputStream getChunk2DOutputStream(File par0File, int x, int z)
    {
        URegion2DFile regionfile = createOrLoadRegion2DFile(par0File, x, z);
        return regionfile.getChunk2DDataOutputStream(x & 15, z & 15);
    }
}

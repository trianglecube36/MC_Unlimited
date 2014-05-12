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

            if (regionsByFilename.size() >= 256)
            {
                clearRegionFileReferences();
            }

            URegionFile regionfile1 = new URegionFile(file3);
            regionsByFilename.put(file3, regionfile1);
            return regionfile1;
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
     * Returns an input stream for the specified chunk. Args: worldDir, chunkX, chunkZ
     */
    public static DataInputStream getChunkInputStream(File par0File, int x, int y, int z)
    {
        URegionFile regionfile = createOrLoadRegionFile(par0File, x, y, z);
        return regionfile.getChunkDataInputStream(x & 4, y & 4, z & 4);
    }

    /**
     * Returns an output stream for the specified chunk. Args: worldDir, chunkX, chunkZ
     */
    public static DataOutputStream getChunkOutputStream(File par0File, int x, int y, int z)
    {
        URegionFile regionfile = createOrLoadRegionFile(par0File, x, y, z);
        return regionfile.getChunkDataOutputStream(x & 4, y & 4, z & 4);
    }
}

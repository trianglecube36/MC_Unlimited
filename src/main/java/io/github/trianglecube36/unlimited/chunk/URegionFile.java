package io.github.trianglecube36.unlimited.chunk;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class URegionFile
{
    private static final byte[] emptySector = new byte[4096];
    private final File fileName;
    private RandomAccessFile dataFile;
    private final int[] offsets = new int[4096]; //note: pigs can't fly... o and, the last 12 bits are the sector count
    private final int[] chunkTimestamps = new int[4096];
    private BitSet sectorFree;
    private int sizeDelta;
    private long lastModified;

    public URegionFile(File par1File)
    {
        this.fileName = par1File;
        this.sizeDelta = 0;

        try
        {
            if (par1File.exists())
            {
                this.lastModified = par1File.lastModified();
            }

            this.dataFile = new RandomAccessFile(par1File, "rw");
            int i;

            if (this.dataFile.length() < 32768L)
            {
                for (i = 0; i < 4096; ++i)
                {
                    this.dataFile.writeInt(0);
                }

                for (i = 0; i < 4096; ++i)
                {
                    this.dataFile.writeInt(0);
                }

                this.sizeDelta += 32768;
            }

            if ((this.dataFile.length() & 4095L) != 0L)
            {
                for (i = 0; (long)i < (this.dataFile.length() & 4095L); ++i)
                {
                    this.dataFile.write(0);
                }
            }

            i = (int)this.dataFile.length() / 4096;
            this.sectorFree = new BitSet(i);
            this.sectorFree.set(8, i); // 8 false's , true's ...

            this.dataFile.seek(0L);
            int k;
            int j;
            for (j = 0; j < 4096; ++j)
            {
                k = this.dataFile.readInt();
                this.offsets[j] = k;

                i = k & 4095;
                if (k != 0 && (k >> 12) + i <= this.sectorFree.size())
                {
                    for (int l = 0; l < i; ++l)
                    {
                        this.sectorFree.clear((k >> 12) + l);
                    }
                }
            }

            for (j = 0; j < 4096; ++j)
            {
                k = this.dataFile.readInt();
                this.chunkTimestamps[j] = k;
            }
        }
        catch (IOException ioexception)
        {
            ioexception.printStackTrace();
        }
    }

    public synchronized DataInputStream getChunkDataInputStream(int x, int y, int z)
    {
        if (this.outOfBounds(x, y, z))
        {
            return null;
        }
        else
        {
            try
            {
                int k = this.getOffset(x, y, z);

                if (k == 0)
                {
                    return null;
                }
                else
                {
                    int off = k >> 12;
                    int count = k & 4095;

                    if (off + count > this.sectorFree.size())
                    {
                        return null;
                    }
                    else
                    {
                        this.dataFile.seek((long)(off * 4096));
                        int dataLength = this.dataFile.readInt();

                        if (dataLength > 4096 * count)
                        {
                            return null;
                        }
                        else if (dataLength <= 0)
                        {
                            return null;
                        }
                        else
                        {
                            byte type = this.dataFile.readByte();
                            byte[] abyte;

                            if (type == 1)
                            {
                                abyte = new byte[dataLength - 1];
                                this.dataFile.read(abyte);
                                return new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(abyte))));
                            }
                            else if (type == 2)
                            {
                                abyte = new byte[dataLength - 1];
                                this.dataFile.read(abyte);
                                return new DataInputStream(new BufferedInputStream(new InflaterInputStream(new ByteArrayInputStream(abyte))));
                            }
                            else
                            {
                                return null;
                            }
                        }
                    }
                }
            }
            catch (IOException ioexception)
            {
                return null;
            }
        }
    }

    @SuppressWarnings("resource")
	public DataOutputStream getChunkDataOutputStream(int x, int y, int z)
    {
        return this.outOfBounds(x, y, z) ? null : new DataOutputStream(new DeflaterOutputStream(new URegionFile.ChunkBuffer(x, y, z)));
    }
    
    private int freeIndex(){
    	for(int i = 0;i < this.sectorFree.size();i++){
    		if(this.sectorFree.get(i)){
    			return i;
    		}
    	}
    	return -1;
    }
    
    protected synchronized void write(int x, int y, int z, byte[] dataArray, int numbites)
    {
        try
        {
            int l = this.getOffset(x, y, z);
            int off = l >> 12;
            int count = l & 4095;
            int newcount = (numbites + 5) / 4096 + 1;

            if (newcount >= 4096)
            {
                return;
            }

            if (off != 0 && count == newcount)
            {
                this.write(off, dataArray, numbites);
            }
            else
            {
                int firstTrue;

                for (firstTrue = 0; firstTrue < count; ++firstTrue)
                {
                    this.sectorFree.set(off + firstTrue);
                }

                firstTrue = freeIndex();
                int sNewCount = 0;
                int iteration;

                if (firstTrue != -1)
                {
                    for (iteration = firstTrue; iteration < this.sectorFree.size(); ++iteration)
                    {
                        if (sNewCount != 0)
                        {
                            if (this.sectorFree.get(iteration))
                            {
                                ++sNewCount;
                            }
                            else
                            {
                                sNewCount = 0;
                            }
                        }
                        else if (this.sectorFree.get(iteration)) //note: slides us up to the new first true if last one there was not enough space
                        {
                            firstTrue = iteration;
                            sNewCount = 1;
                        }

                        if (sNewCount >= newcount)
                        {
                            break;
                        }
                    }
                }

                if (sNewCount >= newcount)
                {
                    off = firstTrue;
                    this.setOffset(x, y, z, firstTrue << 12 | newcount);

                    for (iteration = 0; iteration < newcount; ++iteration)
                    {
                        this.sectorFree.clear(off + iteration);
                    }

                    this.write(off, dataArray, numbites);
                }
                else
                {
                    this.dataFile.seek(this.dataFile.length());
                    off = this.sectorFree.size();

                    for (iteration = 0; iteration < newcount; ++iteration)
                    {
                        this.dataFile.write(emptySector);
                        this.sectorFree.set(this.sectorFree.size());
                    }

                    this.sizeDelta += 4096 * newcount;
                    this.write(off, dataArray, numbites);
                    this.setOffset(x, y, z, off << 12 | newcount);
                }
            }

            this.setChunkTimestamp(x, y, z, (int)(System.currentTimeMillis() / 1000L));
        }
        catch (IOException ioexception)
        {
            ioexception.printStackTrace();
        }
    }

    private void write(int sector, byte[] dataArray, int numbites) throws IOException
    {
        this.dataFile.seek((long)(sector * 4096));
        this.dataFile.writeInt(numbites + 1);
        this.dataFile.writeByte(2);
        this.dataFile.write(dataArray, 0, numbites);
    }

    private boolean outOfBounds(int x, int y, int z)
    {
        return x < 0 || x >= 16 || y < 0 || y >= 16 || z < 0 || z >= 16;
    }

    private int getOffset(int x, int y, int z)
    {
        return this.offsets[x + y * 16 + z * 256];
    }

    public boolean isChunkSaved(int x, int y, int z)
    {
        return this.getOffset(x, y, z) != 0;
    }

    private void setOffset(int x, int y, int z, int offset) throws IOException
    {
        this.offsets[x + y * 16 + z * 256] = offset;
        this.dataFile.seek((long)((x + y * 16 + z * 256) * 4));
        this.dataFile.writeInt(offset);
    }

    private void setChunkTimestamp(int x, int y, int z, int timeStamp) throws IOException
    {
        this.chunkTimestamps[x + y * 16 + z * 256] = timeStamp;
        this.dataFile.seek((long)(16384 + (x + y * 16 + z * 256) * 4));
        this.dataFile.writeInt(timeStamp);
    }

    public void close() throws IOException
    {
        if (this.dataFile != null)
        {
            this.dataFile.close();
        }
    }

    class ChunkBuffer extends ByteArrayOutputStream
    {
        private int chunkX;
        private int chunkY;
        private int chunkZ;

        public ChunkBuffer(int x, int y, int z)
        {
            super(4096);
            this.chunkX = x;
            this.chunkY = y;
            this.chunkZ = z;
        }

        public void close() throws IOException
        {
            URegionFile.this.write(this.chunkX, this.chunkY, this.chunkZ, this.buf, this.count);
        }
    }
}

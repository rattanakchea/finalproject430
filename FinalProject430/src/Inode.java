/*
 * Inode Class
 * @data May 3, 2014
 */
public class Inode {
	private final static int iNodeSize = 32; // fix to 32 bytes
	private final static int directSize = 11; // # direct pointers
	public int length; // file size in bytes
	public short count; // # file-table entries pointing to this
	public short flag; // 0 = unused, 1 = used, ...
	public short direct[] = new short[directSize]; // direct pointers
	public short indirect; // a indirect pointer

	Inode() { // a default constructor
		length = 0;
		count = 0;
		flag = 1;
		for (int i = 0; i < directSize; i++)
			direct[i] = -1;
		indirect = -1;
	}

	Inode(short iNumber) { // retrieving inode from disk
		// design it by yourself.
		
		
	}

	int toDisk(short iNumber) { // save to disk as the i-th inode
		byte[] writeToInode = new byte[this.iNodeSize];
		
		int index = 0;
		
		SysLib.int2bytes(this.length, writeToInode, index);
		
		index += 4;
		SysLib.short2bytes(this.count, writeToInode, index);
		
		index += 2;
		SysLib.short2bytes(this.flag, writeToInode, index);
		
		for (int i = 2; i <= this.directSize * 2; i += 2){
			index += i;
			SysLib.short2bytes(this.direct[i], writeToInode, index);
		}
		
		index += 2;
		SysLib.short2bytes(this.indirect, writeToInode, index);
		
		int blockToWriteTo = 1 + (iNumber / 16);
		
		byte[] bufferToDisk = new byte[Disk.blockSize];
		SysLib.rawread(blockToWriteTo, bufferToDisk);
		
		int offsetInThatBlock = iNumber % 16 * 32;
		
		System.arraycopy(writeToInode, 0, bufferToDisk, destPos, offsetInThatBlock);
		
		return 1;
	}
}

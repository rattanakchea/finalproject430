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
	
	// retrieving inode from disk
	Inode(short iNumber) {
		// design it by yourself.
		int blockNumber = 1 + iNumber/16;
		byte[] data = new byte[Disk.blockSize];
		SysLib.rawread(blockNumber, data);  //read into data
		int offset = iNumber % 16 * 32;
		
		this.length = SysLib.bytes2int(data, offset);
		offset += 4;
		this.count = SysLib.bytes2short(data, offset);
		offset += 2;
		this.flag = SysLib.bytes2short(data, offset);
		offset += 2;
		
		//direct[] = new short[11]
		for (int i=0; i < 11; i++){
			this.direct[i] = SysLib.bytes2short(data, offset);
			offset += 2;
		}
		
		this.indirect = SysLib.bytes2short(data, offset);
		
		
	}

	int toDisk(short iNumber) { // save to disk as the i-th inode
		byte[] writeToInode = new byte[this.iNodeSize];
		
		int index = 0;
		SysLib.cerr("before writing length to writeToInode byte array.... \n");
		SysLib.int2bytes(this.length, writeToInode, index);
		SysLib.cerr("after writing length to writeToInode byte array.... \n");
		
		index += 4;
		SysLib.short2bytes(this.count, writeToInode, index);
		
		index += 2;
		SysLib.short2bytes(this.flag, writeToInode, index);
		
		index += 2;
		for (int i = 0; i < this.directSize; i++){
			SysLib.cerr("writing direct to writeToInode byte array index: "+ index+" \n");
			SysLib.short2bytes(this.direct[i], writeToInode, index);
			index += 2;
		}
		
		SysLib.cerr("after writing direct to writeToInode byte array.... \n");
		
		
		SysLib.short2bytes(this.indirect, writeToInode, index);
		
		
		SysLib.cerr("after writing indirect to writeToInode byte array.... \n");
		
		int blockToWriteTo = 1 + (iNumber / 16);
		
		byte[] bufferToDisk = new byte[Disk.blockSize];
		SysLib.rawread(blockToWriteTo, bufferToDisk);
		
		int offsetInThatBlock = iNumber % 16 * this.iNodeSize;
		
		System.arraycopy(writeToInode, 0, bufferToDisk, offsetInThatBlock, this.iNodeSize);
		
		SysLib.rawwrite(blockToWriteTo, bufferToDisk);
		
		return 1;
	}
}

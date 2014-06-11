import javax.naming.spi.DirStateFactory;

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
	
	public static final short UNUSED = 0;
	public static final short USED = 1;
	public static final short READ = 2;
	public static final short WRITE = 3;
	public static final short DELETE = 4;
	
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
	/*
	 * toDisk method
	 */
	public int toDisk(short iNumber) { // save to disk as the i-th inode
		byte[] writeToInode = new byte[this.iNodeSize];
		
		int index = 0;
		SysLib.int2bytes(this.length, writeToInode, index);
		
		index += 4;
		SysLib.short2bytes(this.count, writeToInode, index);
		
		index += 2;
		SysLib.short2bytes(this.flag, writeToInode, index);
		
		index += 2;
		for (int i = 0; i < this.directSize; i++){
			SysLib.short2bytes(this.direct[i], writeToInode, index);
			index += 2;
		}
		
		SysLib.short2bytes(this.indirect, writeToInode, index);
		
		int blockToWriteTo = 1 + (iNumber / 16);
		
		byte[] bufferToDisk = new byte[Disk.blockSize];
		SysLib.rawread(blockToWriteTo, bufferToDisk);
		
		int offsetInThatBlock = iNumber % 16 * this.iNodeSize;
		System.arraycopy(writeToInode, 0, bufferToDisk, offsetInThatBlock, this.iNodeSize);	
		SysLib.rawwrite(blockToWriteTo, bufferToDisk);
		
		return 1;
	}
	
	/*
	 * findIndexBlock
	 */
	public short findIndexBlock(){
		return this.indirect;
	}
	
	/*
	 * registerIndexBlock
	 */
	boolean registerIndexBlock(short num){
		for (int i=0; i < this.directSize; i++){
			if (this.direct[i] == -1) {
				return false;
			}
		}
		if (this.indirect != -1){
			return false;
		}
		
		this.indirect = num;
		byte[] data = new byte[512];
		for (int j=0; j<256; j++) {
			SysLib.short2bytes((short)-1, data, j*2);
		}
		SysLib.rawwrite(num, data);
		return true;
	}
	

	/*
	 * need to commands
	 */
	int findTargetBlock(int offset){
		if (offset < 0) return -1;
		else if (offset < directSize) {
			//direct dump
			return direct[offset];
		}
		//get the index within indirect block
		int indirect_offset = offset - directSize;
		
		//read from this indirect block the short at indirect offset
		return SysLib.bytes2short(readIndirectBlock(), indirect_offset);
	}
	
	private byte[] readIndirectBlock(){
		byte[] indrect_block = new byte[Disk.blockSize];
		//read entire indirect block
		SysLib.rawread(indirect, indrect_block);
		return indrect_block;
	}
	
	/*
	 * need to write
	 */
	int registerTargetBlock(int offset, short indexBlockNumber){
		int index_offset = offset / Disk.blockSize;
		if (index_offset < 11) {
			if (this.direct[index_offset] >= 0)
				return -1;
			
			if (index_offset > 0 && (this.direct[index_offset-1]) == -1)
				return -2;
			
			this.direct[index_offset] = indexBlockNumber;
			return 0;
		}
		if (this.indirect < 0){
			return -3;
		}
		byte[] data = new byte[Disk.blockSize];
		int j = index_offset - directSize;
		if (SysLib.bytes2short(data, j*2) > 0) {
			SysLib.cerr("indexBlock, indirectNumber : " + j);
			SysLib.cerr(" "+ SysLib.bytes2short(data, j*2));
			return -1;
		}
		SysLib.short2bytes(indexBlockNumber, data, j*2);
		SysLib.rawwrite(this.indirect, data);
		return 0;
		
	}
	
	/*
	 * need to write
	 */
	byte[] unregisterIndexBlock(){
		if (this.indirect >= 0){
			byte[] data = new byte[Disk.blockSize];
			SysLib.rawread(this.indirect, data);
			this.indirect = -1;
			return data;
		}
		return null;
	}
	
}

/* 
 * Inode Class
 * @data June 12, 2014
 * @author Chamnap Lim
 * @author Rattanak Chea
 */
import javax.naming.spi.DirStateFactory;

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
	
	/* ----------------------------------------------------------------------//
	 * default constructor
	 * @purpose: initialize an Inode
	 */
	Inode() {
		length = 0;
		count = 0;
		flag = 1;
		for (int i = 0; i < directSize; i++)
			direct[i] = -1;
		indirect = -1;
	}
	
	/* ----------------------------------------------------------------------//
	 * Inode constructor with iNumber passed in
	 * @para: iNumber
	 */
	Inode(short iNumber) {  //retrieve Inode from disk
		int blockNumber = 1 + iNumber/16;
		byte[] data = new byte[Disk.blockSize];
		SysLib.rawread(blockNumber, data);  //read into data
		int offset = iNumber % 16 * 32;
		
		//load length, count, and flag
		this.length = SysLib.bytes2int(data, offset);
		offset += 4;  //int has 4 bytes
		this.count = SysLib.bytes2short(data, offset);
		offset += 2; //short has 2 bytes
		this.flag = SysLib.bytes2short(data, offset);
		offset += 2; //short has 2 bytes
		
		for (int i=0; i < directSize; i++){
			//load direct pointers
			this.direct[i] = SysLib.bytes2short(data, offset);
			offset += 2;
		}
		this.indirect = SysLib.bytes2short(data, offset);	
	}
	
	/* ----------------------------------------------------------------------//
	 * toDisk
	 * @pupose: save to the disk as the i-th Inode
	 * @para: iNumber
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
			//add direct pointers
			SysLib.short2bytes(this.direct[i], writeToInode, index);
			index += 2;
		}
		
		SysLib.short2bytes(this.indirect, writeToInode, index);
		
		int blockToWriteTo = 1 + (iNumber / 16);
		
		byte[] bufferToDisk = new byte[Disk.blockSize];
		SysLib.rawread(blockToWriteTo, bufferToDisk);
		
		int offsetInThatBlock = iNumber % 16 * this.iNodeSize;
		System.arraycopy(writeToInode, 0, bufferToDisk, offsetInThatBlock, this.iNodeSize);
		//write the iNode block to disk
		SysLib.rawwrite(blockToWriteTo, bufferToDisk);
		
		return 1;
	}
	
	/* ----------------------------------------------------------------------//
	 * findIndexBlock()
	 * @return indirect pointer
	 */
	public short findIndexBlock(){
		return this.indirect;
	}
	
	/* ----------------------------------------------------------------------//
	 * registerIndexBlock
	 * @purpose; register index block
	 * @return: true on success, false otherwise
	 */
	boolean registerIndexBlock(short num){
		for (int i=0; i < this.directSize; i++){
			if (this.direct[i] == -1) {
				//index block does not exist
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
	

	/* ----------------------------------------------------------------------
	 * findTargetBlock()
	 * @purpose: finds the target block of an inode based on the offset of
	 * 			the file passed in.
	 * @return: An int of the block found, or -1 if there is an error.
	 */
	int findTargetBlock(int offset){
		int targetBlock = offset/512;  //divided by blocksize
		if (targetBlock < this.directSize){
			return this.direct[targetBlock];
		}
		if (this.indirect < 0)
			return -1; //error
		
		//load the indexBlock to find the indirect pointer
		byte[] indirectArray = new byte[Disk.blockSize];
		SysLib.rawread(targetBlock, indirectArray);
		
		targetBlock -= this.directSize;
		return SysLib.bytes2short(indirectArray, targetBlock * 2);
	}
	
	/* ----------------------------------------------------------------------
	 * registerTargetBlock()
	 * Purpose: Registers the block passed in. If it needs to allocate an
	 * 			index block it will
	 * Returns: The int of the block it registers or -1 if there is an error.
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
	
	/* ----------------------------------------------------------------------
	 * unregisterIndexBlock()
	 * Purpose: marking -1 to index blocks
	 * Returns: byte array from the index block
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

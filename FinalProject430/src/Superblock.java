
public class Superblock {
	private final int defaultInodeBlocks = 64;
	public int totalBlocks; // the number of disk blocks
	public int totalInodes; // the number of inodes
	public int freeList;	// the block number of the free list's head
	
	public Superblock(int diskSize){
		// read the superblock from disk
		byte[] superblock = new byte[Disk.blockSize];
		SysLib.rawread(0, superblock);
		
		SysLib.cerr("befor read from index 0 for totalBlocks, diskSize: " +diskSize+ " \n");
		
		totalBlocks = SysLib.bytes2int(superblock, 0);
		
		SysLib.cerr("after read from index 0 for totalBlocks: " +totalBlocks+ " \n");
		
		totalInodes = SysLib.bytes2int(superblock, 4);
		
		SysLib.cerr("after read from index 4 for totalInodes: " +totalInodes+ " \n");
		
		freeList = SysLib.bytes2int(superblock, 8);
		SysLib.cerr("after read from index 8 for freeList: " +freeList+ " \n");
		
		if (totalBlocks == diskSize && totalInodes > 0 && freeList >= 2){
			// disk content are valid
			return;
		}else{
			SysLib.cerr("Now execute else condition.... \n");
			// need to format disk
			totalBlocks = diskSize;
			format(defaultInodeBlocks);
		}
	}
	
	public boolean format(int inodeBlocks){
		int totalByteForInode = inodeBlocks * 32;
		double totalBlockforInode = (double)totalByteForInode / 512;
		int totalBlockForInodeInInt = (int) Math.ceil(totalBlockforInode);  // 4
		
		totalInodes = inodeBlocks;
		freeList = totalBlockForInodeInInt + 1;
		
		Inode inode = new Inode();
		inode.flag = 0;
		
		SysLib.cerr("Before writing Inodes to Inode blocks.... \n");
		
		// wirte Inode to disk from block 1 to 4
		for (int i = 0; i < this.totalInodes; i++){
			inode.toDisk((short)i);
		}
		
		SysLib.cerr("after writing Inodes to Inode blocks.... \n");
		
		byte[] blockContent;
		for (int i = freeList; i < this.totalBlocks; i++){ // last block should store -1
			blockContent = new byte[Disk.blockSize];
			SysLib.int2bytes(i + 1, blockContent, 0);	// store next block Number to current for link to next block
			SysLib.rawwrite(i, blockContent);
		}
		
		this.sync();
		
		return true;
	}
	
	private void sync(){
		// store superblock
		byte[] blockZero = new byte[Disk.blockSize];

		SysLib.int2bytes(this.totalBlocks, blockZero, 0);
		SysLib.int2bytes(this.totalInodes, blockZero, 4);
		SysLib.int2bytes(this.freeList, blockZero, 8);

		SysLib.rawwrite(0, blockZero);
		// -------------done with superblock---------------------
	}
	
	// -------------------------getFreeBlock---------------------------------------
	public int getFreeBlock(){
		int i = this.freeList;
		
		if (i != -1){
			byte[] blockContent = new byte[Disk.blockSize];
			SysLib.rawread(i, blockContent);
			
			this.freeList = SysLib.bytes2int(blockContent, 0);
			
			SysLib.int2bytes(0, blockContent, 0);
			SysLib.rawwrite(i, blockContent);
		}
		
		return i;
	}
	
	// -------------------------returnBlock---------------------------------------
	public boolean returnBlock(int blockId){
		if (blockId >= 0){
			byte[] blockContent = new byte[512];
			// Initialize to zero
			for (int i = 0; i < Disk.blockSize; i++)
				blockContent[i] = 0;
			
			SysLib.int2bytes(this.freeList, blockContent, 0);	// set the link
			SysLib.rawwrite(blockId, blockContent);	// write back to disk
			
			this.freeList = blockId;
			return true;
		}
		
		return false;
	}
}

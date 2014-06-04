
public class Superblock {
	private final int defaultInodeBlocks = 64;
	public int totalBlocks; // the number of disk blocks
	public int totalInodes; // the number of inodes
	public int freeList;	// the block number of the free list's head
	
	public Superblock(int diskSize){
		// read the superblock from disk
		byte[] superblock = new byte[Disk.blockSize];
		SysLib.rawread(0, superblock);
		
		totalBlocks = SysLib.bytes2int(superblock, 0);
		totalInodes = SysLib.bytes2int(superblock, 4);
		freeList = SysLib.bytes2int(superblock, 8);
		
		if (totalBlocks == diskSize && totalInodes > 0 && freeList >= 2){
			// disk content are valid
			return;
		}else{
			// need to format disk
			totalBlocks = diskSize;
			format(defaultInodeBlocks);
		}
	}
	
	private void format(int inodeBlocks){
		int totalByteForInode = inodeBlocks * 32;
		double totalBlockforInode = (double)totalByteForInode / 512;
		int totalBlockForInodeInInt = (int) Math.ceil(totalBlockforInode);  // 4
		
		totalInodes = inodeBlocks;
		freeList = totalBlockForInodeInInt + 1;
		
		Inode inode = new Inode();
		inode.flag = 0;
		
		// wirte Inode to disk from block 1 to 4
		for (int i = 0; i < this.totalInodes; i++){
			inode.toDisk((short)i);
		}
		
		byte[] blockContent;
		for (int i = freeList; i < this.totalBlocks; i++){ // last block should store -1
			blockContent = new byte[Disk.blockSize];
			SysLib.int2bytes(i + 1, blockContent, 0);	// store next block Number to current for link to next block
			SysLib.rawwrite(i, blockContent);
		}
		
		this.sync();
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
	
	public int getFreeBlock(){
		int i = this.freeList;
		
		if (i != -1){
			byte[] blockContent = new byte[Disk.blockSize];
			SysLib.rawread(i, blockContent);
			int newFreeList = SysLib.bytes2int(blockContent, 0);
			
		}
		
		return 1;
	}
}

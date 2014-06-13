/* 
 * SuperBlock Class
 * @data June 12, 2014
 * @author Chamnap Lim
 * @author Rattanak Chea
 */

public class SuperBlock {
	private final int defaultInodeBlocks = 64;
	public int totalBlocks; // the number of disk blocks
	public int totalInodes; // the number of inodes
	public int freeList;	// the block number of the free list's head
	
	/* ----------------------------------------------------------------
	 * SuperBlock constructors
	 * @purpose: initialize totalBlocks, totalInodes, freeList
	 * 			and format disk
	 * @para: diskSize
	 */
	public SuperBlock(int diskSize){
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
	
	/* ----------------------------------------------------------------
	 * format(int inode)
	 * @para: inodeBlocks
	 * @purpose: format the superblock with number of inodeBlocks
	 */
	public boolean format(int inodeBlocks){
		int totalByteForInode = inodeBlocks * 32;
		//number blocks needed for Inodes
		double totalBlockforInode = (double)totalByteForInode / 512;
		int totalBlockForInodeInInt = (int) Math.ceil(totalBlockforInode);  // 4
		
		totalInodes = inodeBlocks;
		
		//first freeblock follows iNodeBlocks
		freeList = totalBlockForInodeInInt + 1;
		
		//instantiate new Inode
		Inode inode = new Inode();
		inode.flag = 0;
		
		// write Inode to disk from block 1 to 4
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
		return true;
	}
	
	public void sync(){
		// store superblock
		byte[] blockZero = new byte[Disk.blockSize];
		
		SysLib.int2bytes(this.totalBlocks, blockZero, 0);
		SysLib.int2bytes(this.totalInodes, blockZero, 4);
		SysLib.int2bytes(this.freeList, blockZero, 8);

		SysLib.rawwrite(0, blockZero);
	}
	
	/* ---------------------------------------------------------------
	 * getFreeBlock()
	 * @returns: the number of the next free block to be used in
	 * 			either the direct or indirect for the Inode
	 */
	public int getFreeBlock(){
		int i = this.freeList;  //block number from freelist	
		if (i != -1){
			//instantiate a new byte array data
			byte[] blockContent = new byte[Disk.blockSize];
			SysLib.rawread(i, blockContent);
			//freelist points to next block
			this.freeList = SysLib.bytes2int(blockContent, 0);
			SysLib.int2bytes(0, blockContent, 0);
			SysLib.rawwrite(i, blockContent);
		}
		//if i=-1, no free block to return, so return -1
		return i;
	}
	
	/* ---------------------------------------------------------------
	 * returnBlock()
	 * @para: blockId
	 * @returns: true if successful return a block, else false
	 */
	public boolean returnBlock(int blockId){
		if (blockId >= 0){
			byte[] blockContent = new byte[512];
			// Initialize to zero
			for (int i = 0; i < Disk.blockSize; i++)
				blockContent[i] = 0;

			SysLib.int2bytes(this.freeList, blockContent, 0);	// set the link
			//write next free block pointer to new free block
			SysLib.rawwrite(blockId, blockContent);	// write back to disk
			
			//freelist points to new block
			this.freeList = blockId;
			return true;
		}
		return false;
	}
}

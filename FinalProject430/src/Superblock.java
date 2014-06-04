
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
		
		// store superblock
		byte[] blockZero = new byte[Disk.blockSize];
		
		SysLib.int2bytes(this.totalBlocks, blockZero, 0);
		SysLib.int2bytes(this.totalInodes, blockZero, 4);
		SysLib.int2bytes(this.freeList, blockZero, 8);
		
		SysLib.rawwrite(0, blockZero);
		//-------------done with superblock---------------------
		
		for (int i = 1; i <= totalBlockForInodeInInt; i++){
			byte[] eachBlock = new byte[Disk.blockSize];
			int length = 0;
			short count = 0;
			short flag = 0;
			short[] direct = new short[11];
			for (int index = 0; index < 11; index++) direct[index] = -1;
			short indirect = -1;
			for (int j = 0 ; j < 512; j += 32){
				SysLib.int2bytes(0, eachBlock, j+0);
				SysLib.short2bytes((short)0, eachBlock, j+4);
			}
		}
		
	}
}

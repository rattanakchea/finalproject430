
public class TestSimple {

	public static void main(String[] args) {
		int inodes = 65;
		int inodesInByte = 65 * 32;
		double totalBlockForInode = (double)inodesInByte / 512;
		
		System.out.println("inodes: " + inodes);
		System.out.println("indoesInByte: " + inodesInByte);
		System.out.println("total block for Inode: " + Math.ceil(totalBlockForInode));
		
		
		byte[] superblock = new byte[Disk.blockSize];
		SysLib.int2bytes(1000, superblock, 0);
		SysLib.int2bytes(64, superblock, 4);
		
		int totalBlocks = SysLib.bytes2int(superblock, 0);
		int totalInodes = SysLib.bytes2int(superblock, 4);
		
		System.out.println("total blocks: " + totalBlocks);
		System.out.println("total inodes: " + totalInodes);
		
		
	}

}

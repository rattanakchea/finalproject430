
public class TestNap extends Thread {
	public void run(){
		byte[] superblock = new byte[Disk.blockSize];
		SysLib.rawread(0, superblock);
		
		
		int totalBlock = SysLib.bytes2int(superblock, 0);
		
		
		int totalInode = SysLib.bytes2int(superblock, 4);
		int freeList = SysLib.bytes2int(superblock, 8);
		
		SysLib.cout(totalBlock + "\n");
		SysLib.cout(totalInode + "\n");
		SysLib.cout(freeList + "\n");
		
		SysLib.cout("\n");
		
		SysLib.exit();
	}
}

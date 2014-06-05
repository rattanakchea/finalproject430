
public class TestSuperBlock extends Thread {
	public void run(){
		SuperBlock superBlock = new SuperBlock(1000);
		
		SysLib.cerr("alfter initial superBlock..\n");
		
		superBlock.format(48);
		
		byte[] superblock = new byte[512];
		
		SysLib.cerr("alfter allocate superlock byte array \n");
	    
		SysLib.rawread( 0, superblock );
	    
	    SysLib.cerr("alfter read block 0 from disk \n");
	    
	    int totalBlocks = SysLib.bytes2int( superblock, 0 );
	    int inodeBlocks = SysLib.bytes2int( superblock, 4 );
	    int freeList = SysLib.bytes2int( superblock, 8 );
	    
	    SysLib.cerr("totalBlocks = " + totalBlocks + " \n");
	    
	    if ( totalBlocks != 1000 ) {
	      SysLib.cout( "totalBlocks = " + totalBlocks + " (wrong)\n" );
	    }
		
		
		SysLib.exit();
	}
}

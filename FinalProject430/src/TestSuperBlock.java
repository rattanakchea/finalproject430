
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
		
	    Directory directory = new Directory(superBlock.totalInodes);
	    
	    short i = directory.ialloc("readme.txt");
	    SysLib.cerr("finish ialloc \n");
	    
	    short j = directory.namei("readme.txt");
	    if ( i == j){
	    	SysLib.cerr("i is equeal \n");
	    }else{
	    	SysLib.cerr("i is not equeal \n");
	    }
	    SysLib.cerr("i : " + i + " \n");
	    SysLib.cerr("j : " + j + " \n");
	    
	    if (directory.ifree(i)){
	    	SysLib.cerr("true\n");
	    }else{
	    	SysLib.cerr("false\n");
	    }
		
		SysLib.exit();
	}
}

/*
 * FileSystem Class
 * @data May 3, 2014
 */
public class FileSystem {
	private SuperBlock superblock;
	private Directory directory;
	private FileTable filetable;
	
	public static final int SEEK_SET = 0;
	public static final int SEEK_CUR = 1;
	public static final int SEEK_END = 2;

	public FileSystem(int diskBlocks) {
		superblock = new SuperBlock(diskBlocks);
		directory = new Directory(superblock.totalInodes);
		filetable = new FileTable(directory);

		// read the "/" file from disk
		FileTableEntry dirEnt = this.open("/", "r");
		int dirSize = fsize(dirEnt);
		if (dirSize > 0) {
			// directory has some data
			byte[] dirData = new byte[dirSize];
			read(dirEnt, dirData);
			directory.byte2directory(dirData);
		}
		this.close(dirEnt);
	}

	public boolean format(int paramInt) {
//		while (!this.filetable.fempty()) {	
//		}
		this.superblock.format(paramInt);

//		this.directory = new Directory(this.superblock.inodeBlocks);
//
//		this.filetable = new FileTable(this.directory);

		return true;
	}
	
	public FileTableEntry open(String fileName, String mode) throws InterruptedException{
		FileTableEntry localFileTableEntry = filetable.falloc(fileName, mode);
		
		if (mode.equalsIgnoreCase("w") && !this.deallocateAllBlocks(localFileTableEntry))
			return null;
		return localFileTableEntry;
	}

	// methods
	public void close(FileTableEntry dirEnt) {

	}

	// methods
	public int read(FileTableEntry dirEnt, byte[] data) {
		if (dirEnt.mode.equalsIgnoreCase("w") || dirEnt.mode.equalsIgnoreCase("a")){
			return -1;
		}
		
		int i = 0;
		int j = dirEnt.inode.length;
		
		synchronized(dirEnt){
			while( (j > 0) && (dirEnt.seekPtr < this.fsize(dirEnt))){
				int targetBlock = dirEnt.inode.findTargetBlock(dirEnt.seekPtr);
				if (targetBlock == -1) break;
				
				byte[] arrayOfByte = new byte[Disk.blockSize];
				SysLib.rawread(targetBlock, arrayOfByte);
				
				int m = dirEnt.seekPtr % Disk.blockSize;
				int n = Disk.blockSize - m;
				int i1 = fsize(dirEnt) - dirEnt.seekPtr;
				int i2 = Math.min(Math.min(n, j), i1);
				
				System.arraycopy(arrayOfByte, m, data, i, i2);
				
				dirEnt.seekPtr += i2;
				i += i2;
				j -= i2;
			}
		}
		
		return 1;
	}

	// methods
	public int fsize(FileTableEntry dirEnt) {
		synchronized(dirEnt){
			return dirEnt.inode.length;
		}
	}

	// methods
	public int write(int fd, byte[] buffer) {
		return 1;
	}

	// methods
	public int delete(String fileName) {
		return 1;
	}

	// methods
	public int seek(int fd, int offset, int whence) {
		return 1;
	}
	
	private boolean deallocateAllBlocks(FileTableEntry fte){
		if (fte.inode.count != -1){	// file has opened before
			return false;
		}
		
		byte[] inderectBlockContent = fte.inode.unregisterIndexBlock();	
		if (inderectBlockContent != null){
			int i = 0, j;
			while ( (j = SysLib.bytes2short(inderectBlockContent, i)) != -1 ){
				superblock.returnBlock(j);
			}
		}
		
		for (int i = 0; i < 11; i++){
			if (fte.inode.direct[i] != -1){
				superblock.returnBlock(fte.inode.direct[i]);
				fte.inode.direct[i] = -1;
			}
		}
		
		fte.inode.toDisk(fte.iNumber);
		return true;
	}

}

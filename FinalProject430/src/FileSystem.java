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
		return null;
	}

	// methods
	public void close(FileTableEntry dirEnt) {

	}

	// methods
	public void read(FileTableEntry dirEnt, byte[] data) {

	}

	// methods
	public int fsize(FileTableEntry dirEnt) {
		if (dirEnt == null)
			return -1;
		
		return dirEnt.inode.length;
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

}

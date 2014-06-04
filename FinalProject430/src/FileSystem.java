/*
 * FileSystem Class
 * @data May 3, 2014
 */
public class FileSystem {
	private Superblock superblock;
	private Directory directory;
	private FileTableEntry filetable;

	public FileSystem(int diskBlocks) {
		superblock = new Superblock(diskBlocks);
		directory = new Directory(superblock.totalInodes);
		filetable = new FileTableEntry(directory);

		// read the "/" file from disk
		FileTableEntry dirEnt = open("/", "r");
		int dirSize = fsize(dirEnt);
		if (dirSize > 0) {
			// directory has some data
			byte[] dirData = new byte[dirSize];
			read(dirEnt, dirData);
			directory.byte2directory(dirData);
		}
		close(dirEnt);
	}

	// methods
	public void close(FileTableEntry dirEnt) {

	}

	// methods
	public void read(FileTableEntry dirEnt, byte[] data) {

	}

	// methods
	public int fsize(FileTableEntry dirEnt) {
		return 1;
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

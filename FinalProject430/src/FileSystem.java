/*
 * FileSystem Class
 * @data May 3, 2014
 */
public class FileSystem {
	private Superblock superblock;
	pirvate Directory directory;
	private FileStructureTable filetable;
	
	public FileSystem(int diskBlocks){
		superblock = new SuperBlock(diskBlocks);
		directory = new Directory(superblock.totalInodes);
	}
}

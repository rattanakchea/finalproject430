/**
 * Inode Class
 * @date June 2, 2014
 * 
 */
public class Inode {
	private final static int iNodeSize = 32;	//fix to 32 bytes
	private final static int directSize = 11;	//#direct pointers
	
	public int length;		//file size in bytes
	public short count;		//# file-table entries pointing to this
	public short flag;		// 0 = unused, 1 = used
	public short direct[] = new short[directSize];	//direct pointers
	public short indirect;	//an indirect pointer
}

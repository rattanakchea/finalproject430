public class Directory{
	private static int maxChars = 30;	// max characters of each file name
	
	private int fsize[];	// each element stores a different file size.
	private char fnames[][];    // each element stores a different file name.
	
	public Directory(int maxInumber) { // directory constructor
		fsize = new int[maxInumber]; // maxInumber = max files
		for (int i = 0; i < maxInumber; i++)
			fsize[i] = 0; // all file size initialized to 0
		fnames = new char[maxInumber][maxChars];
		String root = "/"; // entry(inode) 0 is "/"
		fsize[0] = root.length(); // fsize[0] is the size of "/".
		root.getChars(0, fsize[0], fnames[0], 0); // fnames[0] includes "/"
	}
	
	public int bytes2directory(byte data[]) {
		// assumes data[] received directory information from disk
		// initializes the Directory instance with this data[]
		return 1;
	}
	
	public byte[] directory2bytes() {
		// converts and return Directory information into a plain byte array
		// this byte array will be written back to disk
		// note: only meaningfull directory information should be converted
		// into bytes.
		return new byte[1];
	}
	 
	public short ialloc(String filename) {
		// filename is the one of a file to be created.
		// allocates a new inode number for this filename
		return 1;
	}
	
	public boolean ifree(short iNumber) {
		// deallocates this inumber (inode number)
		// the corresponding file will be deleted.
		return false;
	}
	
	public short namei(String filename) {
		// returns the inumber corresponding to this filename
		return 1;
	}
}
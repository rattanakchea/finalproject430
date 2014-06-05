public class Directory{
	private static int maxChars = 30;	// max characters of each file name
	
	private int fsize[];//number of filename'characters, each element stores a different file size.
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
	
	public void bytes2directory(byte data[]) {
		// assumes data[] received directory information from disk
		// initializes the Directory instance with this data[]
		int i = 0;
		for (int j=0; i< this.fsize.length; j++){
			this.fsize[j] = SysLib.bytes2int(data, i);
			i += 4;
		}
		for (int j=0; j<this.fnames.length; j++, i+= maxChars * 2){
			String str = new String(data, i, maxChars * 2);
			str.getChars(0, this.fsize[j], this.fnames[j], 0);
		}
	}
	
	public byte[] directory2bytes() {
		// converts and return Directory information into a plain byte array
		// this byte array will be written back to disk
		// note: only meaningful directory information should be converted
		// into bytes.
		byte[] data = new byte[this.fsize.length * 4 + this.fnames.length * maxChars * 2];
		int i = 0;
		for (int j=0; i< this.fsize.length; j++){
			SysLib.int2bytes(this.fsize[j], data, i);
			i += 4;
		}
		for (int j=0; j<this.fnames.length; j++, i+= maxChars * 2){
			
			String str = new String(this.fnames[j], 0, this.fsize[j]);  //copy fname[j] to str
			byte[] data2 = str.getBytes();  //convert str to byte
			System.arraycopy(data2, 0, data, i, data2.length);
		}
		
		return data;
	}
	/*
	 * ialloc()
	 * check for empty fsize[] position 
	 *  
	 */
	public short ialloc(String filename) {
		// filename is the one of a file to be created.
		// allocates a new inode number for this filename
		for (int i=0; i<this.fsize.length; i++){
			if (this.fsize[i] == 0){  //empty iNode
				this.fsize[i] = Math.min(filename.length(), maxChars);
				filename.getChars(0, this.fsize[i], this.fnames[i], 0);
				return (short)i;
			}
		}
		return -1;
	}
	/*
	 * ifree()
	 * 
	 *  
	 */
	public boolean ifree(short iNumber) {
		// deallocates this inumber (inode number)
		// the corresponding file will be deleted.
		if (iNumber < 0 || iNumber >= this.fsize.length )
			return false;
		if (this.fsize[iNumber] != 0) {
			this.fsize[iNumber] = 0;
			return true;
		}
		
		return false;
	}
	/*
	 * namei()
	 * @return iNumber of the filename parameter if found
	 * else return -1  
	 */
	public short namei(String filename) {
		// returns the inumber corresponding to this filename
		for (int i=0; i<this.fsize.length; i++){
			String fn = String.valueOf(this.fnames[i], 0, this.fsize[i]);
			if (fn.equals(filename)){
				return (short)i;
			}	
		}
		return -1;
	}
}
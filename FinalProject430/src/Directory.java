/* 
 * Directory Class
 * @data June 12, 2014
 * @author Chamnap Lim
 * @author Rattanak Chea
 */

public class Directory{
	private static int maxChars = 30;	// max characters of each file name
	private int fsize[];//number of filename'characters, each element stores a different file size.
	private char fnames[][];    // each element stores a different file name.
	
	// directory constructor
	public Directory(int maxInumber) { 
		fsize = new int[maxInumber]; // maxInumber = max files
		for (int i = 0; i < maxInumber; i++)
			fsize[i] = 0; // all file size initialized to 0
		fnames = new char[maxInumber][maxChars];
		String root = "/"; // entry(inode) 0 is "/"
		fsize[0] = root.length(); // fsize[0] is the size of "/".
		root.getChars(0, fsize[0], fnames[0], 0); // fnames[0] includes "/"
	}
	
	
	/*-----------------------------------------------------------------
	 * byte2directory()
	 * @para: a byte array
	 * @purpose: take a byte array, and gets the file size based on the
	 * 			offset.
	 * @return: none
	 */
	public void bytes2directory(byte data[]) {
		// assumes data[] received directory information from disk
		// initializes the Directory instance with this data[]
		int i = 0;
		for (int j=0; i< this.fsize.length; j++){
			//load name length of this directory entry
			this.fsize[j] = SysLib.bytes2int(data, i);
			i += 4;
		}
		for (int j=0; j<this.fnames.length; j++, i+= maxChars * 2){
			//load names of this directory entry
			String str = new String(data, i, maxChars * 2);
			str.getChars(0, this.fsize[j], this.fnames[j], 0);
		}
	}
	
	/*-----------------------------------------------------------------
	 * directory2bytes()
	 * @para: none
	 * @purpose: converts and return Directory information into a plain byte array 
	 * this byte array will be written back to disk
	 * note: only meaningful directory information should be converted
	 * into bytes.
	 * @return: A byte array
	 */
	public byte[] directory2bytes() {
		//find the length of byte array to be written to disk
		int length = this.fsize.length * 4 + this.fnames.length * maxChars * 2;
		//create a byte array data 
		byte[] data = new byte[length];
		int i = 0;
		
		//loop through the array and pass the int2bytes
		for (int j=0; i< this.fsize.length; j++){
			SysLib.int2bytes(this.fsize[j], data, i);
			i += 4;
		}
		
		//loop through the array and update the name to
		//the directory
		for (int j=0; j<this.fnames.length; j++, i+= maxChars * 2){
			String str = new String(this.fnames[j], 0, this.fsize[j]);  //copy fname[j] to str
			byte[] data2 = str.getBytes();  //convert str to byte
			System.arraycopy(data2, 0, data, i, data2.length);
		}
		
		return data;
	}
	/*-------------------------------------------------------------------
	 * ialloc()
	 * @para: filename is the one of a file to be created.
	 * @purpose: allocates a directory, i.e a new inode number for this filename
	 *  @return: A short, the iNumber of the iNode used for this new file
	 */
	public short ialloc(String filename) {
		
		//loop through the fsize array until empty iNode is found
		for (int i=0; i<this.fsize.length; i++){
			if (this.fsize[i] == 0){  //empty iNode
				//filename length cannot be longer than maxChars
				this.fsize[i] = Math.min(filename.length(), maxChars);
				filename.getChars(0, this.fsize[i], this.fnames[i], 0);
				return (short)i;
			}
		}
		return -1;
	}
	/*
	 * ifree()
	 * @purpose: deallocates this inumber (inode number)
	 * 			the corresponding file will be deleted.
	 * @return : true on success, false otherwise.
	 */
	public boolean ifree(short iNumber) {
		//error check the iNumber number
		if (iNumber < 0 || iNumber >= this.fsize.length )
			return false;
		
		//not yet deallocated
		if (this.fsize[iNumber] != 0) {
			this.fsize[iNumber] = 0;
			return true;
		}
		return false;
	}
	/*
	 * namei()
	 * @para: filename string
	 * @return iNumber of the filename parameter if found
	 * if it is not found, return -1  
	 */
	public short namei(String filename) {
		// loop through the fsize to look for filename
		for (int i=0; i<this.fsize.length; i++){
			//convert to a string
			String fn = String.valueOf(this.fnames[i], 0, this.fsize[i]);
			//foudn the filename
			if (fn.equals(filename)){
				return (short)i;
			}	
		}
		return -1;
	}
}
import java.util.Vector;


public class FileTable {
	private Vector<FileTableEntry> table; // the actual entity of this file table
	private Directory dir; // the root directory
	
	public FileTable(Directory directory) { // constructor
		table = new Vector<FileTableEntry>(); // instantiate a file (structure) table
		dir = directory; // receive a reference to the Director
	}
	
	// major public methods
	public synchronized FileTableEntry falloc(String filename, String mode) throws InterruptedException {
		// allocate a new file (structure) table entry for this file name
		// allocate/retrieve and register the corresponding inode using dir
		// increment this inode's count
		// immediately write back this inode to the disk
		// return a reference to this file (structure) table entry
		
		short iNumber = -1;
		Inode inode = null;
		
		while(true){
			iNumber = filename.equals("/") ? 0 : dir.namei(filename);
			if (iNumber >= 0){
				inode = new Inode(iNumber);
				
				if (mode.equals("r")){
					if (inode.flag == Inode.UNUSED || inode.flag == Inode.USED || inode.flag == Inode.READ){
						inode.flag = Inode.READ;
						// no need to wait
						break;
					}else if (inode.flag == Inode.WRITE){
						// wait for write to exit						
						wait();
						break;
					}else if (inode.flag == Inode.DELETE){
						iNumber = -1;	// no more open
						return null;
					}
				}else{	// mode is w, w+, or a
					// ensure that WRITE and READ should both wait in this case when intending to write to this file
					if (inode.flag == Inode.UNUSED || inode.flag == Inode.USED){
						// the inode has never been modified so we will set it to writer mode and we're done
						inode.flag = Inode.WRITE;
						break;	// no need to wait
					}else if (inode.flag == Inode.WRITE || inode.flag == Inode.READ){
						// cannot write to the file, wait to be woken up
						wait();
						break;
					}else if (inode.flag == Inode.DELETE){ // the file has already been deleted
						iNumber = -1; // no more open
						return null;
					}
				}
			} else {	// the iNumber is negative so the file doesn't exist
				iNumber = dir.ialloc(filename);
				inode = new Inode();
				// we are creating a new inode to this file with the USED flag, write
		        // it to the disk below
		        break;
			}
		}
		
		inode.count++;
		inode.toDisk(iNumber);
		FileTableEntry fte = new FileTableEntry(inode, iNumber, mode);
		table.add(fte);
		
		return fte;
	}
	
	public synchronized boolean ffree(FileTableEntry e) {
		// receive a file table entry reference
		// save the corresponding inode to the disk
		// free this file table entry.
		// return true if this file table entry found in my table
	}
	
	public synchronized boolean fempty() {
		return table.isEmpty(); // return if table is empty
	}
	
}

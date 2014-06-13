/* 
 * FileTable Class
 * @data June 12, 2014
 * @author Chamnap Lim
 * @author Rattanak Chea
 */
import java.util.Vector;

public class FileTable {
	private Vector<FileTableEntry> table; // the actual entity of this file table
	private Directory dir; // the root directory
	
	public FileTable(Directory directory) { // constructor
		table = new Vector<FileTableEntry>(); // instantiate a file (structure) table
		dir = directory; // receive a reference to the Director
	}
	
	/* ---------------------------------------------------------------
	 * major public methods
	// allocate a new file (structure) table entry for this file name
	// allocate/retrieve and register the corresponding inode using dir
	// increment this inode's count
	// immediately write back this inode to the disk
	// return a reference to this file (structure) table entry
	
	 * FileTableEntry is created every time file is opened; doesn't matter if it is existed or not, but
	 * if file is existed, iNode will be the same and count inside Inode will be incremented.
	 */
	public synchronized FileTableEntry falloc(String filename, String mode) throws InterruptedException {
		
		short iNumber = -1;
		Inode inode = null;
		
		while(true){
			iNumber = filename.equals("/") ? 0 : dir.namei(filename);
			if (iNumber >= 0){	// if file existed in directory
				inode = new Inode(iNumber);	// load iNode from disk, which will have all information such as count, flag...
				
				if (mode.equals("r")){
					if (inode.flag == Inode.UNUSED || inode.flag == Inode.USED || inode.flag == Inode.READ){
						inode.flag = Inode.READ;
						// no need to wait
						break;
					}else if (inode.flag == Inode.WRITE){
						// wait for write to exit						
						wait();
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
					}else if (inode.flag == Inode.DELETE){ // the file has already been deleted
						iNumber = -1; // no more open
						return null;
					}
				}
			} else {	// the iNumber is negative so the file doesn't exist
				if (mode.equalsIgnoreCase("r")) // can't read because file is not exist in the directory
					return null;
				
				iNumber = dir.ialloc(filename);
				inode = new Inode();
				// we are creating a new inode to this file with the USED flag, write
		        // it to the disk below
				inode.flag = Inode.WRITE;
		        break;
			}
		}
		
		inode.count++;
		inode.toDisk(iNumber);
		FileTableEntry fte = new FileTableEntry(inode, iNumber, mode);
		table.add(fte);
		
		return fte;
	}
	
	/* ----------------------------------------------------------------------//
	 * ffree()
	 * @purpose: remove the FileTableEntry from the fileTable
	 * @para: FileTableEntry
	// @return : true if it was to fee the FileTableEntry, false otherwise
	 */
	public synchronized boolean ffree(FileTableEntry e) {
		// receive a file table entry reference
		// save the corresponding inode to the disk
		// free this file table entry.
		// return true if this file table entry found in my table
		if (table.remove(e)){
			e.inode.count--;
			e.inode.toDisk(e.iNumber);
			
			if (e.inode.flag == Inode.READ || e.inode.flag == Inode.WRITE){
				// last thread to close file
				if (e.inode.count == 0){
					e.inode.flag = Inode.USED;
				}
				notify();
			}
			return true;
		}
		
		return false;
	}
	
	/* ----------------------------------------------------------------------//
	 * fempty()
	 * @purpose: check if the filetable is empty. should be called before
	 * 			starting a format
	 * @return : return true if table is empty
	 */
	public synchronized boolean fempty() {
		return table.isEmpty();
	}
	
	/* ----------------------------------------------------------------------//
	 * getEntryAtInumber()
	 * @purpose: find the FileTableEntry specified by iNumber
	 * @return : return FileTableentry at iNumber if found
	 * otherwise return null
	 */
	public FileTableEntry getEntryAtInumber(int iNumber){
		for (int i = 0; i < table.size(); i++){
			FileTableEntry fte = table.get(i);
			if (fte.iNumber == iNumber){
				return fte;
			}
		}
		return null;
	}
	
}

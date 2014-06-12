/*
 * FileSystem Class
 * @data June 12, 2014
 */
public class FileSystem {
	private SuperBlock superblock;
	private Directory directory;
	private FileTable filetable;
	
	public static final int SEEK_SET = 0;
	public static final int SEEK_CUR = 1;
	public static final int SEEK_END = 2;

	public FileSystem(int diskBlocks) throws InterruptedException {
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
			directory.bytes2directory(dirData);
		}
		this.close(dirEnt);
	}

	public boolean format(int totalNodes) {

		this.superblock.format(totalNodes);
		this.directory = new Directory(totalNodes);
		this.filetable = new FileTable(directory);

		return true;
	}
	
	public FileTableEntry open(String fileName, String mode) throws InterruptedException{
		FileTableEntry localFileTableEntry = filetable.falloc(fileName, mode);
		
		if (mode.equalsIgnoreCase("w") && !this.deallocateAllBlocks(localFileTableEntry))
			return null;
		return localFileTableEntry;
	}

	// close()
	// Purpose: closes file and return boolean
	// return true if it able to close file
	public boolean close(FileTableEntry fte) {
		synchronized(fte){
			if (--fte.count > 0){
				return true;
			}
		}
		return filetable.ffree(fte);
	}

	// methods
	public int read(FileTableEntry fte, byte[] buffer) {
		if (fte.mode.equalsIgnoreCase("w") || fte.mode.equalsIgnoreCase("a")){
			return -1;
		}
		
		int bufferRead = 0;
		int bufferLength = buffer.length;
		int fileSize = this.fsize(fte);
		
		synchronized(fte){
			while (bufferLength > 0 && fte.seekPtr < fsize(fte)){
				// get current block
				int block = fte.inode.findTargetBlock(fte.seekPtr);
				
				// current block is null
				if (block == -1){
					return -1;
				}
				
				// array of byte for reading from disk
				byte[] readFromDisk = new byte[Disk.blockSize];
				
				// read data from current block
				SysLib.rawread(block, readFromDisk);
				
				// tempPtr for pointing where it is in current block
				int tempPtr = fte.seekPtr % 512;
				
				// check how much left in this block
				int different = Disk.blockSize - tempPtr;
				
				// check how much file left
				int fileLeft = fileSize - fte.seekPtr;
				
				if (bufferLength > different){	// amount want to read is greater than amount has left in current block
					if (fileLeft > different){	// can read the rest of byte left in current block
						System.arraycopy(readFromDisk, tempPtr, buffer, bufferRead, different);
						
						// update amount of read
						bufferRead += different;
						
						// update seek pointer to new position
						fte.seekPtr += different;
						
						// update buffer length
						bufferLength -= different;
					}else{
						System.arraycopy(readFromDisk, tempPtr, buffer, bufferRead, fileLeft);
						
						// update amount of read
						bufferRead += fileLeft;
						
						// update seek pointer to new position
						fte.seekPtr += fileLeft;
						
						// update buffer length
						bufferLength -= fileLeft;
					}
				}else{
					if (fileLeft > bufferLength){
						System.arraycopy(readFromDisk, tempPtr, buffer, bufferRead, bufferLength);
						
						// update amount of read
						bufferRead += bufferLength;
						
						// update seek pointer to new position
						fte.seekPtr += bufferLength;
						
						// update buffer length
						bufferLength -= bufferLength;
					}else{
						System.arraycopy(readFromDisk, tempPtr, buffer, bufferRead, fileLeft);
						
						// update amount of read
						bufferRead += fileLeft;
						
						// update seek pointer to new position
						fte.seekPtr += fileLeft;
						
						// update buffer length
						bufferLength -= fileLeft;
					}
				}
			}
		}
		
		return bufferRead;
	}

	// methods
	public int fsize(FileTableEntry dirEnt) {
		synchronized(dirEnt){
			return dirEnt.inode.length;
		}
	}

	// write
	public int write(FileTableEntry fte, byte[] buffer) {
		
		if (fte.mode.equalsIgnoreCase("r")){
			return -1;
		}
		
		int bufferWritten = 0;
		int bufferLength = buffer.length;
		
		synchronized(fte){
			// keep doing till there is nothing in the buffer
			while (bufferLength > 0){
				// get current block
				int block = fte.inode.findTargetBlock(fte.seekPtr);
				// the current block is null
				if (block == -1){
					short tempBlock = (short) this.superblock.getFreeBlock();
					
					switch (fte.inode.registerTargetBlock(fte.seekPtr, tempBlock)){
					case 0:
						break;
					case -2:
					case -1:
						SysLib.cerr("Thread OS: panic on write\n");
					case -3: // indirect block hasn't initialize yet
						// initialize indirect block
						short newBlock = (short) this.superblock.getFreeBlock();
						if (!fte.inode.registerIndexBlock(newBlock)){
							SysLib.cerr("Thread OS: panic on write\n");
							return -1;
						}
						
						if (fte.inode.registerTargetBlock(fte.seekPtr, tempBlock) != 0){
							SysLib.cerr("ThreadOS: panic on write\n");
				            return -1;
						}
						break;
					}
					block = tempBlock;
				}
				
				byte[] tempData = new byte[Disk.blockSize];
				
				SysLib.rawread(block, tempData);
				
				// create tempPtr to go through file
				int tempPtr = fte.seekPtr % Disk.blockSize;
				
				// find the different between current block and tempPtr
				int different = Disk.blockSize - tempPtr;
				
				// if the different left is larger than the buffer left
				if ( different > bufferLength){
					// copy buffer
					System.arraycopy(buffer, bufferWritten, tempData, tempPtr, bufferLength);
					// write to disk
					SysLib.rawwrite(block, tempData);
					
					// update seek pointer with new position
					fte.seekPtr += bufferLength;
					
					// update the amount that has written
					bufferWritten += bufferLength;
					
					// update buffer length
					bufferLength = 0;
				}else{
					System.arraycopy(buffer, bufferWritten, tempData, tempPtr, different);
					// write to disk
					SysLib.rawwrite(block, tempData);
					
					// update seek pointer with new position
					fte.seekPtr += different;
					
					// update the amount that has written
					bufferWritten += different;
					
					// update buffer length that has left
					bufferLength -= different;
				}
				
				// update the length of the file
				if (fte.seekPtr > fte.inode.length)
					fte.inode.length = fte.seekPtr;
			}
			
			// save inode to disk
			fte.inode.toDisk(fte.iNumber);
			
			// return the amount that has written to file
			return bufferWritten;
		}
	}

	// delete()
	// Purpose: remove file. Remove fileName from directory and file table entry from filetable
	public boolean delete(String fileName) throws InterruptedException {
		FileTableEntry fte = open(fileName, "w");
		
		short iNum = fte.iNumber;
		// remove this filen name from directory
		boolean removeFromDirectory = directory.ifree(iNum);
		
		// close file table entry
		boolean closeFTE = close(fte);
		
		return closeFTE && removeFromDirectory;
	}

	// seek()
	// Purpose: seeks through file as specified by user. The user can't seek 
	//			before a file or more than the size of a file.
	// return: 0 success. return -1 = failure
	public int seek(FileTableEntry ftEnt, int offset, int whence) {
		synchronized(ftEnt){
			// set seek pointer to offset from beginning of the file
			if (whence == this.SEEK_SET){
				if (offset < 0) offset = 0; // user gives negative number for offset
				if (offset > fsize(ftEnt)) offset= fsize(ftEnt);	// offset can't greater than file size
				ftEnt.seekPtr = offset;
			} else if (whence == this.SEEK_CUR){ // set seek pointer to offset + current seek pointer position
				int newSeekPointer = offset + ftEnt.seekPtr;
				if (newSeekPointer < 0) newSeekPointer = 0;
				if (newSeekPointer > fsize(ftEnt))	newSeekPointer = fsize(ftEnt);
				ftEnt.seekPtr = newSeekPointer;
			} else if (whence == this.SEEK_END){
				int newSeekPointer = offset + fsize(ftEnt);
				if (newSeekPointer < 0) newSeekPointer = 0;
				if (newSeekPointer > fsize(ftEnt)) newSeekPointer = fsize(ftEnt);
				ftEnt.seekPtr = newSeekPointer;
			}else{
				return -1;
			}
		}
		return ftEnt.seekPtr;
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
	
	public int sync() throws InterruptedException{
		// open directory
		FileTableEntry fte = open("/", "w");
		
		// create byte array to store directory content
		byte[] directoryByte = directory.directory2bytes();
		
		// write the file table entry to the byte directory
		write(fte, directoryByte);
		
		// close file entry table
		close(fte);
		
		superblock.sync();
		
		return 0;
	}

}

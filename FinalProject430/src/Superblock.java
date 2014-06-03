/**
 * @author rattanak
 * @date June 2, 2014
 * 
 */
public class Superblock {
	public int totalBlocks;  //the number of disk blocks
	public int totalInodes;  //the number of inodes
	public int freeList;	//the block number of the free list's head
	
	//default constructor
	public Superblock(){
		
		
	}
}

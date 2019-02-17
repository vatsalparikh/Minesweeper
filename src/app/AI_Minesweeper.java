/*
==================================================================================================================
Project - Minesweeper

This AI program solves a minesweeper game. It queries the user for a cell and based on the input from the user it
builds a knowledge base and take further decisions.

Run to begin Minesweeper solver. First enter vertical length of the board, followed by horizontal width of the board.
Program will query for moves in the form of X, Y. X corresponds to horizontal position, Y corresponds to vertical
position. Numerical labels along the top and left side of the output indicate position.   

Legend:
? - uncovered cell
* - cell marked as mine
1-8 - searched cell, numeric indicates how many adjacent mines
==================================================================================================================
*/

package app;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class AI_Minesweeper {
	
	static int length, width;
	static char[][] cluesBoard; //holds the clues queried from the user, board that is printed
	static boolean[][] cluesFound;
	static Queue<int[]> queue;
	static int[][][][] probClue; //our knowledge base: at index [a][b][c][d], holds the local adjacent cell clues on position [c][d] from the clue in [a][b]
	static LinkedHashMap<String,String> treeOfInfluence=new LinkedHashMap<String,String>();
	
	public static void main(String[] args){
		System.out.println("Initializing Minesweeper...");
		Scanner in = new Scanner(System.in);
		queue = new LinkedList<int[]>();
				
		System.out.println("Enter length of board: ");
		length = in.nextInt();
		System.out.println("Enter width of board: ");
		width = in.nextInt();
		
		probClue = new int[length][width][3][3]; // -1 - Unknown, 0 - Found/Safe
		cluesFound = new boolean[length][width];
		
		cluesBoard = createBoard(length, width);
		populateProbClue(length,width); // builds the initial knowledge base of the board in a 4D array
		
		int x = width/2 + 1;
		int y = length/2 + 1;
		int[] XY = getXY(x, y);
		
		System.out.println("First move (Row,Col) : (" + x + "," + y+")");
		
		treeOfInfluence.put(constructString(x, y),"Random query to User");
		
		System.out.println("Enter numeric state of specified cell 0-9: ");
		System.out.println("(If mine, enter 9)");
		printBoard(cluesBoard);
		int state = in.nextInt();
		
		putProbClue(XY[0], XY[1], state); // Enters the clue into the knowledge base and updates the surrounding local cells
		
		while(checkGameInPlay()){	//loop while in play
			expandProbClues(); // keeps track of the number of local surrounding unknown cells in 4D array
			neighbourClues(); // marks cells either mine or safe if there are any
			neighbourExplore(); // manipulates the knowledge base with the local surrounding to find mine if any possible
			
			System.out.println("Clues: ");
			printBoard(cluesBoard);
			boolean userInput=false;
			int[] nextXY;
			if (!queue.isEmpty()){
				nextXY = queue.remove();
			} else {
				nextXY = nextRequestedXY();
				userInput=true;
			}
			int[]next = getCoordinate(nextXY[0], nextXY[1]);
			System.out.println("Enter state of next move: " + next[0] + ", " + next[1]);
			if(userInput)
				treeOfInfluence.put(constructString(next[0], next[1]),"Random query to User"); // Constructs the chain of influencce in the form of (child,parent)
			
			int command = in.nextInt();
			if(command == 9) {
				break;
			}
			putProbClue(nextXY[0], nextXY[1], command);
			
		}
		
		System.out.println("################# Game Over. Final board: #################");
		printBoard(cluesBoard);
		
		System.out.println("Do you want to view the Chain of Influence? Y/N");
		String option=in.next();
		if(option.equals("Y") || option.equals("y")) {
				printHashMap(treeOfInfluence);
		}
		
		in.close();
	}
	
	/*
	 * Initially populates the probClue 4D array based on the position of a particular cell in the board
	 */
	public static void populateProbClue(int length, int width) {
		for(int a=0; a<probClue.length; a++) {
			for(int b=0; b<probClue[0].length; b++) {
				for(int i=0;i<3;i++) {
					for(int j=0;j<3;j++) {
							probClue[a][b][i][j]=-1;
					}
				}
				int outBound=0;
				if(a==0 || b==0 || a==probClue.length-1 || b==probClue[0].length-1) {
					outBound=3; // Cells at the corner (1,1) (1,n) (n,1) (n,n) have only 3 surrounding cells
					if((a==0 && b==0) || (a==probClue.length-1 && b==probClue[0].length-1) || (a==0 && b==probClue[0].length-1) || (a==probClue.length-1 && b==0))
						outBound=5; //cells at the edges (1,2( (2,1) etc. have only 5 surrounding cells
				}
				probClue[a][b][1][1]=8-outBound;
			}
		}
		
		for(int i=0; i<width; i++) {
			for(int j=0; j<3; j++) {
				probClue[0][i][0][j]=0;
				probClue[length-1][i][2][j]=0;
			}
		}
		
		for(int i=0; i<length; i++) {
			for(int j=0; j<3; j++) {
				probClue[i][0][j][0]=0;
				probClue[i][width-1][j][2]=0;
			}
		}
		expandProbClues();
	}
	
	/*
	 * Function to place the clue in clueBoard, and add clue to our knowledge base. 
	 * Updates probClue for (x,y) cell and also the surrounding cells knowledge of this particular cell.
	 */
	public static void putProbClue(int x, int y, int clue) {
		
		if (clue == 9){
			cluesBoard[x][y] = '*';
		} else {
			cluesBoard[x][y] = (char)(clue+48);
		}
		
		probClue[x][y][1][1]=clue;
		cluesFound[x][y]=true;
		
		if (x-1>=0 && y-1>=0) {
			probClue[x-1][y-1][2][2]=0;
			if(cluesBoard[x-1][y-1] == '?')
				--probClue[x-1][y-1][1][1]; //when there is a safe cell found at (3,3), the number of unknown cells at (2,2) reduces by 1
		}
		if (x-1>=0 && y>=0) {
			probClue[x-1][y][2][1]=0;
			if(cluesBoard[x-1][y] == '?')
				--probClue[x-1][y][1][1];
		}
		if (x-1>=0 && y+1<width) {
			probClue[x-1][y+1][2][0]=0;
			if(cluesBoard[x-1][y+1] == '?')
				--probClue[x-1][y+1][1][1];
		}
		if (x>=0 && y-1>=0) {
			probClue[x][y-1][1][2]=0;
			if(cluesBoard[x][y-1] == '?')
				--probClue[x][y-1][1][1];
		}
		if (x>=0 && y+1<width) {
			probClue[x][y+1][1][0]=0;
			if(cluesBoard[x][y+1] == '?')
				--probClue[x][y+1][1][1];
		}
		if (x+1<length && y-1>=0) {
			probClue[x+1][y-1][0][2]=0;
			if(cluesBoard[x+1][y-1] == '?')
				--probClue[x+1][y-1][1][1];
		}
		if (x+1<length && y>=0) {
			probClue[x+1][y][0][1]=0;
			if(cluesBoard[x+1][y] == '?')
				--probClue[x+1][y][1][1];
		}
		if (x+1<length && y+1<width) {
			probClue[x+1][y+1][0][0]=0;
			if(cluesBoard[x+1][y+1] == '?')
				--probClue[x+1][y+1][1][1];
		}
		
		
		if(probClue[x][y][1][1] == 0) {
			for (int[] row : probClue[x][y]) {
			    Arrays.fill(row, 0);
			}
			
			if (x-1>=0 && y-1>=0 && cluesBoard[x-1][y-1] == '?' && cluesFound[x-1][y-1]==false) {
				pushToQueue(x-1,y-1); // when the cell value of (i,j) is 0, all the surrounding cells are all safe.
				treeOfInfluence.put(constructString(x-1+1, y-1+1),constructString(x+1, y+1));
			}
			if (x-1>=0 && y>=0 && cluesBoard[x-1][y] == '?' && cluesFound[x-1][y]==false) {
				pushToQueue(x-1,y);
				treeOfInfluence.put(constructString(x-1+1, y+1),constructString(x+1, y+1));
			}
			if (x-1>=0 && y+1<width && cluesBoard[x-1][y+1] == '?' && cluesFound[x-1][y+1]==false) {
				pushToQueue(x-1,y+1);
				treeOfInfluence.put(constructString(x-1+1, y+1+1),constructString(x+1, y+1));
			}
			if (x>=0 && y-1>=0 && cluesBoard[x][y-1] == '?' && cluesFound[x][y-1]==false) {
				pushToQueue(x,y-1);
				treeOfInfluence.put(constructString(x+1, y-1+1),constructString(x+1, y+1));
			}
			if (x>=0 && y+1<width && cluesBoard[x][y+1] == '?' && cluesFound[x][y+1]==false) {
				pushToQueue(x,y+1);
				treeOfInfluence.put(constructString(x+1, y+1+1),constructString(x+1, y+1));
			}
			if (x+1<length && y-1>=0 && cluesBoard[x+1][y-1] == '?' && cluesFound[x+1][y-1]==false) {
				pushToQueue(x+1,y-1);
				treeOfInfluence.put(constructString(x+1+1, y-1+1),constructString(x+1, y+1));
			}
			if (x+1<length && y>=0 && cluesBoard[x+1][y] == '?' && cluesFound[x+1][y]==false) {
				pushToQueue(x+1,y);
				treeOfInfluence.put(constructString(x+1+1, y+1),constructString(x+1, y+1));
			}
			if (x+1<length && y+1<width && cluesBoard[x+1][y+1] == '?' && cluesFound[x+1][y+1]==false) {
				pushToQueue(x+1,y+1);
				treeOfInfluence.put(constructString(x+1+1, y+1+1),constructString(x+1, y+1));
			}
		}
		expandProbClues();
		neighbourClues();
		neighbourExplore();
	}
	
	/*
	 * Add a cell to the queue to be queried, marked as queried.
	 */
	public static void pushToQueue(int x, int y) {
		queue.add(new int[]{x, y});
		cluesFound[x][y]=true;
	}
	
	/*
	 *  Recounts the number of unknown adjacent cell for each (a,b) and updates it in (a,b,1,1)
	 */
	public static void expandProbClues() {
		for(int a=0;a<probClue.length;a++) {
			for(int b=0;b<probClue[0].length;b++) {
				int tempVal=0;
				for(int i=0; i<3; i++) {
					for(int j=0; j<3; j++) {
						if(!(i==1 && j==1))
							tempVal+=probClue[a][b][i][j];
					}
				}
				if(cluesBoard[a][b]=='*') {
					probClue[a][b][1][1]=0;
				} else if(cluesBoard[a][b]=='?') {
					probClue[a][b][1][1]=Math.abs(tempVal);
				} else if(cluesBoard[a][b]!='*' && cluesBoard[a][b]!='?') {
					probClue[a][b][1][1]=Character.getNumericValue(cluesBoard[a][b])-neighbourMines(a, b);
				}
			}
		}
	}
	
	/*
	 * Function to Automatically flag adjacent cells as mines if number of adjacent mines equals number of adjacent unknown cells,
	 * also marks adjacent cells as clear if all adjacent mines are already found.
	 */
	public static void neighbourClues() {
		for(int i=0; i<length; i++) {
			for(int j=0; j<width; j++) {
				if(cluesBoard[i][j] != '*' && cluesBoard[i][j] != '?'  && (Character.getNumericValue(cluesBoard[i][j])-neighbourMines(i,j)) == neighbourUnknowns(i, j)) {
					if (i-1>=0 && j-1>=0 && cluesBoard[i-1][j-1] == '?') {
						flagMines(i-1,j-1);
						treeOfInfluence.put(constructString(i-1+1, j-1+1),constructString(i+1, j+1));
					}
					if (i-1>=0 && j>=0 && cluesBoard[i-1][j] == '?') {
						flagMines(i-1,j);
						treeOfInfluence.put(constructString(i-1+1, j+1),constructString(i+1, j+1));
					}
					if (i-1>=0 && j+1<width && cluesBoard[i-1][j+1] == '?') {
						flagMines(i-1,j+1);
						treeOfInfluence.put(constructString(i-1+1, j+1+1),constructString(i+1, j+1));
					}
					if (i>=0 && j-1>=0 && cluesBoard[i][j-1] == '?') {
						flagMines(i,j-1);
						treeOfInfluence.put(constructString(i+1, j-1+1),constructString(i+1, j+1));
					}
					if (i>=0 && j+1<width && cluesBoard[i][j+1] == '?') {
						flagMines(i,j+1);
						treeOfInfluence.put(constructString(i+1, j+1+1),constructString(i+1, j+1));
					}
					if (i+1<length && j-1>=0 && cluesBoard[i+1][j-1] == '?') {
						flagMines(i+1,j-1);
						treeOfInfluence.put(constructString(i+1+1, j-1+1),constructString(i+1, j+1));
					}
					if (i+1<length && j>=0 && cluesBoard[i+1][j] == '?') {
						flagMines(i+1,j);
						treeOfInfluence.put(constructString(i+1+1, j+1),constructString(i+1, j+1));
					}
					if (i+1<length && j+1<width && cluesBoard[i+1][j+1] == '?') {
						flagMines(i+1,j+1);
						treeOfInfluence.put(constructString(i+1+1, j+1+1),constructString(i+1, j+1));
					}
				}
				
				if(cluesBoard[i][j] != '*' && cluesBoard[i][j] != '?'  && (Character.getNumericValue(cluesBoard[i][j])-neighbourMines(i,j)) == 0) {
					if (i-1>=0 && j-1>=0 && cluesBoard[i-1][j-1] == '?' && cluesFound[i-1][j-1]==false) {
						pushToQueue(i-1,j-1);
						treeOfInfluence.put(constructString(i-1+1, j-1+1),constructString(i+1, j+1));
					}
					if (i-1>=0 && j>=0 && cluesBoard[i-1][j] == '?' && cluesFound[i-1][j]==false) {
						pushToQueue(i-1,j);
						treeOfInfluence.put(constructString(i-1+1, j+1),constructString(i+1, j+1));
					}
					if (i-1>=0 && j+1<width && cluesBoard[i-1][j+1] == '?' && cluesFound[i-1][j+1]==false) {
						pushToQueue(i-1,j+1);
						treeOfInfluence.put(constructString(i-1+1, j+1+1),constructString(i+1, j+1));
					}
					if (i>=0 && j-1>=0 && cluesBoard[i][j-1] == '?' && cluesFound[i][j-1]==false) {
						pushToQueue(i,j-1);
						treeOfInfluence.put(constructString(i+1, j-1+1),constructString(i+1, j+1));
					}
					if (i>=0 && j+1<width && cluesBoard[i][j+1] == '?' && cluesFound[i][j+1]==false) {
						pushToQueue(i,j+1);
						treeOfInfluence.put(constructString(i+1, j+1+1),constructString(i+1, j+1));
					}
					if (i+1<length && j-1>=0 && cluesBoard[i+1][j-1] == '?' && cluesFound[i+1][j-1]==false) {
						pushToQueue(i+1,j-1);
						treeOfInfluence.put(constructString(i+1+1, j-1+1),constructString(i+1, j+1));
					}
					if (i+1<length && j>=0 && cluesBoard[i+1][j] == '?' && cluesFound[i+1][j]==false) {
						pushToQueue(i+1,j);
						treeOfInfluence.put(constructString(i+1+1, j+1),constructString(i+1, j+1));
					}
					if (i+1<length && j+1<width && cluesBoard[i+1][j+1] == '?' && cluesFound[i+1][j+1]==false) {
						pushToQueue(i+1,j+1);
						treeOfInfluence.put(constructString(i+1+1, j+1+1),constructString(i+1, j+1));
					}
				}
			}
		}
	}
	
	/*
	 * Function to explore related knowledge base facts in probClue and mark known mines as mine.
	 * This function manipulates the knowledge base by finding the difference between one cell's KB and another
	 */
	public static void neighbourExplore() {
		
		for(int i=0; i<probClue.length; i++) {
			for(int j=0; j<probClue[0].length; j++) {
				if(cluesBoard[i][j] != '?' && cluesBoard[i][j] != '0' && cluesBoard[i][j] != '*') {
					int[][] leftTop = null, centreTop = null, rightTop = null, leftCentre = null, centre = null, rightCentre = null, leftBot = null, centreBot = null, rightBot = null;
					
					if (i-1>=0 && j-1>=0 && cluesBoard[i-1][j-1] != '?')	leftTop=copy2DArray(probClue[i-1][j-1]);
					if (i-1>=0 && j>=0 && cluesBoard[i-1][j] != '?')	centreTop=copy2DArray(probClue[i-1][j]);
					if (i-1>=0 && j+1<width && cluesBoard[i-1][j+1] != '?')	rightTop=copy2DArray(probClue[i-1][j+1]);
					if (i>=0 && j-1>=0 && cluesBoard[i][j-1] != '?')	leftCentre=copy2DArray(probClue[i][j-1]);
					centre=copy2DArray(probClue[i][j]);
					if (i>=0 && j+1<width && cluesBoard[i][j+1] != '?')	rightCentre=copy2DArray(probClue[i][j+1]);
					if (i+1<length && j-1>=0 && cluesBoard[i+1][j-1] != '?')	leftBot=copy2DArray(probClue[i+1][j-1]);
					if (i+1<length && j>=0 && cluesBoard[i+1][j] != '?')	centreBot=copy2DArray(probClue[i+1][j]);
					if (i+1<length && j+1<width && cluesBoard[i+1][j+1] != '?')	rightBot=copy2DArray(probClue[i+1][j+1]);

					if(leftTop!=null) {
						centre[0][1]-=leftTop[1][2];
						centre[1][0]-=leftTop[2][1];
						centre[1][1]-=leftTop[1][1];
					}
					if(rightTop!=null) {
						centre[0][1]-=rightTop[1][0];
						centre[1][2]-=rightTop[2][1];
						centre[1][1]-=rightTop[1][1];
					}
					if(leftBot!=null) {
						centre[1][0]-=leftBot[0][1];
						centre[2][1]-=leftBot[1][2];
						centre[1][1]-=leftBot[1][1];
					}
					if(rightBot!=null) {
						centre[1][2]-=rightBot[0][1];
						centre[2][1]-=rightBot[1][0];
						centre[1][1]-=rightBot[1][1];
					}
					if(centreTop!=null) {
						for(int a=0; a<2; a++) {
							for(int b=0; b<3; b=b+2) {
								centre[a][b]-=centreTop[a+1][b];
							}
						}
						centre[1][1]-=centreTop[1][1];
					}
					if(leftCentre!=null) {
						for(int a=0; a<3; a=a+2) {
							for(int b=0; b<2; b++) {
								centre[a][b]-=leftCentre[a][b+1];
							}
						}
						centre[1][1]-=leftCentre[1][1];
					}
					if(rightCentre!=null) {
						for(int a=0; a<3; a=a+2) {
							for(int b=1; b<3; b++) {
								centre[a][b]-=rightCentre[a][b-1];
							}
						}
						centre[1][1]-=rightCentre[1][1];
					}
					if(centreBot!=null) {
						for(int a=1; a<3; a++) {
							for(int b=0; b<3; b=b+2) {
								centre[a][b]-=centreBot[a-1][b];
							}
						}
						centre[1][1]-=centreBot[1][1];
					}
					

					int count=0;
					for(int x=0;x<3;x++) {
						for(int y=0;y<3;y++) {
							if(!(x==1 && y==1) && centre[x][y]==-1)	count++;
						}
					}
					if(centre[1][1] == count) {
						for(int x=0;x<3;x++) {
							for(int y=0;y<3;y++) {
								if(centre[x][y]==-1) {
									flagMines(i-1+x, j-1+y);
									treeOfInfluence.put(constructString(i-1+x, j-1+y),constructString(i+1, j+1));
								}
							}
						}
					}
					
				}
			}
		}
	}
	
	/*
	 * Function to count the number of adjacent unknown cells.
	 */
	public static int neighbourUnknowns(int x, int y) {
		int neighourUnknowns=0;
		if (x-1>=0 && y-1>=0 && cluesBoard[x-1][y-1] == '?')	++neighourUnknowns;
		if (x-1>=0 && y>=0 && cluesBoard[x-1][y] == '?')	++neighourUnknowns;
		if (x-1>=0 && y+1<width && cluesBoard[x-1][y+1] == '?')	++neighourUnknowns;
		if (x>=0 && y-1>=0 && cluesBoard[x][y-1] == '?')	++neighourUnknowns;
		if (x>=0 && y+1<width && cluesBoard[x][y+1] == '?')	++neighourUnknowns;
		if (x+1<length && y-1>=0 && cluesBoard[x+1][y-1] == '?')	++neighourUnknowns;
		if (x+1<length && y>=0 && cluesBoard[x+1][y] == '?')	++neighourUnknowns;
		if (x+1<length && y+1<width && cluesBoard[x+1][y+1] == '?')	++neighourUnknowns;
		
		return neighourUnknowns;
	}
	
	/*
	 * Function to count the number of adjacent mines.
	 */
	public static int neighbourMines(int x, int y) {
		int neighbourMines=0;
		if (x-1>=0 && y-1>=0 && cluesBoard[x-1][y-1] == '*')	++neighbourMines;
		if (x-1>=0 && y>=0 && cluesBoard[x-1][y] == '*')	++neighbourMines;
		if (x-1>=0 && y+1<width && cluesBoard[x-1][y+1] == '*')	++neighbourMines;
		if (x>=0 && y-1>=0 && cluesBoard[x][y-1] == '*')	++neighbourMines;
		if (x>=0 && y+1<width && cluesBoard[x][y+1] == '*')	++neighbourMines;
		if (x+1<length && y-1>=0 && cluesBoard[x+1][y-1] == '*')	++neighbourMines;
		if (x+1<length && y>=0 && cluesBoard[x+1][y] == '*')	++neighbourMines;
		if (x+1<length && y+1<width && cluesBoard[x+1][y+1] == '*')	++neighbourMines;

		return neighbourMines;
	}
	
	/*
	 * Flags a cell as containing a mine. Updates the adjacent mines count in adjacentMines.
	 */
	public static void flagMines(int i, int j) {
		cluesBoard[i][j] = '*';
		for (int[] row : probClue[i][j])
		    Arrays.fill(row, 0);
		
		System.out.println("Mine Found at ::: Row-"+(i+1)+" Column-"+(j+1));
		
		if (i-1>=0 && j-1>=0) {
			probClue[i-1][j-1][2][2]=0;
			--probClue[i-1][j-1][1][1];
		}
		if (i-1>=0 && j>=0) {
			probClue[i-1][j][2][1]=0;
			--probClue[i-1][j][1][1];
		}
		if (i-1>=0 && j+1<width) {
			probClue[i-1][j+1][2][0]=0;
			--probClue[i-1][j+1][1][1];
		}
		if (i>=0 && j-1>=0) {
			probClue[i][j-1][1][2]=0;
			--probClue[i][j-1][1][1];
		}
		if (i>=0 && j+1<width) {
			probClue[i][j+1][1][0]=0;
			--probClue[i][j+1][1][1];
		}
		if (i+1<length && j-1>=0) {
			probClue[i+1][j-1][0][2]=0;
			--probClue[i+1][j-1][1][1];
		}
		if (i+1<length && j>=0) {
			probClue[i+1][j][0][1]=0;
			--probClue[i+1][j][1][1];
		}
		if (i+1<length && j+1<width) {
			probClue[i+1][j+1][0][0]=0;
			--probClue[i+1][j+1][1][1];
		}
		
		expandProbClues();
	}
	
	/*
	 * Function to grab the next Cell to query.
	 */
	public static int[] nextRequestedXY(){
		int[] XY = new int[2];
		int x, y;
		do{
			x = (int)(Math.random() * length);
			y = (int)(Math.random() * width);
		} while (cluesBoard[x][y]!='?');
		XY[0] = x;
		XY[1] = y;
		return XY;
	}
	
	/*
	 * Function to initialize cluesBoard
	 */
	public static char[][] createBoard(int length, int width){
		char[][] clues = new char[length][width];
		for (int i=0; i<length; i++){
			for (int j=0; j<width; j++){
				clues[i][j] = '?';
			}
		}
		return clues;
	}
	
	/*
	 * Function to convert input into appropriate array indices.
	 */
	public static int[] getXY(int input1, int input2){
		int x = input1-1;
		int y = input2-1;
		return new int[]{x, y};
	}
	
	/*
	 * Function to convert array indices to board coordinates.
	 */
	public static int[] getCoordinate(int x, int y){
		int input1 = x+1;
		int input2 = y+1;
		return new int[]{input1, input2};
	}
	
	/*
	 * Function to print cluesBoard
	 */
	public static void printBoard(char[][] board){
		if (board == null){
			return;
		}
		System.out.print("    ");
		for (int j = 0; j<board[0].length; j++){
			System.out.print((j+1) + " ");
		}
		System.out.println();
		System.out.println();
		for (int i = 0; i< board.length; i++){
			System.out.print(i+1 + "   ");
			for (int j = 0; j < board[0].length; j++){
				System.out.print(board[i][j]);
				if (j == board[0].length - 1) {
					System.out.println(" ");
				} else {
					System.out.print(" ");
				}
			}
		}
		System.out.println();
	}
	
	/*
	 * Function to determine if game is in play by detecting mines (*) on the board, or if there are any more unsearched (?) cells
	 */
	public static boolean checkGameInPlay(){
		for (int i=0; i<cluesBoard.length; i++){
			for (int j=0; j<cluesBoard[0].length; j++){
				if (cluesBoard[i][j]=='?') return true;
			}
		}
		return false;
	}
	
	/*
	 * Function to determine if char is numeric. Used to read clues.
	 */
	public static boolean isNumClue(char c){
		if (c=='0' || c=='1' || c=='2' || c=='3' || c=='4' || c=='5' || c=='6' || c=='7' || c=='8' || c=='9') return true;
		return false;
	}

	/*
	 * Function to create a copy of a 2D array.
	 */
	public static int[][] copy2DArray(int[][] inputArr) {
		int[][] copyArr = new int[inputArr.length][inputArr[0].length];
		for(int i=0;i<inputArr.length;i++) {
			for(int j=0;j<inputArr[0].length;j++) {
				copyArr[i][j]=inputArr[i][j];
			}
		}
		return copyArr;
	}
	
	/*
	 * Function to convert (i,j) into a string
	 */
	public static String constructString(int i, int j) {
		return i+"-"+j;
	}
	
	/*
	 * Function to print the chain of influence knowledge Base
	 */
	public static void printHashMap(LinkedHashMap<String,String> map) {
		System.out.println();
		System.out.println("Child -> Parent");
		System.out.println();
		
		for(int i=1;i<=length;i++) {
			for(int j=1;j<=width;j++) {
				String key=i+"-"+j;
				System.out.print(key+" -> ");
				while((! map.get(key).equals("Random query to User"))) {
					key = map.get(key);
					System.out.print(key+" -> ");
				}
				System.out.println("Random query to User");
			}
		}
	}
}
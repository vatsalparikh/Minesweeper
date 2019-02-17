/*
================================================================================================================
Project - Minesweeper

Class for Part 1 of Minesweeper Assignment; 

Legend:
? - uncovered cell
M - cell marked as mine
C - cell marked as clear
1 - searched cell, numeric indicates how many adjacent mines
* - searched cell, indicates hit mine, game over
================================================================================================================
*/

package app;
import java.util.Scanner;
import java.util.Stack;

public class Minesweeper{
	
	static int length, width;
	static int[][] adjacencyBoard; //holds how many unknown adjacent cells
	static int[][] adjacentMines; //holds how many marked mines in adjacent cells
	static char[][] cluesBoard;
	static boolean boardChanged = false;
	static Stack<int[]> stack;
	
	public static void main(String[] args){
		System.out.println("Initializing Minesweeper...");
		Scanner in = new Scanner(System.in);
		stack = new Stack<int[]>();
		
		System.out.println("Enter length of board: ");
		length = in.nextInt();
		System.out.println("Enter width of board: ");
		width = in.nextInt();
		
		cluesBoard = createBoard(length, width);	
		adjacencyBoard = countAdjacent(length, width);
		adjacentMines = initAdjacentMines(length, width);
		
		int x = width/2 + 1;
		int y = length/2 + 1;
		int[] XY = getXY(x, y);
		
		System.out.println("First move: " + x + ", " + y);
		System.out.println("(x by y coordinates)");
		
		System.out.println("Enter numeric state of specified cell: ");
		System.out.println("(If mine, enter 9)");
		printBoard(cluesBoard);
		int state = in.nextInt();
		
		putClue(XY[0], XY[1], state);
		printBoard(adjacencyBoard);
		//printBoard(cluesBoard);
		
		while(checkGameInPlay()){
			while(boardChanged){
				boardChanged = false;
				expandClues();
			}
			System.out.println("Clues: ");
			printBoard(cluesBoard);
			System.out.println("Open Adjacent Spaces: ");
			printBoard(adjacencyBoard);
			System.out.println("Adjacent Mines: ");
			printBoard(adjacentMines);
			
			int[] nextXY;
			if (!stack.isEmpty()){
				nextXY = stack.pop();
			} else {
				nextXY = nextRequestedXY();
			}
			int[]next = getCoordinate(nextXY[0], nextXY[1]);
			System.out.println("Enter state of next move: " + next[0] + ", " + next[1]);
			int command = in.nextInt();
			putClue(nextXY[0], nextXY[1], command);
			
		}
		
		System.out.println("Game Over. Final board: ");
		printBoard(cluesBoard);
		in.close();
		
	}
	
//	public static boolean checkValidMove(int input1, int input2){
//		if (input1>=1 && input1<=width && input2>=1 && input2<=length){
//			return true;
//		}
//		return false;
//	}
	
	public static void putClue(int x, int y, int clue){
		if (clue == 0){
			cluesBoard[x][y] = '0';
			if ((x-1)>=0 && (x-1)<length && (y-1)>=0 && (y-1)<width && cluesBoard[x-1][y-1] == '?'){
				cluesBoard[x-1][y-1] = 'C';
				stack.push(new int[]{x-1, y-1});
			}
			if ((x)>=0 && (x)<length && (y-1)>=0 && (y-1)<width && cluesBoard[x][y-1] == '?'){
				cluesBoard[x][y-1] = 'C';
				stack.push(new int[]{x, y-1});
			}
			if ((x+1)>=0 && (x+1)<length && (y-1)>=0 && (y-1)<width && cluesBoard[x+1][y-1] == '?'){
				cluesBoard[x+1][y-1] = 'C';
				stack.push(new int[]{x+1, y-1});
			}
			if ((x-1)>=0 && (x-1)<length && (y)>=0 && (y)<width && cluesBoard[x-1][y] == '?'){
				cluesBoard[x-1][y] = 'C';
				stack.push(new int[]{x-1, y});
			}
			if ((x+1)>=0 && (x+1)<length && (y)>=0 && (y)<width && cluesBoard[x+1][y] == '?'){
				cluesBoard[x+1][y] = 'C';
				stack.push(new int[]{x+1, y});
			}
			if ((x-1)>=0 && (x-1)<length && (y+1)>=0 && (y+1)<width && cluesBoard[x-1][y+1] == '?'){
				cluesBoard[x-1][y+1] = 'C';
				stack.push(new int[]{x-1, y+1});
			}
			if ((x)>=0 && (x)<length && (y+1)>=0 && (y+1)<width && cluesBoard[x][y+1] == '?'){
				cluesBoard[x][y+1] = 'C';
				stack.push(new int[]{x, y+1});
			}
			if ((x+1)>=0 && (x+1)<length && (y+1)>=0 && (y+1)<width && cluesBoard[x+1][y+1] == '?'){
				cluesBoard[x+1][y+1] = 'C';
				stack.push(new int[]{x+1, y+1});
			}
		}
		if (clue == 9){
			cluesBoard[x][y] = '*';
		} else {
			cluesBoard[x][y] = (char)(clue+48);
		}
		reduceAdjacentNumber(x,y);
		boardChanged = true;
	}
	
	public static void expandClues(){
		for (int x=0; x<length; x++){
			for (int y=0; y<width; y++){
				if (cluesBoard[x][y] == (char)(adjacencyBoard[x][y] + adjacentMines[x][y] + 48)){
					if ((x-1)>=0 && (x-1)<length && (y-1)>=0 && (y-1)<width && cluesBoard[x-1][y-1] == '?'){
						flagMine(x-1,y-1);
						reduceAdjacentNumber(x-1,y-1);
					}
					if ((x)>=0 && (x)<length && (y-1)>=0 && (y-1)<width && cluesBoard[x][y-1] == '?'){
						flagMine(x,y-1);
						reduceAdjacentNumber(x,y-1);
					}
					if ((x+1)>=0 && (x+1)<length && (y-1)>=0 && (y-1)<width && cluesBoard[x+1][y-1] == '?'){
						flagMine(x+1,y-1);
						reduceAdjacentNumber(x+1,y-1);
					}
					if ((x-1)>=0 && (x-1)<length && (y)>=0 && (y)<width && cluesBoard[x-1][y] == '?'){
						flagMine(x-1,y);
						reduceAdjacentNumber(x-1,y);
					}
					if ((x+1)>=0 && (x+1)<length && (y)>=0 && (y)<width && cluesBoard[x+1][y] == '?'){
						flagMine(x+1,y);
						reduceAdjacentNumber(x+1,y);
					}
					if ((x-1)>=0 && (x-1)<length && (y+1)>=0 && (y+1)<width && cluesBoard[x-1][y+1] == '?'){
						flagMine(x-1,y+1);
						reduceAdjacentNumber(x-1,y+1);
					}
					if ((x)>=0 && (x)<length && (y+1)>=0 && (y+1)<width && cluesBoard[x][y+1] == '?'){
						flagMine(x,y+1);
						reduceAdjacentNumber(x,y+1);
					}
					if ((x+1)>=0 && (x+1)<length && (y+1)>=0 && (y+1)<width && cluesBoard[x+1][y+1] == '?'){
						flagMine(x+1,y+1);
						reduceAdjacentNumber(x+1,y+1);
					}
				} else if (cluesBoard[x][y] == (char)(adjacentMines[x][y] + 48)){
					if ((x-1)>=0 && (x-1)<length && (y-1)>=0 && (y-1)<width && cluesBoard[x-1][y-1] == '?'){
						cluesBoard[x-1][y-1] = 'C';
						stack.push(new int[]{x-1, y-1});
					}
					if ((x)>=0 && (x)<length && (y-1)>=0 && (y-1)<width && cluesBoard[x][y-1] == '?'){
						cluesBoard[x][y-1] = 'C';
						stack.push(new int[]{x, y-1});
					}
					if ((x+1)>=0 && (x+1)<length && (y-1)>=0 && (y-1)<width && cluesBoard[x+1][y-1] == '?'){
						cluesBoard[x+1][y-1] = 'C';
						stack.push(new int[]{x+1, y-1});
					}
					if ((x-1)>=0 && (x-1)<length && (y)>=0 && (y)<width && cluesBoard[x-1][y] == '?'){
						cluesBoard[x-1][y] = 'C';
						stack.push(new int[]{x-1, y});
					}
					if ((x+1)>=0 && (x+1)<length && (y)>=0 && (y)<width && cluesBoard[x+1][y] == '?'){
						cluesBoard[x+1][y] = 'C';
						stack.push(new int[]{x+1, y});
					}
					if ((x-1)>=0 && (x-1)<length && (y+1)>=0 && (y+1)<width && cluesBoard[x-1][y+1] == '?'){
						cluesBoard[x-1][y+1] = 'C';
						stack.push(new int[]{x-1, y+1});
					}
					if ((x)>=0 && (x)<length && (y+1)>=0 && (y+1)<width && cluesBoard[x][y+1] == '?'){
						cluesBoard[x][y+1] = 'C';
						stack.push(new int[]{x, y+1});
					}
					if ((x+1)>=0 && (x+1)<length && (y+1)>=0 && (y+1)<width && cluesBoard[x+1][y+1] == '?'){
						cluesBoard[x+1][y+1] = 'C';
						stack.push(new int[]{x+1, y+1});
					}
				}
			}
		}
	}
	
	public static void flagMine(int x, int y){ //flag x, y as mine
		cluesBoard[x][y] = 'M';
		boardChanged=true;
		if ((x-1)>=0 && (x-1)<length && (y-1)>=0 && (y-1)<width){
			adjacentMines[x-1][y-1]++;
		}
		if ((x)>=0 && (x)<length && (y-1)>=0 && (y-1)<width){
			adjacentMines[x][y-1]++;
		}
		if ((x+1)>=0 && (x+1)<length && (y-1)>=0 && (y-1)<width){
			adjacentMines[x+1][y-1]++;
		}
		if ((x-1)>=0 && (x-1)<length && (y)>=0 && (y)<width){
			adjacentMines[x-1][y]++;
		}
		if ((x+1)>=0 && (x+1)<length && (y)>=0 && (y)<width ){
			adjacentMines[x+1][y]++;
		}
		if ((x-1)>=0 && (x-1)<length && (y+1)>=0 && (y+1)<width ){
			adjacentMines[x-1][y+1]++;
		}
		if ((x)>=0 && (x)<length && (y+1)>=0 && (y+1)<width ){
			adjacentMines[x][y+1]++;
		}
		if ((x+1)>=0 && (x+1)<length && (y+1)>=0 && (y+1)<width ){
			adjacentMines[x+1][y+1]++;
		}
	}
	
	public static void reduceAdjacentNumber(int x, int y){ //flag x, y as mine
		boardChanged = true;
		if ((x-1)>=0 && (x-1)<length && (y-1)>=0 && (y-1)<width){
			adjacencyBoard[x-1][y-1]--;
		}
		if ((x)>=0 && (x)<length && (y-1)>=0 && (y-1)<width){
			adjacencyBoard[x][y-1]--;
		}
		if ((x+1)>=0 && (x+1)<length && (y-1)>=0 && (y-1)<width){
			adjacencyBoard[x+1][y-1]--;
		}
		if ((x-1)>=0 && (x-1)<length && (y)>=0 && (y)<width){
			adjacencyBoard[x-1][y]--;
		}
		if ((x+1)>=0 && (x+1)<length && (y)>=0 && (y)<width ){
			adjacencyBoard[x+1][y]--;
		}
		if ((x-1)>=0 && (x-1)<length && (y+1)>=0 && (y+1)<width ){
			adjacencyBoard[x-1][y+1]--;
		}
		if ((x)>=0 && (x)<length && (y+1)>=0 && (y+1)<width ){
			adjacencyBoard[x][y+1]--;
		}
		if ((x+1)>=0 && (x+1)<length && (y+1)>=0 && (y+1)<width ){
			adjacencyBoard[x+1][y+1]--;
		}
	}
	
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
	
	public static int[][] countAdjacent(int length, int width){ //counts the unknown adjacent cells
		int[][] countAdjacent = new int[length][width];
		for (int x=0; x<length; x++){
			for (int y=0; y<width; y++){
				int count = 0;
				if ((x-1)>=0 && (x-1)<length && (y-1)>=0 && (y-1)<width && cluesBoard[x-1][y-1] == '?') count++;
				if ((x)>=0 && (x)<length && (y-1)>=0 && (y-1)<width && cluesBoard[x][y-1] == '?') count++;
				if ((x+1)>=0 && (x+1)<length && (y-1)>=0 && (y-1)<width && cluesBoard[x+1][y-1] == '?') count++;
				if ((x-1)>=0 && (x-1)<length && (y)>=0 && (y)<width && cluesBoard[x-1][y] == '?') count++;
				if ((x+1)>=0 && (x+1)<length && (y)>=0 && (y)<width && cluesBoard[x+1][y] == '?') count++;
				if ((x-1)>=0 && (x-1)<length && (y+1)>=0 && (y+1)<width && cluesBoard[x-1][y+1] == '?') count++;
				if ((x)>=0 && (x)<length && (y+1)>=0 && (y+1)<width && cluesBoard[x][y+1] == '?') count++;
				if ((x+1)>=0 && (x+1)<length && (y+1)>=0 && (y+1)<width && cluesBoard[x+1][y+1] == '?') count++;
				
				countAdjacent[x][y] = count; //unknown
			}
		}
		
		return countAdjacent;
	}
	
	public static char[][] createBoard(int length, int width){
		char[][] clues = new char[length][width];
		for (int i=0; i<length; i++){
			for (int j=0; j<width; j++){
				clues[i][j] = '?';
			}
		}
		return clues;
	}
	
	public static int[][] initAdjacentMines(int length, int width){
		int[][] mines = new int[length][width];
		for (int i=0; i<length; i++){
			for (int j=0; j<width; j++){
				mines[i][j] = 0;
			}
		}
		return mines;
	}
	public static int[] getXY(int input1, int input2){
		int x = length-input2;
		int y = input1-1;
		return new int[]{x, y};
	}
	
	public static int[] getCoordinate(int x, int y){
		int input1 = y+1;
		int input2 = length-x;
		return new int[]{input1, input2};
	}
	
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
			System.out.print(length-i + "   ");
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
	
	public static void printBoard(int[][] board){
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
			System.out.print(length-i + "  ");
			for (int j = 0; j < board[0].length; j++){
				if (board[i][j]<0){
					System.out.print(board[i][j]);
				} else {
					System.out.print(" " + board[i][j]);
				}
				if (j == board[0].length - 1) {
					System.out.println();
				}
			}
		}
		System.out.println();
	}
	
	public static boolean checkGameInPlay(){
		boolean unsolved = false;
		for (int i=0; i<cluesBoard.length; i++){
			for (int j=0; j<cluesBoard[0].length; j++){
				if (cluesBoard[i][j]=='?') unsolved = true;
				if (cluesBoard[i][j]=='*') return false;
			}
		}
		if (unsolved == true) return true;
		return false;
	}
	
	public static boolean isNumClue(char c){
		if (c=='0' || c=='1' || c=='2' || c=='3' || c=='4' || c=='5' || c=='6' || c=='7' || c=='8' || c=='9') return true;
		return false;
	}
}


package ubc.cosc322;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 
 * @author Josh Basham 38346623
 * @author Ryan Klughart
 * @editor 
 */
public class AiPlayer{

	int side;
	int enemySide;
	boolean start;
	String gameboard ="";
	
	// State used at the start of each move
	int[][] stateFinal = new int[11][11];
	//int[][] state = new int[11][11];
	//Values for different identifiers
	int emptySpace = 0;
	int arrowedSpace =3;
	int count = 0;
   	int[][] Our_Queen_positions = new int[2][4];
	int[][] Other_Queen_positions = new int[2][4];
   	int[][] OT = new int[11][11];
	int[][] ET = new int[11][11];
	int territoryWeight;
	int maxDepth;
	double initialTerritory;
	//0 new location, 1 is heuristic value, 2 is arrow position, 3 is old location
	
	//Used in abp algorithm. Max/min util is 100 because it is 10x10 board.
	
	int[][] move2 = new int[4][2];
	int[][] movev = new int[4][2];	
	
	
	public AiPlayer() { // constructor for AiPlayer
		
		
	}
	// Initialize the state at the begining of the game
	public int[][] initState(Map<String, Object> msgDetails) {
    	String gameboard = msgDetails.get("game-state").toString() //Retrieves the board as a string
    			.replaceAll("\\[", "")
    			.replaceAll("\\]", "")
    			.replaceAll("\\,","")
    			.replaceAll("\\ ", "");
    	
    	  int[][] twoD_arr = new int[11][11];
          for(int i = 1; i <11;i++) 
              for(int j = 1; j <11;j++) {
                  //System.out.print(gameboard.charAt((i11)+j)); //Output the board numerically into the console
                  twoD_arr[i][j] = Character.getNumericValue(gameboard.charAt((i*11)+j)); //Store the board in a 2D array 
              }
      	return twoD_arr;
  	}
	
//Print Current state
	public void printState() {

          for(int i = 1; i <11;i++) {
              for(int j = 1; j <11;j++) {
                  System.out.print(stateFinal[i][j]); //Output the board numerically into the console
                  //Store the board in a 2D array 
              }
              System.out.println(""); 
          }
  	}
	
  	int[][] calculateMove() {
	// Output starts with 1 at the bottom left.
	// Theory: Alpha-beta pruning, with a heuristic based off of mobility and territory.
	// Queens can move in any direction in a straight line at any depth, just like chess
	// TODO multithreading if too slow. One queen per thread
	
		int[][] output = new  int[4][2];
		//0 new location, 1 is heuristic value, 2 is arrow position, 3 is old location
  	  
	 	//output = alphaBetaS(stateFinal);	
    
    	output = highestValue(stateFinal);
		
		return output;
	}
	
	
	// Heuristic of the ai.
	public int determineValue(int[][] state, int[][] move){ // heuristic
		
		DTmove(state,move,1); // Calculates what the territory will be if move happens
		double T= TW(); // Calculates the territory value of the move
		int M =  determineMobility2(state, move); // Determine mobility of move
		int output = (int)(territoryWeight*T+M); //Change territory weight in test file
		//System.out.println("Value: " + output);
		return (output);
	}
	
	//Used to avoid state = null
	public int [][] duplicateState(int [][] state){
		int[][] newState = new int[11][11];
		for(int i=1; i<=10;i++)
			for(int j =1; j<=10; j++)
				newState[i][j] = state[i][j];
		return newState;
	}
	
	//Initialize territory states
	public int[][] Highstate(){
		int[][] state = new int[11][11];
		for(int i=1; i<=10;i++) 
			for(int j =1; j<=10; j++) 
				state[i][j]=9;
		return state;
				
	}
	
	//Used to determine the Territory of the current state
	public int DT(int[][] state, int depth) {
	
		if(depth >maxDepth)
			return 0;
	
	    int[][] tempState = new int[11][11];
	    
		tempState = duplicateState(state);
	

	    ArrayList<int[]> OQ = this.findQueens(tempState);
	    ArrayList<int[]> EQ = this.findEnemyQueens(tempState);
	    for(int[] queen: OQ)
	    	CAH3(tempState,queen,depth,true);
	    for(int[] queen: EQ)
	    	CAH3(tempState,queen,depth,false);
		return 0;
	}
	
	//Used to determine the territory for a move, Territory is the minimum number of moves required to get to a certain spot on the board,
	//the less moves to get to a spot the better the territory, we want to find a move that maximizes our territory and minimizes the other teams
	public int DTmove(int[][] state,int[][] move, int depth) {
		
		if(depth >maxDepth) // Only find the positions that we can get to within maxDepth moves
			return 0;
//	
	    int[][] tempState = new int[11][11];
		tempState = duplicateState(state);
		tempState = updateState1(tempState,move);

	    ArrayList<int[]> OQ = this.findQueens(tempState); // Our Queens
	    ArrayList<int[]> EQ = this.findEnemyQueens(tempState);
	    for(int[] queen: OQ)
	    	CTH(tempState,queen,depth+1,true); // plus 1 depth for our queens since the first moves are determined in CalcActionsHelper
	    for(int[] queen: EQ)
	    	CTH(tempState,queen,depth,false);
		return 0;
	}
	
	//Used to determine the territory using the current state/level/queen
	public void CAH3(int[][] state, int[] queen,int level,boolean ours){

		//System.out.println("Queen at:"+queen[0]+" "+queen[1]);
			 int[] queenTemp = new int[2];	 
			 //for each next depth of position in single direction by case
			 
			   Map<Integer, int[]> move = new HashMap<Integer, int[]>();
			
				boolean nextMoveValid;
				int x;
				int y;
				int queenrow = queen[0];
				int queencol = queen[1];
				int[][] move1 = new int[4][2];
			 // up
				 //for queens vert coordinate
				
				 queenTemp[1] = queencol;
				 loop:
				 for(int j=queenrow+1; j<=10; j++) {
					 
					 queenTemp[0] = j;
					 move1[0][0] = queenTemp[0];
					 move1[0][1] = queenTemp[1];
					 move1[3][0] = queen[0];
					 move1[3][1] = queen[1];
					 
					 if(isValid(move1, state)) {
						// System.out.println("up Queen move valid at"+queenTemp[0]+" "+queenTemp[1]);
						 if(ours) {
							 //System.out.println("ours");
							 if(OT[move1[0][0]][move1[0][1]]>level) { // Change value at next queen position in territory if level
								                                      // is less than the current value
								 OT[move1[0][0]][move1[0][1]] = level;
								 
							 }
						 }
					     else {
					    	 if(ET[move1[0][0]][move1[0][1]]>level) // same for enemy queens
								 ET[move1[0][0]][move1[0][1]] = level;
					     }
						 int[][] tempState = new int[11][11];
					 	 tempState = duplicateState(state);
						 tempState = updateState1(tempState,move1);
						 DT(tempState,level+1);
					     move1 = new int[4][2];
					    }
					 

					 else {
					 break loop;} 
					 }
				
				 
				 
				 // down-right
			
				  nextMoveValid = true;
					 x = queenrow;
				 	 y = queencol;
				 	 
				 while(nextMoveValid) {
					 
				 	 move1 = new int[4][2];
				 	 queenTemp[0] = --x;
					 queenTemp[1] = ++y;
					 
				
					 
					 move1[0][0] = queenTemp[0];
					 move1[0][1] = queenTemp[1];
					 move1[3][0] = queen[0];
					 move1[3][1] = queen[1];
					 nextMoveValid = isValid(move1, state);
					 if(isValid(move1, state)) {
							// System.out.println("up Queen move valid at"+queenTemp[0]+" "+queenTemp[1]);
						 if(ours) {
							 //System.out.println("ours");
							 if(OT[move1[0][0]][move1[0][1]]>level) {
								 OT[move1[0][0]][move1[0][1]] = level;
								 
							 }
						 }
					     else {
					    	 if(ET[move1[0][0]][move1[0][1]]>level)
								 ET[move1[0][0]][move1[0][1]] = level;
					     }
						 int[][] tempState = new int[11][11];
					 	 tempState = duplicateState(state);
						 tempState = updateState1(tempState,move1);
						 DT(tempState,level+1);
					     move1 = new int[4][2];
						    }
					 else { break;}
					 
				 }
				 
				 
				 //right
				 queenTemp[0] = queenrow;
				 loop:
					 for(int j=queencol+1; j<=10; j++) {
						 move1 = new int[4][2]; 
						 queenTemp[1] = j;
						 move1[0][0] = queenTemp[0];
						 move1[0][1] = queenTemp[1];
						 move1[3][0] = queen[0];
						 move1[3][1] = queen[1];
						 if(isValid(move1, state)) {
							 if(ours) {
								 //System.out.println("ours");
								 if(OT[move1[0][0]][move1[0][1]]>level) {
									 OT[move1[0][0]][move1[0][1]] = level;
									 
								 }
							 }
						     else {
						    	 if(ET[move1[0][0]][move1[0][1]]>level)
									 ET[move1[0][0]][move1[0][1]] = level;
						     }
							 int[][] tempState = new int[11][11];
						 	 tempState = duplicateState(state);
							 tempState = updateState1(tempState,move1);
							 DT(tempState,level+1);
						     move1 = new int[4][2];
						}
						 else break loop;
						 }

				//down
				
				 queenTemp[1] = queencol;
				 loop:
					 for(int j=queenrow-1; j>00; j--) {
						 move1 = new int[4][2]; 
						 queenTemp[0] = j;
						 move1[0][0] = queenTemp[0];
						 move1[0][1] = queenTemp[1];
						 move1[3][0] = queen[0];
						 move1[3][1] = queen[1];
						 if(isValid(move1, state)) {
							 if(ours) {
								 //System.out.println("ours");
								 if(OT[move1[0][0]][move1[0][1]]>level) {
									 OT[move1[0][0]][move1[0][1]] = level;
									 
								 }
							 }
						     else {
						    	 if(ET[move1[0][0]][move1[0][1]]>level)
									 ET[move1[0][0]][move1[0][1]] = level;
						     }
							 int[][] tempState = new int[11][11];
						 	 tempState = duplicateState(state);
							 tempState = updateState1(tempState,move1);
							 DT(tempState,level+1);
						     move1 = new int[4][2];
						}
						 else break loop;
						 }
				 
				 //down-left		 
			  nextMoveValid = true;
					 x = queenrow;
				 	 y = queencol;
				 	 
				 while(nextMoveValid) {
					 
					 move1 = new int[4][2]; 
				 	 queenTemp[0] = --x;
					 queenTemp[1] = --y;
					 
					 move1[0][0] = queenTemp[0];
					 move1[0][1] = queenTemp[1];
					 move1[3][0] = queen[0];
					 move1[3][1] = queen[1];
					 nextMoveValid = isValid(move1, state);
					 if(nextMoveValid) { 
						 if(ours) {
							 //System.out.println("ours");
							 if(OT[move1[0][0]][move1[0][1]]>level) {
								 OT[move1[0][0]][move1[0][1]] = level;
								 
							 }
						 }
					     else {
					    	 if(ET[move1[0][0]][move1[0][1]]>level)
								 ET[move1[0][0]][move1[0][1]] = level;
					     }
						 int[][] tempState = new int[11][11];
					 	 tempState = duplicateState(state);
						 tempState = updateState1(tempState,move1);
						 DT(tempState,level+1);
					     move1 = new int[4][2];
					    
					 }
					 else { break;}
					 
				 }
				 

				 //left 
				 queenTemp[0] = queenrow;
				 loop:
					 for(int j=queencol-1; j>0; j--) {
						 move1 = new int[4][2];  
						 queenTemp[1] = j;
						 move1[0][0] = queenTemp[0];
						 move1[0][1] = queenTemp[1];
						 move1[3][0] = queen[0];
						 move1[3][1] = queen[1];
						 if(isValid(move1, state)) {
							 if(ours) {
								 //System.out.println("ours");
								 if(OT[move1[0][0]][move1[0][1]]>level) {
									 OT[move1[0][0]][move1[0][1]] = level;
									 
								 }
							 }
						     else {
						    	 if(ET[move1[0][0]][move1[0][1]]>level)
									 ET[move1[0][0]][move1[0][1]] = level;
						     }
							 int[][] tempState = new int[11][11];
						 	 tempState = duplicateState(state);
							 tempState = updateState1(tempState,move1);
							 DT(tempState,level+1);
						     move1 = new int[4][2];
						}
						 else break loop;
						 }
				 
				 
				 //up left
			  nextMoveValid = true;
					 x = queenrow;
				 	 y = queencol;
				 	 
				 while(nextMoveValid) {
					 
					 move1 = new int[4][2];  
				 	 queenTemp[0] = ++x;
					 queenTemp[1] = --y;
					 
					 move1[0][0] = queenTemp[0];
					 move1[0][1] = queenTemp[1];
					 move1[3][0] = queen[0];
					 move1[3][1] = queen[1];
					 nextMoveValid = isValid(move1, state);
					 if(nextMoveValid) { 
						 if(ours) {
							 //System.out.println("ours");
							 if(OT[move1[0][0]][move1[0][1]]>level) {
								 OT[move1[0][0]][move1[0][1]] = level;
								 
							 }
						 }
					     else {
					    	 if(ET[move1[0][0]][move1[0][1]]>level)
								 ET[move1[0][0]][move1[0][1]] = level;
					     }
						 int[][] tempState = new int[11][11];
					 	 tempState = duplicateState(state);
						 tempState = updateState1(tempState,move1);
						 DT(tempState,level+1);
					     move1 = new int[4][2];
					 }
					 else { break;}
					 
				 }
				 //up right
				 nextMoveValid = true;
				 x = queenrow;
			 	 y = queencol;
			 	 
			 while(nextMoveValid) {
				 move1 = new int[4][2];  
			 	 queenTemp[0] = ++x;
				 queenTemp[1] = ++y;
				 
				 move1[0][0] = queenTemp[0];
				 move1[0][1] = queenTemp[1];
				 move1[3][0] = queen[0];
				 move1[3][1] = queen[1];
				 nextMoveValid = isValid(move1, state);
				 if(nextMoveValid) { 
					 if(ours) {
						 //System.out.println("ours");
						 if(OT[move1[0][0]][move1[0][1]]>level) {
							 OT[move1[0][0]][move1[0][1]] = level;
							 
						 }
					 }
				     else {
				    	 if(ET[move1[0][0]][move1[0][1]]>level)
							 ET[move1[0][0]][move1[0][1]] = level;
				     }
					 int[][] tempState = new int[11][11];
				 	 tempState = duplicateState(state);
					 tempState = updateState1(tempState,move1);
					 DT(tempState,level+1);
				     move1 = new int[4][2];
				 }
				 else { break;}
				 
			 }
				 
		
		//0 new location, 1 is heuristic value, 2 is arrow position, 3 is old location
		 

}
	//Used to find the moves of the queens at the current level and update the territory, similar to CAH3 but with DTmove
	
	public void CTH(int[][] state, int[] queen,int level,boolean ours){ 

		//System.out.println("Queen at:"+queen[0]+" "+queen[1]);
			 int[] queenTemp = new int[2];	 
			 //for each next depth of position in single direction by case
			 
			   Map<Integer, int[]> move = new HashMap<Integer, int[]>();
			
				boolean nextMoveValid;
				int x;
				int y;
				int queenrow = queen[0];
				int queencol = queen[1];
				int[][] move1 = new int[4][2];
			
				
				// Find next up positions of queen
				 queenTemp[1] = queencol;
				 loop:
				 for(int j=queenrow+1; j<=10; j++) {
					 
					 queenTemp[0] = j;
					 move1[0][0] = queenTemp[0]; //next queen position
					 move1[0][1] = queenTemp[1];
					 move1[3][0] = queen[0]; //current queen position
					 move1[3][1] = queen[1];
					 
					 if(isValid(move1, state)) {
						// System.out.println("up Queen move valid at"+queenTemp[0]+" "+queenTemp[1]);
						 if(ours) {
							 //System.out.println("ours");
							 if(OT[move1[0][0]][move1[0][1]]>level) { // level is the number of moves required to get to the position of the 
								 OT[move1[0][0]][move1[0][1]] = level; // next queen position, if the position could have been moved to at a earlier 
								                                      // level then nothing changes.
							 }
						 }
					     else {
					    	 if(ET[move1[0][0]][move1[0][1]]>level) // Similar with enemy queen
								 ET[move1[0][0]][move1[0][1]] = level;
					     }
					
						 DTmove(state,move1,level+1); // Determine territory of the move at the next level
					     move1 = new int[4][2];
					    }
					 

					 else {
					 break loop;} 
					 }
				
				 
				 
				 // down-right
			
				  nextMoveValid = true;
					 x = queenrow;
				 	 y = queencol;
				 	 
				 while(nextMoveValid) {
					 
				 	 move1 = new int[4][2];
				 	 queenTemp[0] = --x;
					 queenTemp[1] = ++y;
					 
				
					 
					 move1[0][0] = queenTemp[0];
					 move1[0][1] = queenTemp[1];
					 move1[3][0] = queen[0];
					 move1[3][1] = queen[1];
					 nextMoveValid = isValid(move1, state);
					 if(isValid(move1, state)) {
							// System.out.println("up Queen move valid at"+queenTemp[0]+" "+queenTemp[1]);
						 if(ours) {
							 //System.out.println("ours");
							 if(OT[move1[0][0]][move1[0][1]]>level) {
								 OT[move1[0][0]][move1[0][1]] = level;
								 
							 }
						 }
					     else {
					    	 if(ET[move1[0][0]][move1[0][1]]>level)
								 ET[move1[0][0]][move1[0][1]] = level;
					     }
				
						 DTmove(state,move1,level+1);
					     move1 = new int[4][2];
						    }
					 else { break;}
					 
				 }
				 
				 
				 //right
				 queenTemp[0] = queenrow;
				 loop:
					 for(int j=queencol+1; j<=10; j++) {
						 move1 = new int[4][2]; 
						 queenTemp[1] = j;
						 move1[0][0] = queenTemp[0];
						 move1[0][1] = queenTemp[1];
						 move1[3][0] = queen[0];
						 move1[3][1] = queen[1];
						 if(isValid(move1, state)) {
							 if(ours) {
								 //System.out.println("ours");
								 if(OT[move1[0][0]][move1[0][1]]>level) {
									 OT[move1[0][0]][move1[0][1]] = level;
									 
								 }
							 }
						     else {
						    	 if(ET[move1[0][0]][move1[0][1]]>level)
									 ET[move1[0][0]][move1[0][1]] = level;
						     }
						
							 DTmove(state,move1,level+1);
						     move1 = new int[4][2];
						}
						 else break loop;
						 }

				//down
				
				 queenTemp[1] = queencol;
				 loop:
					 for(int j=queenrow-1; j>00; j--) {
						 move1 = new int[4][2]; 
						 queenTemp[0] = j;
						 move1[0][0] = queenTemp[0];
						 move1[0][1] = queenTemp[1];
						 move1[3][0] = queen[0];
						 move1[3][1] = queen[1];
						 if(isValid(move1, state)) {
							 if(ours) {
								 //System.out.println("ours");
								 if(OT[move1[0][0]][move1[0][1]]>level) {
									 OT[move1[0][0]][move1[0][1]] = level;
									 
								 }
							 }
						     else {
						    	 if(ET[move1[0][0]][move1[0][1]]>level)
									 ET[move1[0][0]][move1[0][1]] = level;
						     }
						
							 DTmove(state,move1,level+1);
						     move1 = new int[4][2];
						}
						 else break loop;
						 }
				 
				 //down-left		 
			  nextMoveValid = true;
					 x = queenrow;
				 	 y = queencol;
				 	 
				 while(nextMoveValid) {
					 
					 move1 = new int[4][2]; 
				 	 queenTemp[0] = --x;
					 queenTemp[1] = --y;
					 
					 move1[0][0] = queenTemp[0];
					 move1[0][1] = queenTemp[1];
					 move1[3][0] = queen[0];
					 move1[3][1] = queen[1];
					 nextMoveValid = isValid(move1, state);
					 if(nextMoveValid) { 
						 if(ours) {
							 //System.out.println("ours");
							 if(OT[move1[0][0]][move1[0][1]]>level) {
								 OT[move1[0][0]][move1[0][1]] = level;
								 
							 }
						 }
					     else {
					    	 if(ET[move1[0][0]][move1[0][1]]>level)
								 ET[move1[0][0]][move1[0][1]] = level;
					     }
					
						 DTmove(state,move1,level+1);
					     move1 = new int[4][2];
					    
					 }
					 else { break;}
					 
				 }
				 

				 //left 
				 queenTemp[0] = queenrow;
				 loop:
					 for(int j=queencol-1; j>0; j--) {
						 move1 = new int[4][2];  
						 queenTemp[1] = j;
						 move1[0][0] = queenTemp[0];
						 move1[0][1] = queenTemp[1];
						 move1[3][0] = queen[0];
						 move1[3][1] = queen[1];
						 if(isValid(move1, state)) {
							 if(ours) {
							
								 if(OT[move1[0][0]][move1[0][1]]>level) {
									 OT[move1[0][0]][move1[0][1]] = level;
									 
								 }
							 }
						     else {
						    	 if(ET[move1[0][0]][move1[0][1]]>level)
									 ET[move1[0][0]][move1[0][1]] = level;
						     }
				
							 DTmove(state,move1,level+1);
						     move1 = new int[4][2];
						}
						 else break loop;
						 }
				 
				 
				 //up left
			  nextMoveValid = true;
					 x = queenrow;
				 	 y = queencol;
				 	 
				 while(nextMoveValid) {
					 
					 move1 = new int[4][2];  
				 	 queenTemp[0] = ++x;
					 queenTemp[1] = --y;
					 
					 move1[0][0] = queenTemp[0];
					 move1[0][1] = queenTemp[1];
					 move1[3][0] = queen[0];
					 move1[3][1] = queen[1];
					 nextMoveValid = isValid(move1, state);
					 if(nextMoveValid) { 
						 if(ours) {
							 if(OT[move1[0][0]][move1[0][1]]>level) {
								 OT[move1[0][0]][move1[0][1]] = level;
								 
							 }
						 }
					     else {
					    	 if(ET[move1[0][0]][move1[0][1]]>level)
								 ET[move1[0][0]][move1[0][1]] = level;
					     }
						 DTmove(state,move1,level+1);
					     move1 = new int[4][2];
					 }
					 else { break;}
					 
				 }
				 //up right
				 nextMoveValid = true;
				 x = queenrow;
			 	 y = queencol;
			 	 
			 while(nextMoveValid) {
				 move1 = new int[4][2];  
			 	 queenTemp[0] = ++x;
				 queenTemp[1] = ++y;
				 
				 move1[0][0] = queenTemp[0];
				 move1[0][1] = queenTemp[1];
				 move1[3][0] = queen[0];
				 move1[3][1] = queen[1];
				 nextMoveValid = isValid(move1, state);
				 if(nextMoveValid) { 
					 if(ours) {
						 //System.out.println("ours");
						 if(OT[move1[0][0]][move1[0][1]]>level) {
							 OT[move1[0][0]][move1[0][1]] = level;
							 
						 }
					 }
				     else {
				    	 if(ET[move1[0][0]][move1[0][1]]>level)
							 ET[move1[0][0]][move1[0][1]] = level;
				     }
					 DTmove(state,move1,level+1);
				     move1 = new int[4][2];
				 }
				 else { break;}
				 
			 }
				 
		
		//0 new location, 1 is heuristic value, 2 is arrow position, 3 is old location
		 

}
	
//Used to Calculate the value of the territories
public double TW() {
	double sum = 0;
	for(int i=1; i<=10;i++) { // loop through entire board
		for(int j =1; j<=10; j++) {
			int n = OT[i][j]; 
			int m = ET[i][j];
			if(n==m & n<9) // If we can get to the position in the same amount of moves +0.2
				sum =  sum+0.2;
			if(n<m)// if we can get to position i,j in less moves +1
				sum = sum+1;
			if(n>m)// if enemy can get to position i,j in less moves -1
				sum = sum-1;
		}
		}
	
	return sum;
}

// Finds the total number of possible moves we can make and the enemy team can
// make after a certain move, want to find a move that maximizes our moves and minimizes the enemy moves
	public int determineMobility2(int[][] state, int[][] move) {
		//System.out.println("DetermineMobility2");
		int[][] tempState = new int[11][11];
		tempState = duplicateState(stateFinal);
		tempState = updateState(tempState,move);
		ArrayList<int[]> queens = findQueens(tempState);
		ArrayList<int[]> enemyQueens = findEnemyQueens(tempState);
		
		int om = calcNumActions(tempState,queens);
		int tm = calcNumActions(tempState,enemyQueens);
		//System.out.println("Our Moves:"+om+"Enemy moves: "+tm);
		
		return om-tm;
	}
	
	//Calculates the total number of moves a team can make for all queens
	public int calcNumActions(int[][] state, ArrayList<int[]> Queens) {

		
		
		int total=0;
		for(int[] queen: Queens ) {
			if(state!=null) {
				int numMoves = calcActionsHelper2(state, queen);
				total = total + numMoves;
			}
			else
				System.out.println("Null state");
			
			
		}
		//0 new location, 1 is heuristic value, 2 is arrow position, 3 is old location
		
		return total;
		
	}
	
	//Calculates the total number of moves for a single queen
	public int calcActionsHelper2(int[][] state, int[] queen){
		
		 if(state == null) {
			 System.out.println("removed null state");
			 return 0;
		 }
		 int count=0;
		// System.out.println("Queen at:"+queen[0]+" "+queen[1]);
			 int[] queenTemp = {0,0};	 
			 //for each next depth of position in single direction by case
			 
			
				boolean nextMoveValid;
				int x;
				int y;
				int queenrow = queen[0];
				int queencol = queen[1];
				int[][] move1 = new int[4][2];
				
			    // up
				
				
				 queenTemp[1] = queencol;
				 loop:
				 for(int j=queenrow; j<=10; j++) {
					 
					 queenTemp[0] = j;
					 move1[0][0] = queenTemp[0];
					 move1[0][1] = queenTemp[1];
					 move1[3][0] = queen[0];
					 move1[3][1] = queen[1];
					 
					 if(isValid(move1, state)) {
						count++;
					    move1 = new int[4][2];
					    }
					 else break loop;
					 }

				 // down-right
			
				  nextMoveValid = true;
					 x = queenrow;
				 	 y = queencol;
				 	 
				 while(nextMoveValid) {
					 
				 	 move1 = new int[4][2];
				 	 queenTemp[0] = --x;
					 queenTemp[1] = ++y;
					 
				
					 
					 move1[0][0] = queenTemp[0];
					 move1[0][1] = queenTemp[1];
					 move1[3][0] = queen[0];
					 move1[3][1] = queen[1];
					 nextMoveValid = isValid(move1, state);
					 if(nextMoveValid) { 

						 count++;
					    
					 }
					 else { break;}
					 
				 }
				 
				 
				 //right
				 queenTemp[0] = queenrow;
				 loop:
					 for(int j=queencol+1; j<=10; j++) {
						 move1 = new int[4][2]; 
						 queenTemp[1] = j;
						 move1[0][0] = queenTemp[0];
						 move1[0][1] = queenTemp[1];
						 move1[3][0] = queen[0];
						 move1[3][1] = queen[1];
						 if(isValid(move1, state)) {

							 count++;
						}
						 else break loop;
						 }

				//down
				
				 queenTemp[1] = queencol;
				 loop:
					 for(int j=queenrow-1; j>00; j--) {
						 move1 = new int[4][2]; 
						 queenTemp[0] = j;
						 move1[0][0] = queenTemp[0];
						 move1[0][1] = queenTemp[1];
						 move1[3][0] = queen[0];
						 move1[3][1] = queen[1];
						 if(isValid(move1, state)) {

							 count++;
						}
						 else break loop;
						 }
				 
				 //down-left		 
			  nextMoveValid = true;
					 x = queenrow;
				 	 y = queencol;
				 	 
				 while(nextMoveValid) {
					 
					 move1 = new int[4][2]; 
				 	 queenTemp[0] = --x;
					 queenTemp[1] = --y;
					 
					 move1[0][0] = queenTemp[0];
					 move1[0][1] = queenTemp[1];
					 move1[3][0] = queen[0];
					 move1[3][1] = queen[1];
					 nextMoveValid = isValid(move1, state);
					 if(nextMoveValid) { 

						 count++;
					    
					 }
					 else { break;}
					 
				 }
				 

				 //left 
				 queenTemp[0] = queenrow;
				 loop:
					 for(int j=queencol-1; j>0; j--) {
						 move1 = new int[4][2];  
						 queenTemp[1] = j;
						 move1[0][0] = queenTemp[0];
						 move1[0][1] = queenTemp[1];
						 move1[3][0] = queen[0];
						 move1[3][1] = queen[1];
						 if(isValid(move1, state)) {

							 count++;
						}
						 else break loop;
						 }
				 
				 
				 //up left
			  nextMoveValid = true;
					 x = queenrow;
				 	 y = queencol;
				 	 
				 while(nextMoveValid) {
					 
					 move1 = new int[4][2];  
				 	 queenTemp[0] = ++x;
					 queenTemp[1] = --y;
					 
					 move1[0][0] = queenTemp[0];
					 move1[0][1] = queenTemp[1];
					 move1[3][0] = queen[0];
					 move1[3][1] = queen[1];
					 nextMoveValid = isValid(move1, state);
					 if(nextMoveValid) { 

						 count++;
					    
					 }
					 else { break;}
					 
				 }
				 //up right
				 nextMoveValid = true;
				 x = queenrow;
			 	 y = queencol;
			 	 
			 while(nextMoveValid) {
				 move1 = new int[4][2];  
			 	 queenTemp[0] = ++x;
				 queenTemp[1] = ++y;
				 
				 move1[0][0] = queenTemp[0];
				 move1[0][1] = queenTemp[1];
				 move1[3][0] = queen[0];
				 move1[3][1] = queen[1];
				 nextMoveValid = isValid(move1, state);
				 if(nextMoveValid) { 
					 count++;
				    
				 }
				 else { break;}
				 
			 }
				 
		
		//0 new location, 1 is heuristic value, 2 is arrow position, 3 is old location
		 
		return count;

}



	
	public ArrayList<int[]> findQueens(int[][] state){
		
		ArrayList<int[]> queens = new ArrayList<int[]>();
		for(int i =0; i<11; i++) {
			for(int j=0; j<11; j++) {
			if(state[i][j] == side) {
				int[] temp = {i, j};
				queens.add(temp);
			}
			}

		}
		
		return queens;
	}
	public ArrayList<int[]> findEnemyQueens(int[][] state){
		
		ArrayList<int[]> queens = new ArrayList<int[]>();
		for(int i =0; i<11; i++) {
			for(int j=0; j<11; j++) {
			if(state[i][j] == enemySide) {
				int[] temp = {i, j};
				queens.add(temp);
			}
			}

		}
		
		return queens;
	}
	
	// method that checks if a queen has moves available
	public boolean hasMoves(int[][] state, int[] queen) {
		int x = queen[0];
		int y = queen[1];
		
		
		if(x==10 && y == 10) {
			if(state[x-1][y] == 0 || state[x][y-1] == 0) {
				return true;
			}
		}
		if(x == 10) {
			if(state[x][y+1] == 0 ||state[x][y-1] == 0 || state[x-1][y] == 0) {
				return true;
			}
		}
		if(y == 10) {
			if(state[x+1][y] == 0 ||state[x-1][y] == 0  || state[x][y-1] == 0) {
				return true;
			}
		}
		
		
		if(state[x+1][y] == 0 ||state[x-1][y] == 0  || state[x][y-1] == 0 | state[x][y+1] == 0) {
			return true;
		}
	
		return false;
	}
	
	// returns list of potential actions for a game state
	// Finds all the possible combinations of moves ie, All the possible next queen positions with all the 
	// arrow positions of the possible next queen position
	
	public ArrayList<int[][]> calcActions(int[][] state) {
		ArrayList<int[][]> actions = new ArrayList<int[][]>();
		//System.out.println(state == null);
		if(state == null) return null;
		//Adds all actions together for each queen

		ArrayList<int[][]> cahReturn = new ArrayList<int[][]>();
		int end = 0;
		int start;
		
		for(int[] queen: findQueens(state)) {
			//System.out.println("find queen done: "+ queen[0] + " " + queen[1]);
			//if(hasMoves(state,queen)) {
			cahReturn = calcActionsHelper(state, queen); // Finds all moves for a single queen
			//}
			if(cahReturn!=null) {
			start = end;
			end = end+cahReturn.size();
			for(int i = start;i<end;i++) {
				actions.add(i,cahReturn.get(i-start)); // Add all moves into actions
			}
			System.out.println("********************"+cahReturn.size());
		}
			}
		
//		System.out.println("Actions #: "+ actions.size());
//		System.out.println(actions.toString());
		return actions;
		
	}
	
	//Determines all potential movements for queen in the current state
	public ArrayList<int[][]> calcActionsHelper(int[][] state, int[] queen){
		
			 ArrayList<int[][]> out = new ArrayList<int[][]>();
			 //for each direction
			 if(state == null) {
				 System.out.println("removed null state");
				 return null;
			 }
			
				 int[] queenTemp = {0,0};	 
				 //for each next depth of position in single direction by case
				
				
					boolean nextMoveValid;
					int x;
					int y;
					int queenrow = queen[0];
					int queencol = queen[1];
					int[][] move1 = new int[4][2];
				 // up
					 //for queens vert coordinate
					
					 queenTemp[1] = queencol;
					 loop:
					 for(int j=queenrow; j<=10; j++) {
						 
						 queenTemp[0] = j;
						 move1[0][0] = queenTemp[0];
						 move1[0][1] = queenTemp[1];
						 move1[3][0] = queen[0];
						 move1[3][1] = queen[1];
						 
						 if(isValid(move1, state)) {
							 if(OT[move1[0][0]][move1[0][1]]>1) { // Initialize the territory for our next potential moves DTmove finds level 2,3..
								 OT[move1[0][0]][move1[0][1]] = 1;
								 
							 }
	//						System.out.println("Queen move valid at"+queenTemp[0]+" "+queenTemp[1]);
						    for(int[][] moves: calcActionArrow(state, move1, queenTemp)) //Calculates all the possible arrow positions
						    	out.add(moves);                                                      //for the next potential queen position
						     
						
						    move1 = new int[4][2];
						    }
						 else break loop;
						 }
				
					 
					 
					 // down-right
				
					  nextMoveValid = true;
						 x = queenrow;
					 	 y = queencol;
					 	 
					 while(nextMoveValid) {
						 
					 	 move1 = new int[4][2];
					 	 queenTemp[0] = --x;
						 queenTemp[1] = ++y;
						 
					
						 
						 move1[0][0] = queenTemp[0];
						 move1[0][1] = queenTemp[1];
						 move1[3][0] = queen[0];
						 move1[3][1] = queen[1];
						 nextMoveValid = isValid(move1, state);
						 if(nextMoveValid) { 
							 if(OT[move1[0][0]][move1[0][1]]>1) {
								 OT[move1[0][0]][move1[0][1]] = 1;
								 
							 }
//							 System.out.println("dor Queen move valid at"+queenTemp[0]+" "+queenTemp[1]);
							 for(int[][] moves: calcActionArrow(state, move1, queenTemp)) 
							     out.add(moves);
						    
						 }
						 else { break;}
						 
					 }
					 
					 
					 //right
					 queenTemp[0] = queenrow;
					 loop:
						 for(int j=queencol+1; j<=10; j++) {
							 move1 = new int[4][2]; 
							 queenTemp[1] = j;
							 move1[0][0] = queenTemp[0];
							 move1[0][1] = queenTemp[1];
							 move1[3][0] = queen[0];
							 move1[3][1] = queen[1];
							 if(isValid(move1, state)) {
								 if(OT[move1[0][0]][move1[0][1]]>1) {
									 OT[move1[0][0]][move1[0][1]] = 1;
									 
								 }
				//				 System.out.println("r Queen move valid at"+queenTemp[0]+" "+queenTemp[1]);
								 for(int[][] moves: calcActionArrow(state, move1, queenTemp)) 
								     out.add(moves);
							}
							 else break loop;
							 }

					//down
					
					 queenTemp[1] = queencol;
					 loop:
						 for(int j=queenrow-1; j>00; j--) {
							 move1 = new int[4][2]; 
							 queenTemp[0] = j;
							 move1[0][0] = queenTemp[0];
							 move1[0][1] = queenTemp[1];
							 move1[3][0] = queen[0];
							 move1[3][1] = queen[1];
							 if(isValid(move1, state)) {
								 if(OT[move1[0][0]][move1[0][1]]>1) {
									 OT[move1[0][0]][move1[0][1]] = 1;
									 
								 }
				//				 System.out.println("d Queen move valid at"+queenTemp[0]+" "+queenTemp[1]);
								 for(int[][] moves: calcActionArrow(state, move1, queenTemp)) 
								     out.add(moves);
							}
							 else break loop;
							 }
					 
					 //down-left		 
				  nextMoveValid = true;
						 x = queenrow;
					 	 y = queencol;
					 	 
					 while(nextMoveValid) {
						 
						 move1 = new int[4][2]; 
					 	 queenTemp[0] = --x;
						 queenTemp[1] = --y;
						 
						 move1[0][0] = queenTemp[0];
						 move1[0][1] = queenTemp[1];
						 move1[3][0] = queen[0];
						 move1[3][1] = queen[1];
						 nextMoveValid = isValid(move1, state);
						 if(nextMoveValid) { 
							 if(OT[move1[0][0]][move1[0][1]]>1) {
								 OT[move1[0][0]][move1[0][1]] = 1;
								 
							 }
							// System.out.println("dl Queen move valid at"+queenTemp[0]+" "+queenTemp[1]);
							 for(int[][] moves: calcActionArrow(state, move1, queenTemp)) 
							     out.add(moves);
						    
						 }
						 else { break;}
						 
					 }
					 
	
					 //left 
					 queenTemp[0] = queenrow;
					 loop:
						 for(int j=queencol-1; j>0; j--) {
							 move1 = new int[4][2];  
							 queenTemp[1] = j;
							 move1[0][0] = queenTemp[0];
							 move1[0][1] = queenTemp[1];
							 move1[3][0] = queen[0];
							 move1[3][1] = queen[1];
							 if(isValid(move1, state)) {
								 if(OT[move1[0][0]][move1[0][1]]>1) {
									 OT[move1[0][0]][move1[0][1]] = 1;
									 
								 }
								 //System.out.println("l Queen move valid at"+queenTemp[0]+" "+queenTemp[1]);
								 for(int[][] moves: calcActionArrow(state, move1, queenTemp)) 
								     out.add(moves);
							}
							 else break loop;
							 }
					 
					 
					 //up left
				  nextMoveValid = true;
						 x = queenrow;
					 	 y = queencol;
					 	 
					 while(nextMoveValid) {
						 
						 move1 = new int[4][2];  
					 	 queenTemp[0] = ++x;
						 queenTemp[1] = --y;
						 
						 move1[0][0] = queenTemp[0];
						 move1[0][1] = queenTemp[1];
						 move1[3][0] = queen[0];
						 move1[3][1] = queen[1];
						 nextMoveValid = isValid(move1, state);
						 if(nextMoveValid) { 
							 if(OT[move1[0][0]][move1[0][1]]>1) {
								 OT[move1[0][0]][move1[0][1]] = 1;
								 
							 }
							 //System.out.println("ul Queen move valid at"+queenTemp[0]+" "+queenTemp[1]);
							 for(int[][] moves: calcActionArrow(state, move1, queenTemp)) 
							     out.add(moves);
						    
						 }
						 else { break;}
						 
					 }
					 //up right
					 nextMoveValid = true;
					 x = queenrow;
				 	 y = queencol;
				 	 
				 while(nextMoveValid) {
					 move1 = new int[4][2];  
				 	 queenTemp[0] = ++x;
					 queenTemp[1] = ++y;
					 
					 move1[0][0] = queenTemp[0];
					 move1[0][1] = queenTemp[1];
					 move1[3][0] = queen[0];
					 move1[3][1] = queen[1];
					 nextMoveValid = isValid(move1, state);
					 if(nextMoveValid) { 
						 if(OT[move1[0][0]][move1[0][1]]>1) {
							 OT[move1[0][0]][move1[0][1]] = 1;
							 
						 }
						 //System.out.println("ur Queen move valid at"+queenTemp[0]+" "+queenTemp[1]);
						 for(int[][] moves: calcActionArrow(state, move1, queenTemp)) 
						     out.add(moves);
					    
					 }
					 else { break;}
					 
				 }
					 
			
			//0 new location, 1 is heuristic value, 2 is arrow position, 3 is old location
			 
			return out;

	}
	
	//Used to determine if a move is valid given the current state
	public boolean isValid(int[][] move,int[][] state) {
			int[] nextPos  = move[0];
			int[] currPos  = move[3];
			
			try {
	
			if(nextPos[0] >10 || nextPos[0] < 1 || nextPos[1] >10 || nextPos[1] < 1) {
	//			System.out.println("pos or  false");
				return false;
			}
			
			if(state[nextPos[0]][nextPos[1]] == 0) {
				return true;
			} 
			
			else return false;
			}
			catch(Exception e) {
				e.printStackTrace();
				return false;
			}
		
		}
    //Used to determine if an arrow is valid 
	public boolean isValid(int[][] state,int[] arrow) {

		try {
			//System.out.println("*"+stateFinal[arrow[0]][arrow[1]]);
			int x = arrow[0];
			int y = arrow[1];
		if(x<1||y<1||x>10||y>10)
			return false;
		else
		if(state[arrow[0]][arrow[1]] == 0) {
			return true;
		}else {
		
			return false;
		}
		}
		catch(Exception e) {
			return false;
		}
	}
	//Calculates all potential arrow positions for a queen in the current state
	public ArrayList<int[][]> calcActionArrow(int[][] state, int[][] move, int[] queen) {
			
		ArrayList<int[][]> out = new ArrayList<int[][]>();
//			System.out.println("new CAA");
			 if(state == null) {
				 System.out.println("removed null state");
				 return null;
			 }
			
			 
			 	boolean nextMoveValid;
				int	x;
				int	y;
			 
				 int[] arrowTemp = {0,0};
				 
				 //for each next depth of position in single direction by case


				 // up
					 //for queens vert coordinate
				 
				 int queenrow = queen[0];
				 int queencol = queen[1];
				 int counter =0;
				 int[][] move1 = move;
				 //int[][] moveTemp;
				 
				 	arrowTemp[1] = queencol;
				 	loop:
					 for(int j=queenrow+1; j<=10; j++) {	
//						 System.out.println("Arrow case 1");
						 
						
						 arrowTemp[0] = j;
						 
						 if(isValid(state,arrowTemp)) { 
							 //moveTemp.put(2, arrowTemp);
							  
							 move1[2][0] = arrowTemp[0];
							 move1[2][1] = arrowTemp[1];
		//					System.out.println("Up Valid at "+arrowTemp[0]+" "+arrowTemp[1]);
						 move1[1][0]=determineValue(state,move1); // Now that we have the next position of the queen, and the
						                                          //arrow position, determine the heuristic for the move
						
						 out.add(move1); // add move to all potential moves
						 move1 = new int[4][2];
						 move1[0][0] = move[0][0];
						 move1[0][1] = move[0][1];
						 move1[3][0] = move[3][0];
						 move1[3][1] = move[3][1];
						 //System.out.println("Move get "+move.get(2)[0]+" "+move.get(2)[1]);
						 
							 }	
						 else
							 break loop;
					 }

//					 // up-right
//					
				 	
					 x = queenrow;
					 y = queencol;
					 nextMoveValid=true;
					 
					 while(nextMoveValid) {
//						 System.out.println("Arrow case 4");
						 arrowTemp[0] = ++x;
						 arrowTemp[1] = ++y;
					
						 nextMoveValid = isValid(state,arrowTemp);
						 
						 if(nextMoveValid){ 	
							  
							 move1[2][0] = arrowTemp[0];
							 move1[2][1] = arrowTemp[1];
		//					System.out.println("Up right Valid at "+arrowTemp[0]+" "+arrowTemp[1]);
						 move1[1][0]=determineValue(state,move1);
						
						 out.add(move1);
						 move1 = new int[4][2];
						 move1[0][0] = move[0][0];
						 move1[0][1] = move[0][1];
						 move1[3][0] = move[3][0];
						 move1[3][1] = move[3][1];
						 }
					 }
					 //right
					 
				     arrowTemp[0] = queenrow;
					 breakloop:
					 for(int j=queencol+1; j<=10; j++) {	
//						 System.out.println("Arrow case 3");
						 arrowTemp[1] = j;
						
						 
						 if(isValid(state,arrowTemp)) { 
							  
							 move1[2][0] = arrowTemp[0];
							 move1[2][1] = arrowTemp[1];
			//				System.out.println("right Valid at "+arrowTemp[0]+" "+arrowTemp[1]);
						 move1[1][0]=determineValue(state,move1);
						
						 out.add(move1);
						 move1 = new int[4][2];
						 move1[0][0] = move[0][0];
						 move1[0][1] = move[0][1];
						 move1[3][0] = move[3][0];
						 move1[3][1] = move[3][1];
						 }
						 else
							 break breakloop;
					 }
//
					 //down-right
					 
					 x = queenrow;
					 y = queencol;
					 nextMoveValid=true;
					 
					 while(nextMoveValid) {
						 arrowTemp[0] = --x;
						 arrowTemp[1] = ++y;
					
						 nextMoveValid = isValid(state,arrowTemp);
						 
						 if(nextMoveValid){ 	
							  
							 move1[2][0] = arrowTemp[0];
							 move1[2][1] = arrowTemp[1];
			//				System.out.println("downright Valid at "+arrowTemp[0]+" "+arrowTemp[1]);
						 move1[1][0]=determineValue(state,move1);
						
						 out.add(move1);
						 move1 = new int[4][2];
						 move1[0][0] = move[0][0];
						 move1[0][1] = move[0][1];
						 move1[3][0] = move[3][0];
						 move1[3][1] = move[3][1];
						 }
						 }

					 //down
					 
					 arrowTemp[1] = queencol;
					 breakloop:
					 for(int j=queenrow-1; j>0; j--) {	
//						 System.out.println("Arrow case 5");
						 arrowTemp[0] = j;
						
						 if(isValid(state,arrowTemp)) { 
							  
							 move1[2][0] = arrowTemp[0];
							 move1[2][1] = arrowTemp[1];
			//				System.out.println("down Valid at "+arrowTemp[0]+" "+arrowTemp[1]);
						 move1[1][0]=determineValue(state,move1);
						
						 out.add(move1);
						 move1 = new int[4][2];
						 move1[0][0] = move[0][0];
						 move1[0][1] = move[0][1];
						 move1[3][0] = move[3][0];
						 move1[3][1] = move[3][1];
								 }	
						 else
							 break breakloop;
							 
						 }
//					 
//
//					 //down-left
					 x = queenrow;
					 y = queencol;
					 nextMoveValid=true;
					 
					 while(nextMoveValid) {
//						 System.out.println("Arrow case 4");
						 arrowTemp[0] = --x;
						 arrowTemp[1] = --y;
						
						 nextMoveValid = isValid(state,arrowTemp);
//						 if(x<1||y<1||x>10||y>10)
//								nextMoveValid=false;
						 
						 if(nextMoveValid){ 	
							  
							 move1[2][0] = arrowTemp[0];
							 move1[2][1] = arrowTemp[1];
		//					System.out.println("downleft Valid at "+arrowTemp[0]+" "+arrowTemp[1]);
						 move1[1][0]=determineValue(state,move1);
						
						 out.add(move1);
						 move1 = new int[4][2];
						 move1[0][0] = move[0][0];
						 move1[0][1] = move[0][1];
						 move1[3][0] = move[3][0];
						 move1[3][1] = move[3][1];
						 }
						 }

//					 //left 
					 arrowTemp[0] = queenrow;
					 breakloop:
					 for(int j=queencol-1; j>0; j--) {	
//						 System.out.println("Arrow case 3");
						 arrowTemp[1] = j;
						 
						 
						 if(isValid(state,arrowTemp)) { 
							  
							 move1[2][0] = arrowTemp[0];
							 move1[2][1] = arrowTemp[1];
	//						System.out.println("left Valid at "+arrowTemp[0]+" "+arrowTemp[1]);
						 move1[1][0]=determineValue(state,move1);
						
						 out.add(move1);
						 move1 = new int[4][2];
						 move1[0][0] = move[0][0];
						 move1[0][1] = move[0][1];
						 move1[3][0] = move[3][0];
						 move1[3][1] = move[3][1];
								 }	 
						 else
							 break breakloop;
					 }
//
//					 
//					 //up-left
					 x = queenrow;
					 y = queencol;
					 nextMoveValid=true;
					 
					 while(nextMoveValid) {
//						 System.out.println("Arrow case 4");
						 arrowTemp[0] = ++x;
						 arrowTemp[1] = --y;
					
						 nextMoveValid = isValid(state,arrowTemp);
						 
						 if(nextMoveValid){ 	
							  
							 move1[2][0] = arrowTemp[0];
							 move1[2][1] = arrowTemp[1];
	//						System.out.println("Upleft Valid at "+arrowTemp[0]+" "+arrowTemp[1]);
						 move1[1][0]=determineValue(state,move1);
						
						 out.add(move1);
						 move1 = new int[4][2];
						 move1[0][0] = move[0][0];
						 move1[0][1] = move[0][1];
						 move1[3][0] = move[3][0];
						 move1[3][1] = move[3][1];
						 }
						 }

			return out;
	}
	
	
	// quick implementation to pick best heuristic action
	public int[][] highestValue(int[][]state){
		int[][] bestAction = new int[4][2];
		
		ArrayList<int[][]> actions = calcActions(state);
		//System.out.println(actions.get(1));
		bestAction[1][0] = -1000;
		System.out.println("Sorting....");
		for(int[][] move: actions) 
//			System.out.println(move[2][0]+" "+move[2][1]);
		{
			//System.out.println(move.get(2)[0]+" "+move.get(2)[1]);
		//if(bestAction[1][0] == 0) bestAction = move;
		if(bestAction[1][0]<move[1][0]) {
			bestAction = move;
		}
		}
		System.out.println("Done. New Pos:" + bestAction[0][0] + "  "+bestAction[0][1] +
				" Old Pos: " + bestAction[3][0] + "  "+ bestAction[3][1] +
				" Arrow Pos:" +  bestAction[2][0] + "  "+bestAction[2][1] );
		return bestAction;	
	}

	// Alpha-beta pruning function, not working yet
///	int[] term = {1,0};
	public int[][] alphaBetaS(int[][] state) {
		// 0 is move, 1[0] is value in move.
		System.out.println("ALPHA BETA ACTIVATED");
		int[][] negInf = new int[4][2];
		int[][] posInf = new int[4][2];
		posInf[1][0] = 100;
		negInf[1][0] = -100;
		int[][] tempState = state;
		int[][] move = new int[4][2];
		move = maxValue(tempState,move, negInf, posInf);
		return move;
	}
//Part of ABP
	public int[][] maxValue(int [][] state,int[][] move, int[][] a, int[][] b) {
		System.out.println("MAX VALUE");
//		if(move[4] == term) {}else {
//		//negInf
//		} 
		
		movev[1][0] = -100; 
		ArrayList<int [][]> actions = calcActions(state);
		//if(actions == null) return move;
		for(int[][] movea: actions ) {

			move2 = minValue(updateState(state, movea), movea, a,b);
			if(move2[1][0]==0)
				return move2;
			if(move2[1][0] > movev[1][0]) {
				movev = move2;
				a = max(a,movev); 
			}
			if(movev[1][0]>=b[1][0]) {
				return movev;
			
			}	
		}
		return movev;
	}
	//Part of ABP
	public int[][] minValue(int [][] state, int[][] move, int[][] a,int[][] b) {
		System.out.println("MINVALUE");
//		if(move.get(4) == term) {}else {
//		//negInf
//		}
		movev[1][0] = +100;
		ArrayList<int[][]> actions = calcActions(state);
		if(actions == null) return move;
		for(int[][] movea: actions ) {
			
			move2 = maxValue(updateState(state, movea), movea, a,b);
			if(move2[1][0]==0)
				return move2;
			if(move2[1][0] < movev[1][0]) {
				movev = move2;
				b = min(b,movev);
			}
			if(movev[1][0]<=a[1][0]) {
				return movev;
			}	
			
		}
		return movev;
	}
	//returns max value move //Part of ABP
	public int[][] max( int[][] movea ,  int[][] moveb) {
		int a = movea[1][0];
		int b = moveb[1][0];	
		if(a>b) {
			return movea;
		}else if(a==b){
			
			return movea;
		}else {
			return moveb;
		}
	}
	
	//returns min value move//Part of ABP
	public int[][] min( int[][] movea ,  int[][] moveb) {
		int a = movea[1][0];
		int b = moveb[1][0];	
		
		if(a>b) {
			return moveb;
		}else if(a==b){
			
			return moveb;
		}else {
			return movea;
		}
	}


	
	//Updates the state with a move that has an arrow position, used for making a move, and updating the actual state
	public int[][] updateState(int[][] state, int[][] move) {
		//0:next 1:value 2: arrow 3: current 
//		System.out.println("updateState");
//		printGameBoard(state);
//		System.out.println();
	//if(isValid(move,state) && isValid(state,move[2])) {
		int[] update;	
		update = move[3];
		int value = state[update[0]][update[1]];
		state[update[0]][update[1]] = emptySpace;
		
		int[] update2 = move[0];
		state[update2[0]][update2[1]] = value;
		
		int[] update3 = move[2];
		state[update3[0]][update3[1]] = arrowedSpace;
		
		//printGameBoard(state);
		//System.out.println("after");
	//	stateFinal = state;
		return state;
	//}else {
		//return null;
		//}
	
	}
	// Used to update a state with a move that does not have an arrow, used to update
	// the temporary states for calculating territory
	public int[][] updateState1(int[][] state, int[][] move) {
		
		int[] update;	
		update = move[3];
		int value = state[update[0]][update[1]];
		state[update[0]][update[1]] = emptySpace;
		
		int[] update2 = move[0];
		state[update2[0]][update2[1]] = value;
		
		int[] update3 = move[2];
		state[update3[0]][update3[1]] = arrowedSpace;
		
		//printGameBoard(state);
		//System.out.println("after");
	//	stateFinal = state;
		return state;
	
	
	}


	// Testing: Prints game board and puts it into a 2D Array
	public void printGameBoard(int[][] state) {
		
		for(int i = 1; i <=10;i++) {
    		for(int j = 1; j <=10;j++) {
   			System.out.print(state[11-i][j]);
    		}
    		System.out.println("");
    	}
    		
	}
	
	
	// on every enemy move update the current state
	 public void enemyMove(Map<String, Object> msg) {
		 // {queen-position-current=[4, 10], queen-position-next=[4, 8], arrow-position=[4, 6]}\
		 int[] eCurr = new int[2];
		 int[] eNext = new int[2];
		 int[] eArrow = new int[2];
		 
		 
		String currentMsg = msg.get("queen-position-current").toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\ ", "");
		String [] cmsg = currentMsg.split("\\,");
//		System.out.println(currentMsg);
//		System.out.println(cmsg[0]);	 
		eCurr[0]= Integer.parseInt(String.valueOf(cmsg[0]));
		eCurr[1]= Integer.parseInt(String.valueOf(cmsg[1]));
		 
//		System.out.println("curr:"+eCurr[0] + "  " + eCurr[1]);
		
		
		 
		String nextMsg = msg.get("queen-position-next").toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\ ", "");
//		System.out.println(nextMsg);
		String[] nmsg = nextMsg.split("\\,");
		
		 
		eNext[0]= Integer.parseInt(String.valueOf(nmsg[0]));
		eNext[1]= Integer.parseInt(String.valueOf(nmsg[1]));
//		System.out.println("next:"+ eNext[0] + "  " + eNext[1]);
		
		
		
		String arrowMsg = msg.get("arrow-position").toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\ ", "");
		String[] amsg = arrowMsg.split("\\,");
//		System.out.println(arrowMsg);
		eArrow[0]= Integer.parseInt(String.valueOf(amsg[0]));
		eArrow[1]= Integer.parseInt(String.valueOf(amsg[1]));
		
//		System.out.println("arrow:" + eArrow[0] + "  " + eArrow[1]);
		
		// Update enemy state assuming it is valid
		
		stateFinal[eCurr[0]][eCurr[1]] = emptySpace;
		stateFinal[eNext[0]][eNext[1]] = enemySide;
		stateFinal[eArrow[0]][eArrow[1]]= arrowedSpace;
	 }






}
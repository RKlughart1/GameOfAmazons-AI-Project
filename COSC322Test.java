
package ubc.cosc322;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import sfs2x.client.entities.Room;
import ygraph.ai.smartfox.games.BaseGameGUI;
import ygraph.ai.smartfox.games.GameClient;
import ygraph.ai.smartfox.games.GameMessage;
import ygraph.ai.smartfox.games.GamePlayer;
import ygraph.ai.smartfox.games.amazons.AmazonsGameMessage;

/**
 * An example illustrating how to implement a GamePlayer
 * @author Yong Gao (yong.gao@ubc.ca)
 * Jan 5, 2021
 *
 */
public class COSC322Test extends GamePlayer{

    private GameClient gameClient = null; 
    private BaseGameGUI gamegui = null;
	private int counter = 0;
    private String userName = null;
    private String passwd = null;
    // 2 for L(black), 1 for R(white)
    public int side;

    
    AiPlayer ai = new AiPlayer();
	
    /**
     * The main method
     * @param args for name and passwd (current, any string would work)
     */
    public static void main(String[] args) {	
    
    	COSC322Test player = new COSC322Test("user2", "pass"); // Can change user and pass to any string 
    	
    	if(player.getGameGUI() == null) {
    		player.Go();
    	}
    	else {
    		BaseGameGUI.sys_setup();
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                	player.Go();
                }
            });
    	}
    }
	
    /**
     * Any name and passwd 
     * @param userName
      * @param passwd
     */
    public COSC322Test(String userName, String passwd) {
    	this.userName = userName;
    	this.passwd = passwd;
        
    	//To make a GUI-based player, create an instance of BaseGameGUI
    	//and implement the method getGameGUI() accordingly
    	this.gamegui = new BaseGameGUI(this); 
    	
    }
 


    @Override
    public void onLogin() {
 
    	userName = gameClient.getUserName();
    	if(gamegui != null) {
    		gamegui.setRoomInformation(gameClient.getRoomList());
    	}
    }

	@SuppressWarnings("unchecked")
	@Override 
    public boolean handleGameMessage(String messageType, Map<String, Object> msgDetails) {
    	//This method will be called by the GameClient when it receives a game-related message
    	//from the server.
		int[][] output = new int[4][2];
    	//For a detailed description of the message types and format, 
    	//see the method GamePlayer.handleGameMessage() in the game-client-api document. 
		ArrayList<Integer> queenCurr = new ArrayList<Integer>();
	    ArrayList<Integer> queenNext = new ArrayList<Integer>();
	    ArrayList<Integer> arrowPoss = new ArrayList<Integer>();

		//If game state update, update the game board
    	if(messageType.equals(GameMessage.GAME_STATE_BOARD)) {
    		System.out.println("GM state");
    		 gamegui.setGameState((ArrayList<Integer>) msgDetails.get("game-state"));
    		 ai.stateFinal = ai.initState(msgDetails);
    		 ai.territoryWeight = 4;//How much the heuristic depends on territory
    		 ai.maxDepth = 2; // Territory - dont care about positions we cant get to after maxDepth moves
    		                  

    	}	
    	
		// Initialize AI when game starts
    	if(messageType.equals(GameMessage.GAME_ACTION_START)) {
            System.out.println("Game Start: "+ msgDetails.toString());
          
            String blackside = msgDetails.get("player-black").toString();
            if(blackside.equals(userName)) {
                System.out.println("I am black side");
                
                ai.side = 1;//1
                ai.enemySide=2;
                
            	ai.OT = ai.Highstate(); // Initialize both teams territory to all 9(default state) OT- Our Territory
        	    ai.ET = ai.Highstate(); // ET - Enemy Territory
        	    ai.DT(ai.stateFinal,1); // Calculate the current territory
        	    //ai.initialTerritory = ai.TW(); // Value of the current territory
        	    
                output = ai.calculateMove();
                
                ai.printGameBoard(ai.OT);
        		System.out.println("");
        		ai.printGameBoard(ai.ET);
            	System.out.println("Next: " + output[0][0] + " " + output[0][1]);
            	System.out.println("arrowing: "+ output[2][0]+ " " + output[2][1]);	
            	System.out.println("curr: " + + output[3][1] + " " + output[3][1]); 
            	
            	//*********** Indexing for a move, 0:next queen position 1:value 2: arrow 3: current ***************//
            	
                queenCurr.add(output[3][0]);
                queenCurr.add(output[3][1]);
                queenNext.add(output[0][0]);
                queenNext.add(output[0][1]);
                arrowPoss.add(output[2][0]);
                arrowPoss.add(output[2][1]);
                
                ai.updateState(ai.stateFinal, output);
                gamegui.updateGameState(queenCurr, queenNext, arrowPoss);
                gameClient.sendMoveMessage(queenCurr, queenNext, arrowPoss);
                this.counter++;

 
            }else { 
                System.out.println("I am white side");
                ai.side = 2;//2
                ai.enemySide=1;
            }
            return true;
        }
 
		// On enemy move, log move, then send next move.
    	if(messageType.equals(GameMessage.GAME_ACTION_MOVE)) { 
    		
    		//System.out.println("GM action move:" + msgDetails.toString());
    		gamegui.updateGameState(msgDetails);
    		ai.enemyMove(msgDetails);//Read enemy move and update the state

       		
       		ai.OT = ai.Highstate();
    	    ai.ET = ai.Highstate();
    	    ai.DT(ai.stateFinal,1); 
    	    

    		output = ai.calculateMove(); 
    		ai.printGameBoard(ai.OT);
    		System.out.println("");
    		ai.printGameBoard(ai.ET);
  
    		System.out.println("Next: " + output[0][0] + " " + output[0][1]);
        	System.out.println("arrowing: "+ output[2][0]+ " " + output[2][1]);	
        	System.out.println("curr: " + + output[3][1] + " " + output[3][1]); 
        	
        	//*********** Indexing for a move, 0:next queen position 1:value 2: arrow 3: current ***************//
        	
            queenCurr.add(output[3][0]);
            queenCurr.add(output[3][1]);
            queenNext.add(output[0][0]);
            queenNext.add(output[0][1]);
            arrowPoss.add(output[2][0]);
            arrowPoss.add(output[2][1]);
            this.counter++;
            System.out.println(this.counter);
            if(this.counter==15) // start making more calculated moves late game
            	ai.maxDepth=3;
            if(this.counter==25)
            	ai.maxDepth=4;
            if(this.counter >=46 || output[2][0]==0) { // 2*46 = 92 which is the maximum arrow positions allowed
            	System.out.println("Game Over");
            	return false;
            }
            
            ai.updateState(ai.stateFinal,output); // Update state after our move
            gamegui.updateGameState(queenCurr, queenNext, arrowPoss);
            gameClient.sendMoveMessage(queenCurr, queenNext, arrowPoss);
            
    	
    	
//    	ai.printGameBoard(ai.stateFinal);
//    	
//    	
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) { //Delay each move to see how the game plays out	
			e.printStackTrace();
    	}
		
		return true;
    	}
    	
      	return true;
    	
    }

    @Override
    public String userName() {
    	return userName;
    }

	@Override
	public GameClient getGameClient() {
		// TODO Auto-generated method stub
		return this.gameClient;
	}

	@Override
	public BaseGameGUI getGameGUI() {
		// TODO Auto-generated method stub
		return  this.gamegui; 
	}

	@Override
	public void connect() {
		// TODO Auto-generated method stub
    	gameClient = new GameClient(userName, passwd, this);			
 

	}
	
	
}//end of class
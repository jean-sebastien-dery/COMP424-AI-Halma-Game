package s260430688;

import halma.CCBoard;
import boardgame.Board;

public class myPlayer2 extends s260430688Player {
	/**
	 * Constructor.
	 * 
	 * @param name My McGill student number that will be used to grade the project.
	 */
	public myPlayer2() {
		// Instantiates my implementation with my McGill ID.
		super("260430682");
	}
	
	/**
	 * Constructor.
	 * 
	 * @param s The student ID.
	 */
	public myPlayer2(String s) {
		super(s);
	}
    
	/**
	 * Creates the board for the current player.
	 */
    public Board createBoard() { return new CCBoard(); }
}

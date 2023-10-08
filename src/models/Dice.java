package models;

public class Dice {
	
	private char letter;
	private boolean isVisited = false;
	
	/**
	 * constructor
	 * @param letter the letter
	 */
	public Dice(char letter) {
		this.letter = letter;
	}
	
	/**
	 * 
	 * @return the letter
	 */
	public char getLetter() {
		return letter;
	}
	
	/**
	 * 
	 * @return isVisited
	 */
	public boolean isVisited() {
		return isVisited;
	}
	
	/**
	 * setter of isVisited
	 * @param isVisited
	 */
	public void setIsVisited(boolean isVisited) {
		this.isVisited = isVisited;
	}

	@Override
	public String toString() {
		return "Dice [letter=" + letter + "]";
	}
	
	

}

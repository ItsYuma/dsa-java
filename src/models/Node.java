package models;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class Node implements Comparable<Node> {
	
	private char letter;
	private Node[] children;
	private boolean isWord;
	
	/**
	 * constructor
	 * @param letter the letter
	 * @param isWord if the node is a word
	 */
	public Node (char letter, boolean isWord) {
		this.letter = letter;
		this.isWord = isWord;
	}
	
	/**
	 * 
	 * @return letter
	 */
	public char getLetter() {
		return letter;
	}
	
	/**
	 * 
	 * @return isWord
	 */
	public boolean isWord() {
		return isWord;
	}
	
	/**
	 * setter of isWord
	 */
	public void setIsWord() {
		this.isWord = true;
	}
	
	/**
	 * initiale, sort and return the children
	 * @return a list of the children
	 */
	public List<Node> getChildren() {
		if(children == null) return new ArrayList<Node>();
		
		List<Node> list = Arrays.asList(children);
		Collections.sort(list);
		return list;
	}
	
	
	/**
	 * add a element to the array children
	 * @param child the Node to add
	 */
	public void addChild(Node child) {
		if(children == null) children = new Node[0];
		
		children = Arrays.copyOf(children, children.length + 1);
		children[children.length - 1] = child;
	}
	
	/**
	 * 
	 * @return true if the propery children is null or have a size of under or equals to 0
	 */
	public boolean isLeaf() {
		if(children == null) return true;
		
		return children.length <= 0;
	}
	
	
	/**
	 * check if a Node is in the property children
	 * @param child the Node to check
	 * @return true if children contains child, false otherwise
	 */
	public boolean containLetterInChildren(Node child) {
		if(children == null) return false;
		
		for(Node n : children) {
			if(n.equals(child)) return true;
		}
		return false;
	}
	
	/**
	 * get the element in children that is similar to child
	 * @param child the node we want
	 * @return true if the property children contains child, null otherwise
	 */
	public Node getChildFromNode(Node child) {
		if(children == null) return null;
		
		for(Node n : children) {
			if (n.equals(child)) return n;
		}
		return null;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(letter, children);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Node))
			return false;
		Node other = (Node) obj;
		return letter == other.letter;
	}

	@Override
	public int compareTo(Node o) {
		Node other = (Node) o;
		return letter - other.letter;
	}

}

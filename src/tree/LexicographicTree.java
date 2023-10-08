package tree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import models.Node;
 
public class LexicographicTree {
	
	private Node root;
	
	//TODO verifier argument ? null ? char special ? espace ?
	
	/*
	 * CONSTRUCTORS
	 */
	
	/**
	 * Constructor : creates an empty lexicographic tree.
	 */
	public LexicographicTree() {
		root = new Node(' ', false);
	}
	
	/**
	 * Constructor : creates a lexicographic tree populated with words 
	 * @param filename A text file containing the words to be inserted in the tree 
	 */
	public LexicographicTree(String filename) {
		this();
		
		try(BufferedReader reader = Files.newBufferedReader(Paths.get(filename))){
			String line;
			while((line = reader.readLine()) != null) {
				insertWord(line);
			}
		} 
		catch (FileNotFoundException e) {
			System.out.println("Fichier non trouvé");
		}
		catch (IOException e) {
			System.out.println("Une erreur est survenue lors de la lecture du fichier");
		}
	}
	
	/*
	 * PUBLIC METHODS
	 */
	
	/**
	 * Returns the number of words present in the lexicographic tree.
	 * @return The number of words present in the lexicographic tree
	 */
	public int size() {
		int sizeTotal = root.isWord() ? 1 : 0;
		
		sizeTotal += recursiveSize(root);
		return sizeTotal;
	}

	/**
	 * Inserts a word in the lexicographic tree if not already present.
	 * @param word A word
	 */
	public void insertWord(String word) {
		Node actualNode = root;
		
		StringBuilder wordRefactor = new StringBuilder();
		
		
		//checks each character to remove the bad ones
		for(char c : word.toCharArray()) {
			if (Character.isLetter(c) || c == '-' || c == '\'') {
				wordRefactor.append(c);
			}
		}
		
		//if the word is empty, I set the property isWord of the root to true
		if(word.isEmpty()) {
			root.setIsWord();
			return;
		}
		
		for(int i = 0; i < wordRefactor.length(); i++) {
			
			if(!actualNode.containLetterInChildren(new Node(wordRefactor.charAt(i), false))) {
				actualNode.addChild(new Node(wordRefactor.charAt(i), false));
			}
			
			actualNode = actualNode.getChildFromNode(new Node(wordRefactor.charAt(i), false));
		}
		
		//once we have searched the whole word, I set the property isWord of the node that contains the last character to true
		actualNode.setIsWord();
	}
	
	/**
	 * Determines if a word is present in the lexicographic tree.
	 * @param word A word
	 * @return True if the word is present, false otherwise
	 */
	public boolean containsWord(String word) {
		if(word == null) throw new IllegalArgumentException("L'argument word ne doit pas etre null");
		
		//if the word is blank,I check if the property isWord of the root is true or false
		if(word.isBlank()) {
			return root.isWord();
		}
		
        Node actualNode = root;
        
        for (int i = 0; i < word.length(); i++) {
        	
        	if(actualNode == null) return false;
        	
            actualNode = actualNode.getChildFromNode(new Node(word.charAt(i), false)); 
            
        }
        
        return actualNode != null && actualNode.isWord();
	}
	
	/**
	 * Determines if a word is present in the lexicographic tree.
	 * @param word A word
	 * @return True if the word is present, false otherwise
	 */
	public boolean containsPrefix(String prefix) {
		
        Node actualNode = root;
        
        for (int i = 0; i < prefix.length(); i++) {
        	
        	if(actualNode == null) return false;
        	
            actualNode = actualNode.getChildFromNode(new Node(prefix.charAt(i), false));
        }
        
        return actualNode != null;
	}
	
	/**
	 * Returns an alphabetic list of all words starting with the supplied prefix.
	 * If 'prefix' is an empty string, all words are returned.
	 * @param prefix Expected prefix
	 * @return The list of words starting with the supplied prefix
	 */
	public List<String> getWords(String prefix) {
		if(prefix == null) throw new IllegalArgumentException("L'argument word ne doit pas etre null");
		
		if(prefix.isBlank()) {
			return getAllWordRecursive(root, new ArrayList<String>(), "");
		}
		else {
			prefix.trim();
			return getWordRecursive(root, new ArrayList<String>(), prefix ,prefix);
		}
	}

	/**
	 * Returns an alphabetic list of all words of a given length.
	 * If 'length' is lower than or equal to zero, an empty list is returned.
	 * @param length Expected word length
	 * @return The list of words with the given length
	 */
	public List<String> getWordsOfLength(int length) {
		List<String> words = new ArrayList<String>();
		if(length <= 0) return words;
		return getWordsOfLengthRecursive(root, words, "", length);
	}

	/*
	 * PRIVATE METHODS
	 */
	
	/**
	 * iterate through each node and each node's children and every time that a node is a word we increase the counter
	 * @param actualNode the actual Node
	 * @return the number of words
	 */
	private int recursiveSize(Node actualNode) {
		int sizeTotal = 0;
		
		//stop the method when the actualNode dont have any child
		if(actualNode.isLeaf()) {
			return sizeTotal;
		}
		else {
			//loop through all the children
			for(Node child : actualNode.getChildren()) {
				if(child.isWord()) {
					sizeTotal++;					
				}
				
				sizeTotal += recursiveSize(child);
				
			}
		}
		return sizeTotal;
	}
	
	/*private boolean containWordRecursive(Node n, String word) {
		Node child = new Node(word.charAt(0), false);
		if(word.length() == 1) {
			return n.containLetterInChildren(child) && n.getChildFromNode(child).isWord(); 
		}
		else {
			if(n.containLetterInChildren(child)) {
				return containWordRecursive(n.getChildFromNode(child), word.substring(1));
			}
			return false;
		}
	}*/
	
	/**
	 * iterate through each node and each node's children and concat every node letter to make word 
	 * @param actualNode the actual node
	 * @param words the words' list
	 * @param word the actual concat of letter
	 * @return a list of word
	 */
	private List<String> getAllWordRecursive(Node actualNode, List<String> words, String word) {
		//if the node is a leaf, he dont have more child, so we return the words' list and stop the recursivity
		if(actualNode.isLeaf()) {
			words.add(word);
			return words;
		}
		else {
			if(actualNode.isWord() && !actualNode.isLeaf()) words.add(word.toString());
			
			for(Node child : actualNode.getChildren()) {
				words.addAll(getAllWordRecursive(child, new ArrayList<String>(), word + child.getLetter()));
			}
			return words;
		}
	}
	/**
	 * iterate through each node (starting to the one that letter is the last of the prefix) and each node's children
	 * to get all words with the prefix 
	 * @param actualNode the actualNode 
	 * @param words the list of words
	 * @param wordPrefix the prefix
	 * @param prefix the prefix that we gonna substring until blank
	 * @return a list of word
	 */
	private List<String> getWordRecursive(Node actualNode, List<String> words ,String wordPrefix, String prefix) {
		//if the prefix is blank, call another method to get all the words of the tree that start with wordPrefix
		if(prefix.isBlank()) {
			words.addAll(getAllWordRecursive(actualNode, new ArrayList<String>(), wordPrefix));
			return words;
		}
		//check if some words with that prefix exist, when prefix equals "", we start to search word with it
		else {
			
			Node child = actualNode.getChildFromNode(new Node(prefix.charAt(0), false));
			
			//if the actualNode dont have a child with this letter, child gonna be null 
			if(child == null) return words;
			
			prefix = prefix.length() == 1 ? "" : prefix.substring(1);
			return getWordRecursive(child, words, wordPrefix, prefix);
		}
	}
	
	/**
	 * iterate through each node and each node's children until the depth equals the parameter length
	 * @param actualNode the actual node
	 * @param words the list of word
	 * @param word the actual word that is the concat of the actualNode and his parents
	 * @param length the length of word that we want
	 * @param isWord boolean that tell if "word" is a word
	 * @return a list a word of the length 'length'
	 */
	private List<String> getWordsOfLengthRecursive(Node actualNode, List<String> words, String word, int length) {
		//stop the recursivity when we are at the depth that we want
		if(length == 0) {
			if(actualNode.isWord())words.add(word);
			return words;
		}
		else {
			//loop until we reach the correct depth
			for(Node child : actualNode.getChildren()) {
				words = (getWordsOfLengthRecursive(child, words, word + child.getLetter(), length-1));
			}
		}
		return words;
	}
	
	/*
	 * TEST FUNCTIONS
	 */
		
	private static String numberToWordBreadthFirst(long number) {
		String word = "";
		int radix = 13;
		do {
			word = (char)('a' + (int)(number % radix)) + word;
			number = number / radix;
		} while(number != 0);
		return word;
	}
	
	private static void testDictionaryPerformance(String filename) {
		long startTime;
		int repeatCount = 20;
		
		// Create tree from list of words
		startTime = System.currentTimeMillis();
		System.out.println("Loading dictionary...");
		LexicographicTree dico = null;
		for (int i = 0; i < repeatCount; i++) {
			dico = new LexicographicTree(filename);
		}
		System.out.println("Load time : " + (System.currentTimeMillis() - startTime) / 1000.0);
		System.out.println("Number of words : " + dico.size());
		System.out.println();
		
		// Search existing words in dictionary
		startTime = System.currentTimeMillis();
		System.out.println("Searching existing words in dictionary...");
		File file = new File(filename);
		for (int i = 0; i < repeatCount; i++) {
			Scanner input;
			try {
				input = new Scanner(file);
				while (input.hasNextLine()) {
				    String word = input.nextLine();
				    boolean found = dico.containsWord(word);
				    if (!found) {
				    	//System.out.println(word + " / " + word.length() + " -> " + found);
				    }
				}
				input.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Search time : " + (System.currentTimeMillis() - startTime) / 1000.0);
		System.out.println();

		// Search non-existing words in dictionary
		startTime = System.currentTimeMillis();
		System.out.println("Searching non-existing words in dictionary...");
		for (int i = 0; i < repeatCount; i++) {
			Scanner input;
			try {
				input = new Scanner(file);
				while (input.hasNextLine()) {
				    String word = input.nextLine() + "xx";
				    boolean found = dico.containsWord(word);
				    if (found) {
				    	System.out.println(word + " / " + word.length() + " -> " + found);
				    }
				}
				input.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Search time : " + (System.currentTimeMillis() - startTime) / 1000.0);
		System.out.println();

		// Search words of increasing length in dictionary
		startTime = System.currentTimeMillis();
		System.out.println("Searching for words of increasing length...");
		for (int i = 0; i < 4; i++) {
			int total = 0;
			for (int n = 0; n <= 28; n++) {
				int count = dico.getWordsOfLength(n).size();
				total += count;
			}
			if (dico.size() != total) {
				System.out.printf("Total mismatch : dict size = %d / search total = %d\n", dico.size(), total);
			}
		}
		System.out.println("Search time : " + (System.currentTimeMillis() - startTime) / 1000.0);
		System.out.println();
	}

	private static void testDictionarySize() {
		final int MB = 1024 * 1024;
		System.out.print(Runtime.getRuntime().totalMemory()/MB + " / ");
		System.out.println(Runtime.getRuntime().maxMemory()/MB);

		LexicographicTree dico = new LexicographicTree();
		long count = 0;
		while (true) {
			dico.insertWord(numberToWordBreadthFirst(count));
			count++;
			if (count % MB == 0) {
				System.out.println(count / MB + "M -> " + Runtime.getRuntime().freeMemory()/MB);
			}
		}
	}
	
	/*
	 * MAIN PROGRAM
	 */
	
	public static void main(String[] args) {
		// CTT : test de performance insertion/recherche
		testDictionaryPerformance("mots/dictionnaire_FR_sans_accents.txt");
		
		// CST : test de taille maximale si VM -Xms2048m -Xmx2048m
		//
		
		testDictionarySize();
	}
}

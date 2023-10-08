package cryptanalysis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import tree.LexicographicTree;

public class DictionaryBasedAnalysis {
	
	private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String DICTIONARY = "mots/dictionnaire_FR_sans_accents.txt";
	
	private static final String CRYPTOGRAM_FILE = "txt/Plus fort que Sherlock Holmes (cryptogram).txt";
	private static final String DECODING_ALPHABET = "VNSTBIQLWOZUEJMRYGCPDKHXAF"; // Sherlock
	
	private static LexicographicTree tree;
	private String cryptogram;

	/*
	 * CONSTRUCTOR
	 */
	public DictionaryBasedAnalysis(String cryptogram, LexicographicTree dict) {
		tree = dict;
		this.cryptogram = cryptogram;
	}
	
	/*
	 * PUBLIC METHODS
	 */

	/**
	 * Performs a dictionary-based analysis of the cryptogram and returns an approximated decoding alphabet.
	 * @param alphabet The decoding alphabet from which the analysis starts
	 * @return The decoding alphabet at the end of the analysis process
	 */
	public String guessApproximatedAlphabet(String alphabet) {
		
		//get all the words of the text
		String[] cryptogramArray = cryptogram.split("\\s+");
		
		
		Set<String> cryptogramSet = new HashSet<String>();
		Set<String> decryptedSet = new HashSet<String>();
		int nbWordDecrypted = 0;
		
		//remove all the duplicate words and the words that are too short to guess a good alphabet
		for(var word: cryptogramArray) {
			if(word.length() >= 3) {
				cryptogramSet.add(word);
			}	
		}
		
		//sort the words by length
		List<String> wordsNotDecrypted = cryptogramSet.stream()
        	.sorted(Comparator.comparingInt(String::length).reversed())
        	.collect(Collectors.toList());
		
		//list that is going to be use to calculate the score of an alphabet
		List<String> allWordsList = new ArrayList<String>(wordsNotDecrypted);
		
		String newAlphabet = alphabet;
		String tempAlphabet = alphabet;
		String actualWordDecrypted = "";
		int count = 0;
		
		do{
			int temp = 0;
			
			for(var word : allWordsList) {
				actualWordDecrypted = applySubstitution(word, tempAlphabet);
				
				if(tree.containsWord(actualWordDecrypted.toLowerCase())){
					temp++;
					decryptedSet.add(word);
				}
			}
			
			//create a new list with only non decrypted words
			List<String> finalTest = new ArrayList<String>();
			
			for(var word : wordsNotDecrypted) {
				if(!decryptedSet.contains(word)) {
					finalTest.add(word);
				}
			}
			
			wordsNotDecrypted = new ArrayList<String>(finalTest);
			
			//update the alphabet if the score was better with the temp alphanet
			if(temp > nbWordDecrypted) {
				nbWordDecrypted = temp;
				newAlphabet = tempAlphabet;
				count = 0;
				temp = 0;
			}
			else {
				count ++;
			}
			
			// in case of we dont have more words to test the decrypt
			if(wordsNotDecrypted.size() == 0) break;
			
			String similarWord = "";
			
			do { 
				similarWord = searchSimilarWord(wordsNotDecrypted.get(0));
				
				//if we dont find a similar word we remove the previous word and retry with another ones
				if(similarWord.isBlank()) {
					wordsNotDecrypted.remove(0);
				}
				
				
			}while(similarWord.equals(""));
			
			tempAlphabet = generateSubstitutionAlphabet(newAlphabet, wordsNotDecrypted.get(0), similarWord.toUpperCase());
			
		}while(count < 7);
		
		return newAlphabet;
		
	}

	/**
	 * Applies an alphabet-specified substitution to a text.
	 * @param text A text
	 * @param alphabet A substitution alphabet
	 * @return The substituted text
	 */
	public static String applySubstitution(String text, String alphabet) {
		
		String textSubstitued = "";
		
		
		for(var letter : text.toCharArray()) {
			if(Character.isLetter(letter)) {
				textSubstitued += alphabet.charAt(LETTERS.indexOf(letter));
			}
			else textSubstitued += letter;
		}
		
		return textSubstitued; 
	}
	
	/*
	 * PRIVATE METHODS
	 */
	
	//public for the tests
	
	/**
	 * Modify the alphabet based on the crypted and decrypted word
	 * @param alphabet the current alphabet
	 * @param encryptedWord the encrypted word
	 * @return a new alphabet
	 */
	public static String generateSubstitutionAlphabet(String alphabet, String encryptedWord, String decryptedWord) {
		char tempChar;
		char[] alphabetArray = alphabet.toUpperCase().toCharArray();
		String tempAlphabet = alphabet;
		
		for(int i = 0; i < decryptedWord.length(); i++){
			char decryptedLetter = decryptedWord.charAt(i); // ex = c 
			char encryptedLetter = encryptedWord.charAt(i); // ex = s		
			
			//save the char that is at the index that is going to be replaced
			tempChar = alphabetArray[encryptedLetter - 65]; 
			
			//if the char at the index is already the good one we dont more
			if(tempChar != decryptedLetter) { 
				
				alphabetArray[encryptedLetter - 65] = decryptedLetter; 
			
				alphabetArray[tempAlphabet.indexOf(decryptedLetter)] = tempChar;
				tempAlphabet = new String(alphabetArray);
			}
			
		}
		return new String(alphabetArray);
	}
	
	/**
	 * search a word that can be the encrypted one in the dictionary
	 * @param encryptedWord the encrypted word
	 * @return the possible decrypted word
	 */
	public static String searchSimilarWord(String encryptedWord) {
		List<String> words = tree.getWordsOfLength(encryptedWord.length());
		
		Map<Character, Integer> charsOccurencesEncrypWord = searchRepetedLetter(encryptedWord);
		
		for(var word : words) {
			word.toUpperCase();
			Map<Character, Integer> charsOccurencesTreeWord = searchRepetedLetter(word);
			
			if(sameIndexOfMultipleOccurence(encryptedWord, word, charsOccurencesEncrypWord, charsOccurencesTreeWord)) {
				return word.toUpperCase();
			}
		}
		
		//in case of there is no similard word
		return "";
	}
	
//	private static boolean sameIndexOfMultipleOccurence(String encryptedWord, String treeWord, Map<Character, Integer> charsOccurencesEncryptWord, Map<Character, Integer> charsOccurencesTreeWord) {
//		char letterEncryptWord = charsOccurencesEncryptWord.keySet().iterator().next();
//		char letterTreeWord = charsOccurencesTreeWord.keySet().iterator().next();
//		int occurence = charsOccurencesTreeWord.values().iterator().next();
//		//Iterator<Integer> occurence = charsOccurencesTreeWord.values().iterator();
//		
//		Iterator<Character> it = charsOccurencesTreeWord.keySet().iterator();
//		
//		/*for(var letterEncryptedWord : charsOccurencesEncryptWord.keySet()) {
//			if(!it.hasNext() ||!occurence.hasNext()) return false;
//			if(!letterAtSameIndex(encryptedWord, treeWord, letterEncryptedWord, it.next(), occurence.next())) return false;
//		}
//		if(it.hasNext()) return false;
//		return true;*:
//		//System.out.println("lettre : " + letterEncryptWord + letterTreeWord);*/
//		
//		return letterAtSameIndex(encryptedWord, treeWord, letterEncryptWord, letterTreeWord, occurence);
//	}
	
	/**
	 * check if 2 words have the same occurences of a letter at the same index
	 * @param encryptedWord the encrypted word
	 * @param treeWord the word of the dictionnary 
	 * @param charsOccurencesEncryptWord map that contains a letter and is number of occurence in the encrypted word
	 * @param charsOccurencesTreeWord map that contains a letter and is number of occurence in the tree word
	 * @return
	 */
	private static boolean sameIndexOfMultipleOccurence(String encryptedWord, String treeWord, Map<Character, Integer> charsOccurencesEncryptWord, Map<Character, Integer> charsOccurencesTreeWord) {
		Iterator<Integer> occurence = charsOccurencesTreeWord.values().iterator();
		Iterator<Character> it = charsOccurencesTreeWord.keySet().iterator();
		Iterator<Integer> occurence2 = charsOccurencesEncryptWord.values().iterator();
		
		for(var letterEncryptedWord : charsOccurencesEncryptWord.keySet()) {
			if(!it.hasNext() || !occurence.hasNext()) return false;
			int oc1 = occurence.next();
			int oc2 = occurence2.next();
			
			if(oc1 != oc2) return false;
			if(!letterAtSameIndex(encryptedWord, treeWord, letterEncryptedWord, it.next(), oc1)) return false;
		}
		if(it.hasNext() || occurence.hasNext()) return false;
		
		return true;
	}
	
	
	/**
	 * check if the index of each occurence is the same for each word's letter
	 * @param encryptedWord the encrypted word
	 * @param treeWord the the word of the dictionnary 
	 * @param letterEncrypWord the letter of thhe encrypted word
	 * @param letterTreeWord the letter of the tree word
	 * @param nbOccurence the nb of occurence of both letters
	 * @return true if each index is the same, false otherwise
	 */
	private static boolean letterAtSameIndex(String encryptedWord, String treeWord, char letterEncrypWord, char letterTreeWord, int nbOccurence) {
		int i = 0;
		
		do {
			if(treeWord.indexOf(letterTreeWord) != encryptedWord.indexOf(letterEncrypWord))return false;
			
			treeWord = treeWord.substring(treeWord.indexOf(letterTreeWord) + 1);
			encryptedWord = encryptedWord.substring(encryptedWord.indexOf(letterEncrypWord) + 1);
			
			i++;
		} while ( i < nbOccurence);
		
		
		return true;
	}
	
	
	/**
	 * create a map with the repeted letter of a word and the number of occurence
	 * @param word the word to iterate through
	 * @return a map with each character that is there at least 2 times and its number of occurence
	 */
	private static LinkedHashMap<Character, Integer> searchRepetedLetter(String word) {
		LinkedHashMap<Character, Integer> letters = new LinkedHashMap<>();  
		
	    for (int i = 0; i < word.length(); i++) {
	        char lettre = word.charAt(i);
	        int nbOccurences = word.length() - word.replace(String.valueOf(lettre), "").length();
	        
	        if (nbOccurences >= 2) {
	            letters.put(lettre, nbOccurences);
	        }
	    }
	    return letters;
	}
	
	/**
	 * Compares two substitution alphabets.
	 * @param a First substitution alphabet
	 * @param b Second substitution alphabet
	 * @return A string where differing positions are indicated with an 'x'
	 */
	private static String compareAlphabets(String a, String b) {
		String result = "";
		for (int i = 0; i < a.length(); i++) {
			result += (a.charAt(i) == b.charAt(i)) ? " " : "x";
		}
		return result;
	}
	
	/**
	 * Load the text file pointed to by pathname into a String.
	 * @param pathname A path to text file.
	 * @param encoding Character set used by the text file.
	 * @return A String containing the text in the file.
	 * @throws IOException
	 */
	private static String readFile(String pathname, Charset encoding) {
		String data = "";
		try {
			data = Files.readString(Paths.get(pathname), encoding);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}
	
	
    /*
	 * MAIN PROGRAM
	 */
	
	public static void main(String[] args) {
		/*
		 * Load dictionary
		 */
		System.out.print("Loading dictionary... ");
		LexicographicTree dict = new LexicographicTree(DICTIONARY);
		System.out.println("done.");
		System.out.println();
		
		/*
		 * Load cryptogram
		 */
		String cryptogram = readFile(CRYPTOGRAM_FILE, StandardCharsets.UTF_8);
//		System.out.println("*** CRYPTOGRAM ***\n" + cryptogram.substring(0, 100));
//		System.out.println();

		/*
		 *  Decode cryptogram
		 */
		DictionaryBasedAnalysis dba = new DictionaryBasedAnalysis(cryptogram, dict);
		String startAlphabet = LETTERS;
		//String startAlphabet = "ZISHNFOBMAVQLPEUGWXTDYRJKC"; // Random alphabet
		String finalAlphabet = dba.guessApproximatedAlphabet(startAlphabet);
		
		// Display final results
		System.out.println();
		System.out.println("Decoding     alphabet : " + DECODING_ALPHABET);
		System.out.println("Approximated alphabet : " + finalAlphabet);
		System.out.println("Remaining differences : " + compareAlphabets(DECODING_ALPHABET, finalAlphabet));
		
		System.out.println();
		
		// Display decoded text
		//YESUMZRWFNVHOBJTGPCDLAIXQK
		System.out.println("*** DECODED TEXT ***\n" + applySubstitution(cryptogram, DECODING_ALPHABET).substring(0, 200));
		System.out.println();
	}
}

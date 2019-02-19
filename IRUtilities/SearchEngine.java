package IRUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.htmlparser.util.ParserException;


public class SearchEngine {
	public static int N;
	public static Set<String> marked = new HashSet<>();
	public static java.util.HashSet<String> stopWords;
	private static Porter porter = new Porter();
	
	public SearchEngine(){
		
	}
	
	public String stem(String str)
	{
		return porter.stripAffixes(str);
	}
	
	public boolean isStopWord(String str)
	{
		return stopWords.contains(str);
	}
	
	public Vector<String> extractWords(String query) throws ParserException{
		SearchEngine searchEngine = new SearchEngine();
		StringTokenizer st = new StringTokenizer(query);
		stopWords = new java.util.HashSet<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader("stopwords.txt"));
			String line;
			while ((line = reader.readLine()) != null) {
				stopWords.add(line);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String input="";
		Vector<String> result = new Vector<String>();
		while (st.hasMoreTokens()) {
			input = st.nextToken();
			//check whether words is stopword, no->add to result
			 if (!searchEngine.isStopWord((input.toLowerCase()))) {
				 boolean letter = true;
				 for (int i=0;i<input.length();i++)
					 if (!(Character.isLetter(input.charAt(i))))
						 letter = false;
				 if (letter)
					 result.add(searchEngine.stem(input));
			 }
		 }
		return result;
	}
	
	public static void searchTitle(String query) {
		PrintStream out = null;
		try {
			out = new PrintStream(new FileOutputStream("query_result.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.setOut(out);
		InvertedIndex titleIndex = null;
		try
        {
        	titleIndex = new InvertedIndex("TI","ht1");
        }
        catch(IOException ex)
        {
        	System.err.println(ex.toString());
        }
		try {
			titleIndex.printTitle(query);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void searchContent(String query) {
		SearchEngine searchEngine = new SearchEngine();
		InvertedIndex contentIndex = null;
		try
        {
        	contentIndex = new InvertedIndex("RM","ht1");
        }
        catch(IOException ex)
        {
        	System.err.println(ex.toString());
        }
		try {
			Vector<String> stemQuery = searchEngine.extractWords(query);
			contentIndex.printContent(stemQuery,N);
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main (String[] args) {
		File inputFile = new File("number of documents.txt");
		try {
			@SuppressWarnings("resource")
			Scanner sc = new Scanner(inputFile);
			N = Integer.parseInt(sc.next());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		@SuppressWarnings("resource")
		String query= args[0];
		searchTitle(query.toLowerCase());
		searchContent(query.toLowerCase());
	}
}

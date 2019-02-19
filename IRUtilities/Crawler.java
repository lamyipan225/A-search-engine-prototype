/* --
COMP4321 Lab2 Exercise
Student Name:
Student ID:
Section:
Email:
*/
package IRUtilities;

import java.util.Vector;

import org.htmlparser.beans.StringBean;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.beans.LinkBean;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.StringTokenizer;



import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileReader;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class Crawler
{
	private static long size=0;
	private Porter porter = new Porter();
	static Queue<String> q = new LinkedList<>();
	static int iteration = 1;
    
    //URLs already visited
    static Set<String> marked = new HashSet<>();
    
    //Start from here
    static String root = "";


	public static java.util.HashSet<String> stopWords;
	private String url;
	Crawler(String _url)
	{
		url = _url;
	}


	public boolean isStopWord(String str)
	{
		return stopWords.contains(str);
	}

	public String stem(String str)
{
	return porter.stripAffixes(str);
}


	public Vector<String> extractWords() throws ParserException

	{

		// extract words in url and return them
		// use StringTokenizer to tokenize the result from StringBean
		// ADD YOUR CODES HERE
		Vector<String> result = new Vector<String>();
		StringBean bean = new StringBean();
		bean.setURL(url);
		bean.setLinks(false);
		String input = "";
		String contents = bean.getStrings();
		StringTokenizer st = new StringTokenizer(contents);
		size = 0;
		while (st.hasMoreTokens()) {
			input = st.nextToken();
			size+=input.length();
			//check whether words is stopword, no->add to result
			 if (!isStopWord(input.toLowerCase())) {
				 boolean letter = true;
				 for (int i=0;i<input.length();i++)
					 if (!(Character.isLetter(input.charAt(i))))
						 letter = false;
				 if (letter)
					 result.add(stem(input));
			 }
		 }
		return result;
	}
	public Vector<String> extractLinks() throws ParserException
	{
		// extract links in url and return them
		// ADD YOUR CODES HERE
		Vector<String> result = new Vector<String>();
		LinkBean bean = new LinkBean();
		bean.setURL(url);
		URL[] urls = bean.getLinks();
		for (URL s : urls) {
		    result.add(s.toString());
		}
		return result;
	}

	public Vector<String> extractTitle() throws ParserException,IOException{
		Vector<String> result = new Vector<String>();
		//Get the title tag from html
		Parser parser = new Parser();
		parser.setResource(url);
		NodeList nodes = parser.extractAllNodesThatMatch(new TagNameFilter("title"));
		for (Node node : nodes.toNodeArray()) {
			TitleTag title = (TitleTag) node;
			result.add(title.toPlainTextString());
		}

		return result;
	}

	public static void bfs() throws IOException{
		try
		{
			q.add(root);
	        while(!q.isEmpty()){ 
	            String s = q.poll();
	            
	            if(!marked.contains(s))
					//bfs
					marked.add(s);
	            if(iteration>5000)return;
	            boolean ok = false;
	            URL url = null;
	            while(!ok){ 
	                try{
	                    url = new URL(s);
	                    String link = new String(url.toString());
	    	            Crawler crawler = new Crawler(url.toString());
	                    Vector<String> title = crawler.extractTitle();
	    	            for(String s1 : title) {
	    	            	System.out.println("Page Title: " + s1.trim());
	    	            }
	    	            
	    	            try
	    	            {
	    	            	InvertedIndex titleIndex = new InvertedIndex("TI","ht1");
	    	            	for(String s1 : title) {
	    	            		titleIndex.addEntry(s1,link,1);
	    	            	}
	    	            	titleIndex.finalize();
	    	            }
	    	            catch(IOException ex)
	    	            {
	    	            	System.err.println(ex.toString());
	    	            }

	    	            //Print URL
	    	            System.out.println("URL: " + url.toString());

	    	            //Extract Last Modified Date
	    	            String date="";
	    	            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
	    	            long date1 = httpCon.getLastModified();
	    	            if (date1 == 0) {
	    	            	for (int i = 0;; i++) {
	    	            		String headerName = httpCon.getHeaderFieldKey(i);
	    	            		String headerValue = httpCon.getHeaderField(i);
	    	            		if (headerName == null && headerValue == null)
	    	            			break;
	    	            		if (headerName!=null)
	    	            			if (headerName.equals("Date")){
	    	            				date = headerValue;
	    	            			}
	    	            	}
	    	            	System.out.println("Last Modification Date: " +date);
	    	            }
	    	            else System.out.println("Last Modification Date: " + new Date(date1));
	    		    
	    	            //Extract Words
	    	            Vector<String> words = crawler.extractWords();
	    	            //Print Size of page
	    	            if (httpCon.getContentLength()==-1)
	    	            	System.out.println("Size of Page: " + size + " characters\n");
	    	            else System.out.println("Size of Page: " + httpCon.getContentLength() + "\n");
	    	            httpCon.disconnect();
	    	            //Print Words
	    	            System.out.println("Words in "+crawler.url+":");
	    	            try
	    	            {
	    	            	InvertedIndex index = new InvertedIndex("RM","ht1");
	    	            	for (int i=0;i<words.size();i++)
	    	            		index.addEntry(words.get(i),link,1);
	    	            	index.printAll(link);
	    	            	index.finalize();
	    	            }
	    	            catch(IOException ex)
	    	            {
	    	            	System.err.println(ex.toString());
	    	            }
	    	            //Extract Links
	    	            Vector<String> links = crawler.extractLinks();
	    	            System.out.println("Links in "+crawler.url+":");
	    	            for(int i = 0; i < links.size(); i++) {
	    	            	System.out.println("Child Link" + (i+1) + ": " + links.get(i));
	    	            }
	    	            System.out.println("\nIteration: " + (iteration));

	    	            System.out.println("-------------------------------------------------------------------------------------------");

	    	            for(int i = 0; i < links.size(); i++){
	    					String tempLink = new String();
	    					if(links.get(i).lastIndexOf("#") == links.get(i).length()-1) {
	    						tempLink = links.get(i).substring(0, links.get(i).length() - 1);
	    						//System.out.println(" # Deleted");
	    						if(tempLink.lastIndexOf("/") == tempLink.length()-1) {
	    							tempLink = tempLink.substring(0, tempLink.length() - 1);
	    							//System.out.println(" / Deleted");
	    						}
	    					}else if(links.get(i).lastIndexOf("/") == links.get(i).length()-1) {
	    						tempLink = links.get(i).substring(0, links.get(i).length() - 1);
	    						//System.out.println(" / Deleted");
	    					}else {
	    						tempLink = links.get(i);
	    					}
	    					if(!marked.contains(tempLink)) {
	    						//bfs
	    						marked.add(tempLink);
	    		                q.add(tempLink);
	    						//extractPage(queue);
	    					}
	    				} 
	                    iteration++;
	                    ok = true;
	                }catch(MalformedURLException e){
	                    //Get next URL from queue
	                    s = q.poll();
	                    ok = false;
	                }catch(IOException e){
	                    //Get next URL from queue
	                    s = q.poll();
	                    ok = false;
	                } catch (ParserException e) {
						// TODO Auto-generated catch block
	                	s = q.poll();
	                    ok = false;
					}
	            }         
	            //Extract Title
	        }
		}finally {
			PrintStream out = new PrintStream(new FileOutputStream("number of documents.txt"));
			System.setOut(out);
			System.out.print(iteration-1);
			}
	}

	public static void main (String[] args)
	{
		System.out.println("Indexing...");
		try {
			InvertedIndex index = new InvertedIndex("RM","ht1");
			index.delEntry();
			index.finalize();
		}
		catch(IOException ex)
        {
            System.err.println(ex.toString());
        }
		try {
			InvertedIndex titleIndex = new InvertedIndex("TI","ht1");
			titleIndex.delEntry();
			titleIndex.finalize();
		}
		catch(IOException ex)
        {
            System.err.println(ex.toString());
        }
		
		//stopwords list preparation
		try {
			stopWords = new HashSet<String>();
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


			//String link = new String("http://www.cs.ust.hk/~dlee/4321/") ;
			//String link = new String("https://www.opentext.com") ;
			root = "http://www.cse.ust.hk";
			PrintStream out = new PrintStream(new FileOutputStream("spider_result.txt"));
			System.setOut(out);
			bfs();
		}catch(FileNotFoundException e){
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		finally {
		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
		System.out.println("End of Indexing.Please check the result in spider_result.txt");
		}
	}

}
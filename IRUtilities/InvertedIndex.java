
package IRUtilities;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Vector;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import java.io.IOException;

public class InvertedIndex
{
    private RecordManager recman;
    private HTree hashtable;

    InvertedIndex(String recordmanager, String objectname) throws IOException
    {
        recman = RecordManagerFactory.createRecordManager(recordmanager);
        long recid = recman.getNamedObject(objectname);
            
        if (recid != 0)
            hashtable = HTree.load(recman, recid);
        else
        {
            hashtable = HTree.createInstance(recman);
            recman.setNamedObject( objectname, hashtable.getRecid() );
        }
    }


    public void finalize() throws IOException
    {
        recman.commit();
        recman.close();                
    } 

    public void addEntry(String word, String url, int y) throws IOException
    {
        // Add a "docX Y" entry for the key "word" into hashtable
        // ADD YOUR CODES HERE
    	String content = (String)hashtable.get(word);
        if (content == null) {
        	content = "1 ";
            content +=  url + " " + y + " ";
        } else {
            if (content.indexOf(url+" ")==-1) {
            	int dfPos = content.indexOf(" ");
            	int df = Integer.parseInt(content.substring(0,dfPos));
            	df++;
            	content = df + content.substring(dfPos,content.length());
            	content +=  url + " " + y + " ";
            }
            else {
            	int left = content.indexOf(url+" ")+url.length();            
            	int right = content.indexOf(" ",left+1);
            	int freq = Integer.parseInt(content.substring(left,right).trim());
            	freq++;
            	content = content.substring(0,left) + " " + freq + content.substring(right,content.length());
            }
        }
        hashtable.put(word, content);
    }
    public void delEntry() throws IOException
    {
        // Delete the word and its list from the hashtable
        // ADD YOUR CODES HERE
    	Vector<String> temp = new Vector<String>();;
    	FastIterator iter = hashtable.keys();
    	String key;
    	while( (key=(String)iter.next()) != null ) {
            temp.add(key);
        }
    	for (int i=0;i<temp.size();i++)
    		hashtable.remove(temp.get(i));
    } 
    public void printAll(String url) throws IOException
    {
        // Print all the data in the hashtable
        // ADD YOUR CODES HERE
        FastIterator iter = hashtable.keys();
        String key;
        int counter = 0;
        while( (key=(String)iter.next()) != null ) {
        	String value = (String)hashtable.get(key);
        	if (!(value.indexOf(url+" ")==-1)) {
            	int left = value.indexOf(url+" ")+url.length();            
            	int right = value.indexOf(" ",left+1);
            	int freq = Integer.parseInt(value.substring(left,right).trim());
            	System.out.print(key+ " " + freq + "; ");
            	if(counter%8 == 0 && counter != 0) {
					System.out.println("");
				}
            	counter++;
            }
        }
        System.out.println("");
        
    }    
    public void printTitle(String query) throws IOException {
    	FastIterator iter = hashtable.keys();
    	ArrayList<Document> ranking = new ArrayList<Document>();
    	String key;
    	while( (key=(String)iter.next()) != null ) {
            if (key.toLowerCase().contains(query)) {
            	String value = (String)hashtable.get(key);
            	String[] seqTokens = value.split(" ");
            	int df = Integer.parseInt(seqTokens[0]);
            	for (int i=0;i<df;i++) {
            		if(!SearchEngine.marked.contains(seqTokens[i+1])) {
            			SearchEngine.marked.add(seqTokens[i+1]);
            			System.out.println(key);
            			System.out.println(seqTokens[i+1]);
            		}
            	}
            }
        }
    }
    
    public void printContent(Vector<String> query,int N) throws IOException {
    	ArrayList<Document> ranking = new ArrayList<Document>();
		for (String s:query) {
	    	String content = (String)hashtable.get(s);
	    	if (content == null) 
	    		continue;
	    	else {
	    		String[] seqTokens = content.split(" ");
	    		int df = Integer.parseInt(seqTokens[0]);
	    		double idf = log((double)N/(double)df,2);
	    		int tfmax = Integer.parseInt(seqTokens[2]);
	    		for (int i=0;i<df;i++)
	    			if (Integer.parseInt(seqTokens[i*2+2])>tfmax)
	    				tfmax = Integer.parseInt(seqTokens[i*2+2]);
	    		for (int i=0;i<df;i++) {
	    			int tf = Integer.parseInt(seqTokens[i*2+2]);
	    			double partialScore = (double)tf/(double)tfmax*idf;
	    			boolean exist = false;
	    			for (Document doc:ranking) {
	    				if (doc.url.equals(seqTokens[i*2+1])) {
	    					doc.score=doc.score+partialScore;
		    				exist = true;
		    				break;
	    				}
	    			}
	    			if (!exist) {
	    				Document newDoc = new Document(seqTokens[i*2+1]);
	    				newDoc.score=partialScore;
	    				ranking.add(newDoc);
	    			}
	    		}
	    	}
		}
		Comparator<Document> c = new Comparator<Document>() {  
            @Override  
            public int compare(Document d1, Document d2) {  
                if(d1.score<d2.score)  
                    return 1;   
                else return -1;  
            }  
        };        
		ranking.sort(c);
		for (Document doc:ranking) {
			if (!SearchEngine.marked.contains(doc.url)) {
				Vector<String> result = new Vector<String>();
				//Get the title tag from html
				Parser parser = new Parser();
				try {
					parser.setResource(doc.url);
					NodeList nodes = parser.extractAllNodesThatMatch(new TagNameFilter("title"));
					for (Node node : nodes.toNodeArray()) {
						TitleTag title = (TitleTag) node;
						result.add(title.toPlainTextString());
					}
				} catch (ParserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for(String s1 : result) {
	            	System.out.println(s1.trim());
	            }
				System.out.println(doc.url);
				SearchEngine.marked.add(doc.url);
			}
		}
    }
    
    static double log(double x, double base)
    {
        return (Math.log(x)/Math.log(base));
    }
}
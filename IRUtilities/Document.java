package IRUtilities;

public class Document implements Comparable<Document>{

	double score = 0;
	String url;
	
	public Document(String url) {
		this.url = url;
	}

	@Override
	public int compareTo(Document doc) {
		// TODO Auto-generated method stub
		if (this.score>(doc.score))
			return -1;
		else if (this.score<doc.score)
			return 1;
		else return 0;
	}
	
	public boolean equals(Document doc) {
		if (this.url.equals(doc.url))
			return true;
		return false;
	}
}

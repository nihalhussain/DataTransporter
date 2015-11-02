package Util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import data.transporter.core.Resources;
import edu.northwestern.at.utils.corpuslinguistics.stemmer.LancasterStemmer;

public class TFIDF {
	
	public LinkedHashMap<String, Integer> termFrequencyCounter (String content)
	{
		String[] terms = content.split(" ");
		LinkedHashMap<String, Integer> termFrequency = new LinkedHashMap<String, Integer>();
		
		//PorterStemmer s1 = new PorterStemmer();
		LancasterStemmer s = new LancasterStemmer();
		
		for(String term:terms)
		{
			String stemmedTerm = s.stem(term);
			//System.out.println(term + " : " + stemmedTerm);
			int count =0;
			if(termFrequency.containsKey(stemmedTerm))
			{
				count = termFrequency.get(stemmedTerm);
			}
			count++;
			termFrequency.put(stemmedTerm, count);
		}
		return termFrequency;
	}
	
	public void run()
	{
		Resources r = new Resources();
		LinkedHashMap<Integer, LinkedHashMap<Integer, String>> rows = new LinkedHashMap<Integer, LinkedHashMap<Integer,String>>();
		try {
			Class.forName(r.getDatabaseDriver());

			String connectionString = r.getDatabaseConnectionString();
			String userName = r.getUsername();
			String password = r.getPassword();
			Connection conn = DriverManager.getConnection(connectionString, userName, password); //r.getDatabaseConnectionString(), r.getUsername(), r.getPassword());
			Statement st = conn.createStatement();
			String query = "select post,blogpost_id,blogsite_id from blogtrackers.blogposts";
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void databaseUpdate(String content,int post_id,int blog_id)
	{
		TFIDF fvb = new TFIDF();
		content = content.toLowerCase();
		content = fvb.cleanContent(content);
		content = fvb.removeStopwords(content);
		LinkedHashMap<String, Integer> termFrequency = fvb.termFrequencyCounter(content);
		Resources r = new Resources();
		try {
			Class.forName(r.getDatabaseDriver());

			String connectionString = r.getDatabaseConnectionString();
			String userName = r.getUsername();
			String password = r.getPassword();
			Connection conn = DriverManager.getConnection(connectionString, userName, password); //r.getDatabaseConnectionString(), r.getUsername(), r.getPassword());
			Statement st = conn.createStatement();
			Set<String> terms = termFrequency.keySet();
			Iterator<String> i = terms.iterator();
			while(i.hasNext())
			{
				String term = i.next();
				
				String query = "select count(blogpost_id) from blogtrackers.tfidf where term = \'" + term + "\'";
				ResultSet rs = st.executeQuery(query);
				rs.next();
				int tFrequency = termFrequency.get(term);
				int documentFrequency = rs.getInt(1);
				documentFrequency++;
				query = "insert into blogtrackers.tfidf(blogpost_id,blogsite_id,term,term_frequency,document_frequency) values(" + post_id + "," + blog_id + ",\'" + term + "\'," 
						+ tFrequency + "," + documentFrequency + ")";
				st.executeUpdate(query);
				query = "update blogtrackers.tfidf set document_frequency = " + documentFrequency + " where term = \'" + term + "\'";
				st.executeUpdate(query);
			}
			conn.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public String cleanContent(String content)
	{
		char[] unwantedCharacters = {'\b','\n','\n','\f','\t','\r','\'','0','1','2','3','4','5','6','7','8','9',
				'!','@','#','$','%','^','&','*','(',')','_','-','"','+','=',',','.','/',':',';','[',']','{','}',
				'<','>','?','|','~','‚','–','”','„','«','‡','•','`','©','¤','£','¾','\\','"','\'','’','“','‘','’',
				'—','™','…'};
		for(char c: unwantedCharacters)
		{
			content = content.replace(c, ' ');
		}
		while(content.contains("  "))
		{
			content = content.replace("  ", " ");
		}
		return content;
	}
	public String removeStopwords(String content)
	{
		BufferedReader br = null;
		String line = "";
		String words = "";
		try
		{
			br = new BufferedReader(new FileReader("stopwords.txt"));
			while ((line = br.readLine()) != null) 
			{
				words = words + "," + line;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		words.toLowerCase();
		String[] stopwords = words.split(",");
		for(String stopword:stopwords)
		{
			stopword = " " + stopword + " ";
			int length = stopword.length();
			while(content.contains(stopword) && length > 0)
			{
				content = content.replace(stopword, " ");
			}
		}
		while(content.contains("  "))
		{
			content = content.replace("  ", " ");
		}
		return content;
	}
}

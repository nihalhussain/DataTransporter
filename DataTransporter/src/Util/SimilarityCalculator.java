package Util;

import java.math.MathContext;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import data.transporter.core.Resources;

public class SimilarityCalculator {
	SimilarityCalculator sc;

	public void run(boolean bloglevel)
	{
		Resources r = new Resources();
		sc = new SimilarityCalculator();
		try
		{
			Class.forName(r.getDatabaseDriver());
			String connectionString = r.getDatabaseConnectionString();
			String userName = r.getUsername();
			String password = r.getPassword();
			Connection conn = DriverManager.getConnection(connectionString, userName, password); //r.getDatabaseConnectionString(), r.getUsername(), r.getPassword());
			Statement st = conn.createStatement();
			ResultSet rs;
			if(bloglevel)
			{

			}
			else
			{
				String query = "select distinct blogpost_id from blogtrackers.blogposts";
				int[] posts = new int[100000];
				int count = 0;
				rs = st.executeQuery(query);
				while(rs.next())
				{
					int post_id = rs.getInt(1);
					posts[count] = post_id;
					count++;
				}
				for(int i = 0; i < (posts.length - 1); i++)
				{
					for(int j = i + 1; j < posts.length; j++)
					{
						if(posts[j]!= 0)
						{
						double cosine = sc.postSimilarity(posts[i], posts[j]);
						if(Double.isNaN(cosine))
						{
							cosine = 0;
						}
						double similarity = cosine * 100000; //Math.acos(cosine);
						st.execute("insert into blogtrackers.posts_similarity(post_id1,post_id2,cosine) values(" + posts[i] + "," + 
									posts[j] + "," + similarity + ")");
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public double postSimilarity(int pid1,int pid2)
	{
		sc = new SimilarityCalculator();
		LinkedHashMap<String, Double> featureVector1 = sc.postFeatureVector(pid1);
		LinkedHashMap<String, Double> featureVector2 = sc.postFeatureVector(pid2);
		double productResult = 0;

		Set<String> set1 = featureVector1.keySet();
		Iterator<String> i = set1.iterator();
		while(i.hasNext())
		{
			String term = i.next();
			double tfidf = featureVector1.get(term);
			double tfidf2 = 0;
			if(featureVector2.containsKey(term))
			{
				tfidf2 = featureVector2.get(term);
			}
			tfidf = tfidf * tfidf2;
			productResult = productResult + tfidf;
			/*
			if(featureVector1.containsKey(term) && featureVector2.containsKey(term))
			{
				productResult = productResult + 1;
			}
			*/
		}
		double length1 = sc.vectorLength(featureVector1);
		double length2 = sc.vectorLength(featureVector2);
		double cosine = productResult/(length1 * length2);

		System.out.println(cosine);
		return cosine;
	}

	public double vectorLength(LinkedHashMap<String, Double> featureVector)
	{
		double length = 0;
		Set<String> s = featureVector.keySet();
		Iterator<String> i = s.iterator();
		while(i.hasNext())
		{
			double value = featureVector.get(i.next());
			length = length + value * value;
		}
		return length;
	}

	public LinkedHashMap<String, Double> postFeatureVector (int id)
	{
		LinkedHashMap<String, Double> featureVector = new LinkedHashMap<String,Double>();
		Resources r = new Resources();

		try
		{
			Class.forName(r.getDatabaseDriver());
			String connectionString = r.getDatabaseConnectionString();
			String userName = r.getUsername();
			String password = r.getPassword();
			Connection conn = DriverManager.getConnection(connectionString, userName, password); //r.getDatabaseConnectionString(), r.getUsername(), r.getPassword());
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("select count(*) from blogtrackers.blogposts");
			rs.next();
			int totalPosts = rs.getInt(1);

			String query = "SELECT term,term_frequency,document_frequency FROM blogtrackers.tfidf where blogpost_id = " + id;

			rs = st.executeQuery(query);
			while(rs.next())
			{
				String term = rs.getString(1);
				int termFrequency = rs.getInt(2);
				int documentFrequency = rs.getInt(3);
				double tfidf = termFrequency * (1+ Math.log10(totalPosts/documentFrequency));
				featureVector.put(term, tfidf);
			}
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return featureVector;
	}

}

package blogs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Vector;

import data.transporter.core.Resources;
import network.hyperlinks.HTMLLinkExtractor;
import network.hyperlinks.HtmlLink;

/**
 * @author Nihal
 * This class formats the data crawled from nobordersard.wordpress.com to a format that blogtrackers supports
 */

public class NoBordersard {

	Resources r;
	String query;
	String category;
	NoBordersard nb;
	String[] postpermalink = new String[1000];
	String[] blogname = new String[1000];
	String[] author = new String[1000];
	String[] date = new String[1000];
	String[] title = new String[1000];
	String[] content = new String[1000];
	String[] contentHtml = new String[1000];
	String[] comments = new String[1000];
	String[] commentsHtml = new String[1000];
	String[] commentsNum = new String[1000];
	String[] tags = new String[1000];
	String[] scope = new String[1000];
	String[] lastCrawled = new String[1000];

	public NoBordersard() {
		// TODO Auto-generated constructor stub
		
		this.query = "select * from ukraine_db.temp_nobordersard where POSTPERMALINK like '%//%/%/%/%/' and FLAG != 2";
		r = new Resources();
	}
	
	/**
	 * execution starts from this method. This method is called from main method.
	 */
	public String run() throws Exception
	{
		nb = new NoBordersard();
		String lastCrawl = "";

		Class.forName(r.getDatabaseDriver());
		String connectionString = r.getDatabaseConnectionString();
		String userName = r.getUsername();
		String password = r.getPassword();
		Connection conn = DriverManager.getConnection(connectionString, userName, password); //r.getDatabaseConnectionString(), r.getUsername(), r.getPassword());
		Statement st = conn.createStatement();

		ResultSet rs = st.executeQuery(query);
		int count = 0;
		while(rs.next())
		{
			postpermalink[count] = rs.getString(1);
			blogname[count] = rs.getString(2);
			author[count] = rs.getString(3);
			date[count] = rs.getString(4);
			title[count] = rs.getString(5);
			content[count] = rs.getString(6);
			contentHtml[count] = rs.getString(7);
			comments[count] = rs.getString(8);
			commentsHtml[count] = rs.getString(9);
			commentsNum[count] = rs.getString(10);
			tags[count] = rs.getString(11);
			scope[count] = rs.getString(12);
			lastCrawled[count] = rs.getString(14);

			count++;
		}

		LinkedList<String> listOfPostpermalinks = new LinkedList<String>();

		for(int i=0;i<count;i++)
		{
			Boolean insert = true;
			//System.out.println(postpermalink[i]);

			if(listOfPostpermalinks.contains(postpermalink[i]))
			{
				insert = false;
			}
			if(insert)
			{
				String temp = contentHtml[i];
				LinkedList<String> outLinks = nb.linkExtractor(temp);
				int outLinksNum = outLinks.size();

				String date1 = nb.dateConverter(date[i]);

				String contents = content[i];
				if(contents != null)
				{
					contents = contents.replace("\"", "");
					contents = contents.replace("\'", "");
				}
				String titles = title[i];
				if(titles != null)
				{
					titles = titles.replace("\"", "");
					titles = titles.replace("\'", "");
				}
				String authors = author[i];
				if(authors != null)
				{
					authors = authors.replace("\"", "");
					authors = authors.replace("\'", "");
				}
				authors = "NoBordersard";
				String tag = tags[i];
				if(tag != null)
				{
					tag = tag.replace("\"", "");
					tag = tag.replace("\'", "");
				}
				scope[i] = scope[i];

				String insertQuery = "insert into blogtrackers.blogposts (title,date,blogger,categories,post,"
						+ "post_length,num_outlinks,num_inlinks,num_comments,permalink,blogsite_id,tags) values"
						+ "(\""+titles+"\",\""+date1+"\",\""+authors+"\",\""+ category +"\",\""+contents+"\","
						+contents.length()+","+outLinksNum+",0,"+commentsNum[i]+",\""+
						postpermalink[i]+"\",49,\""+tag+"\")";
				
				/*
				 * need to change blogsite_id
				 */

				r.runQuery(insertQuery);
				//TFIDF fvb = new TFIDF();

				query = "UPDATE ukraine_db.temp_nobordersard set FLAG = 2 where POSTPERMALINK =\'" + postpermalink[i] 
						+ "\'";
				r.runQuery(query);

				String selectQuery = "select blogpost_id from blogtrackers.blogposts where permalink = '" + postpermalink[i] + "'";
				rs = st.executeQuery(selectQuery);				
				rs.next();

				int blogpost_id = rs.getInt(1);
				//fvb.databaseUpdate(contents,blogpost_id,1)
				

				for(String link:outLinks)
				{
					String outLinkInsert = "insert into blogtrackers.outlinks (link,blogpost_id) values(\"" + link + "\"," + blogpost_id + ")";
					//System.out.println(outLinkInsert);
					r.runQuery(outLinkInsert);
				}
				listOfPostpermalinks.add(postpermalink[i]);
			}
			lastCrawl = lastCrawled[i];
		}
		query = "UPDATE blogtrackers.blogsites SET last_crawled = \"" + lastCrawl + "\" where blogsite_id = 49";
		r.runQuery(query);
		conn.close();

		return "done";
	}

	/**
	 * this method uses HTMLLinkExtractor class to get all the links on the page
	 * This method identifies proper outlinks by removing links that tags or advertisements or social media links
	 * 
	 */
	public LinkedList<String> linkExtractor (String contentHtml)
	{
		LinkedList<String> outlinks = new LinkedList<String>();
		HTMLLinkExtractor he = new HTMLLinkExtractor();

		Vector<HtmlLink> links = he.grabHTMLLinks(contentHtml);

		Iterator<HtmlLink> it = links.iterator();
		while(it.hasNext())
		{
			HtmlLink obj = it.next();
			String linkText = obj.linkText;
			String link = obj.link;
			if(link.contains("http") && !(linkText.contains("<IMG ") || linkText.contains("<img ")))
			{
				outlinks.add(link);
			}
		}
		//System.out.println(count);

		return outlinks;
	}

	/**
	 * this method converts the date into yyyy-mm-dd format or yyyy-mm-dd hh:mm format
	 */
	public String dateConverter(String date)
	{
		LinkedHashMap<String, String> months = new LinkedHashMap<String, String>();
		months.put("gennaio", "01");
		months.put("febbraio", "02");
		months.put("marzo", "03");
		months.put("aprile", "04");
		months.put("maggio", "05");
		months.put("giugno", "06");
		months.put("luglio", "07");
		months.put("agosto", "08");
		months.put("settembre", "09");
		months.put("ottobre", "10");
		months.put("novembre", "11");
		months.put("dicembre", "12");

		LinkedHashMap<Integer, String> number = new LinkedHashMap<Integer, String>();
		number.put(1, "01");
		number.put(2, "02");
		number.put(3, "03");
		number.put(4, "04");
		number.put(5, "05");
		number.put(6, "06");
		number.put(7, "07");
		number.put(8, "08");
		number.put(9, "09");
		
		String convertedDate = null;

		int index = date.indexOf(" ");
		String d = date.substring(0, index);
		date = date.substring(index + 1);
		index = date.indexOf(" ");
		String month = date.substring(0, index);
		String year = date.substring(index + 1);
		String mon = months.get(month);
		if(number.containsKey(Integer.parseInt(d)))
		{
			d = number.get(Integer.parseInt(d));
		}
		
		convertedDate = year + '-' + mon +'-' + d;
		return convertedDate;
	}
}

package blogs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Vector;

import Util.TFIDF;
import data.transporter.core.Resources;
import network.hyperlinks.HTMLLinkExtractor;
import network.hyperlinks.HtmlLink;

/**
 * @author Nihal
 * This class formats the data crawled from afieldfviewsblog.wordpress.com to a format that blogtrackers supports
 */

public class AFieldOfViewsBlog {

	Resources r;
	String query;
	String category;
	AFieldOfViewsBlog afvb;
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

	public AFieldOfViewsBlog() {
		// TODO Auto-generated constructor stub

		this.query = "select * from ukraine_db.temp_afieldofviewblog where POSTCONTENT not like '' and FLAG != 2";
		r = new Resources();
		//afvb = new AFieldOfViewsBlog();
	}

	/**
	 * execution starts from this method. This method is called from main method.
	 */
	public String run() throws Exception
	{
		afvb = new AFieldOfViewsBlog();
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
				LinkedList<String> outLinks = afvb.linkExtractor(temp);
				int outLinksNum = outLinks.size();

				String date1 = afvb.dateConverter(date[i]);

				String contents = content[i];
				if(contents != null)
				{
					contents = contents.replace("\"", "");
					contents = contents.replace("\'", "");
					int index = contents.indexOf("About these ads");
					if(index != -1)
					{
						contents = contents.substring(0, index);
					}
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
						postpermalink[i]+"\",1,\""+tag+"\")";

				r.runQuery(insertQuery);
				TFIDF fvb = new TFIDF();

				query = "UPDATE ukraine_db.temp_afieldofviewblog set FLAG = 2 where POSTPERMALINK =\'" + postpermalink[i] 
						+ "\'";
				r.runQuery(query);

				String selectQuery = "select blogpost_id from blogtrackers.blogposts where permalink = '" + postpermalink[i] + "'";
				rs = st.executeQuery(selectQuery);				
				rs.next();

				int blogpost_id = rs.getInt(1);
				fvb.databaseUpdate(contents,blogpost_id,1);

				for(String link:outLinks)
				{
					String outLinkInsert = "insert into blogtrackers.outlinks (link,blogpost_id) values(\"" + link + "\"," + blogpost_id + ")";
					//System.out.println(outLinkInsert);
					r.runQuery(outLinkInsert);
				}

				afvb.commentsSplitter(blogpost_id, comments[i]);

				listOfPostpermalinks.add(postpermalink[i]);
			}
			lastCrawl = lastCrawled[i];
		}
		query = "UPDATE blogtrackers.blogsites SET last_crawled = \"" + lastCrawl + "\" where blogsite_id = 1";
		r.runQuery(query);
		conn.close();

		return "done";
	}

	/**
	 * This method splits comments into individual comments, extracts the comment date and the commenter
	 */
	
	public void commentsSplitter(int blogpost_id,String comments)
	{
		afvb = new AFieldOfViewsBlog();
		int index = comments.indexOf("Reply");
		while(index != -1)
		{
			index = index + 5;
			String comment = comments.substring(0, index);
			comment = afvb.trim(comment);
			int index2 = comment.indexOf("\n");
			String commenter = comment.substring(0,index2);
			comment = afvb.trim(comment.substring(index2+1));
			index2 = comment.indexOf("\n");
			String comment_date = comment.substring(0, index2);
			comment = afvb.trim(comment.substring(index2+1));
			index2 = comment.indexOf("Reply");
			String text = comment.substring(0, index2);
			text = afvb.trim(text);
			comment_date = afvb.dateConverter(comment_date);

			/*
			System.out.println("commenter " + commenter);
			System.out.println("comment_date " + comment_date);
			System.out.println("comment " + text);
			 */

			String insertQuery = "insert into blogtrackers.blogcomments(blogname,comment,blogpost_id,commenter,comment_date) "
					+ "values(\"A Field Of View Blog\",\"" + text + "\"," + blogpost_id + ",\"" + commenter + "\",\"" + comment_date + "\")";
			r.runQuery(insertQuery);

			if(comments.length()>(index + 1))
			{
				comments = comments.substring(index + 1);
				index = comments.indexOf("Reply");
			}
			else
			{
				index = -1;
			}
		}
	}

	/**
	 * this is a custom trim method
	 */
	public String trim(String text)
	{	
		int index = text.indexOf("\n");
		while(index != -1 && index == 0)
		{
			text = text.substring(1);
			index = text.indexOf("\n");
		}
		int l = text.length();
		while(text.charAt(l-1) == '\n')
		{
			text = text.substring(0, l-1);
			l = text.length();
		}
		return text;
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
			if(link.contains("http") && !linkText.contains("<IMG ") && 
					!linkText.contains("About these ads") && !linkText.contains("<SPAN>") && 
					!link.contains("/tag") && !link.contains("/category/"))
			{
				outlinks.add(link);
			}
			if(link.contains("/category/"))
			{
				category = linkText;
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
		months.put("January", "01");
		months.put("February", "02");
		months.put("March", "03");
		months.put("April", "04");
		months.put("May", "05");
		months.put("June", "06");
		months.put("July", "07");
		months.put("August", "08");
		months.put("September", "09");
		months.put("October", "10");
		months.put("November", "11");
		months.put("December", "12");

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

		String date1 = date.replace("Posted on ", "");
		int index1 = date1.indexOf(' ');
		String month = date1.substring(0, index1);
		int index2 = date1.indexOf(", ");
		String d = date1.substring(index1 + 1, index2);
		String year = date1.substring(index2 + 2);
		String mon = months.get(month);
		if(number.containsKey(Integer.parseInt(d)))
		{
			d = number.get(Integer.parseInt(d));
		}

		convertedDate = year + '-' + mon +'-' + d;
		return convertedDate;
	}

}

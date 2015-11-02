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
 * This class formats the data crawled from sinkuartel.org to a format that blogtrackers supports
 */

public class Sinkuartel {

	Resources r;
	String query;
	String category;
	Sinkuartel sktl;
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
	
	public Sinkuartel() {
		// TODO Auto-generated constructor stub
		this.query = "SELECT * FROM ukraine_db.temp_sinkuratel where POSTAUTHOR not like '' or POSTDATEBLOG not like '' and FLAG != 2";
		r = new Resources();
	}
	
	/**
	 * execution starts from this method. This method is called from main method.
	 */
	public String run() throws Exception
	{
		sktl = new Sinkuartel();
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
				LinkedList<String> outLinks = sktl.linkExtractor(temp);
				int outLinksNum = outLinks.size();
				//System.out.println(postpermalink[i] + "\t" + outLinksNum);
				
				String date1 = sktl.dateConverter(date[i]);

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
						postpermalink[i]+"\",51,\""+tag+"\")";

				r.runQuery(insertQuery);
				
				query = "UPDATE ukraine_db.temp_sinkuratel set FLAG = 2 where POSTPERMALINK =\'" + postpermalink[i] 
						+ "\'";
				r.runQuery(query);

				String selectQuery = "select blogpost_id from blogtrackers.blogposts where permalink = '" + postpermalink[i] + "'";
				rs = st.executeQuery(selectQuery);				
				rs.next();

				int blogpost_id = rs.getInt(1);
				
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
		query = "UPDATE blogtrackers.blogsites SET last_crawled = \"" + lastCrawl + "\" where blogsite_id = 51";
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
		months.put("de Enero de", "01");
		months.put("de Febrero de", "02");
		months.put("de Marzo de", "03");
		months.put("de Abril de", "04");
		months.put("de Mayo de", "05");
		months.put("de Junio de", "06");
		months.put("de Julio de", "07");
		months.put("de Agosto de", "08");
		months.put("de Septiembre de", "09");
		months.put("de Octubre de", "10");
		months.put("de Noviembre de", "11");
		months.put("de Diciembre de", "12");
		
		String convertedDate = null;

		int index = date.indexOf(" ");
		String d = date.substring(0, index);
		date = date.substring(index + 1);
		index = date.lastIndexOf(" ");
		String time = date.substring(index + 1);
		date = date.substring(0,index);
		index = date.lastIndexOf(" ");
		String month = date.substring(0, index);
		String year = date.substring(index + 1);
		String mon = months.get(month);
		
		convertedDate = year + '-' + mon +'-' + d + " " + time + ":00";
		return convertedDate;
	}

	
}

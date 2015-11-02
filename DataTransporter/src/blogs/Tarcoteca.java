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
 * This class formats the data crawled from tarcoteca.blogspot.nl to a format that blogtrackers supports
 */

public class Tarcoteca {

	Resources r;
	String query;
	String category;
	Tarcoteca tct;
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

	public Tarcoteca() {
		// TODO Auto-generated constructor stub
		this.query = "SELECT * FROM ukraine_db.temp_tarcoteca where POSTPERMALINK != 'http://tarcoteca.blogspot.nl/' and POSTPERMALINK not like '%search?updated%' and FLAG != 3";
		r = new Resources();
	}

	/**
	 * execution starts from this method. This method is called from main method.
	 */
	public String run() throws Exception
	{
		tct = new Tarcoteca();
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
				LinkedList<String> outLinks = tct.linkExtractor(temp);
				int outLinksNum = outLinks.size();
				//System.out.println(postpermalink[i] + "\t" + outLinksNum);

				String date1 = tct.dateConverter(date[i]);

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
					tag = tag.replace("Etiquetas: ", "");
				}
				if(category == null)
				{
					category = scope[i];
				}
				
				String insertQuery = "insert into blogtrackers.blogposts (title,date,blogger,categories,post,"
						+ "post_length,num_outlinks,num_inlinks,num_comments,permalink,blogsite_id,tags) values"
						+ "(\""+titles+"\",\""+date1+"\",\""+authors+"\",\""+ category +"\",\""+contents+"\","
						+contents.length()+","+outLinksNum+",0,"+commentsNum[i]+",\""+
						postpermalink[i]+"\",52,\""+tag+"\")";

				r.runQuery(insertQuery);

				query = "UPDATE ukraine_db.temp_tarcoteca set FLAG = 2 where POSTPERMALINK =\'" + postpermalink[i] 
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
		query = "UPDATE blogtrackers.blogsites SET last_crawled = \"" + lastCrawl + "\" where blogsite_id = 52";
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
		months.put("de enero de", "01");
		months.put("de febrero de", "02");
		months.put("de marzo de", "03");
		months.put("de abril de", "04");
		months.put("de mayo de", "05");
		months.put("de junio de", "06");
		months.put("de julio de", "07");
		months.put("de agosto de", "08");
		months.put("de septiembre de", "09");
		months.put("de octubre de", "10");
		months.put("de noviembre de", "11");
		months.put("de diciembre de", "12");

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

		int index = date.indexOf(", ");
		date = date.substring(index + 2);
		index = date.indexOf(" ");
		String d = date.substring(0, index);
		date = date.substring(index + 1);
		index = date.lastIndexOf(" ");
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

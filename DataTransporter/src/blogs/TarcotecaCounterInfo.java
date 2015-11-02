package blogs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedList;

import data.transporter.core.Resources;

public class TarcotecaCounterInfo extends Tarcoteca {

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
	
	public TarcotecaCounterInfo() {
		// TODO Auto-generated constructor stub
		query = "SELECT * FROM ukraine_db.temp_tarcotecacounterinfo where POSTPERMALINK != 'http://tarcotecacounterinfo.blogspot.com/' and POSTPERMALINK not like '%search?updated%' and POSTAUTHOR != '' and FLAG !=3";
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
				}
				if(category == null)
				{
					category = scope[i];
				}
				
				String insertQuery = "insert into blogtrackers.blogposts (title,date,blogger,categories,post,"
						+ "post_length,num_outlinks,num_inlinks,num_comments,permalink,blogsite_id,tags) values"
						+ "(\""+titles+"\",\""+date1+"\",\""+authors+"\",\""+ category +"\",\""+contents+"\","
						+contents.length()+","+outLinksNum+",0,"+commentsNum[i]+",\""+
						postpermalink[i]+"\",53,\""+tag+"\")";

				r.runQuery(insertQuery);

				query = "UPDATE ukraine_db.temp_tarcotecacounterinfo set FLAG = 2 where POSTPERMALINK =\'" + postpermalink[i] 
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
		query = "UPDATE blogtrackers.blogsites SET last_crawled = \"" + lastCrawl + "\" where blogsite_id = 53";
		r.runQuery(query);
		conn.close();

		return "done";
	}
	
}
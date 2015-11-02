package Util;

import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import org.xml.sax.InputSource;

import com.alchemyapi.api.AlchemyAPI;

import data.transporter.core.Resources;

public class SentimentExtractor {

	SentimentExtractor se1;

	public SentimentExtractor() {
		// TODO Auto-generated constructor stub
		
	}
	
	public void run(AlchemyAPI alchemyObj) throws Exception
	{
		se1 = new SentimentExtractor();
		Resources r = new Resources();
		Class.forName(r.getDatabaseDriver());
		String connectionString = r.getDatabaseConnectionString();
		String userName = r.getUsername();
		String password = r.getPassword();
		Connection conn = DriverManager.getConnection(connectionString, userName, password); //r.getDatabaseConnectionString(), r.getUsername(), r.getPassword());
		Statement st = conn.createStatement();
		String query = "select blogpost_id,post from blogtrackers.blogposts where sentiment = 0";
		
		ResultSet rs = st.executeQuery(query);
		int count = 0;
		while(rs.next())
		{
			int id = rs.getInt(1);
			String content = rs.getString(2);
			double sentiment = 0;
			try
			{
			sentiment = se1.getSentiment(content,alchemyObj);
			if(Double.isNaN(sentiment))
			{
				sentiment = 0;
			}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(id + ":" + sentiment);
			query = "UPDATE blogtrackers.blogposts set sentiment = " + sentiment + " where blogpost_id =" + id;
			r.runQuery(query);
			
			count++;
		}
	}
	
	public double getSentiment(String text,AlchemyAPI alchemyObj) throws Exception{
        double sentiment =0;
        Document doc = alchemyObj.TextGetTextSentiment(text);
		try {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            
            sentiment = getScore(writer.toString());
            
        } catch (TransformerException ex) {
            ex.printStackTrace();
        }
		return sentiment;
    }
	public static double getScore (String result) throws Exception
	{
		double sentiment = 0;
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(result));

		Document doc = db.parse(is);
		NodeList nodes= doc.getElementsByTagName("docSentiment");

		for(int i = 0; i <nodes.getLength() ; i++)
		{
			Element element = (Element) nodes.item(i);

			NodeList name = element.getElementsByTagName("score");
			Element line = (Element) name.item(0);

			sentiment = Double.parseDouble(getCharacterDataFromElement(line));

			/*
			NodeList title = element.getElementsByTagName("type");
			line = (Element) title.item(0);
			System.out.println("Type: " + getCharacterDataFromElement(line));
			 */
		}

		return sentiment;
	}
	public static String getCharacterDataFromElement(Element e) {
		Node child = e.getFirstChild();
		if (child instanceof CharacterData) {
			CharacterData cd = (CharacterData) child;
			return cd.getData();
		}
		return "";
	}
}

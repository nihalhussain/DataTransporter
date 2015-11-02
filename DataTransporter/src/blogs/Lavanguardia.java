package blogs;

import java.util.LinkedHashMap;

public class Lavanguardia {

	public String converter(String date)
	{

		String convertedDate = null;
		
		int index1 = date.indexOf(" - ");
		String time = date.substring(index1+3, date.length());
		time = time.replace("h", "");
		date = date.substring(0, index1);
		index1 = date.indexOf(':');
		if(index1 != -1)
		{
			date = date.substring(index1+2);
		}
		index1 = date.indexOf('/');
		String d = date.substring(0, index1);
		date = date.substring(index1+1);
		index1 = date.indexOf('/');
		String month = date.substring(0, index1);
		String year = date.substring(index1+1, date.length());
		//String mon = months.get(month);
		
		//if(mon)
		
		convertedDate = year + '-' + month +'-' + d + " " + time;
		System.out.println(convertedDate);
		
		return convertedDate;
	}
}

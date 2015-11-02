package blogs;

import java.util.LinkedHashMap;

public class EnriaBlog {
	
	public String converter(String date)
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
		
		String convertedDate = null;
		
		int index1 = date.indexOf(' ');
		String d = date.substring(0, index1);
		date = date.substring(index1+1, date.length());
		index1 = date.indexOf(' ');
		String month = date.substring(0, index1);
		String year = date.substring(index1+1, date.length());
		String mon = months.get(month);
		
		convertedDate = year + '-' + mon +'-' + d;
		//System.out.println(convertedDate);
		
		return convertedDate;
	}

}

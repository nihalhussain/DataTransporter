package blogs;

import java.util.LinkedHashMap;

public class ENItarTassComBlog {

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
		
		int index = date.indexOf(' ');
		String month = months.get(date.substring(0, index));
		date = date.substring(index+1);
		index = date.indexOf(", ");
		String d = date.substring(0, index);
		date = date.substring(index+2);
		index = date.indexOf(':');
		String hours = date.substring(0, index);
		//System.out.println(hours);
		if(number.containsKey(Integer.parseInt(hours)))
		{
			hours = number.get(Integer.parseInt(hours));
		}
		//System.out.println(hours);
		String minutes = date.substring(index+1);
		
		String date1 = "2014-"+month+"-"+d+" "+hours+":"+minutes;
		//System.out.println(date1);
		return date1;
	}
}

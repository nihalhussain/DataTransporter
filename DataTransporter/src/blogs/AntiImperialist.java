package blogs;

import java.util.LinkedHashMap;

public class AntiImperialist {
	
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
		
		String convertedDate = null;
		
		int index1 = date.indexOf(", ");
		date = date.substring(index1+2);
		index1 = date.indexOf(" ");
		String month = date.substring(0, index1);
		date = date.substring(index1+1,date.length());
		index1 = date.indexOf(',');
		String d = date.substring(0,index1);
		String year = date.substring(index1 + 2);
		
		String mon = months.get(month);
		if(number.containsKey(Integer.parseInt(d)))
		{
			d = number.get(Integer.parseInt(d));
		}
		
		convertedDate = year + '-' + mon +'-' + d;
		//System.out.println(date);
		//System.out.println(convertedDate);
		return convertedDate;
	}

	
}

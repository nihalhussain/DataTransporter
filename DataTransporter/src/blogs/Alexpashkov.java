package blogs;

import java.util.LinkedHashMap;

public class Alexpashkov {

	public String converter(String date)
	{
		//System.out.println(date);
		
		LinkedHashMap<String, String> months = new LinkedHashMap<String, String>();
		months.put("Январь", "01");
		months.put("Февраль", "02");
		months.put("Март", "03");
		months.put("Апрель", "04");
		months.put("Май", "05");
		months.put("Июнь", "06");
		months.put("Июль", "07");
		months.put("Август", "08");
		months.put("Сентябрь", "09");
		months.put("Октябрь", "10");
		months.put("Ноябрь", "11");
		months.put("Декабрь", "12");
		

		String convertedDate = null;
		
		int index1 = date.indexOf(',');
		String date1 = date.substring(index1+2, date.length());
		index1 = date1.indexOf(" ");
		String d = date1.substring(0, index1);
		date1 = date1.substring(index1+1, date1.length());
		index1 = date1.indexOf(" ");
		String month = date1.substring(0, index1);
		date1 = date1.substring(index1+1, date1.length());
		index1 = date1.indexOf(" ");
		String year = date1.substring(0,index1);
		String time = date1.substring(index1+1, date1.length());
		
		
		
		
		/*
		System.out.println(d);
		System.out.println(month);
		System.out.println(year);
		System.out.println(time);
		*/
		
		
		String mon = months.get(month);
		//System.out.println(month + "\t" + mon);
		if(mon == null)
		{
			System.out.println("error");
		}
		
		convertedDate = year + '-' + mon +'-' + d + " "+time;
		//System.out.println(date1);
		//System.out.println(convertedDate);
		
		return convertedDate;
	}
	
}

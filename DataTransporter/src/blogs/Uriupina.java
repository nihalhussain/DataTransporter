package blogs;

public class Uriupina {

	public String converter(String date)
	{
		
		String convertedDate = null;
		
		int index1 = date.indexOf('-');
		String d = date.substring(0, index1);
		date = date.substring(index1+1, date.length());
		index1 = date.indexOf('-');
		String month = date.substring(0, index1);
		String year = date.substring(index1+1, date.length());
		//String mon = months.get(month);
		
		convertedDate = year + '-' + month +'-' + d;
		System.out.println(convertedDate);
		
		return convertedDate;
	}

}

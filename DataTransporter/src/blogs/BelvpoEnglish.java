package blogs;

public class BelvpoEnglish {
	public String converter(String date)
	{
		int index=date.indexOf('.');
		String d = date.substring(0, index);
		date = date.substring(index+1, date.length());
		index = date.indexOf('.');
		String month = date.substring(0, index);
		String year = date.substring(index+1);
		
		String date1 = year + '-'+month+'-'+d;
		return date1;
	}
}

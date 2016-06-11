package solution;

public class JSONUtils {

	public static String trimWhitespace(String string) {
		return string.trim();
	}

	public static String wholeString(String tailString) throws JSONException {
		ensureStartsQuote(tailString);
		for (int i = 1;i<tailString.length();i++){
			char iChar = tailString.charAt(i);
			if (iChar =='"'){
				int indexAfterSecondQuote = i+1;
				return tailString.substring(0,indexAfterSecondQuote);
			}else if(iChar == '\\'){
				i++;
				continue;
			}
		}
		throw new JSONException("End of string not found");

	}

	public static int lenWholeString(String tailString) throws JSONException {
		
		ensureStartsQuote(tailString);
		
		return wholeString(tailString).length();
	}
	
	private static void ensureStartsQuote(String string) throws JSONException{
		int indexOfFirstQuote = string.indexOf('"');
		if (indexOfFirstQuote != 0){
			throw new JSONException("Didn't start with double quote");
		}
	}

}

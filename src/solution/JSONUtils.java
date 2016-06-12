package solution;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONUtils {

	public static String trimLeftWhitespaceAndCommaColon(String string) {
		int lenLeftWhiteSpace = lengthLeftWhiteSpaceAndCommaColon(string);
		string = string.substring(lenLeftWhiteSpace);
		char first = string.charAt(0);
		if ( first == ',' || first == ':' ){
			return trimLeftWhitespaceAndCommaColon(string.substring(1));
		}else{
			return string;
		}
	}

	public static String wholeString(String tailString) throws JSONException {
		tailString = JSONUtils.trimLeftWhitespaceAndCommaColon(tailString);
		ensureStartsQuote(tailString);
		
		String wholeString = "";
		boolean found = false;
		for (int i = 1;i<tailString.length();i++){
			char iChar = tailString.charAt(i);
			if (iChar =='"'){
				int indexAfterSecondQuote = i+1;
				wholeString = tailString.substring(0,indexAfterSecondQuote);
				found = true;
				break;
			}else if(iChar == '\\'){
				i++;
				continue;
			}
		}
		if (found){
			String trimmed = JSONUtils.trimLeftWhitespaceAndCommaColon(wholeString);
			String noQuotes = JSONUtils.removeWrappingQuotes(trimmed);
			return noQuotes;
		}else{
			throw new JSONException("End of string not found");
		}
		

	}

	public static String removeWrappingQuotes(String trimmed) throws JSONException {
		ensureStartsQuote(trimmed);
		int indexFirstQuote = 0;
		int indexClosingQuote = trimmed.lastIndexOf('"');
		
		if (indexClosingQuote!=trimmed.length()-1){
			throw new JSONException("trying to remove wrapping quotes but terminating quote not at end");
		}
		
		String noWrappingQuotes = trimmed.substring(indexFirstQuote+1,indexClosingQuote);
		return noWrappingQuotes;
	}

	public static int lenWholeString(String tailString) throws JSONException {
		
		int lenLeftWhiteSpace = lengthLeftWhiteSpaceAndCommaColon(tailString);
		tailString = JSONUtils.trimLeftWhitespaceAndCommaColon(tailString);
		
		ensureStartsQuote(tailString);
		
		int lenQuotes = 2;
		
		return wholeString(tailString).length() + lenLeftWhiteSpace+lenQuotes;
	}
	
	private static void ensureStartsQuote(String string) throws JSONException{
		int indexOfFirstQuote = string.indexOf('"');
		if (indexOfFirstQuote != 0){
			throw new JSONException("Didn't start with double quote : *"+string.substring(0));
		}
	}
	
	private static int lengthLeftWhiteSpace(String string){
		String regex = "\\S";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(string);
		if (matcher.find()){
			int indexNonWhiteSpace = matcher.start();
			return indexNonWhiteSpace;
		}else{
			return string.length();
		}
	}
	
	public static int lengthLeftWhiteSpaceAndCommaColon(String string){
		int lenBeforePotentialComma = lengthLeftWhiteSpace(string);
		String noLeftWhitespace = string.substring(lenBeforePotentialComma);
		char first = noLeftWhitespace.charAt(0);
		if (first == ',' || first == ':'){
			return lenBeforePotentialComma + lengthLeftWhiteSpaceAndCommaColon(noLeftWhitespace.substring(1)) +1;//add 1 for char
		}else{
			return lenBeforePotentialComma;
		}
	}
	
	public static boolean nextNonWhiteCharIs(char target,String containing) throws JSONException{
		
		int amountSpaceUntilNext = JSONUtils.lengthLeftWhiteSpaceAndCommaColon(containing);
		String noWhite = containing.substring(amountSpaceUntilNext);
		
		if (noWhite.length() ==0){
			throw new JSONException("No chars except whitespace in nextNonWhiteCharIs");
		}
		char firstChar = noWhite.charAt(0);
		return (firstChar == target);
		
	}
	
	private static boolean indexIsLower(int attempt,int bestSoFar){
		return (attempt >= 0 && attempt < bestSoFar);
	}

	public static int indexOfPropertyStartChars(String string) {
		char[] startChars = {'"','{','['};
		int lowestIndex = -1;
		for (char attemptChar : startChars){
			int attemptIndex = string.indexOf(attemptChar);
			if ( indexIsLower(attemptIndex,lowestIndex) || lowestIndex == -1){
				lowestIndex = attemptIndex;
			}
		}
		return lowestIndex;
	}

	public static boolean nextNonWhiteCharCloses(String string, char opening) throws JSONException {
		char closing;
		if (opening == '"'){
			closing = '"';
		}else if (opening == '{'){
			closing = '}';
		}else if (opening == '['){
			closing = ']';
		}else{
			throw new JSONException("Unexpected opening char when testing nextNonWhiteCharCloses");
		}
		return nextNonWhiteCharIs(closing,string);
	}
	
	public static boolean nextNonWhiteCharIsClosing(String string) throws JSONException {
		return (nextNonWhiteCharIs('"',string)
				|| nextNonWhiteCharIs('}',string)
				|| nextNonWhiteCharIs(']',string));
	}
	
}

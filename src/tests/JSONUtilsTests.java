package tests;

import static org.junit.Assert.*;

import org.junit.Test;

import solution.JSONException;
import solution.JSONUtils;

public class JSONUtilsTests {

	@Test
	public void tTrimWhitespaceSpace() {
		String noSpaceJSON = "{\"name\":\"John\"}";
		String rightSpace = " ";
		String spaceJSON = "  " + noSpaceJSON +rightSpace;
		String resultJSON = JSONUtils.trimLeftWhitespaceAndCommaColon(spaceJSON);
		assertEquals(noSpaceJSON+rightSpace,resultJSON);
	}
	
	@Test
	public void tTrimWhitespaceTab() {
		String noTabJSON = "{\"name\":\"John\"}";
		String rightTab = " \t";
		String tabJSON = " \t " + noTabJSON + rightTab;
		String resultJSON = JSONUtils.trimLeftWhitespaceAndCommaColon(tabJSON);
		assertEquals(noTabJSON+rightTab,resultJSON);
	}
	
	@Test
	public void tTrimWhitespaceAndCommaComma() {
		String noWhiteSpaceOrComma = "{\"name\":\"John\"}";
		String withWhitespaceComma = "   ,   " + noWhiteSpaceOrComma;
		String result = JSONUtils.trimLeftWhitespaceAndCommaColon(withWhitespaceComma);
		assertEquals(noWhiteSpaceOrComma,result);
	}
	
	@Test
	public void tWholeStringExtraEnd() throws JSONException{
		String trailingEnd = "\"I like to party\" in the evening";
		String expected = "I like to party";
		
		String result = JSONUtils.wholeString(trailingEnd);
		assertEquals(expected,result);
	}
	
	@Test(expected = JSONException.class)
	public void tWholeStringExtraStart() throws JSONException{
		String extraStart = "People say that \"I like to party\"";
		String expected = "I like to party";
		
		String result = JSONUtils.wholeString(extraStart);
		assertEquals(expected,result);
	}
	
	@Test
	public void tWholeStringEscaped() throws JSONException{
		String escapedString = "\"The " + '\\' + '"' + " is a double quote mark\"";
		String result = JSONUtils.wholeString(escapedString);
		
		String expectedString = JSONUtils.removeWrappingQuotes(escapedString);
		
		assertEquals(expectedString,result);
		
	}
	
	@Test
	public void tWholeStringEscapedAndExtraEnd() throws JSONException{
		String pureEscapedString = "\"The " + '\\' + '"' + " is a double quote mark\"";
		String escapedString =  pureEscapedString + " and I like it";
		
		String expectedString = JSONUtils.removeWrappingQuotes(pureEscapedString);
		
		String result = JSONUtils.wholeString(escapedString);
		assertEquals(expectedString,result);
		
	}
	
	@Test
	public void tLenWholeString() throws JSONException{
		String quote = "\"Yes\"";
		int expectedLength = 5;
		
		int resultLength = JSONUtils.lenWholeString(quote);
		
		assertEquals(expectedLength,resultLength);
		
	}
	
	@Test
	public void tLenWholeStringExtraEnd()throws JSONException{
		String quote = "\"Yes\" said Same";
		int expectedLength = 5;
		
		int resultLength = JSONUtils.lenWholeString(quote);
		
		assertEquals(expectedLength,resultLength);
	}
	
	@Test
	(expected = JSONException.class)
	public void tLenWholeStringExtraStart()throws JSONException{
		String quote = "I think Sam said \"Yes\"";
		int expectedLength = 5;
		
		int resultLength = JSONUtils.lenWholeString(quote);
		
		assertEquals(expectedLength,resultLength);
	}
	
	@Test
	public void tLenWholeStringIncludesQuote() throws JSONException{
		String quote = "\"Y\\\"ES\"";
		int expectedLength = 7;
		
		int resultLength = JSONUtils.lenWholeString(quote);
		
		assertEquals(expectedLength,resultLength);
		
	}
	
	@Test
	public void tNoWrappingQuotes() throws JSONException{
		
		String noQuotes = "We will fight them on the beaches";
		String quotedString = "\"" + noQuotes + "\"";
		
		String result = JSONUtils.removeWrappingQuotes(quotedString);
		
		assertEquals(noQuotes,result);
		
	}
	

}

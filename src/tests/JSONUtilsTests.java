package tests;

import static org.junit.Assert.*;

import org.junit.Test;

import solution.JSONException;
import solution.JSONUtils;

public class JSONUtilsTests {

	@Test
	public void tTrimWhitespaceSpace() {
		String noSpaceJSON = "{\"name\":\"John\"}";
		String spaceJSON = "  " + noSpaceJSON + " ";
		String resultJSON = JSONUtils.trimWhitespace(spaceJSON);
		assertEquals(noSpaceJSON,resultJSON);
	}
	
	@Test
	public void tTrimWhitespaceTab() {
		String noTabJSON = "{\"name\":\"John\"}";
		String tabJSON = " \t " + noTabJSON + " \t";
		String resultJSON = JSONUtils.trimWhitespace(tabJSON);
		assertEquals(noTabJSON,resultJSON);
	}
	
	@Test
	public void tWholeStringExtraEnd() throws JSONException{
		String trailingEnd = "\"I like to party\" in the evening";
		String expected = "\"I like to party\"";
		
		String result = JSONUtils.wholeString(trailingEnd);
		assertEquals(expected,result);
	}
	
	@Test(expected = JSONException.class)
	public void tWholeStringExtraStart() throws JSONException{
		String extraStart = "People say that \"I like to party\"";
		String expected = "\"I like to party\"";
		
		String result = JSONUtils.wholeString(extraStart);
		assertEquals(expected,result);
	}
	
	@Test
	public void tWholeStringEscaped() throws JSONException{
		String escapedString = "\"The " + '\\' + '"' + " is a double quote mark\"";
		String result = JSONUtils.wholeString(escapedString);
		assertEquals(escapedString,result);
		
	}
	
	@Test
	public void tWholeStringEscapedAndExtraEnd() throws JSONException{
		String pureEscapedString = "\"The " + '\\' + '"' + " is a double quote mark\"";
		String escapedString =  pureEscapedString + " and I like it";
		
		String result = JSONUtils.wholeString(escapedString);
		assertEquals(pureEscapedString,result);
		
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
	public void tLenWholeStringIncludesQuote() throws JSONException{
		String quote = "\"Y\\\"ES\"";
		int expectedLength = 7;
		
		int resultLength = JSONUtils.lenWholeString(quote);
		
		assertEquals(expectedLength,resultLength);
		
	}
	

}

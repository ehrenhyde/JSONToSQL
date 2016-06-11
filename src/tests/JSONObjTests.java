package tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.junit.Test;

import solution.JSONException;
import solution.JSONFile;
import solution.JSONObj;
import solution.TestJSONFileNames;

public class JSONObjTests {

	
	@Test
	public void tJSONObjJustVals() throws IOException, JSONException{
		JSONFile testFile = new JSONFile(TestJSONFileNames.JUST_VALS);
		String json = testFile.readString();
		
		JSONObj jsonObj = new JSONObj(json);
		
		TreeMap<String,String> valProps = jsonObj.getValProps();
		
		TreeMap<String,String> expected = new TreeMap<String,String>();
		expected.put("name", "Test");
		expected.put("code", "QUT");
		expected.put("worldRanking", "32");
		
		assertValProps(expected,valProps);
	}
	
	private void assertValProps(TreeMap<String,String> expected,TreeMap<String,String> subject){
		TreeMap<String,String> expectedRemaining = new TreeMap<String,String>();
		expectedRemaining.putAll(expected);
		
	
		for (Entry<String,String> testedEntry : subject.entrySet()){
			
			if (expectedRemaining.containsKey(testedEntry.getKey())){
				
				String expectedVal = expectedRemaining.get(testedEntry.getKey());
				String actualVal = testedEntry.getValue();
				if (expectedVal.equals(actualVal)){
					expectedRemaining.remove(testedEntry.getKey());
				}else{
					fail("Values not equal for same keys");
				}
			}else{
				fail("Key in subject which was not in expected: " + testedEntry.getKey());
			}
		}
		
		if (!expectedRemaining.isEmpty()){
			fail("Key in expected which was not in subject");
		}
	}

}

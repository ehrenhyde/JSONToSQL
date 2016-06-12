package tests;

import static org.junit.Assert.fail;

import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Map.Entry;

import solution.JSONArray;
import solution.JSONElement;
import solution.JSONObj;

public class TestUtils {
	
	public static void assertAllProps(JSONObj e,JSONObj s){
		assertValProps(e.getValProps(),s.getValProps());
		assertObjProps(e.getObjProps(),s.getObjProps());
		assertArrayProps(e.getArrayProps(),s.getArrayProps());
	}
	
	public static void assertEqual(JSONArray e,JSONArray s){
		for (JSONElement sElement: s){
			if (e.contains(sElement)){
				e.remove(sElement);
			}else{
				fail("Element *"+sElement.getElementJSON()+"* in subject but not in expected array");
			}
		}
		if (!e.isEmpty()){
			fail("Element *"+e.get(0)+"* in expected which was not in subject");
		}
	}
	
	public static void assertArrayProps(TreeMap<String, JSONArray> expected, TreeMap<String, JSONArray> subject) {
		TreeMap<String,JSONArray> eRem = new TreeMap<String,JSONArray>();
		eRem.putAll(expected);
		
		for (Entry<String,JSONArray> sEntry : subject.entrySet()){
			
			if (eRem.containsKey(sEntry.getKey())){
				
				JSONArray eArray = eRem.get(sEntry.getKey());
				JSONArray sArray = sEntry.getValue();
				TestUtils.assertEqual(eArray, sArray);
				eRem.remove(sEntry.getKey());
			}else{
				fail("Array *"+sEntry.getKey()+"* in subjectProps but not in expectedProps");
			}
		}
		
		if (!eRem.isEmpty()){
			fail("Key *"+eRem.firstKey()+"* in expected which was not in subject");
		}
	}

	public static void assertObjProps(TreeMap<String, JSONObj> expected, TreeMap<String, JSONObj> subject) {
		TreeMap<String,JSONObj> eRem = new TreeMap<String,JSONObj>();
		eRem.putAll(expected);
		
		for (Entry<String,JSONObj> testedEntry : subject.entrySet()){
			
			if (eRem.containsKey(testedEntry.getKey())){
				
				JSONObj expectedObj = eRem.get(testedEntry.getKey());
				JSONObj actualObj = testedEntry.getValue();
				
				TestUtils.assertAllProps(expectedObj,actualObj);
				eRem.remove(testedEntry.getKey());
				
			}else{
				fail("Key *"+testedEntry.getKey()+"* in subject which was not in expected");
			}
		}
		
		if (!eRem.isEmpty()){
			fail("Key *"+eRem.firstKey()+"* in expected which was not in subject");
		}
	}

	public static void assertValProps(TreeMap<String,String> expected,TreeMap<String,String> subject){
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
				fail("Key *"+testedEntry.getKey()+"* in subject which was not in expected");
			}
		}
		
		if (!expectedRemaining.isEmpty()){
			fail("Key *"+expectedRemaining.firstKey()+"* in expected which was not in subject");
		}
	}
	
	public static String generateSimpleJSONValuePair(String keyName,String value){
		return "\"" + keyName + "\"" + ":" + "\"" + value + "\"";
	}
	
	public static String generateSimpleJSONObject(ArrayList<String> keyValuePairs){
		String json = "";
		json+="{";
		
		boolean firstPair = true;
		for( String keyValuePair : keyValuePairs){
			if (!firstPair){
				json+=",";
			}
			json+=keyValuePair;
			firstPair=false;
		}
		json+="}";
		return json;
	}

}

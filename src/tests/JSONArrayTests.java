package tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.TreeMap;

import org.junit.Test;

import solution.JSONArray;
import solution.JSONElement;
import solution.JSONException;

public class JSONArrayTests {

	@Test
	public void tJSONAraryJustVals() throws JSONException{
		
		JSONArray s = new JSONArray("[	\"Bob\",\"Dave\",\"Sally\",\"Sam\",\"Kate\"	]");
		
		ArrayList<JSONElement> eList = new ArrayList<JSONElement>();
		eList.add(new JSONElement("\"Bob\""));
		eList.add(new JSONElement("\"Dave\""));
		eList.add(new JSONElement("\"Sally\""));
		eList.add(new JSONElement("\"Sam\""));
		eList.add(new JSONElement("\"Kate\""));
		JSONArray e = new JSONArray(eList);
		
		TestUtils.assertEqual(e, s);
		
	}
	
	@Test
	public void tJSONAraryJustObjs() throws JSONException{
		
		String json = "";
		json += "[";
		
		ArrayList<String> keyValuePairs1 = new ArrayList<String>();
		keyValuePairs1.add(TestUtils.generateSimpleJSONValuePair("name", "Bob"));
		keyValuePairs1.add(TestUtils.generateSimpleJSONValuePair("age", "23"));
		json+=TestUtils.generateSimpleJSONObject(keyValuePairs1);
		
		ArrayList<String> keyValuePairs2 = new ArrayList<String>();
		keyValuePairs2.add(TestUtils.generateSimpleJSONValuePair("name", "Sally"));
		keyValuePairs2.add(TestUtils.generateSimpleJSONValuePair("age", "42"));
		json+=TestUtils.generateSimpleJSONObject(keyValuePairs2);
		
		ArrayList<String> keyValuePairs3 = new ArrayList<String>();
		keyValuePairs3.add(TestUtils.generateSimpleJSONValuePair("name", "James"));
		keyValuePairs3.add(TestUtils.generateSimpleJSONValuePair("age", "64"));
		json+=TestUtils.generateSimpleJSONObject(keyValuePairs3);
		
		json+="]";
		
		JSONArray s = new JSONArray(json);
		
		ArrayList<JSONElement> eElements= new ArrayList<JSONElement>();
		eElements.add(new JSONElement(TestUtils.generateSimpleJSONObject(keyValuePairs1)));
		eElements.add(new JSONElement(TestUtils.generateSimpleJSONObject(keyValuePairs2)));
		eElements.add(new JSONElement(TestUtils.generateSimpleJSONObject(keyValuePairs3)));
		JSONArray e = new JSONArray(eElements);
		
		TestUtils.assertEqual(e, s);
		
	}
	
	@Test
	public void tLenWholeArray() throws JSONException{
		String kvPair1Name = TestUtils.generateSimpleJSONValuePair("name", "Bob");
		String kvPair1Age = TestUtils.generateSimpleJSONValuePair("age", "53");
		ArrayList<String> kvPairs1 = new ArrayList<String>();
		kvPairs1.add(kvPair1Name);
		kvPairs1.add(kvPair1Age);
		String obj1 = TestUtils.generateSimpleJSONObject(kvPairs1);
		
		String singleVal1 = "\"SingleVal\"";
		
		String kvPair2Name = TestUtils.generateSimpleJSONValuePair("name", "Jil");
		String kvPair2Age = TestUtils.generateSimpleJSONValuePair("age", "34");
		ArrayList<String> kvPairs2 = new ArrayList<String>();
		kvPairs2.add(kvPair2Name);
		kvPairs2.add(kvPair2Age);
		String obj2 = TestUtils.generateSimpleJSONObject(kvPairs2);
		
		String justArray = "[ " + obj1 + "," + singleVal1 + ", " + obj2 + " ]";
		
		String arrayWithWhiteLeft = "   \t :  " + justArray;
		
		String arrayWithExtra = arrayWithWhiteLeft + " , } ] \" asdf ";
		
		int eLen = arrayWithWhiteLeft.length();
		
		int sLen = JSONArray.lenWholeArray(arrayWithExtra);
		
		assertEquals(eLen,sLen);
	}
	
	@Test
	public void tJSONArrayIntoTreeMap() throws JSONException{
		JSONArray in = new JSONArray("[	\"Bob\",\"Dave\",\"Sally\",\"Sam\",\"Kate\"	]");
		JSONArray e = in;
		
		TreeMap<String,JSONArray> props = new TreeMap<String,JSONArray>();
		
		props.put("Names",in);
		
		in = null;
		
		assertEquals(false,(e==null));
		
		JSONArray out = props.get("Names");
		
		assertEquals(false,(out==null));
		
		assertEquals(e,out);
		
		ArrayList<JSONElement> outElements = out.getElements();
		JSONElement bobNameElement = outElements.get(0);
		String bobJSON = bobNameElement.getElementJSON();
		
		assertEquals("Bob",bobJSON);
	}

}

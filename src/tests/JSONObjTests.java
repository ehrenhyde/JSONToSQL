package tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.junit.Test;

import solution.JSONArray;
import solution.JSONElement;
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
		
		TestUtils.assertValProps(expected,valProps);
	}
	
	@Test
	public void tJSONObbJustObjs()throws IOException,JSONException{
		JSONFile testFile = new JSONFile(TestJSONFileNames.JUSTOBJS);
		String json = testFile.readString();
		
		JSONObj jsonObj = new JSONObj(json);
		
		TreeMap<String,JSONObj> eRootObjProps = new TreeMap<String,JSONObj>();
		
		JSONObj foundingDetails = new JSONObj("{ \"date\":\"07/02/1993\",\"founder\":\"Steve\"	}");
		eRootObjProps.put("foundingDetails", foundingDetails);
		
		JSONObj rankingDetails = new JSONObj("{		\"australiaRanking\":\"4\",	\"internationalRanking\":\"23\"	}");
		eRootObjProps.put("rankingDetails", rankingDetails);
		
		TestUtils.assertObjProps(eRootObjProps,jsonObj.getObjProps());
		
	}
	
	@Test
	public void tJSONObjJustArrays()throws IOException,JSONException{
		JSONFile testFile = new JSONFile(TestJSONFileNames.JUSTARRAYS);
		String json = testFile.readString();
		
		JSONObj jsonObj = new JSONObj(json);
		
		TreeMap<String,JSONArray> eRootArrayProps = new TreeMap<String,JSONArray>();
		
		JSONArray acedemics = new JSONArray("[	\"Bob\",\"Dave\",\"Sally\",\"Sam\",\"Kate\"	]");
		eRootArrayProps.put("acedemics", acedemics);
	
		JSONArray acedemicsOut = eRootArrayProps.get("acedemics");
		
		boolean acedemicsIsNull = (acedemicsOut==null);
		assertEquals(false,acedemicsIsNull);
		
		JSONArray schools = new JSONArray("[	{\"name\":\"Science\",\"code\":\"SCI\"	},{\"name\":\"Maths\",\"code\":\"MAT\"	}	]");
		eRootArrayProps.put("schools", schools);
		
		TestUtils.assertArrayProps(eRootArrayProps,jsonObj.getArrayProps());
		
	}
	
	@Test
	public void tThreeTypes() throws IOException, JSONException{
		JSONFile testFile = new JSONFile(TestJSONFileNames.THREE_TYPES);
		String json = testFile.readString();
		
		JSONObj s = new JSONObj(json);
		
		TreeMap<String,String> eRootVals = new TreeMap<String,String>();
		eRootVals.put("name", "Queensland University of Technology");
		
		TreeMap<String,JSONObj> eRootObjs = new TreeMap<String,JSONObj>();
		JSONObj eFoundingDetails = new JSONObj("\"foundingDetails\":{\"date\":\"07/02/1993\",\"founder\":\"Steve\"}");
		eRootObjs.put("foundingDetails", eFoundingDetails);
		
		TreeMap<String, JSONArray> eRootArrays = new TreeMap<String,JSONArray>();
		JSONArray schools = new JSONArray("\"schools\": [	{\"name\":\"Science\",\"code\":\"SCI\"	},{\"name\":\"Maths\",\"code\":\"MAT\"	}	]");
		eRootArrays.put("schools", schools);
		
		TestUtils.assertValProps(eRootVals, s.getValProps());
		TestUtils.assertObjProps(eRootObjs, s.getObjProps());
		TestUtils.assertArrayProps(eRootArrays, s.getArrayProps());
	}
	
	@Test
	public void tLenWholeObj() throws JSONException{
		
		String json = "{ \"name\":\"Bob\",  \"age\"  : \"23\" }";
		
		String jsonWrap = "  :   \t  " + json;
		
		String jsonWrapAndExtra = jsonWrap + "  \t , \"trash\"  : {}";
		
		int lenWrap = jsonWrap.length();
		
		int lenWholeObj = JSONObj.lenWholeObj(jsonWrapAndExtra);
		
		assertEquals(lenWrap,lenWholeObj);
	}
	

}

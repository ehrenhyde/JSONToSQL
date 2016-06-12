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
import solution.JSONSingleVal;
import solution.TestJSONFileNames;

public class JSONObjTests {

	
	@Test
	public void tJSONObjJustVals() throws IOException, JSONException{
		JSONFile testFile = new JSONFile(TestJSONFileNames.JUST_VALS);
		String json = testFile.readString();
		
		JSONObj jsonObj = new JSONObj(json);
		
		TreeMap<String,JSONSingleVal> valProps = jsonObj.getValProps();
		
		TreeMap<String,JSONSingleVal> expected = new TreeMap<String,JSONSingleVal>();
		expected.put("name", new JSONSingleVal("\"Test\""));
		expected.put("code", new JSONSingleVal( "\"QUT\""));
		expected.put("worldRanking", new JSONSingleVal( "\"32\""));
		
		TestUtils.assertValProps(expected,valProps);
	}
	
	@Test
	public void tJSONObbJustObjs()throws IOException,JSONException{
		JSONFile testFile = new JSONFile(TestJSONFileNames.JUST_OBJS);
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
		JSONFile testFile = new JSONFile(TestJSONFileNames.JUST_ARRAYS);
		String json = testFile.readString();
		
		JSONObj jsonObj = new JSONObj(json);
		
		TreeMap<String,JSONArray> eRootArrayProps = new TreeMap<String,JSONArray>();
		
		JSONArray acedemics = new JSONArray("[	\"Bob\",\"Dave\",\"Sally\",\"Sam\",\"Kate\"	]");
		eRootArrayProps.put("acedemics", acedemics);
		
		JSONArray schools = new JSONArray("[	{\"name\":\"Science\",\"code\":\"SCI\"	},{\"name\":\"Maths\",\"code\":\"MAT\"	}	]");
		eRootArrayProps.put("schools", schools);
		
		TestUtils.assertArrayProps(eRootArrayProps,jsonObj.getArrayProps());
		
	}
	
	@Test
	public void tThreeTypes() throws IOException, JSONException{
		JSONFile testFile = new JSONFile(TestJSONFileNames.THREE_TYPES);
		String json = testFile.readString();
		
		JSONObj s = new JSONObj(json);
		
		TreeMap<String,JSONSingleVal> eRootSingleVals = new TreeMap<String,JSONSingleVal>();
		eRootSingleVals.put("name",new JSONSingleVal("\"Queensland University of Technology\""));
		
		TreeMap<String,JSONObj> eRootObjs = new TreeMap<String,JSONObj>();
		JSONObj eFoundingDetails = new JSONObj("\"foundingDetails\":{\"date\":\"07/02/1993\",\"founder\":\"Steve\"}");
		eRootObjs.put("foundingDetails", eFoundingDetails);
		
		TreeMap<String, JSONArray> eRootArrays = new TreeMap<String,JSONArray>();
		JSONArray schools = new JSONArray("\"schools\": [	{\"name\":\"Science\",\"code\":\"SCI\"	},{\"name\":\"Maths\",\"code\":\"MAT\"	}	]");
		eRootArrays.put("schools", schools);
		
		TestUtils.assertValProps(eRootSingleVals, s.getValProps());
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

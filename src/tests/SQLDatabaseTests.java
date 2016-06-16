package tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TreeMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import solution.JSONArray;
import solution.JSONException;
import solution.JSONFile;
import solution.JSONObj;
import solution.JSONSingleVal;
import solution.SQLColumnReference;
import solution.SQLDatabase;
import solution.SQLIdColumn;
import solution.SQLObjException;
import solution.SQLTable;
import solution.TestJSONFileNames;

public class SQLDatabaseTests {

	private SQLDatabase db;

	private volatile static int testId = 0;

	private synchronized static int getNextTestId() {
		testId++;
		return testId;
	}

	@Before
	public void setupDB() throws SQLException, IOException {
		this.db = new SQLDatabase("JUnitTest" + getNextTestId());
	}

	@After
	public void removeDb() throws SQLException {
		this.db.terminate();
	}

	@Test
	public void addSomeTables() throws SQLException {

		SQLTable units = new SQLTable("unit");
		units.addValueColumn("name");
		units.addValueColumn("failureRate");

		SQLTable uni = new SQLTable("uni");
		uni.addValueColumn("isStateRun");
		uni.addValueColumn("universityName");

		db.addTable("unit", units);
		db.addTable("uni", uni);
	}

	@Test
	public void readAccess() throws SQLException {

		String sql = "select 1+1 as SimpleSum;";
		ResultSet resultSet = db.executeQuery(sql);
		resultSet.next();
		int simpleSum = resultSet.getInt(1);
		assertEquals(2, simpleSum);
	}

	@Test
	public void writeAccess() throws SQLException {

		String sql = "create table newTestTable( col1 VARCHAR(1024) NULL, col2 VARCHAR(1024) nuLL   ); ";
		db.executeUpdate(sql);

		sql = "INSERT INTO newTestTable values('Jack','Jill');  ";
		db.executeUpdate(sql);

		sql = "select * from newTestTable;";
		ResultSet rs = db.executeQuery(sql);
		rs.next();
		String name1 = rs.getString(1);
		String name2 = rs.getString(2);

		assertEquals("Jack", name1);
		assertEquals("Jill", name2);
	}

	@Test
	public void writeTableSchema() throws SQLException, SQLObjException {

		SQLTable tUnit = new SQLTable("unit");
		tUnit.addValueColumn("name");
		tUnit.addValueColumn("failureRate");

		db.addTable("unit", tUnit);

		db.writeTableSchema("unit");

		ResultSet rs = db.executeQuery("select TABLE_NAME from information_schema.tables where table_name = 'unit'");
		rs.next();
		String tableName = rs.getString(1);
		assertEquals("unit", tableName);
	}

	@Test
	public void writeTableSchemaWithForeignKey() throws SQLException, SQLObjException {

		SQLTable tUnit = new SQLTable("unit");
		tUnit.addValueColumn("name");
		tUnit.addValueColumn("code");
		tUnit.addValueColumn("failureRate");

		SQLTable tUni = new SQLTable("uni");
		tUni.addValueColumn("name");
		tUni.addValueColumn("code");
		tUni.addValueColumn("isStateRun");

		db.addTable("uni", tUni);
		db.addTable("unit", tUnit);

		db.writeTableSchema("uni");
		db.writeTableSchema("unit");

		ResultSet rs = db.executeQuery("select TABLE_NAME from information_schema.tables where table_name = 'unit'");
		rs.next();
		String tableName = rs.getString(1);
		assertEquals("unit", tableName);
	}

	@Test
	public void writeTable() throws SQLObjException, SQLException, JSONException {

		SQLTable tUnit = new SQLTable("unit");
		tUnit.addValueColumn("name");
		tUnit.addValueColumn("code");
		tUnit.addValueColumn("failureRate");

		TreeMap<String, JSONSingleVal> SCI101Vals = new TreeMap<String, JSONSingleVal>();
		SCI101Vals.put("name", new JSONSingleVal("\"Introduction to Science\""));
		SCI101Vals.put("code", new JSONSingleVal("\"SCI101\""));
		SCI101Vals.put("failureRate", new JSONSingleVal("\"23\""));
		tUnit.addRow(SCI101Vals);

		TreeMap<String, JSONSingleVal> SCI102Vals = new TreeMap<String, JSONSingleVal>();
		SCI102Vals.put("name", new JSONSingleVal("\"Experimental Method\""));
		SCI102Vals.put("code", new JSONSingleVal("\"SCI102\""));
		tUnit.addRow(SCI102Vals);

		db.addTable("unit", tUnit);

		db.writeTable("unit");

		ResultSet rsSCI101 = db.executeQuery("SELECT code,name,failureRate from unit WHERE code = 'SCI101'");

		rsSCI101.next();
		String sci101Code = rsSCI101.getString(1);
		String sci101Name = rsSCI101.getString(2);
		String sci101FailureRate = rsSCI101.getString(3);

		assertEquals("SCI101", sci101Code);
		assertEquals("Introduction to Science", sci101Name);
		assertEquals("23", sci101FailureRate);

		ResultSet rsSCI102 = db.executeQuery("SELECT code,name,failureRate from unit WHERE code = 'SCI102'");

		rsSCI102.next();
		String sci102Code = rsSCI102.getString(1);
		String sci102Name = rsSCI102.getString(2);
		String sci102FailureRate = rsSCI102.getString(3);

		assertEquals("SCI102", sci102Code);
		assertEquals("Experimental Method", sci102Name);
		assertEquals(null, sci102FailureRate);
	}

	@Test
	public void createDatabaseFromJSONObj_ValsAndObjs()
			throws IOException, JSONException, SQLException, SQLObjException {
		JSONFile bananaFile = new JSONFile(TestJSONFileNames.BANANA);
		String bananaJSON = bananaFile.readString();
		JSONObj banana = new JSONObj(bananaJSON);

		SQLDatabase bananaDb = new SQLDatabase("Banana", banana);
		bananaDb.writeAll();

		String sql = "select b.colour,n.calories,c.lunch from banana b "
				+ "inner join nutrition n on n.id = b.nutrition "
				+ "inner join consumptionlikelihood c on c.id = b.consumptionlikelihood ";

		ResultSet rs = bananaDb.executeQuery(sql);

		rs.next();
		String colour = rs.getString(1);
		String calories = rs.getString(2);
		String lunch = rs.getString(3);

		assertEquals("yellow", colour);
		assertEquals("4242", calories);
		assertEquals("medium", lunch);

		bananaDb.terminate();

	}

	@Test
	public void createDatabaseFromJSONObj_ArrayOfArrays()
			throws IOException, JSONException, SQLException, SQLObjException {
		JSONFile uniFile = new JSONFile(TestJSONFileNames.UNI_PARTNERS);
		String uniJSON = uniFile.readString();
		JSONObj uni = new JSONObj(uniJSON);

		SQLDatabase partnersUniDb = new SQLDatabase("PartnersUni", uni);
		partnersUniDb.writeAll();

		partnersUniDb.terminate();

	}

	@Test
	public void createDatabaseFromJSONObj_AllThreeTypes()
			throws IOException, JSONException, SQLException, SQLObjException {
		JSONFile uniFile = new JSONFile(TestJSONFileNames.obj_all);
		String uniJSON = uniFile.readString();
		JSONObj uni = new JSONObj(uniJSON);

		SQLDatabase uniDb = new SQLDatabase("objAllUniversity", uni);
		uniDb.writeAll();

		ResultSet rs;
		String sql;
		String uniName;
		String uniFounder;
		String uniCode;
		String schoolName;
		String unitName;
		String unitCode;
		String preReqCode;
		String partnerName;

		sql = "select u.name,fd.founder " + "from objalluniversity u "
				+ "left join foundingdetails fd on fd.id = u.foundingdetails;";

		rs = uniDb.executeQuery(sql);

		rs.next();
		uniName = rs.getString(1);
		uniFounder = rs.getString(2);

		assertEquals("Queensland University of Technology", uniName);
		assertEquals("Steve", uniFounder);

		sql = "select u.code,s.name,ut.name " + "from objalluniversity u "
				+ "left join schools s on s.objalluniversity_id = u.id "
				+ "left join units ut on ut.schools_id = s.id;";

		rs = uniDb.executeQuery(sql);

		rs.next();
		uniCode = rs.getString(1);
		schoolName = rs.getString(2);
		unitName = rs.getString(3);

		assertEquals("QUT", uniCode);
		assertEquals("Science", schoolName);
		assertEquals("Introduction to Science", unitName);

		rs.next();
		uniCode = rs.getString(1);
		schoolName = rs.getString(2);
		unitName = rs.getString(3);

		assertEquals("QUT", uniCode);
		assertEquals("Science", schoolName);
		assertEquals("Experimental Method", unitName);

		rs.absolute(8);
		uniCode = rs.getString(1);
		schoolName = rs.getString(2);
		unitName = rs.getString(3);

		assertEquals("QUT", uniCode);
		assertEquals("Maths", schoolName);
		assertEquals(null, unitName);

		sql = "select ut.code,put.value as PreReq " + "from units ut "
				+ "left join prerequnits put on put.units_id = ut.id;";

		rs = uniDb.executeQuery(sql);

		rs.next();
		unitCode = rs.getString(1);
		preReqCode = rs.getString(2);

		assertEquals("SCI201", unitCode);
		assertEquals("SCI101", preReqCode);

		rs.next();
		unitCode = rs.getString(1);
		preReqCode = rs.getString(2);

		assertEquals("SCI202", unitCode);
		assertEquals("SCI101", preReqCode);

		rs.absolute(6);
		unitCode = rs.getString(1);
		preReqCode = rs.getString(2);

		assertEquals("SCI103", unitCode);
		assertEquals(null, preReqCode);

		sql = "select u.code,pe.name " + "from objalluniversity u "
				+ "left join partners p on p.objalluniversity_id = u.id "
				+ "left join partners"+uniDb.getPostfixForElements()+" pe on pe.partners_id = p.id;";

		rs = uniDb.executeQuery(sql);

		rs.next();
		uniCode = rs.getString(1);
		partnerName = rs.getString(2);

		assertEquals("QUT", uniCode);
		assertEquals("Suncorp", partnerName);

		rs.absolute(5);
		uniCode = rs.getString(1);
		partnerName = rs.getString(2);

		assertEquals("QUT", uniCode);
		assertEquals("University of Queensland", partnerName);

		uniDb.terminate();

	}
	
	@Test
	public void duplicateArrarPropertyNames() throws SQLException, IOException, SQLObjException, JSONException{
		
		JSONFile samAndFriendsFile = new JSONFile(TestJSONFileNames.duplicate_array_property_name);
		String samAndFriendsJSON = samAndFriendsFile.readString();
		JSONObj samAndFriends = new JSONObj(samAndFriendsJSON);

		SQLDatabase samAndFriendsDb = new SQLDatabase("sam", samAndFriends);
		samAndFriendsDb.writeAll();

		ResultSet rs;
		String samName;
		String friendDeg1Name;
		String friendDeg2Name;
		String friendDeg3Name;
		
		String sql;
		
		sql = "select s.name,fe.name,fef.name,feff.value "+
		"from sam s "+
		"inner join friends f on f.sam_id = s.id "+
		"inner join friends"+samAndFriendsDb.getPostfixForElements()+" fe on fe.friends_id = f.id "+
		"inner join friends"+samAndFriendsDb.getPostfixForElements()+"friends fef on fef.friends"+samAndFriendsDb.getPostfixForElements()+"_id = fe.id "+
		"inner join friends"+samAndFriendsDb.getPostfixForElements()+"friendsfriends feff on feff.friends"+samAndFriendsDb.getPostfixForElements()+"friends_id = fef.id;";

		rs = samAndFriendsDb.executeQuery(sql);

		rs.absolute(2);
		samName = rs.getString(1);
		friendDeg1Name = rs.getString(2);
		friendDeg2Name = rs.getString(3);
		friendDeg3Name = rs.getString(4);

		assertEquals("Sam", samName);
		assertEquals("John", friendDeg1Name);
		assertEquals("Chris", friendDeg2Name);
		assertEquals("Sally", friendDeg3Name);
		
	}
	
	@Test
	public void array_obj() throws SQLException, IOException, SQLObjException, JSONException{
		
		JSONFile mealsFile = new JSONFile(TestJSONFileNames.ARRAY_OBJ);
		String mealsJSON = mealsFile.readString();
		JSONArray meals = new JSONArray(mealsJSON);

		SQLDatabase mealsDb = new SQLDatabase("meals", meals);
		mealsDb.writeAll();

		ResultSet rs;
		String mealName;
		String yumFactor;
		String chefName;
		String review;
		
		String sql;
		
		sql = "select m.name,m.yumFactor,c.name,r.review "+
"from meals m "+
"left join chef c on c.id = m.chef "+
"left join reviews r on r.Meals_id = m.id;";

		rs = mealsDb.executeQuery(sql);

		rs.absolute(1);
		mealName = rs.getString(1);
		chefName = rs.getString(3);

		assertEquals("cream brulee", mealName);
		assertEquals("Sam the Chef", chefName);
		
		rs.absolute(2);
		mealName = rs.getString(1);
		review = rs.getString(4);

		assertEquals("toad in the hole", mealName);
		assertEquals("A wonderful hearty meal", review);
		
		rs.absolute(4);
		mealName = rs.getString(1);
		yumFactor = rs.getString(2);

		assertEquals("pickled snails", mealName);
		assertEquals("9", yumFactor);
		
	}
	
	@Test
	public void array_singleval() throws SQLException, IOException, SQLObjException, JSONException{
		
		JSONFile daysFile = new JSONFile(TestJSONFileNames.ARRAY_SINGLEVAL);
		String daysJSON = daysFile.readString();
		JSONArray days = new JSONArray(daysJSON);

		SQLDatabase daysDb = new SQLDatabase("days", days);
		daysDb.writeAll();

		ResultSet rs;
		String name;
		
		String sql;
		
		sql = "select value "+
"from days;";

		rs = daysDb.executeQuery(sql);

		rs.absolute(2);
		name = rs.getString(1);
		
		assertEquals("Tuesday", name);
		
		rs.absolute(5);
		name = rs.getString(1);
		
		assertEquals("Friday", name);
	}
	
	@Test
	public void array_array() throws SQLException, SQLObjException, JSONException, IOException{
		JSONFile timecountersFile = new JSONFile(TestJSONFileNames.ARRAY_ARRAY);
		String timecountersJSON = timecountersFile.readString();
		JSONArray timecounters = new JSONArray(timecountersJSON);

		SQLDatabase timecountersDb = new SQLDatabase("timecounters", timecounters);
		timecountersDb.writeAll();

		ResultSet rs;
		String timecounterId;
		String singleval;
		String objpropval;
		String subarrayval;
		
		String sql;
		
		sql = "select t.id,te.value,te.year,tee.value from timecounters t "+
"left join timecounters_e te on te.TimeCounters_id = t.id "+
"left join timecounters_e_e tee on tee.TimeCounters_e_id = te.id "+
"where t.id = 1 and te.id = 2;";

		rs = timecountersDb.executeQuery(sql);

		rs.absolute(1);
		timecounterId = rs.getString(1);
		singleval = rs.getString(2);

		assertEquals("1", timecounterId);
		assertEquals("Tuesday", singleval);
		
		sql = "select t.id,te.value,te.year,tee.value from timecounters t "+
				"left join timecounters_e te on te.TimeCounters_id = t.id "+
				"left join timecounters_e_e tee on tee.TimeCounters_e_id = te.id "+
				"where t.id = 2 and te.id = 13;";

						rs = timecountersDb.executeQuery(sql);
		
		rs.absolute(1);
		timecounterId = rs.getString(1);
		objpropval = rs.getString(3);

		assertEquals("2", timecounterId);
		assertEquals("2015", objpropval);
		
		sql = "select t.id,te.value,te.year,tee.value from timecounters t "+
				"left join timecounters_e te on te.TimeCounters_id = t.id "+
				"left join timecounters_e_e tee on tee.TimeCounters_e_id = te.id "+
				"where t.id = 3 and tee.id = 3;";

						rs = timecountersDb.executeQuery(sql);
		
		
		rs.absolute(1);
		timecounterId = rs.getString(1);
		subarrayval = rs.getString(4);

		assertEquals("3", timecounterId);
		assertEquals("March", subarrayval);
		
		
	}
	
	@Test
	public void singleval() throws SQLException, SQLObjException, JSONException, IOException{
		JSONFile singlevalFile = new JSONFile(TestJSONFileNames.SINGLEVAL);
		String singlevalJSON = singlevalFile.readString();
		JSONSingleVal singleval = new JSONSingleVal(singlevalJSON);

		SQLDatabase singlevalDb = new SQLDatabase("singleval", singleval);
		singlevalDb.writeAll();

		ResultSet rs;
		String dbVal;
	
		String sql;
		
		sql = "select * from singleval";

		rs = singlevalDb.executeQuery(sql);

		rs.absolute(1);
		dbVal = rs.getString(2);

		assertEquals("Well done", dbVal);
		
		
	}
}

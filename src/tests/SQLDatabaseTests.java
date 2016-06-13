package tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import solution.SQLColumnReference;
import solution.SQLDatabase;
import solution.SQLIdColumn;
import solution.SQLTable;

public class SQLDatabaseTests {
	
	private SQLDatabase db;
	
	private volatile static int testId = 0;
	
	private synchronized static int getNextTestId(){
		testId++;
		return testId;
	}
	
	@Before 
	public void setupDB() throws SQLException, IOException{
		this.db = new SQLDatabase("JUnitTest" + getNextTestId());
	}
	
	@After 
	public void removeDb() throws SQLException{
		this.db.terminate();
	}

	@Test
	public void addSomeTables() throws SQLException {
			
		SQLIdColumn unitsIdCol = new SQLIdColumn("idUnitCode");
		SQLTable units = new SQLTable(unitsIdCol);
		units.addValueColumn("name");
		units.addValueColumn("failureRate");
		
		SQLIdColumn uniIdCol = new SQLIdColumn("idUniCode");
		SQLTable uni = new SQLTable(uniIdCol);
		uni.addValueColumn("isStateRun");
		uni.addValueColumn("universityName");
		
		db.addTable("unit", units);
		db.addTable("uni", uni);	
	}
	
	@Test
	public void readAccess() throws SQLException{

		String sql = "select 1+1 as SimpleSum;";
		ResultSet resultSet = db.executeQuery(sql);
		resultSet.next();
		int simpleSum = resultSet.getInt(1);
		assertEquals(2,simpleSum);
	}
	
	@Test
	public void writeAccess() throws SQLException{
		
		String sql = "create table newTestTable( col1 VARCHAR(1024) NULL, col2 VARCHAR(1024) nuLL   ); ";
		db.executeUpdate(sql);
		
		sql="INSERT INTO newTestTable values('Jack','Jill');  ";
		db.executeUpdate(sql);
		
		sql = "select * from newTestTable;";
		ResultSet rs = db.executeQuery(sql);
		rs.next();
		String name1 = rs.getString(1);
		String name2 = rs.getString(2);
		
		assertEquals("Jack",name1);
		assertEquals("Jill",name2);
	}
	
	@Test
	public void writeTable() throws SQLException{
		
		SQLIdColumn unitsIdCol = new SQLIdColumn("idUnitCode");
		SQLTable tUnit = new SQLTable(unitsIdCol);
		tUnit.addValueColumn("name");
		tUnit.addValueColumn("failureRate");
	
		db.addTable("unit", tUnit);
		
		db.writeTable("unit");
		
		ResultSet rs = db.executeQuery("select TABLE_NAME from information_schema.tables where table_name = 'unit'");
		rs.next();
		String tableName = rs.getString(1);
		assertEquals("unit",tableName);
	}
	
	@Test
	public void writeTableWithForeignKey() throws SQLException{
		
		SQLIdColumn unitsIdCol = new SQLIdColumn("unitCode");
		SQLTable tUnit = new SQLTable(unitsIdCol);
		tUnit.addValueColumn("name");
		tUnit.addValueColumn("failureRate");
		SQLColumnReference uniColRef = new SQLColumnReference("uni", "code");
		tUnit.addForeignKeyColumn("uniIdCode", uniColRef);
		
		SQLIdColumn uniIdCol = new SQLIdColumn("code");
		SQLTable tUni = new SQLTable(uniIdCol);
		tUni.addValueColumn("name");
		tUni.addValueColumn("isStateRun");
		
		db.addTable("uni", tUni);
		db.addTable("unit", tUnit);
		
		db.writeTable("uni");
		db.writeTable("unit");
		
		ResultSet rs = db.executeQuery("select TABLE_NAME from information_schema.tables where table_name = 'unit'");
		rs.next();
		String tableName = rs.getString(1);
		assertEquals("unit",tableName);
	}

}

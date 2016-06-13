package tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import solution.SQLDatabase;
import solution.SQLIdColumn;
import solution.SQLTable;

public class SQLDatabaseTests {
	
	private SQLDatabase db;
	
	@Before @Test
	public void setupDB() throws SQLException, IOException{
		 this.db = new SQLDatabase("jsonTestFromJava");
	}

	@Test
	public void addSomeTables() {
			
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
		ResultSet resultSet = db.executeSQL(sql);
		resultSet.next();
		int simpleSum = resultSet.getInt(1);
		assertEquals(2,simpleSum);
	}

}

package tests;

import static org.junit.Assert.*;

import org.junit.Test;

import solution.SQLColumnReference;
import solution.SQLIdColumn;
import solution.SQLTable;

public class SQLTableTests {

	@Test
	public void simplePrintSQLStatement() {
		SQLIdColumn unitsIdCol = new SQLIdColumn("idUnitCode");
		SQLTable units = new SQLTable(unitsIdCol);
		units.addValueColumn("name");
		units.addValueColumn("failureRate");
		SQLColumnReference uniColRef = new SQLColumnReference("uni", "idCode");
		units.addForeignKeyColumn("uniIdCode", uniColRef);
		
		String sSQL = units.tableSQL("jsonTest", "unit");
		
		String e = "";
		e+="CREATE TABLE `jsonTest`.`unit` (";
		e+="`idUnitCode` VARCHAR(1024) NOT NULL, ";
		e+="PRIMARY KEY (`idUnitCode`),";
		e+="`failureRate` VARCHAR(1024) NULL,";
		e+="`name` VARCHAR(1024) NULL,";
		e+="`uniIdCode` VARCHAR(1024) NOT NULL, ";
		e+="INDEX `uniIdCode_idx` (`uniIdCode` ASC),";
		e+="CONSTRAINT `uniIdCode_idCode` ";
		e+="FOREIGN KEY (`uniIdCode`) ";
		e+="REFERENCES `jsonTest`.`uni` (`idCode`) ";
		e+="ON DELETE NO ACTION ";
		e+="ON UPDATE NO ACTION";
		e+=");";
		assertEquals(e,sSQL);
	}

}

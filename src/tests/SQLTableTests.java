package tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import org.junit.Test;

import solution.JSONException;
import solution.JSONSingleVal;
import solution.SQLColumnReference;
import solution.SQLForeignKeyColumn;
import solution.SQLIdColumn;
import solution.SQLObjException;
import solution.SQLSimpleValColumn;
import solution.SQLTable;

public class SQLTableTests {

	@Test
	public void simplePrintSQLStatement() {
		SQLTable units = new SQLTable("unit");
		units.addValueColumn("name");
		units.addValueColumn("code");
		units.addValueColumn("failureRate");
		SQLColumnReference uniColRef = new SQLColumnReference("uni", "idCode");
		units.addForeignKeyColumn("uniIdCode", uniColRef);
		
		String sSQL = units.tableSQL("jsonTest", "unit");
		
		String e = "";
		e+="CREATE TABLE `jsonTest`.`unit` (";
		e+="`id` INTEGER NOT NULL, ";
		e+="PRIMARY KEY (`id`),";
		e+="`code` VARCHAR(1024) NULL,";
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

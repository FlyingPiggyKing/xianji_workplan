package com.liming.workplan.utils;

import static com.liming.workplan.utils.Constants.BK;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class DataConvertTool {
	private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static List<String> convertObjectsToString(Object[] rowArr) {
		
		String strValue;
		List<String> rowList = new ArrayList<String>();
		for(Object item : rowArr) {
			strValue = convertObjectToString(item);
			rowList.add(strValue);
		}
		
		return rowList;
	}
	
	public static String convertObjectToString(Object item) {
		String strValue;
		if(item instanceof Timestamp) {
			Timestamp timeItem = (Timestamp)item;
			strValue = DF.format(timeItem);
		} else if(item == null) {
			strValue = BK;
		} else if(item instanceof BigDecimal) {
			BigDecimal numberItem = (BigDecimal)item;
			strValue = numberItem.toString();
		} else if(item instanceof Double) {
			Double doubleItem = (Double)item;
			strValue = doubleItem.toString();
		} else if(item instanceof Integer) {
			Integer intItem = (Integer)item;
			strValue = intItem.toString();
		} else if(item instanceof Double) {
			Double doubleItem = (Double)item;
			strValue = doubleItem.toString();
		} else {
			strValue = (String)item;
		}
		return strValue;
	}

}

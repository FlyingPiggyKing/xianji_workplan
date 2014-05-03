package com.liming.workplan.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;

public class JsonTool {
	private JsonTool() {}
	
	public static JSONObject convertListConfToJson(List<Map<String, Object>> rowConfig) {
		JSONArray rowConfArrJson = JSONFactoryUtil.createJSONArray();
		for(Map<String, Object> cellConfig : rowConfig) {
			Iterator<String> cellPropertyIter = cellConfig.keySet().iterator();
			String cellPropertyKey = null;
			JSONObject cellConfJson = JSONFactoryUtil.createJSONObject();
			while(cellPropertyIter.hasNext()) {
				cellPropertyKey = cellPropertyIter.next();
				Object cellPropertyValue = cellConfig.get(cellPropertyKey);
				if(cellPropertyValue instanceof List) {
					JSONArray cellPropValListJson = JSONFactoryUtil.createJSONArray();
					List<String> cellPropValList = (List<String>)cellPropertyValue;
					for(String proValue : cellPropValList) {
						cellPropValListJson.put(proValue);
					}
					cellConfJson.put(cellPropertyKey, cellPropValListJson);
				} else {
					cellConfJson.put(cellPropertyKey, (String)cellPropertyValue);
				}
			}
			rowConfArrJson.put(cellConfJson);
		}
		JSONObject containerJson = JSONFactoryUtil.createJSONObject();
		containerJson.put("rowConfigJson", rowConfArrJson);
		return containerJson;
	}
	
	public static JSONObject convertMapStringToJson(List<Map<String, String>> result) {
		JSONArray rowsJson = JSONFactoryUtil.createJSONArray();
		for(Map<String, String> row : result) {
			JSONObject rowJson = JSONFactoryUtil.createJSONObject();
			Set<String> keys = row.keySet();		
			for(String key : keys) {
				String value = row.get(key);
				rowJson.put(key, value);
			}
			rowsJson.put(rowJson);
		}
		JSONObject containerJson = JSONFactoryUtil.createJSONObject();
		containerJson.put("data", rowsJson);
		return containerJson;
	}
	
	public static JSONArray convertMapObjectToJson(List<Map<String, Object>> result) {
		JSONArray rowsJson = JSONFactoryUtil.createJSONArray();
		for(Map<String, Object> row : result) {
			JSONObject rowJson = JSONFactoryUtil.createJSONObject();
			Set<String> keys = row.keySet();		
			for(String key : keys) {
				Object value = row.get(key);
				if(value instanceof Boolean) {
					rowJson.put(key, (Boolean)value);
				} else {
					rowJson.put(key, (String)value);
				}
				
			}
			rowsJson.put(rowJson);
		}
//		JSONObject containerJson = JSONFactoryUtil.createJSONObject();
//		containerJson.put("data", rowsJson);
		return rowsJson;
	}
	
//	public static JSONArray convertTableHederToJson(List<String[]> columns) {
//		JSONArray columnsJson = JSONFactoryUtil.createJSONArray();
//		for(String[] column : columns) {
//			JSONObject rowJson = JSONFactoryUtil.createJSONObject();
//			rowJson.put("key", column[0]);
//			rowJson.put("label", column[1]);
//			columnsJson.put(rowJson);
//		}
//		return columnsJson;
//	}
	
	public static JSONObject convertNumberToJson(String name, Long number) {
		JSONObject count = JSONFactoryUtil.createJSONObject();
		count.put(name, number);
		return count;
	}
	
	public static JSONObject convertStringToJson(String name, String value) {
		JSONObject count = JSONFactoryUtil.createJSONObject();
		count.put(name, value);
		return count;
	}
	
	public static JSONObject convertStatMapToJson(Map<String, String> map) {
		JSONObject objJson = JSONFactoryUtil.createJSONObject();
		Set<String> keys = map.keySet();
		for(String key : keys) {
			objJson.put(key, map.get(key));
		}
		JSONObject containerJson = JSONFactoryUtil.createJSONObject();
		containerJson.put("data", objJson);
		return containerJson;
	}
}

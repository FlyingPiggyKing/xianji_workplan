package com.liming.workplan.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkplanDataThreadLocal {
	private static ThreadLocal<Map<String, Object>> dataMapThreadLocal = new ThreadLocal<Map<String, Object>>();
	public static List<Map<String, String>> getDisplayData(String nodeType) {
		List<Map<String, String>> displayData = (List<Map<String, String>>)dataMapThreadLocal.get().get(nodeType);
		return displayData;
	}
	
	public static void setDisplayData(String nodeType, List<Map<String, String>> data) {
		Map<String, Object> container = (Map<String, Object>)dataMapThreadLocal.get();
		if(container == null) {
			container = new HashMap<String, Object>();
			dataMapThreadLocal.set(container);
		}
		container.put(nodeType, data);
	}
}

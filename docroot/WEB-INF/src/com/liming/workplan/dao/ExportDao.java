package com.liming.workplan.dao;

import java.util.List;
import java.util.Map;

import org.hibernate.ScrollableResults;

public interface ExportDao {
	public ScrollableResults getPublishedResearchPorjectsScroll(Map<String, Object> searchObj);
	public int getBatchResultByScrolling(ScrollableResults scrollResult, Integer beginIndex, List<Object[]> batchResult);
}

package com.liming.workplan.service;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.liming.workplan.model.pojo.ResearchAchievement;

public interface ResearchAchievementService {
	public void addResearchAchievement(Map<String, Object> paramMap, List<Map<String, Object>> fileParams);
	public List<Map<String, Object>> getRowConfiguration();
	public List<Map<String, Object>> getSearchConfiguration();
	public List<Map<String, String>> loadPublishedResearchAchievement(String[] searchParams, int pageNumber, int pageSize, String sortColumn, String order);
	public List<Map<String, String>> loadUnPublishedResearchAchievement(int pageNumber, int pageSize, String sortColumn, String order);
	public List<Map<String, Object>> getPublishedTableHeader();
	public List<Map<String, Object>> getUnPublishedTableHeader();
	public void fillDisplayTable(ResearchAchievement node, Map<String, String> row);
	public void updateNodes(List<ResearchAchievement> nodes);
	public void deleteResearchAchievements(String[] ids);
	public Long getPublishedCount(String[] searchParams);
	public Long getUnPublishedCount();
	public void exportResult(String[] searchParams, OutputStream os);
}

package com.liming.workplan.service;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.liming.workplan.model.pojo.ResearchProject;

public interface ResearchProjectService {
	public void addResearchProject(Map<String, Object> paramMap, List<Map<String, Object>> fileParams);
	public List<Map<String, Object>> getRowConfiguration();
	public List<Map<String, Object>> getSearchConfiguration();
	public List<Map<String, String>> loadPublishedResearchProject(String[] searchParams, int pageNumber, int pageSize, String sortColumn, String order);
	public List<Map<String, String>> loadUnPublishedResearchProject(int pageNumber, int pageSize, String sortColumn, String order);
	public List<String[]> getPublishedTableHeader();
	public List<String[]> getUnPublishedTableHeader();
	public void fillDisplayTable(ResearchProject node, Map<String, String> row);
	public void updateNodes(List<ResearchProject> nodes);
	public void deleteResearchProjects(String[] ids);
	public Long getPublishedCount(String[] searchParams);
	public Long getUnPublishedCount();
	public Map<String, String> getStatistics(String[] searchParams);
	public List<String[]> getStatisticsHeader();
	public void exportResult(String[] searchParams, OutputStream os);
}

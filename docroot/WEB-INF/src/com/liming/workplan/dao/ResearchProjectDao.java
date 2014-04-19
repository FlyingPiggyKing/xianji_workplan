package com.liming.workplan.dao;

import java.util.List;
import java.util.Map;

import org.hibernate.ScrollableResults;

import com.liming.workplan.model.pojo.ResearchProject;

public interface ResearchProjectDao extends ExportDao {
	public ResearchProject create();
	public void persist(ResearchProject transientInstance);
	public List<ResearchProject> getPublishedResearchPorjects(Map<String, Object> searchObj, int pageNumber, int pageSize, String sortColumn, String order);
	public List<ResearchProject> getUnPublishedResearchPorjects(long userId, int pageNumber, int pageSize, String sortColumn, String order);
	public void updateNodes(List<ResearchProject> nodes);
	public void deleteResearchProjects(String[] ids, long userId);
	public Long getPublishedCount(Map<String, Object> searchObj);
	public Long getUnPublishedCount(long userId);
	public Map<String, Object> getStatistics(Map<String, Object> searchObj);
}

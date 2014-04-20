package com.liming.workplan.dao;

import java.util.List;
import java.util.Map;

import com.liming.workplan.model.pojo.ResearchAchievement;

public interface ResearchAchievementDao extends ExportDao {
	public ResearchAchievement create();
	public void persist(Object transientInstance);
	public List<Object> getPublishedNodes(String type, Map<String, Object> searchObj, int pageNumber, int pageSize, String sortColumn, String order);
	public List<Object> getUnPublishedNodes(String type, long userId, int pageNumber, int pageSize, String sortColumn, String order);
	public void updateNodes(List<ResearchAchievement> nodes);
	public void deleteNodes(String type, String[] ids, long userId);
	public Long getPublishedCount(String type, Map<String, Object> searchObj);
	public Long getUnPublishedCount(String type, long userId);
}

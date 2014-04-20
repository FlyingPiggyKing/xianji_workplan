package com.liming.workplan.service.impl;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.liferay.portal.model.User;
import com.liming.workplan.dao.ExportDao;
import com.liming.workplan.dao.ResearchAchievementDao;
import com.liming.workplan.model.pojo.ResearchAchievement;
import com.liming.workplan.service.ResearchAchievementService;
import com.liming.workplan.utils.Constants;
import com.liming.workplan.utils.UserThreadLocal;

public class ResearchAchievementServiceImpl extends WorkPlanNodeBaseServiceImpl
		implements ResearchAchievementService {

	private final String TARGET_TYPE = "ResearchAchievement";
	private final String FORMAT_DOUBLE = "#.00000";
	private enum TableColumn {
		NODEID("nodeId"),
		TYPE("type"),
		SUB_TYPE("subType"),
		ACHIEVEMENT_NAME("achievementName"),
		ACHIEVEMENT_AUTHOR("achievementAuthor"),
		PUBLISH_CHANNEL("publishChannel"),
		PUBLISH_DETAIL("publishDetail"),
		LANGUAGE("language"),
		REMARK("remark");
		
		private final String value;
        private TableColumn(String value) {
            this.value = value;
        }
        public String value() {
        	return value;
        }
	}
	
	
	private ResearchAchievementDao researchAchievementDao;	

	@Override
	public void addResearchAchievement(String[] itemArray) {
		for(String item : itemArray) {
			String[] values = item.split(Constants.VALUE_SEP);//
			Map<String, Object> valueObject = convertStringToObj(values);
			ResearchAchievement researchAchievement = researchAchievementDao.create();
			initWorkPlanNode(researchAchievement, valueObject);
			fillValues(researchAchievement, "setter", valueObject);
			
			researchAchievementDao.persist(researchAchievement);
		}
	}
	
	
	protected String getNodeType() {
		return TARGET_TYPE;
	}
	
	public List<Map<String, String>> loadPublishedResearchAchievement(String[] params, int pageNumber, int pageSize, String sortColumn, String order) {
		Map<String, Object> searchObject = convertStringToObj(params);
		List<Object> researchAchievements = researchAchievementDao.getPublishedNodes(TARGET_TYPE, searchObject, pageNumber, pageSize, sortColumn, order);
		List<Map<String, String>> result = new ArrayList<Map<String, String>>(researchAchievements.size());
		for(Object node : researchAchievements) {
			Map<String, String> row = new HashMap<String, String>();
			fillDisplayTable((ResearchAchievement)node, row);
			result.add(row);
		}
		return result;
	}

	public void fillDisplayTable(ResearchAchievement node, Map<String, String> row) {
		super.fillDisplayTable(node, row);
		row.put(TableColumn.NODEID.value(), Integer.toString(node.getNodeId()));
		row.put(TableColumn.TYPE.value(), node.getType());
		row.put(TableColumn.SUB_TYPE.value(), node.getSubType());
		row.put(TableColumn.ACHIEVEMENT_NAME.value(), node.getAchievementName());
		row.put(TableColumn.ACHIEVEMENT_AUTHOR.value(), node.getAchievementAuthor());
		row.put(TableColumn.PUBLISH_CHANNEL.value(), node.getPublishChannel());
		row.put(TableColumn.PUBLISH_DETAIL.value(), node.getPublishDetail());
		row.put(TableColumn.LANGUAGE.value(), node.getLanguage());
		row.put(TableColumn.REMARK.value(), node.getRemark());
	}
	
	public List<Map<String, String>> loadUnPublishedResearchAchievement(int pageNumber, int pageSize, String sortColumn, String order) {
		User currentUser = UserThreadLocal.getCurrentUser();
		List<Object> researchAchievements = researchAchievementDao.getUnPublishedNodes(TARGET_TYPE, currentUser.getUserId(), pageNumber, pageSize, sortColumn, order);
		List<Map<String, String>> result = new ArrayList<Map<String, String>>(researchAchievements.size());
		for(Object node : researchAchievements) {
			Map<String, String> row = new HashMap<String, String>();
			row.put(UnPublishColumn.STATUS.value(), ((ResearchAchievement)node).getStatus());
			fillDisplayTable((ResearchAchievement)node, row);
			result.add(row);
		}
		return result;
	}
	
	protected List<String> getTableHeader() {
		List<String> columnValues = new ArrayList<String>();
		
		for(TableColumn column : TableColumn.values()) {
			columnValues.add(column.value());
		}
		return columnValues;
	}
	


	@Override
	public void updateNodes(List<ResearchAchievement> nodes) {
		researchAchievementDao.updateNodes(nodes);
		
	}

	@Override
	public void deleteResearchAchievements(String[] ids) {
		User currentUser = UserThreadLocal.getCurrentUser();
		researchAchievementDao.deleteNodes(TARGET_TYPE, ids, currentUser.getUserId());
	}

	@Override
	public Long getPublishedCount(String[] searchParams) {
		Map<String, Object> searchObject = convertStringToObj(searchParams);
		return researchAchievementDao.getPublishedCount(TARGET_TYPE, searchObject);
	}

	@Override
	public Long getUnPublishedCount() {
		User currentUser = UserThreadLocal.getCurrentUser();
		return researchAchievementDao.getUnPublishedCount(TARGET_TYPE, currentUser.getUserId());
	}

	
	/* 
	 * public download interface.
	 */
	public void exportResult(String[] searchParams, OutputStream os) {
		ExportDao exportDao = (ExportDao)researchAchievementDao;
		List<String> exportHeader = getExportTableHeader();
		Map<String, Object> searchObject = convertStringToObj(searchParams);
		
		super.exportResult(TARGET_TYPE, exportHeader, searchObject, os, exportDao);
	}
	
	
	/* 
	 * used for export the download value.
	 */
	public Object[] convertPojoToObject(Object[] pojos) {
		Object[] objectValues = new Object[12];
		int index = 0;
		
		ResearchAchievement researchAchievement = (ResearchAchievement)pojos[0];
		objectValues[index++] = researchAchievement.getNodeId();
		objectValues[index++] = researchAchievement.getType();
		objectValues[index++] = researchAchievement.getSubType();
		objectValues[index++] = researchAchievement.getAchievementName();
		objectValues[index++] = researchAchievement.getAchievementAuthor();
		objectValues[index++] = researchAchievement.getPublishChannel();
		objectValues[index++] = researchAchievement.getPublishDetail();
		objectValues[index++] = researchAchievement.getLanguage();
		objectValues[index++] = researchAchievement.getRemark();

		Object[] baseObjects = super.convertPojoToObject(pojos);
		System.arraycopy(baseObjects, 0, objectValues, index, baseObjects.length);
		return objectValues;
	}

	public ResearchAchievementDao getResearchAchievementDao() {
		return researchAchievementDao;
	}

	public void setResearchAchievementDao(ResearchAchievementDao researchAchievementDao) {
		this.researchAchievementDao = researchAchievementDao;
	}

}

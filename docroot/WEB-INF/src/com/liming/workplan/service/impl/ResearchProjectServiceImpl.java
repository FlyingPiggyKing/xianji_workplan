package com.liming.workplan.service.impl;

import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.liferay.portal.model.User;
import com.liming.workplan.dao.ExportDao;
import com.liming.workplan.dao.ResearchProjectDao;
import com.liming.workplan.model.pojo.ResearchProject;
import com.liming.workplan.service.ResearchProjectService;
import com.liming.workplan.utils.Constants;
import com.liming.workplan.utils.UserThreadLocal;

public class ResearchProjectServiceImpl extends WorkPlanNodeBaseServiceImpl implements ResearchProjectService {

	private final String TARGET_TYPE = "ResearchProject";
	private final String FORMAT_DOUBLE = "#.00000";
	private enum TableColumn {
		NODEID("nodeId", true),
		TYPE("type", true),
		PROJECT_TYPE("projectType", true),
		PROJECT_NAME("projectName", true),
		SUPPORT_UNIT("supportUnit", true),
		PROJECT_LEVEL("projectLevel", true),
		CHARGER("charger", true),
		ASSISTANT("assistant", true),
		PROJECT_FUNDING("projectFunding", true),
		DELEGATED_DEPARTMENT("delegatedDepartment", true);
		
		private final String value;
		private final boolean isSortable;
        private TableColumn(String value, boolean isSortable) {
            this.value = value;
            this.isSortable = isSortable;
        }
        public String value() {
        	return value;
        }
	}
	
	private enum StatisticsColumn {
		PROJECT_FOUNDING("projectFunding");
		
		private final String value;
        private StatisticsColumn(String value) {
            this.value = value;
        }
        public String value() {
        	return value;
        }
	}
	
	private ResearchProjectDao researchProjectDao;	

	@Override
	public void addResearchProject(Map<String, Object> paramMap, List<Map<String, Object>> fileParams) {
//		for(String item : itemArray) {
//			String[] values = item.split(Constants.VALUE_SEP);//
//			Map<String, Object> valueObject = convertStringToObj(values);
			ResearchProject researchProject = researchProjectDao.create();
			initWorkPlanNode(researchProject, fileParams);
			fillValues(researchProject, "setter", paramMap);
			
			researchProjectDao.persist(researchProject);
//		}
	}
	
	
	protected String getNodeType() {
		return TARGET_TYPE;
	}
	
	public List<Map<String, String>> loadPublishedResearchProject(String[] params, int pageNumber, int pageSize, String sortColumn, String order) {
		Map<String, Object> searchObject = convertStringToObj(params);
		List<Object> researchProjects = researchProjectDao.getPublishedNodes(TARGET_TYPE, searchObject, pageNumber, pageSize, sortColumn, order);
		List<Map<String, String>> result = new ArrayList<Map<String, String>>(researchProjects.size());
		for(Object node : researchProjects) {
			Map<String, String> row = new HashMap<String, String>();
			fillDisplayTable((ResearchProject)node, row);
			result.add(row);
		}
		return result;
	}

	public void fillDisplayTable(ResearchProject node, Map<String, String> row) {
		super.fillDisplayTable(node, row);
		row.put(TableColumn.NODEID.value(), Integer.toString(node.getNodeId()));
		row.put(TableColumn.TYPE.value(), node.getType());
		row.put(TableColumn.PROJECT_TYPE.value(), node.getProjectType());
		row.put(TableColumn.PROJECT_NAME.value(), node.getProjectName());
		row.put(TableColumn.SUPPORT_UNIT.value(), node.getSupportUnit());
		row.put(TableColumn.PROJECT_LEVEL.value(), node.getProjectLevel());
		row.put(TableColumn.CHARGER.value(), node.getCharger());
		row.put(TableColumn.ASSISTANT.value(), node.getAssistant());
		row.put(TableColumn.PROJECT_FUNDING.value(), Double.toString(node.getProjectFunding()));
		row.put(TableColumn.DELEGATED_DEPARTMENT.value(), node.getDelegatedDepartment());
	}
	
	public List<Map<String, String>> loadUnPublishedResearchProject(int pageNumber, int pageSize, String sortColumn, String order) {
		User currentUser = UserThreadLocal.getCurrentUser();
		List<Object> researchProjects = researchProjectDao.getUnPublishedNodes(TARGET_TYPE, currentUser.getUserId(), pageNumber, pageSize, sortColumn, order);
		List<Map<String, String>> result = new ArrayList<Map<String, String>>(researchProjects.size());
		for(Object node : researchProjects) {
			Map<String, String> row = new HashMap<String, String>();
			row.put(UnPublishColumn.STATUS.value(), ((ResearchProject)node).getStatus());
			fillDisplayTable((ResearchProject)node, row);
			result.add(row);
		}
		return result;
	}
	
	protected List<String> getTableHeaderString() {
		List<String> columnValues = new ArrayList<String>();
		
		for(TableColumn column : TableColumn.values()) {
			columnValues.add(column.value());
		}
		return columnValues;
	}
	
	protected List<Boolean> getTableHeaderSorting() {
		List<Boolean> columnValues = new ArrayList<Boolean>();
		
		for(TableColumn column : TableColumn.values()) {
			columnValues.add(column.isSortable);
		}
		return columnValues;
	}

	@Override
	public void updateNodes(List<ResearchProject> nodes) {
		researchProjectDao.updateNodes(nodes);
		
	}

	@Override
	public void deleteResearchProjects(String[] ids) {
		User currentUser = UserThreadLocal.getCurrentUser();
		researchProjectDao.deleteNodes(TARGET_TYPE, ids, currentUser.getUserId());
	}

	@Override
	public Long getPublishedCount(String[] searchParams) {
		Map<String, Object> searchObject = convertStringToObj(searchParams);
		return researchProjectDao.getPublishedCount(TARGET_TYPE, searchObject);
	}

	@Override
	public Long getUnPublishedCount() {
		User currentUser = UserThreadLocal.getCurrentUser();
		return researchProjectDao.getUnPublishedCount(TARGET_TYPE, currentUser.getUserId());
	}

	@Override
	public Map<String, String> getStatistics(String[] searchParams) {
		Map<String, Object> searchObject = convertStringToObj(searchParams);
		Map<String, Object> statistics = researchProjectDao.getStatistics(searchObject);
		Double statDouble = (Double)statistics.get(StatisticsColumn.PROJECT_FOUNDING.value());
		DecimalFormat format = new DecimalFormat(FORMAT_DOUBLE);
		Map<String, String> statDisplay = new HashMap<String, String>();
		statDisplay.put(StatisticsColumn.PROJECT_FOUNDING.value(), format.format(statDouble));
		return statDisplay;
	}

	@Override
	public List<Map<String, Object>> getStatisticsHeader() {
		List<Map<String, Object>> columnValues = new ArrayList<Map<String, Object>>(1);
//		columnValues.add(new String[]{StatisticsColumn.PROJECT_FOUNDING.value(), getLanguageService().getMessage(StatisticsColumn.PROJECT_FOUNDING.value())});
		Map<String, Object> header = new HashMap<String, Object>(2);
		header.put("key", StatisticsColumn.PROJECT_FOUNDING.value());
		header.put("label", getLanguageService().getMessage(StatisticsColumn.PROJECT_FOUNDING.value()));
		columnValues.add(header);
		return columnValues;
	}
	
	/* 
	 * public download interface.
	 */
	public void exportResult(String[] searchParams, OutputStream os) {
		ExportDao exportDao = (ExportDao)researchProjectDao;
		List<String> exportHeader = getExportTableHeader();
		Map<String, Object> searchObject = convertStringToObj(searchParams);
		
		super.exportResult(TARGET_TYPE, exportHeader, searchObject, os, exportDao);
	}
	
	
	/* 
	 * used for export the download value.
	 */
	public Object[] convertPojoToObject(Object pojos) {
		Object[] objectValues = new Object[12];
		int index = 0;
		
		ResearchProject researchProject = (ResearchProject)pojos;
		objectValues[index++] = researchProject.getNodeId();
		objectValues[index++] = researchProject.getType();
		objectValues[index++] = researchProject.getProjectType();
		objectValues[index++] = researchProject.getProjectName();
		objectValues[index++] = researchProject.getSupportUnit();
		objectValues[index++] = researchProject.getProjectLevel();
		objectValues[index++] = researchProject.getCharger();
		objectValues[index++] = researchProject.getAssistant();
		objectValues[index++] = researchProject.getProjectFunding();
		objectValues[index++] = researchProject.getDelegatedDepartment();

		Object[] baseObjects = super.convertPojoToObject(pojos);
		System.arraycopy(baseObjects, 0, objectValues, index, baseObjects.length);
		return objectValues;
	}

	public ResearchProjectDao getResearchProjectDao() {
		return researchProjectDao;
	}

	public void setResearchProjectDao(ResearchProjectDao researchProjectDao) {
		this.researchProjectDao = researchProjectDao;
	}
}

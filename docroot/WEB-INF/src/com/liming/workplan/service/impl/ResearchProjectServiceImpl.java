package com.liming.workplan.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.model.User;
import com.liming.workplan.dao.ResearchProjectDao;
import com.liming.workplan.model.pojo.ResearchProject;
import com.liming.workplan.service.ResearchProjectService;
import com.liming.workplan.utils.Constants;
import com.liming.workplan.utils.UserThreadLocal;

public class ResearchProjectServiceImpl extends WorkPlanNodeBaseServiceImpl implements ResearchProjectService {

	private enum TableColumn {
		NODEID("nodeId"),
		TYPE("type"),
		PROJECT_TYPE("projectType"),
		PROJECT_NAME("projectName"),
		SUPPORT_UNIT("supportUnit"),
		PROJECT_LEVEL("projectLevel"),
		CHARGER("charger"),
		ASSISTANT("assistant"),
		PROJECT_FUNDING("projectFunding"),
		DELEGATED_DEPARTMENT("delegatedDepartment");
//		ATTACHMENT_NAME(), !!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//		ATTACHMENT_URL();
		
		private final String value;
        private TableColumn(String value) {
            this.value = value;
        }
        public String value() {
        	return value;
        }
	}
	
	private enum UnPublishColumn {
		STATUS("status");
		
		private final String value;
        private UnPublishColumn(String value) {
            this.value = value;
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
	
	public ResearchProjectDao getResearchProjectDao() {
		return researchProjectDao;
	}

	public void setResearchProjectDao(ResearchProjectDao researchProjectDao) {
		this.researchProjectDao = researchProjectDao;
	}

	@Override
	public void addResearchProject(String[] itemArray) {
		for(String item : itemArray) {
			String[] values = item.split(Constants.ResearchProjectServiceImpl_SEP);//
			Map<String, Object> valueObject = convertStringToObj(values);
			ResearchProject researchProject = researchProjectDao.create();
			initWorkPlanNode(researchProject, valueObject);
			fillValues(researchProject, "setter", valueObject);
			
			researchProjectDao.persist(researchProject);
		}
	}
	
	
	protected String getNodeType() {
		return "ResearchProject";
	}
	
	public List<Map<String, String>> loadPublishedResearchProject(String[] params, int pageNumber, int pageSize, String sortColumn, String order) {
		Map<String, Object> searchObject = convertStringToObj(params);
		List<ResearchProject> researchProjects = researchProjectDao.getPublishedResearchPorjects(searchObject, pageNumber, pageSize, sortColumn, order);
		List<Map<String, String>> result = new ArrayList<Map<String, String>>(researchProjects.size());
		for(ResearchProject node : researchProjects) {
			Map<String, String> row = new HashMap<String, String>();
			fillDisplayTable(node, row);
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
		row.put(TableColumn.PROJECT_FUNDING.value(), Float.toString(node.getProjectFunding()));
		row.put(TableColumn.DELEGATED_DEPARTMENT.value(), node.getDelegatedDepartment());
	}
	
	public List<Map<String, String>> loadUnPublishedResearchProject(int pageNumber, int pageSize, String sortColumn, String order) {
		User currentUser = UserThreadLocal.getCurrentUser();
		List<ResearchProject> researchProjects = researchProjectDao.getUnPublishedResearchPorjects(currentUser.getUserId(), pageNumber, pageSize, sortColumn, order);
		List<Map<String, String>> result = new ArrayList<Map<String, String>>(researchProjects.size());
		for(ResearchProject node : researchProjects) {
			Map<String, String> row = new HashMap<String, String>();
			row.put(UnPublishColumn.STATUS.value(), node.getStatus());
			fillDisplayTable(node, row);
			result.add(row);
		}
		return result;
	}
	
	public List<String[]> getPublishedTableHeader() {
		List<String[]> columnValues = new ArrayList<String[]>();
		User currentUser = UserThreadLocal.getCurrentUser();
		Locale userLocale = currentUser.getLocale();
		
		for(TableColumn column : TableColumn.values()) {
			String[] columnValue = new String[]{column.value(), getLanguageService().getMessage(column.value(), userLocale)};
			columnValues.add(columnValue);
		}
		columnValues.addAll(super.getTableHeader());
		return columnValues;
	}
	
	public List<String[]> getUnPublishedTableHeader() {
//		Map<String, String> columnValues = new HashMap(TableColumn.values().length + UnPublishColumn.values().length);
		List<String[]> columnValues = new ArrayList<String[]>();
		User currentUser = UserThreadLocal.getCurrentUser();
		Locale userLocale = currentUser.getLocale();
		columnValues.add(new String[]{UnPublishColumn.STATUS.value(), getLanguageService().getMessage(UnPublishColumn.STATUS.value(), userLocale)});
		for(TableColumn column : TableColumn.values()) {
			columnValues.add(new String[]{column.value(), getLanguageService().getMessage(column.value(), userLocale)});
		}
		columnValues.addAll(super.getTableHeader());
		return columnValues;
	}

	@Override
	public void updateNodes(List<ResearchProject> nodes) {
		researchProjectDao.updateNodes(nodes);
		
	}

	@Override
	public void deleteResearchProjects(String[] ids) {
		User currentUser = UserThreadLocal.getCurrentUser();
		researchProjectDao.deleteResearchProjects(ids, currentUser.getUserId());
	}

	@Override
	public Long getPublishedCount(String[] searchParams) {
		Map<String, Object> searchObject = convertStringToObj(searchParams);
		return researchProjectDao.getPublishedCount(searchObject);
	}

	@Override
	public Long getUnPublishedCount() {
		User currentUser = UserThreadLocal.getCurrentUser();
		return researchProjectDao.getUnPublishedCount(currentUser.getUserId());
	}

	@Override
	public Map<String, String> getStatistics(String[] searchParams) {
		Map<String, Object> searchObject = convertStringToObj(searchParams);
		return researchProjectDao.getStatistics(searchObject);
	}

	@Override
	public List<String[]> getStatisticsHeader() {
		List<String[]> columnValues = new ArrayList<String[]>();
		User currentUser = UserThreadLocal.getCurrentUser();
		Locale userLocale = currentUser.getLocale();
		
		for(StatisticsColumn column : StatisticsColumn.values()) {
			String[] columnValue = new String[]{column.value(), getLanguageService().getMessage(column.value(), userLocale)};
			columnValues.add(columnValue);
		}

		return columnValues;
	}

}

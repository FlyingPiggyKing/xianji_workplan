package com.liming.workplan.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.hibernate.ScrollableResults;

import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.model.User;
import com.liming.workplan.dao.ExportDao;
import com.liming.workplan.dao.ResearchProjectDao;
import com.liming.workplan.model.pojo.ResearchProject;
import com.liming.workplan.service.LanguageService;
import com.liming.workplan.service.ResearchProjectService;
import com.liming.workplan.utils.Constants;
import com.liming.workplan.utils.UserThreadLocal;

public class ResearchProjectServiceImpl extends WorkPlanNodeBaseServiceImpl implements ResearchProjectService {

	private final String TARGET_TYPE = "ResearchProject";
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
		List<Object> researchProjects = researchProjectDao.getPublishedResearchPorjects(TARGET_TYPE, searchObject, pageNumber, pageSize, sortColumn, order);
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
		row.put(TableColumn.PROJECT_FUNDING.value(), Float.toString(node.getProjectFunding()));
		row.put(TableColumn.DELEGATED_DEPARTMENT.value(), node.getDelegatedDepartment());
	}
	
	public List<Map<String, String>> loadUnPublishedResearchProject(int pageNumber, int pageSize, String sortColumn, String order) {
		User currentUser = UserThreadLocal.getCurrentUser();
		List<Object> researchProjects = researchProjectDao.getUnPublishedResearchPorjects(TARGET_TYPE, currentUser.getUserId(), pageNumber, pageSize, sortColumn, order);
		List<Map<String, String>> result = new ArrayList<Map<String, String>>(researchProjects.size());
		for(Object node : researchProjects) {
			Map<String, String> row = new HashMap<String, String>();
			row.put(UnPublishColumn.STATUS.value(), ((ResearchProject)node).getStatus());
			fillDisplayTable((ResearchProject)node, row);
			result.add(row);
		}
		return result;
	}
	
	public List<String[]> getPublishedTableHeader() {
		List<String[]> columnValues = new ArrayList<String[]>();
		User currentUser = UserThreadLocal.getCurrentUser();
		Locale userLocale = currentUser.getLocale();
		LanguageService service = getLanguageService();
		for(TableColumn column : TableColumn.values()) {
			String[] columnValue = new String[]{column.value(), service.getMessage(column.value(), userLocale)};
			columnValues.add(columnValue);
		}
		columnValues.addAll(super.getTableHeader());
		return columnValues;
	}
	
	public List<String> getExportTableHeader() {
		List<String> columnValues = new ArrayList<String>();
//		User currentUser = UserThreadLocal.getCurrentUser();
//		Locale userLocale = currentUser.getLocale();
		
		for(TableColumn column : TableColumn.values()) {
			columnValues.add(column.value());
		}
		columnValues.addAll(super.getExportHeader());
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
		Map<String, Object> statistics = researchProjectDao.getStatistics(searchObject);
		Double statDouble = (Double)statistics.get(StatisticsColumn.PROJECT_FOUNDING.value());
		DecimalFormat format = new DecimalFormat("#.00000");
		Map<String, String> statDisplay = new HashMap<String, String>();
		statDisplay.put(StatisticsColumn.PROJECT_FOUNDING.value(), format.format(statDouble));
		return statDisplay;
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
	
	public void exportResult(String[] searchParams, OutputStream os) {
		ExportDao exportDao = (ExportDao)researchProjectDao;
		List<String> exportHeader = getExportTableHeader();

		Map<String, Object> searchObject = convertStringToObj(searchParams);
		super.exportResult(exportHeader, searchObject, os, exportDao);
	}
	
	public Object[] convertPojoToObject(Object[] pojos) {
		Object[] objectValues = new Object[12];
		int index = 0;
		
		ResearchProject researchProject = (ResearchProject)pojos[0];
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


}

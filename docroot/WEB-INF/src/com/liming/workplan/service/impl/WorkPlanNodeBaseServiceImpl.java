package com.liming.workplan.service.impl;


import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.ScrollableResults;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.DateUtil;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liming.workplan.dao.ExportDao;
import com.liming.workplan.model.pojo.Attachment;
import com.liming.workplan.model.pojo.ResearchProject;
import com.liming.workplan.model.pojo.WorkPlanNode;
import com.liming.workplan.model.pojo.WorkflowNode;
import com.liming.workplan.service.LanguageService;
import com.liming.workplan.service.WorkflowService;
import com.liming.workplan.utils.Constants;
import com.liming.workplan.utils.DataConvertTool;
import com.liming.workplan.utils.UserThreadLocal;

public abstract class WorkPlanNodeBaseServiceImpl {
	private static final Log log = LogFactory.getLog(WorkPlanNodeBaseServiceImpl.class);
	private List<Map<String, Object>> rowConfiguration;
	private WorkflowService workflowService;
	private LanguageService languageService;
	private static WritableCellFormat WFC_FONT;
	private static final String COMMER = ",";
	private static final String SEPATOR = "~-~";
	
	private enum TableColumn {
		ATTACHMENT_NAME(Constants.Attachment_NAME, false), 
		ATTACHMENT_URL(Constants.Attachment_URL, false),
		TYPE_DESE(Constants.Attachment_DESC, false);
		
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
	
	protected enum UnPublishColumn {
		STATUS("status", true);
		
		private final String value;
		private final boolean isSortable;
        private UnPublishColumn(String value, boolean isSortable) {
            this.value = value;
            this.isSortable = isSortable;
        }
        public String value() {
        	return value;
        }
	}
	
	public WorkflowService getWorkflowService() {
		return workflowService;
	}
	
	/*
	 * Used for download result data converting.
	 */
	public Object[] convertPojoToObject(Object workplanNode) {
		Object[] objectValues = new Object[2];
		int index = 0;

		List<Attachment> attachs = (List<Attachment>)((WorkPlanNode)workplanNode).getAttachment();
		StringBuilder desc = new StringBuilder();
		StringBuilder name = new StringBuilder();
		for(int attachIndex = 0; attachIndex < attachs.size(); attachIndex++) {
			desc.append(attachs.get(attachIndex).getTypeDesc());
			name.append(attachs.get(attachIndex).getAttachmentName());
			if(attachIndex != attachs.size() - 1) {
				desc.append(COMMER);
				name.append(COMMER);
			}
		}
		objectValues[index++] = desc.toString();
		objectValues[index++] = name.toString();
		return objectValues;
	}
	
	/*
	 * Used for convert pojo to datalist, in order to display in the table of html page.
	 */
	public void fillDisplayTable(WorkPlanNode node, Map<String, String> row) {		
//		row.put(TableColumn.NODEID.value(), Integer.toString(node.getNodeId()));
		Set<Attachment> attachs = (Set<Attachment>)node.getAttachment();
		StringBuilder desc = new StringBuilder();
		StringBuilder name = new StringBuilder();
		StringBuilder url = new StringBuilder();

		Iterator<Attachment> iter = attachs.iterator();
		while(iter.hasNext()) {
			Attachment attach = iter.next();
			desc.append(attach.getTypeDesc());
			name.append(attach.getAttachmentName());
			url.append(attach.getAttachmentURL());
			if(iter.hasNext()) {
				desc.append(SEPATOR);
				name.append(SEPATOR);
				url.append(SEPATOR);
			}
		}
		row.put(TableColumn.ATTACHMENT_NAME.value(), name.toString());
		row.put(TableColumn.ATTACHMENT_URL.value(), url.toString());
		row.put(TableColumn.TYPE_DESE.value(), desc.toString());
	}
	
	public List<Map<String, Object>> getPublishedTableHeader() {
		List<String> tableHeaderString = getTableHeaderString();
		tableHeaderString.addAll(getBaseTableHeaderString());
		List<String> localTableHeaderString = languageService.getLocalTableHeader(tableHeaderString);
		
		List<Boolean> tableHeaderSort = getTableHeaderSorting();
		tableHeaderSort.addAll(getBaseTableHeaderSorting());
		
//		List<String[]> columnValues = new ArrayList<String[]>(tableHeader.size() + baseTableHeader.size());
		
//		columnValues.addAll(service.getLocalTableHeader(tableHeader));
//		columnValues.addAll(service.getLocalTableHeader(baseTableHeader));
		List<Map<String, Object>> tableHeader = generateTableHeader(
				tableHeaderString, localTableHeaderString, tableHeaderSort);

		return tableHeader;
	}

	private List<Map<String, Object>> generateTableHeader(
			List<String> tableHeaderString,
			List<String> localTableHeaderString, List<Boolean> tableHeaderSort) {
		List<Map<String, Object>> tableHeader = new ArrayList<Map<String, Object>>();
		for(int mapIndex = 0; mapIndex < tableHeaderString.size(); mapIndex++) {
			Map<String, Object> mapItem = new HashMap<String, Object>();
			mapItem.put("key", tableHeaderString.get(mapIndex));
			mapItem.put("label", localTableHeaderString.get(mapIndex));
			mapItem.put("remoteSortable", tableHeaderSort.get(mapIndex));
			tableHeader.add(mapItem);
		}
		return tableHeader;
	}
	
	public List<Map<String, Object>> getUnPublishedTableHeader() {
		List<String> tableHeaderString = new ArrayList<String>();
		tableHeaderString.add(UnPublishColumn.STATUS.value());
		tableHeaderString.addAll(getTableHeaderString());
		tableHeaderString.addAll(getBaseTableHeaderString());
		List<String> localTableHeaderString = languageService.getLocalTableHeader(tableHeaderString);
		
		List<Boolean> tableHeaderSort = new ArrayList<Boolean>();
		tableHeaderSort.add(UnPublishColumn.STATUS.isSortable);	
		tableHeaderSort.addAll(getTableHeaderSorting());
		tableHeaderSort.addAll(getBaseTableHeaderSorting());
		
		List<Map<String, Object>> tableHeader = generateTableHeader(
				tableHeaderString, localTableHeaderString, tableHeaderSort);

		return tableHeader;
	}
	
	public List<String> getExportTableHeader() {
		List<String> tableHeaderString = getTableHeaderString();
		tableHeaderString.addAll(getBaseTableHeaderString());
		List<String> localTableHeaderString = languageService.getLocalTableHeader(tableHeaderString);
		return localTableHeaderString;
	}
	
	protected abstract List<String> getTableHeaderString();
	protected abstract List<Boolean> getTableHeaderSorting();
	
	protected List<String> getBaseTableHeaderString() {
		List<String> columnValues = new ArrayList<String>();
		
		columnValues.add(TableColumn.TYPE_DESE.value());
		columnValues.add(TableColumn.ATTACHMENT_NAME.value());
		return columnValues;
	}
	
	protected List<Boolean> getBaseTableHeaderSorting() {
		List<Boolean> sortables = new ArrayList<Boolean>();
		
		sortables.add(TableColumn.TYPE_DESE.isSortable);
		sortables.add(TableColumn.ATTACHMENT_NAME.isSortable);
		return sortables;
	}
	
	

	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}
	
	protected abstract String getNodeType();
	
	protected Map<String, Object> convertStringToObj(String[] items) {
		if(items == null) {
			return new HashMap<String, Object>(1);
		}
		Map<String, Object> valueMap = new HashMap<String, Object>(items.length);
		for(String valueItem : items) {
			String[] valueSeg = valueItem.split("~-~");
			if(valueSeg.length >= 3) {
				if("select".equals(valueSeg[2]) || "text".equals(valueSeg[2])) {
					valueMap.put(valueSeg[0], valueSeg[1]);
				} else if("date".equals(valueSeg[2])) {

				} else if("double".equals(valueSeg[2])) {
					valueMap.put(valueSeg[0], Double.valueOf(valueSeg[1]));
				}
			}
		}
		return valueMap;
	}

	protected void initWorkPlanNode(WorkPlanNode node, List<Map<String, Object>> values) {
		User currentUser = UserThreadLocal.getCurrentUser();
		Date currentDate = DateUtil.newDate();
		node.setAuthor(currentUser.getUserId());
		node.setCreateDate(currentDate);
		node.setStatus(Constants.WorkPlanNode_STATUS_UNPUBLISH);
		//build attachment
		Set<Attachment> sets = new HashSet<Attachment>();
		for(Map<String, Object> valueItem : values) {
			Attachment attach = new Attachment();
			attach.setAttachmentId(((Long)valueItem.get(Constants.Attachment_ID)).toString());
			attach.setAttachmentName(valueItem.get(Constants.Attachment_NAME).toString());
			attach.setAttachmentURL(valueItem.get(Constants.Attachment_URL).toString());
			attach.setTypeDesc(valueItem.get(Constants.Attachment_DESC).toString());
			attach.setWorkplanNode(node);
			fillValues(attach, "super.setter", valueItem);
			sets.add(attach);
			
		}
		
		node.setAttachment(sets);
		//build workflow
		log.debug("build workflow");
		WorkflowNode workflow = new WorkflowNode();
		List<String> roleList = workflowService.getRoleOrderList();
		Role firstRole = null;
		try {
			firstRole = RoleLocalServiceUtil.getRole(currentUser.getCompanyId(), roleList.get(0));
		} catch (PortalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		workflow.setApprovingRoleId(firstRole.getRoleId());
		workflow.setWorkplanNodeId(node.getNodeId());
		workflow.setNodeType(getNodeType());
		workflow.setStatus(Constants.WorkflowNode_NODE_STATUS_ACTIVE);
		node.setWorkflowNode(workflow);
	}
	
	protected void fillValues(Object researchProject, String setterName, Map<String, Object> data) {
		Class targetClass = researchProject.getClass();
		for(Map cell: rowConfiguration) {
			String methodName = (String)cell.get(setterName);
			if(methodName == null) {
				continue;
			}
			String type = (String)cell.get("type");
			Class typeClass = null;
			if("select".equals(type) || "text".equals(type)) {
				typeClass = String.class;
			} else if("date".equals(type)) {
				typeClass = Date.class;
			} else if("double".equals(type)) {
				typeClass = double.class;
			} else if("file".equals(type)) {
				typeClass = String.class;
			}
			try {
				Method method = targetClass.getMethod(methodName, typeClass);
				try {
					if("double".equals(type)) {
						method.invoke(researchProject, ((Double)data.get(cell.get("field"))).doubleValue());
					} else {
						method.invoke(researchProject, data.get(cell.get("field")));
					}
					
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public List<Map<String, Object>> getRowConfiguration() {
		return addLocalNameConfig(null);
	}

	public void setRowConfiguration(List<Map<String, Object>> rowConfiguration) {
		this.rowConfiguration = rowConfiguration;
	}
	

	public List<Map<String, Object>> getSearchConfiguration() {
		return addLocalNameConfig("searchable");
	}

	private List<Map<String, Object>> addLocalNameConfig(String filter) {
		List<Map<String, Object>> searchConfiguration = new ArrayList<Map<String, Object>>(rowConfiguration.size());
		User currentUser = UserThreadLocal.getCurrentUser();
		Locale userLocale = currentUser.getLocale();
		
		if(filter != null) {
			for(Map<String, Object> item : rowConfiguration) {
				if("true".equals(item.get(filter))) {
					item.put("name", languageService.getMessage((String)item.get("field"), userLocale));
					searchConfiguration.add(item);
				} 
			}
		} else {
			for(Map<String, Object> item : rowConfiguration) {
				item.put("name", languageService.getMessage((String)item.get("field"), userLocale));
				searchConfiguration.add(item);
			}
		}
		return searchConfiguration;
	}

	public LanguageService getLanguageService() {
		return languageService;
	}

	public void setLanguageService(LanguageService languageService) {
		this.languageService = languageService;
	}
	
	
	public void exportResult(String type, List<String> columns, Map<String, Object> searchOb, OutputStream os, ExportDao exportDao) {
		User currentUser = UserThreadLocal.getCurrentUser();
		Locale userLocale = currentUser.getLocale();
		WritableWorkbook wwb = null;
		try {
			
			WorkbookSettings workBookSetting = new WorkbookSettings();
			//this setting is used to avoid a huge data download to exhaust the memory, 
			//although it will sacrifice some performance
			workBookSetting.setUseTemporaryFileDuringWrite(true);
			wwb = Workbook.createWorkbook(os, workBookSetting);
			WritableSheet ws = wwb.createSheet("Result", 0);
			ws.getSettings().setDefaultColumnWidth(15);
			int excelRowIndex = 0;
			
			List<List<String>> excelColumnHeaders = new ArrayList<List<String>>();
			excelColumnHeaders.add(languageService.getMessageForRow(columns, userLocale));
			excelRowIndex = sendDataToBrowser(excelColumnHeaders, ws, excelRowIndex);
			
			//do business
			Integer resultIndex = 0;
			
			ScrollableResults scrollResult = exportDao.getPublishedResearchPorjectsScroll(type, searchOb);

			while(resultIndex != -1 && resultIndex <= 65500) {// && resultIndex <= 5000 <-- remove the download limition, this is requested by business.
				List<Object> batchResult = new ArrayList<Object>();

				resultIndex = exportDao.getBatchResultByScrolling(scrollResult, resultIndex, batchResult);
				log.debug("download report number: " + resultIndex);
				List<List<String>> batchOutput = new ArrayList<List<String>>();
				for(Object row : batchResult) {
					batchOutput.add(DataConvertTool.convertObjectsToString(convertPojoToObject(row)));
				}
//				refineResult(outputs, batchOutput);
				batchOutput = languageService.getMessageForRows(batchOutput, userLocale);
				excelRowIndex = sendDataToBrowser(batchOutput, ws, excelRowIndex);
			}
			wwb.write();

		} catch (Exception e){

			
		} finally {

			try {
				wwb.close();
				os.close();
			} catch (WriteException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}
	
	private int sendDataToBrowser(List<List<String>> data, WritableSheet ws, int excelRowIndex) {
//		int endRowIndex = data.size() + beginIndex;
		try {
			
			for(int rowIndex = 0; rowIndex < data.size(); rowIndex++) {
				List<String> headers = data.get(rowIndex);
				WritableCellFormat wcfFC = getWFC_Font();
				for(int colIndex=0; colIndex < headers.size(); colIndex++){
				   Label label = new Label(colIndex, excelRowIndex, headers.get(colIndex), wcfFC);
					ws.addCell(label);
				}
				excelRowIndex++;
			}
			
		} catch (RowsExceededException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return excelRowIndex;
	}
	
	private static WritableCellFormat getWFC_Font(){
		if(WFC_FONT == null){
			WritableFont wf = new WritableFont(WritableFont.TIMES,10,WritableFont.BOLD,false,UnderlineStyle.NO_UNDERLINE,Colour.BLACK);
			WFC_FONT = new WritableCellFormat(wf);
			try{
				WFC_FONT.setBackground(Colour.GREY_25_PERCENT);
			}catch(WriteException we){
				we.printStackTrace();
			}
		}
		return WFC_FONT;
	}
}

package com.liming.workplan.service.impl;


import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
	
	
	private enum TableColumn {
//		NODEID("nodeId"),
		ATTACHMENT_NAME("attachmentName"), 
		ATTACHMENT_URL("attachmentURL"),
		TYPE_DESE("typeDesc");
		
		private final String value;
        private TableColumn(String value) {
            this.value = value;
        }
        public String value() {
        	return value;
        }
	}
	
	protected enum UnPublishColumn {
		STATUS("status");
		
		private final String value;
        private UnPublishColumn(String value) {
            this.value = value;
        }
        public String value() {
        	return value;
        }
	}
	
	public WorkflowService getWorkflowService() {
		return workflowService;
	}
	
	public Object[] convertPojoToObject(Object[] pojos) {
		Object[] objectValues = new Object[2];
		int index = 0;
		if(pojos.length == 1) {
			ResearchProject researchProject = (ResearchProject)pojos[0];
			objectValues[index++] = researchProject.getAttachment().getTypeDesc();
			objectValues[index++] = researchProject.getAttachment().getAttachmentName();
		} else {
			Attachment attachment = (Attachment)pojos[1];

			objectValues[index++] = attachment.getTypeDesc();
			objectValues[index++] = attachment.getAttachmentName();
		}
		return objectValues;
	}
	
	public void fillDisplayTable(WorkPlanNode node, Map<String, String> row) {		
//		row.put(TableColumn.NODEID.value(), Integer.toString(node.getNodeId()));
		row.put(TableColumn.ATTACHMENT_NAME.value(), node.getAttachment().getAttachmentName());
		row.put(TableColumn.ATTACHMENT_URL.value(), node.getAttachment().getAttachmentURL());
		row.put(TableColumn.TYPE_DESE.value(), node.getAttachment().getTypeDesc());
	}
	
	public List<String[]> getPublishedTableHeader() {
		LanguageService service = getLanguageService();
		
		List<String> tableHeader = getTableHeader();
		List<String> baseTableHeader = getBaseTableHeader();
		
		List<String[]> columnValues = new ArrayList<String[]>(tableHeader.size() + baseTableHeader.size());
		
		columnValues.addAll(service.getLocalTableHeader(tableHeader));
		columnValues.addAll(service.getLocalTableHeader(baseTableHeader));

		return columnValues;
	}
	
	public List<String[]> getUnPublishedTableHeader() {
		LanguageService service = getLanguageService();
	
		List<String> tableHeader = getTableHeader();
		List<String> baseTableHeader = getBaseTableHeader();
		
		List<String[]> columnValues = new ArrayList<String[]>(tableHeader.size() + baseTableHeader.size() + 1);
		
		columnValues.add(new String[]{UnPublishColumn.STATUS.value(), service.getMessage(UnPublishColumn.STATUS.value())});	
		columnValues.addAll(service.getLocalTableHeader(tableHeader));
		columnValues.addAll(service.getLocalTableHeader(baseTableHeader));
		
		return columnValues;
	}
	
	public List<String> getExportTableHeader() {
		List<String> columnValues = new ArrayList<String>();
		
		columnValues.addAll(getTableHeader());
		columnValues.addAll(getBaseTableHeader());
		return columnValues;
	}
	
	protected abstract List<String> getTableHeader();
	
	protected List<String> getBaseTableHeader() {
		List<String> columnValues = new ArrayList<String>();
		
		columnValues.add(TableColumn.TYPE_DESE.value());
		columnValues.add(TableColumn.ATTACHMENT_NAME.value());
		return columnValues;
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

	protected void initWorkPlanNode(WorkPlanNode node, Map<String, Object> values) {
		User currentUser = UserThreadLocal.getCurrentUser();
		Date currentDate = DateUtil.newDate();
		node.setAuthor(currentUser.getUserId());
		node.setCreateDate(currentDate);
		node.setStatus(Constants.WorkPlanNode_STATUS_UNPUBLISH);
		//build attachment
		Attachment attach = new Attachment();
		attach.setAttachmentId(((Long)values.get("attachmentId")).toString());
		fillValues(attach, "super.setter", values);
		node.setAttachment(attach);
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
				List<Object[]> batchResult = new ArrayList<Object[]>();

				resultIndex = exportDao.getBatchResultByScrolling(scrollResult, resultIndex, batchResult);
				log.debug("download report number: " + resultIndex);
				List<List<String>> batchOutput = new ArrayList<List<String>>();
				for(Object[] row : batchResult) {
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

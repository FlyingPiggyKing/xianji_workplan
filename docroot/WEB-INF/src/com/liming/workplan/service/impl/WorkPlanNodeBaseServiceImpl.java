package com.liming.workplan.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.DateUtil;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.service.RoleServiceUtil;
import com.liming.workplan.model.pojo.Attachment;
import com.liming.workplan.model.pojo.ResearchProject;
import com.liming.workplan.model.pojo.WorkPlanNode;
import com.liming.workplan.model.pojo.WorkflowNode;
import com.liming.workplan.service.LanguageService;
import com.liming.workplan.service.WorkflowService;
import com.liming.workplan.utils.Constants;
import com.liming.workplan.utils.UserThreadLocal;

public abstract class WorkPlanNodeBaseServiceImpl {
	private static final Log log = LogFactory.getLog(WorkPlanNodeBaseServiceImpl.class);
	private List<Map<String, Object>> rowConfiguration;
	private WorkflowService workflowService;
	private LanguageService languageService;
	
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
	
	public WorkflowService getWorkflowService() {
		return workflowService;
	}
	
	public void fillDisplayTable(WorkPlanNode node, Map<String, String> row) {		
//		row.put(TableColumn.NODEID.value(), Integer.toString(node.getNodeId()));
		row.put(TableColumn.ATTACHMENT_NAME.value(), node.getAttachment().getAttachmentName());
		row.put(TableColumn.ATTACHMENT_URL.value(), node.getAttachment().getAttachmentURL());
		row.put(TableColumn.TYPE_DESE.value(), node.getAttachment().getTypeDesc());
	}
	
	protected List<String[]> getTableHeader() {
		List<String[]> columnValues = new ArrayList<String[]>();
		User currentUser = UserThreadLocal.getCurrentUser();
		Locale userLocale = currentUser.getLocale();
		
		columnValues.add(new String[]{TableColumn.TYPE_DESE.value(), getLanguageService().getMessage(TableColumn.TYPE_DESE.value(), userLocale)});
		columnValues.add(new String[]{TableColumn.ATTACHMENT_NAME.value(), getLanguageService().getMessage(TableColumn.ATTACHMENT_NAME.value(), userLocale)});
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

				} else if("float".equals(valueSeg[2])) {
					valueMap.put(valueSeg[0], Float.valueOf(valueSeg[1]));
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
		attach.setAttachmentId(Double.toString(Math.random()));
		fillValues(attach, "super.setter", values);
		node.setAttachment(attach);
		//build workflow
		log.debug("build workflow");
		WorkflowNode workflow = new WorkflowNode();
		List<String> roleList = workflowService.getRoleOrderList();
		Role firstRole = null;
		try {
			firstRole = RoleServiceUtil.getRole(currentUser.getCompanyId(), roleList.get(0));
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
		int cellIndex = 0;
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
			} else if("float".equals(type)) {
				typeClass = float.class;
			}
			try {
				Method method = targetClass.getMethod(methodName, typeClass);
				try {
					if("float".equals(type)) {
						method.invoke(researchProject, ((Float)data.get(cell.get("field"))).floatValue());
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
			cellIndex++;
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
}

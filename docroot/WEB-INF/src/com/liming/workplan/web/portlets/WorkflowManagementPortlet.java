package com.liming.workplan.web.portlets;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
import com.liming.workplan.service.BeanLocator;
import com.liming.workplan.service.WorkflowService;
import com.liming.workplan.utils.Constants;
import com.liming.workplan.utils.CurrentUserRoleThreadLocal;
import com.liming.workplan.utils.JsonTool;
import com.liming.workplan.utils.UserThreadLocal;
import com.liming.workplan.utils.WorkplanDataThreadLocal;

public class WorkflowManagementPortlet extends MVCPortlet {
	
	private final String WORKFLOW_ENTITY = "workflowEntity";
	
	
	@Override
	public void serveResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws IOException, PortletException {
		boolean isSuccessed = UserThreadLocal.setCurrentUser(PortalUtil.getHttpServletRequest(resourceRequest));
		if(!isSuccessed) {
			return;
		}
		ThemeDisplay themeDisplay = (ThemeDisplay) resourceRequest.getAttribute(WebKeys.THEME_DISPLAY);
		Long groupId = themeDisplay.getLayout().getGroupId();
		User currentUser = UserThreadLocal.getCurrentUser();
		CurrentUserRoleThreadLocal.setCurrentGroupRole(currentUser.getUserId(), groupId);
		
		String cmd = resourceRequest.getParameter(Constants.WorkflowManagementPortlet_RESOURCE_CMD);
		String nodeType = resourceRequest.getParameter(Constants.WorkflowManagementPortlet_PARAM_NODETYPE_KEY);
		WorkflowService workflowService = BeanLocator.getWorkflowService();
		if(Constants.WorkflowManagementPortlet_RESOURCE_CMD_APPROVE.equals(cmd)) {
			String[] ids = resourceRequest.getParameterValues(Constants.WorkflowManagementPortlet_PARAM_IDS_KEY);
			List<Object[]> pojoEntities = (List<Object[]>)resourceRequest.getPortletSession().getAttribute(WORKFLOW_ENTITY);
			if(pojoEntities != null) {
				workflowService.approveWorkflow(nodeType, ids, pojoEntities);
			} else {
				
			}
		} else if(Constants.WorkflowManagementPortlet_RESOURCE_CMD_REJECT.equals(cmd)) {
			String[] ids = resourceRequest.getParameterValues(Constants.WorkflowManagementPortlet_PARAM_IDS_KEY);
			List<Object[]> pojoEntities = (List<Object[]>)resourceRequest.getPortletSession().getAttribute(WORKFLOW_ENTITY);
			if(pojoEntities != null) {
				workflowService.rejectWorkflow(nodeType, ids, pojoEntities);
			} else {
				
			}
			
		} else if(Constants.WorkflowManagementPortlet_RESOURCE_CMD_LOAD.equals(cmd)) {
			int pageNumber = Integer.valueOf((String)resourceRequest.getParameter(Constants.PORTLET_REQUEST_PARAM_PAGE_NUMBER));
			int pageSize = Integer.valueOf((String)resourceRequest.getParameter(Constants.PORTLET_REQUEST_PARAM_PAGE_SIZE));
			
			String sortColumn = resourceRequest.getParameter("sort");
			String sortOrder = resourceRequest.getParameter("order");
			List<Object[]> resultEnitity = null;
			if("undefined".equals(sortColumn) || "null".equals(sortColumn)) {
				resultEnitity = workflowService.getWorkflowsByRoleAndType("ResearchProject", pageNumber, pageSize, null, null);
			} else {
				resultEnitity = workflowService.getWorkflowsByRoleAndType("ResearchProject", pageNumber, pageSize, sortColumn, sortOrder);
			}
			
			resourceRequest.getPortletSession().setAttribute(WORKFLOW_ENTITY, resultEnitity);
			
			List<Map<String, String>> resultData = WorkplanDataThreadLocal.getDisplayData(nodeType);
			JSONObject resultJson = JsonTool.convertResultListToJson(resultData);
			
			String needGneedGetHeaderetHeader = resourceRequest.getParameter("header");
			if(needGneedGetHeaderetHeader != null) {
				List<String[]> header = workflowService.getWorkflowHeader(nodeType);
				JSONArray headerJson = JsonTool.convertTableHederToJson(header);
				resultJson.put("header", headerJson);
			}
			resourceResponse.getWriter().write(resultJson.toString());
		} else if(Constants.WorkflowManagementPortlet_RESOURCE_CMD_COUNT.equals(cmd)) {
			String dataType = resourceRequest.getParameter("dataType");
			Long countL = workflowService.countWorkflow(dataType);
			JSONObject count = JsonTool.convertNumberToJson("count", countL);
			resourceResponse.getWriter().write(count.toString());
		}
		
		
		
		
		
	}
	
	@Override
	public void doView(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {
		boolean isSuccessed = UserThreadLocal.setCurrentUser(PortalUtil.getHttpServletRequest(renderRequest));
		if(!isSuccessed) {
			return;
		}
		super.doView(renderRequest, renderResponse);
	}
}

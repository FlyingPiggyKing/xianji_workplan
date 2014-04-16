package com.liming.workplan.web.portlets;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
import com.liming.workplan.service.BeanLocator;
import com.liming.workplan.service.ResearchProjectService;
import com.liming.workplan.service.ResearchProjectServiceUtil;
import com.liming.workplan.utils.Constants;
import com.liming.workplan.utils.JsonTool;
import com.liming.workplan.utils.UserThreadLocal;

public class WorkPlanManagementPortlet extends MVCPortlet {
	
	
	@Override
	public void serveResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws IOException, PortletException {
		String cmd = resourceRequest.getParameter(Constants.WorkPlanManagementPortlet_RESOURCE_CMD);
		boolean isSuccessed = UserThreadLocal.setCurrentUser(PortalUtil.getHttpServletRequest(resourceRequest));
		if(!isSuccessed) {
			return;
		}
		ResearchProjectService researchProjectService = BeanLocator.getResearchProjectService();
		if(Constants.WorkPlanManagementPortlet_RESOURCE_CMD_ADD.equals(cmd)) {
			String[] researchProjectItems = resourceRequest.getParameterValues("values");
			researchProjectService.addResearchProject(researchProjectItems);
		} else if(Constants.WorkPlanManagementPortlet_RESOURCE_CMD_LOAD_PUBLISHED_NODES.equals(cmd)) {
			String pageNumber = resourceRequest.getParameter("pageNumber");
			String pageSize = resourceRequest.getParameter("pageSize");
			String[] searchParams = resourceRequest.getParameterValues("searchParams");
			String sortColumn = resourceRequest.getParameter("sort");
			String sortOrder = resourceRequest.getParameter("order");
			
			List<Map<String, String>> result = null;
			result = researchProjectService.loadPublishedResearchProject(searchParams, Integer.parseInt(pageNumber), Integer.parseInt(pageSize), sortColumn, sortOrder);
			JSONObject resultJson = JsonTool.convertResultListToJson(result);
			
			String needGneedGetHeaderetHeader = resourceRequest.getParameter("header");
			if(needGneedGetHeaderetHeader != null) {
				List<String[]> header = researchProjectService.getPublishedTableHeader();
				JSONArray headerJson = JsonTool.convertTableHederToJson(header);
				resultJson.put("header", headerJson);
			}
			
			resourceResponse.getWriter().write(resultJson.toString());
		} else if(Constants.WorkPlanManagementPortlet_RESOURCE_CMD_LOAD_UNPUBLISHED_NODES.equals(cmd)) {
			String pageNumber = resourceRequest.getParameter("pageNumber");
			String pageSize = resourceRequest.getParameter("pageSize");
			
			String sortColumn = resourceRequest.getParameter("sort");
			String sortOrder = resourceRequest.getParameter("order");
			
			List<Map<String, String>> result = null;
			result = researchProjectService.loadUnPublishedResearchProject(Integer.parseInt(pageNumber), Integer.parseInt(pageSize), sortColumn, sortOrder);
			
			JSONObject resultJson = JsonTool.convertResultListToJson(result);
			
			String needGneedGetHeaderetHeader = resourceRequest.getParameter("header");
			if(needGneedGetHeaderetHeader != null) {
				List<String[]> header = researchProjectService.getUnPublishedTableHeader();
				JSONArray headerJson = JsonTool.convertTableHederToJson(header);
				resultJson.put("header", headerJson);
			}
			
			resourceResponse.getWriter().write(resultJson.toString());
		} else if(Constants.WorkPlanManagementPortlet_RESOURCE_CMD_DELETE.equals(cmd)) {
			String[] ids = resourceRequest.getParameterValues(Constants.WorkPlanManagementPortlet_PARAM_IDS_KEY);
			researchProjectService.deleteResearchProjects(ids);
		} else if(Constants.WorkPlanManagementPortlet_RESOURCE_CMD_PUBLISH_COUNT.equals(cmd)) {
			String[] searchParams = resourceRequest.getParameterValues("searchParams");
			Long countI = researchProjectService.getPublishedCount(searchParams);
			JSONObject count = JsonTool.convertNumberToJson("count", countI);
			resourceResponse.getWriter().write(count.toString());
		} else if(Constants.WorkPlanManagementPortlet_RESOURCE_CMD_UNPUBLISHED_COUNT.equals(cmd)) {
			Long countI = researchProjectService.getUnPublishedCount();
			JSONObject count = JsonTool.convertNumberToJson("count", countI);
			resourceResponse.getWriter().write(count.toString());
		} else if(Constants.WorkPlanManagementPortlet_RESOURCE_CMD_STATISTICS.equals(cmd)) {
			String[] searchParams = resourceRequest.getParameterValues("searchParams");
			Map<String, String> statistics = researchProjectService.getStatistics(searchParams);
			JSONObject statisticJson = JsonTool.convertStatMapToJson(statistics);
			List<String[]> statHeader = researchProjectService.getStatisticsHeader();
			JSONArray headerJson = JsonTool.convertTableHederToJson(statHeader);
			statisticJson.put("header", headerJson);
			resourceResponse.getWriter().write(statisticJson.toString());
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
		ResearchProjectService researchProjectService = BeanLocator.getResearchProjectService();
		List<Map<String, Object>> rowConfig = researchProjectService.getRowConfiguration();
		JSONObject containerJson = JsonTool.convertListConfToJson(rowConfig);
		renderRequest.setAttribute("ROW_CONFIG_JSON", containerJson.toString());
		
		List<Map<String, Object>> searchConfig = researchProjectService.getSearchConfiguration();
		JSONObject searchJson = JsonTool.convertListConfToJson(searchConfig);
		renderRequest.setAttribute("SEARCH_CONFIG_JSON", searchJson.toString());
		
		super.doView(renderRequest, renderResponse);
	}


	

}

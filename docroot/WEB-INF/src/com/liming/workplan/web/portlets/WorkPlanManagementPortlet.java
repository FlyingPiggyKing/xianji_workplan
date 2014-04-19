package com.liming.workplan.web.portlets;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletResponse;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.util.PortalUtil;
import com.liming.workplan.service.BeanLocator;
import com.liming.workplan.service.ResearchProjectService;
import com.liming.workplan.utils.Constants;
import com.liming.workplan.utils.JsonTool;
import com.liming.workplan.utils.UserThreadLocal;

public class WorkPlanManagementPortlet extends WorlplanBasePortlet {
	
	
	@Override
	public void serveResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws IOException, PortletException {
		String cmd = resourceRequest.getParameter(Constants.WorkPlanManagementPortlet_RESOURCE_CMD);
		boolean isSuccessed = UserThreadLocal.setCurrentUser(PortalUtil.getHttpServletRequest(resourceRequest));
		if(!isSuccessed) {
			return;
		}

		if(Constants.WorlplanBasePortlet_RESOURCE_CMD_ADD.equals(cmd)) {
			addNodes(resourceRequest);
		} else if(Constants.WorlplanBasePortlet_RESOURCE_CMD_LOAD_PUBLISHED_NODES.equals(cmd)) {
			getPublishedNodes(resourceRequest, resourceResponse);
		} else if(Constants.WorlplanBasePortlet_RESOURCE_CMD_LOAD_UNPUBLISHED_NODES.equals(cmd)) {
			getUnPublishedNodes(resourceRequest, resourceResponse);
		} else if(Constants.WorlplanBasePortlet_RESOURCE_CMD_DELETE.equals(cmd)) {
			deleteRejectedNodes(resourceRequest);
		} else if(Constants.WorlplanBasePortlet_RESOURCE_CMD_PUBLISH_COUNT.equals(cmd)) {
			getPublishNodesCount(resourceRequest, resourceResponse);
		} else if(Constants.WorlplanBasePortlet_RESOURCE_CMD_UNPUBLISHED_COUNT.equals(cmd)) {
			getUnPublishNodesCount(resourceResponse);
		} else if(Constants.WorkPlanManagementPortlet_RESOURCE_CMD_STATISTICS.equals(cmd)) {
			getStatistics(resourceRequest, resourceResponse);
		} else if(Constants.WorlplanBasePortlet_RESOURCE_CMD_EXPORT.equals(cmd)) {
			doExport(resourceRequest, resourceResponse);
		}
	}

	protected void addNodes(ResourceRequest resourceRequest) {
		String[] researchProjectItems = resourceRequest.getParameterValues("values");
		ResearchProjectService researchProjectService = BeanLocator.getResearchProjectService();
		researchProjectService.addResearchProject(researchProjectItems);
	}
	
	protected void getPublishedNodes(ResourceRequest resourceRequest,
			ResourceResponse resourceResponse) throws IOException {
		String pageNumber = resourceRequest.getParameter("pageNumber");
		String pageSize = resourceRequest.getParameter("pageSize");
		String[] searchParams = resourceRequest.getParameterValues("searchParams");
		String sortColumn = resourceRequest.getParameter("sort");
		String sortOrder = resourceRequest.getParameter("order");
		
		ResearchProjectService researchProjectService = BeanLocator.getResearchProjectService();
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
	}
	
	protected void getUnPublishedNodes(ResourceRequest resourceRequest,
			ResourceResponse resourceResponse) throws IOException {
		String pageNumber = resourceRequest.getParameter("pageNumber");
		String pageSize = resourceRequest.getParameter("pageSize");
		
		String sortColumn = resourceRequest.getParameter("sort");
		String sortOrder = resourceRequest.getParameter("order");
		
		ResearchProjectService researchProjectService = BeanLocator.getResearchProjectService();
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
	}
	
	protected void deleteRejectedNodes(ResourceRequest resourceRequest) {
		String[] ids = resourceRequest.getParameterValues(Constants.WorkPlanManagementPortlet_PARAM_IDS_KEY);
		ResearchProjectService researchProjectService = BeanLocator.getResearchProjectService();
		researchProjectService.deleteResearchProjects(ids);
	}	
	
	protected void getPublishNodesCount(ResourceRequest resourceRequest,
			ResourceResponse resourceResponse) throws IOException {
		String[] searchParams = resourceRequest.getParameterValues("searchParams");
		ResearchProjectService researchProjectService = BeanLocator.getResearchProjectService();
		Long countI = researchProjectService.getPublishedCount(searchParams);
		JSONObject count = JsonTool.convertNumberToJson("count", countI);
		resourceResponse.getWriter().write(count.toString());
	}
	
	protected void getUnPublishNodesCount(ResourceResponse resourceResponse) throws IOException {
		ResearchProjectService researchProjectService = BeanLocator.getResearchProjectService();
		Long countI = researchProjectService.getUnPublishedCount();
		JSONObject count = JsonTool.convertNumberToJson("count", countI);
		resourceResponse.getWriter().write(count.toString());
	}
	
	protected void getStatistics(ResourceRequest resourceRequest,
			ResourceResponse resourceResponse) throws IOException {
		String[] searchParams = resourceRequest.getParameterValues("searchParams");
		ResearchProjectService researchProjectService = BeanLocator.getResearchProjectService();
		Map<String, String> statistics = researchProjectService.getStatistics(searchParams);
		JSONObject statisticJson = JsonTool.convertStatMapToJson(statistics);
		List<String[]> statHeader = researchProjectService.getStatisticsHeader();
		JSONArray headerJson = JsonTool.convertTableHederToJson(statHeader);
		statisticJson.put("header", headerJson);
		resourceResponse.getWriter().write(statisticJson.toString());
	}
	
	protected void doExport(ResourceRequest resourceRequest,
			ResourceResponse resourceResponse) throws IOException {
		String[] searchParams = resourceRequest.getParameterValues("searchParams");
		HttpServletResponse response = PortalUtil.getHttpServletResponse(resourceResponse);
		response.setContentType("application/vnd.ms-excel;charset=utf-8"); 
		response.addHeader("Content-Disposition", "attachment;filename=export.xls");
		
		OutputStream os = response.getOutputStream();
		ResearchProjectService researchProjectService = BeanLocator.getResearchProjectService();
		researchProjectService.exportResult(searchParams, os);
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

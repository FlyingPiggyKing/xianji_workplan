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
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.util.PortalUtil;
import com.liming.workplan.service.BeanLocator;
import com.liming.workplan.service.ResearchAchievementService;
import com.liming.workplan.utils.Constants;
import com.liming.workplan.utils.JsonTool;
import com.liming.workplan.utils.UserThreadLocal;

public class ResearchAchievementPortlet extends WorlplanBasePortlet {

	protected void addNodes(UploadPortletRequest uploadRequest, List<Map<String, Object>> fileParams) {
//		String[] researchAchievementItems = resourceRequest.getParameterValues("values");
//		ResearchAchievementService researchAchievementService = BeanLocator.getResearchAchievementService();
//		researchAchievementService.addResearchAchievement(researchAchievementItems);
	}
	
	protected void getPublishedNodes(ResourceRequest resourceRequest,
			ResourceResponse resourceResponse) throws IOException {
		String pageNumber = resourceRequest.getParameter("pageNumber");
		String pageSize = resourceRequest.getParameter("pageSize");
		String[] searchParams = resourceRequest.getParameterValues("searchParams");
		String sortColumn = resourceRequest.getParameter("sort");
		String sortOrder = resourceRequest.getParameter("order");
		
		ResearchAchievementService researchAchievementService = BeanLocator.getResearchAchievementService();
		List<Map<String, String>> result = null;
		result = researchAchievementService.loadPublishedResearchAchievement(searchParams, Integer.parseInt(pageNumber), Integer.parseInt(pageSize), sortColumn, sortOrder);
		JSONObject resultJson = JsonTool.convertResultListToJson(result);
		
		String needGneedGetHeaderetHeader = resourceRequest.getParameter("header");
		if(needGneedGetHeaderetHeader != null) {
			List<String[]> header = researchAchievementService.getPublishedTableHeader();
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
		
		ResearchAchievementService researchAchievementService = BeanLocator.getResearchAchievementService();
		List<Map<String, String>> result = null;
		result = researchAchievementService.loadUnPublishedResearchAchievement(Integer.parseInt(pageNumber), Integer.parseInt(pageSize), sortColumn, sortOrder);
		
		JSONObject resultJson = JsonTool.convertResultListToJson(result);
		
		String needGneedGetHeaderetHeader = resourceRequest.getParameter("header");
		if(needGneedGetHeaderetHeader != null) {
			List<String[]> header = researchAchievementService.getUnPublishedTableHeader();
			JSONArray headerJson = JsonTool.convertTableHederToJson(header);
			resultJson.put("header", headerJson);
		}
		
		resourceResponse.getWriter().write(resultJson.toString());
	}
	
	protected void deleteRejectedNodes(ResourceRequest resourceRequest) {
		String[] ids = resourceRequest.getParameterValues(Constants.WorlplanBasePortlet_PARAM_IDS_KEY);
		ResearchAchievementService researchAchievementService = BeanLocator.getResearchAchievementService();
		researchAchievementService.deleteResearchAchievements(ids);
	}	
	
	protected void getPublishNodesCount(ResourceRequest resourceRequest,
			ResourceResponse resourceResponse) throws IOException {
		String[] searchParams = resourceRequest.getParameterValues("searchParams");
		ResearchAchievementService researchAchievementService = BeanLocator.getResearchAchievementService();
		Long countI = researchAchievementService.getPublishedCount(searchParams);
		JSONObject count = JsonTool.convertNumberToJson("count", countI);
		resourceResponse.getWriter().write(count.toString());
	}
	
	protected void getUnPublishNodesCount(ResourceResponse resourceResponse) throws IOException {
		ResearchAchievementService researchAchievementService = BeanLocator.getResearchAchievementService();
		Long countI = researchAchievementService.getUnPublishedCount();
		JSONObject count = JsonTool.convertNumberToJson("count", countI);
		resourceResponse.getWriter().write(count.toString());
	}
	
	protected void doExport(ResourceRequest resourceRequest,
			ResourceResponse resourceResponse) throws IOException {
		String[] searchParams = resourceRequest.getParameterValues("searchParams");
		HttpServletResponse response = PortalUtil.getHttpServletResponse(resourceResponse);
		response.setContentType("application/vnd.ms-excel;charset=utf-8"); 
		response.addHeader("Content-Disposition", "attachment;filename=export.xls");
		
		OutputStream os = response.getOutputStream();
		ResearchAchievementService researchAchievementService = BeanLocator.getResearchAchievementService();
		researchAchievementService.exportResult(searchParams, os);
	}
	
	@Override
	public void doView(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {
		boolean isSuccessed = UserThreadLocal.setCurrentUser(PortalUtil.getHttpServletRequest(renderRequest));
		if(!isSuccessed) {
			return;
		}
		ResearchAchievementService researchAchievementService = BeanLocator.getResearchAchievementService();
		List<Map<String, Object>> rowConfig = researchAchievementService.getRowConfiguration();
		JSONObject containerJson = JsonTool.convertListConfToJson(rowConfig);
		renderRequest.setAttribute("ROW_CONFIG_JSON", containerJson.toString());
		
		List<Map<String, Object>> searchConfig = researchAchievementService.getSearchConfiguration();
		JSONObject searchJson = JsonTool.convertListConfToJson(searchConfig);
		renderRequest.setAttribute("SEARCH_CONFIG_JSON", searchJson.toString());
		
		super.doView(renderRequest, renderResponse);
	}

}

package com.liming.workplan.web.portlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletResponse;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.ServiceContextFactory;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.assetpublisher.util.AssetPublisherUtil;
import com.liferay.portlet.documentlibrary.model.DLFileEntry;
import com.liferay.portlet.documentlibrary.model.DLFolder;
import com.liferay.portlet.documentlibrary.service.DLAppServiceUtil;
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
		String cmd = resourceRequest.getParameter(Constants.WorlplanBasePortlet_RESOURCE_CMD);
		boolean isSuccessed = UserThreadLocal.setCurrentUser(PortalUtil.getHttpServletRequest(resourceRequest));
		if(!isSuccessed) {
			return;
		}

		if(Constants.WorlplanBasePortlet_RESOURCE_CMD_ADD.equals(cmd)) {
			UploadPortletRequest uploadRequest = PortalUtil.getUploadPortletRequest(resourceRequest);
			Map<String, Object> fileParams = uploadFileToUserFolder(
					resourceRequest, uploadRequest);
			addNodes(uploadRequest, fileParams);
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

	



	protected void addNodes(UploadPortletRequest uploadRequest, Map<String, Object> fileParams) {
//		String[] researchProjectItems = resourceRequest.getParameterValues("values");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("type", ParamUtil.getString(uploadRequest, "type"));
		params.put("projectType", ParamUtil.getString(uploadRequest, "projectType"));
		params.put("projectName", ParamUtil.getString(uploadRequest, "projectName"));
		params.put("supportUnit", ParamUtil.getString(uploadRequest, "supportUnit"));
		params.put("projectLevel", ParamUtil.getString(uploadRequest, "projectLevel"));
		params.put("charger", ParamUtil.getString(uploadRequest, "charger"));
		params.put("assistant", ParamUtil.getString(uploadRequest, "assistant"));
		params.put("projectFunding", ParamUtil.getDouble(uploadRequest, "projectFunding"));
		params.put("delegatedDepartment", ParamUtil.getString(uploadRequest, "delegatedDepartment"));
		params.put("typeDesc", ParamUtil.getString(uploadRequest, "typeDesc"));
		params.put("attachmentName", fileParams.get("attachmentName"));
		params.put("attachmentId", fileParams.get("attachmentId"));
//		ThemeDisplay themeDisplay = serviceContext.getThemeDisplay();
//		String fileDownloadUrl = themeDisplay.getPortalURL()+"/c/document_library/get_file?uuid="+fileEntry.getUuid()+"&groupId="+themeDisplay.getScopeGroupId();
		params.put("attachmentURL", fileParams.get("attachmentURL"));
		ResearchProjectService researchProjectService = BeanLocator.getResearchProjectService();
		researchProjectService.addResearchProject(params);
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
		String[] ids = resourceRequest.getParameterValues(Constants.WorlplanBasePortlet_PARAM_IDS_KEY);
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

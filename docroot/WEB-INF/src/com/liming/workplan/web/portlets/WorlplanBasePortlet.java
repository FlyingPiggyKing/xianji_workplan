package com.liming.workplan.web.portlets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.portlet.PortletException;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletResponse;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.upload.FileItem;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.security.permission.ActionKeys;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.ServiceContextFactory;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.assetpublisher.util.AssetPublisherUtil;
import com.liferay.portlet.documentlibrary.DuplicateFileException;
import com.liferay.portlet.documentlibrary.model.DLFileEntry;
import com.liferay.portlet.documentlibrary.model.DLFolder;
import com.liferay.portlet.documentlibrary.service.DLAppServiceUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
import com.liming.workplan.utils.Constants;
import com.liming.workplan.utils.JsonTool;
import com.liming.workplan.utils.UserThreadLocal;

public abstract class WorlplanBasePortlet extends MVCPortlet  {
	public static final String UPLOAD_FILE_PARAM = "attachmentFile";
	public static final String UPLOAD_DESC_PARAM = "attachment";
	
	public static final String SUBMIT_SUCCESS = "submit-success";
	public static final String SUBMIT_FAILURE = "submit-failure";
	public static final String SUBMIT_RESULT = "result";
	
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
			List<Map<String, Object>> fileParams = null;
			try {
				fileParams = uploadFileToUserFolder(resourceRequest, uploadRequest);
			} catch (DuplicateFileException e) {
				HttpServletResponse response = PortalUtil.getHttpServletResponse(resourceResponse);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				resourceResponse.getWriter().write(JsonTool.convertStringToJson("error", "Duplicated File.").toString());
			}
			if(fileParams != null) {
				addNodes(uploadRequest, fileParams);
			}
			
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
		}  else if(Constants.WorlplanBasePortlet_RESOURCE_CMD_EXPORT.equals(cmd)) {
			doExport(resourceRequest, resourceResponse);
		}
	}

	private String getTargetFolder() {
		StringBuilder targetFolder = new StringBuilder();
		targetFolder.append(UserThreadLocal.getCurrentUser().getFullName() + Constants.BK);
		targetFolder.append(UserThreadLocal.getCurrentUser().getUserId());
		String targetFolderName = targetFolder.toString();
		String finalName = targetFolderName.replaceAll(Constants.BK, "_");
		return finalName;
	}
	
	protected List<Map<String, Object>> uploadFileToUserFolder(
			ResourceRequest resourceRequest, UploadPortletRequest uploadRequest)
			throws IOException, DuplicateFileException {
		long parentFolderId = 14058;//need to be changed
		ServiceContext serviceContext = null;
		FileEntry fileEntry = null;
		List<Map<String, Object>> fileParamList = new ArrayList<Map<String, Object>>();
		try {
			serviceContext = ServiceContextFactory.getInstance(
					DLFolder.class.getName(), uploadRequest);
			ThemeDisplay themeDisplay = serviceContext.getThemeDisplay();
			String portalUrl = themeDisplay.getPortalURL();
			String contextUrl = themeDisplay.getPathContext();
			long scopeGroupId = themeDisplay.getScopeGroupId();
			
			String targetFolderName = getTargetFolder();
			long repositoryId = serviceContext.getScopeGroupId();
			Folder targetFolder = null;
			try {
				targetFolder = DLAppServiceUtil.getFolder(repositoryId, parentFolderId, targetFolderName);
			} catch (PortalException e1) {
				
				e1.printStackTrace();
				
				if(targetFolder == null) {
					targetFolder = DLAppServiceUtil.addFolder(repositoryId, parentFolderId, targetFolderName, "", serviceContext);
				}
			} 
			
			Map<String, FileItem[]> paramMaps = uploadRequest.getMultipartParameterMap();
			Map<String, String[]> formParams = uploadRequest.getParameterMap();
			Set<String> keys = paramMaps.keySet();
			for(String key : keys) {
				FileItem[] items = paramMaps.get(key);
				if(!items[0].isFormField()) {
					if(key.indexOf(UPLOAD_FILE_PARAM) != -1) {
						String timeIndex = key.substring(UPLOAD_FILE_PARAM.length());
						String typeName = UPLOAD_DESC_PARAM + timeIndex;
						String[] descs = formParams.get(typeName);
						
						String sourceFileName = items[0].getFileName();
						String contentType = items[0].getContentType();
						String desc = descs[0];
						InputStream inputStream = items[0].getInputStream();
						long size = items[0].getSize();
						String changeLog = ParamUtil.getString(
								uploadRequest, "changeLog");
						fileEntry = DLAppServiceUtil.addFileEntry(
								repositoryId, targetFolder.getFolderId(), sourceFileName, contentType, sourceFileName,
								desc, changeLog, inputStream, size, serviceContext);
						try {
							AssetPublisherUtil.addAndStoreSelection(
									resourceRequest, DLFileEntry.class.getName(),
									fileEntry.getFileEntryId(), -1);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						String fileDownloadUrl = portalUrl + contextUrl 
								+ "/c/document_library/get_file?uuid="
								+ fileEntry.getUuid()+"&groupId="
								+ scopeGroupId;
						Map<String, Object> fileParams = new HashMap<String, Object>();
						fileParams.put(Constants.Attachment_ID, fileEntry.getFileEntryId());
						fileParams.put(Constants.Attachment_URL, fileDownloadUrl);
						fileParams.put(Constants.Attachment_NAME, fileEntry.getTitle());
						fileParams.put(Constants.Attachment_DESC, desc);
						fileParamList.add(fileParams);
						
					}
				}
			}
//			FileItem[] uploadedItems = uploadRequest.getMultipartParameterMap().get("attachment");//[0].getInputStream();
//			for(FileItem uploadedItem : uploadedItems) {
////				String sourceFileName = uploadRequest.getFileName("attachmentName");
//				String sourceFileName = uploadedItem.getFileName();
////				String title = ParamUtil.getString(uploadRequest, "title");
////				String contentType = uploadRequest.getContentType("attachmentName");
//				String contentType = uploadedItem.getContentType();
//				String desc = "";
////				InputStream inputStream = uploadRequest.getFileAsStream("attachmentName");
//				InputStream inputStream = uploadedItem.getInputStream();
////				long size = uploadRequest.getSize("attachmentName");
//				long size = uploadedItem.getSize();
//				String changeLog = ParamUtil.getString(
//						uploadRequest, "changeLog");
//				fileEntry = DLAppServiceUtil.addFileEntry(
//						repositoryId, targetFolder.getFolderId(), sourceFileName, contentType, sourceFileName,
//						desc, changeLog, inputStream, size, serviceContext);
//				try {
//					AssetPublisherUtil.addAndStoreSelection(
//							resourceRequest, DLFileEntry.class.getName(),
//							fileEntry.getFileEntryId(), -1);
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//				String fileDownloadUrl = themeDisplay.getPortalURL() + themeDisplay.getPathContext() + "/c/document_library/get_file?uuid="+fileEntry.getUuid()+"&groupId="+themeDisplay.getScopeGroupId(); //themeDisplay.get.getPortalURL()+
//				Map<String, Object> fileParams = new HashMap<String, Object>();
//				fileParams.put(Constants.Attachment_ID, fileEntry.getFileEntryId());
//				fileParams.put(Constants.Attachment_URL, fileDownloadUrl);
//				fileParams.put(Constants.Attachment_NAME, fileEntry.getTitle());
//				fileParamList.add(fileParams);
//			}

//				DLAppServiceUtil.addFileEntry(repositoryId, folderId, sourceFileName, mimeType, title, description, changeLog, is, size, serviceContext)
		} catch (DuplicateFileException e) {
			throw e;
		} catch (PortalException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SystemException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return fileParamList;
	}
	
	protected abstract void addNodes(UploadPortletRequest uploadRequest, List<Map<String, Object>> fileParams);
	protected abstract void getPublishedNodes(ResourceRequest resourceRequest,
			ResourceResponse resourceResponse) throws IOException;
	protected abstract void getUnPublishedNodes(ResourceRequest resourceRequest,
			ResourceResponse resourceResponse) throws IOException;
	protected abstract void deleteRejectedNodes(ResourceRequest resourceRequest);
	protected abstract void getPublishNodesCount(ResourceRequest resourceRequest,
			ResourceResponse resourceResponse) throws IOException;
	protected abstract void getUnPublishNodesCount(ResourceResponse resourceResponse) throws IOException;
	protected abstract void doExport(ResourceRequest resourceRequest,
			ResourceResponse resourceResponse) throws IOException;
}

package com.liming.workplan.web.portlets;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletException;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
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
import com.liferay.util.bridges.mvc.MVCPortlet;
import com.liming.workplan.utils.Constants;
import com.liming.workplan.utils.UserThreadLocal;

public abstract class WorlplanBasePortlet extends MVCPortlet  {
	
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
			Map<String, Object> fileParams = uploadFileToUserFolder(resourceRequest, uploadRequest);
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
	
	protected Map<String, Object> uploadFileToUserFolder(
			ResourceRequest resourceRequest, UploadPortletRequest uploadRequest)
			throws IOException {
		long parentFolderId = 14058;//need to be changed
		ServiceContext serviceContext = null;
		FileEntry fileEntry = null;
		try {
			serviceContext = ServiceContextFactory.getInstance(
					DLFolder.class.getName(), uploadRequest);
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
			
			
//				long folderId = ParamUtil.getLong(uploadRequest, "folderId");
			String sourceFileName = uploadRequest.getFileName("attachmentName");
			String title = ParamUtil.getString(uploadRequest, "title");
			String contentType = uploadRequest.getContentType("attachmentName");
			String desc = "";
			InputStream inputStream = uploadRequest.getFileAsStream("attachmentName");
			long size = uploadRequest.getSize("attachmentName");
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
//				DLAppServiceUtil.addFileEntry(repositoryId, folderId, sourceFileName, mimeType, title, description, changeLog, is, size, serviceContext)
		} catch (PortalException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SystemException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		ThemeDisplay themeDisplay = serviceContext.getThemeDisplay();
		String fileDownloadUrl = themeDisplay.getPortalURL()+"/c/document_library/get_file?uuid="+fileEntry.getUuid()+"&groupId="+themeDisplay.getScopeGroupId();
		Map<String, Object> fileParams = new HashMap<String, Object>();
		fileParams.put("attachmentId", fileEntry.getFileEntryId());
		fileParams.put("attachmentURL", fileDownloadUrl);
		fileParams.put("attachmentName", fileEntry.getTitle());
		return fileParams;
	}
	
	protected abstract void addNodes(UploadPortletRequest uploadRequest, Map<String, Object> fileParams);
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

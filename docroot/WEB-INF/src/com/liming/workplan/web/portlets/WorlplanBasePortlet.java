package com.liming.workplan.web.portlets;

import java.io.IOException;

import javax.portlet.PortletException;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import com.liferay.portal.util.PortalUtil;
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
		}  else if(Constants.WorlplanBasePortlet_RESOURCE_CMD_EXPORT.equals(cmd)) {
			doExport(resourceRequest, resourceResponse);
		}
	}
	
	protected abstract void addNodes(ResourceRequest resourceRequest);
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

package com.liming.workplan.service;

import java.util.List;
import java.util.Map;

import com.liferay.portal.kernel.bean.PortletBeanLocatorUtil;

public class ResearchProjectServiceUtil {
	private static ResearchProjectService service;
	
	public static ResearchProjectService getService() {
		if(service == null) {
			service = (ResearchProjectService)PortletBeanLocatorUtil.locate("WorkPlanManagement-portlet", "ResearchProjectService");
		}
		
		return service;
	}
	
}

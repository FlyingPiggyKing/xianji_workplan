package com.liming.workplan.utils;

import javax.portlet.ResourceRequest;
import javax.servlet.http.HttpServletRequest;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;

public class UserThreadLocal {
	private static ThreadLocal<User> userThreadLocal = new ThreadLocal<User>();
	public static User getCurrentUser() {
		return userThreadLocal.get();
	}
	
	public static boolean setCurrentUser(HttpServletRequest request) {
		User currentUser = null;
		try {
			currentUser = PortalUtil.getUser(request);
		} catch (PortalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(currentUser == null) {
			return false;
		} else {
			userThreadLocal.set(currentUser);
		}
		return true;
	}
}

package com.liming.workplan.utils;

import java.util.List;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.UserGroupRole;
import com.liferay.portal.service.UserGroupRoleLocalServiceUtil;

public class CurrentUserRoleThreadLocal {
	private static ThreadLocal<List<UserGroupRole>> userThreadLocal = new ThreadLocal<List<UserGroupRole>>();
	public static void setCurrentGroupRole(Long userId, Long groupId) {
		List<UserGroupRole> roles = null;
		try {
			roles = UserGroupRoleLocalServiceUtil.getUserGroupRoles(userId, groupId);
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		userThreadLocal.set(roles);
	}
	
	public static List<UserGroupRole> getCurrentGroupRole() {
		return userThreadLocal.get();
	}

}

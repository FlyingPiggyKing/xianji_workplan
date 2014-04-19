package com.liming.workplan.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;

import com.liferay.portal.model.User;
import com.liming.workplan.service.LanguageService;
import com.liming.workplan.utils.UserThreadLocal;

public class LanguageServiceImpl implements LanguageService, ApplicationContextAware {
	
	private MessageSource resources;

	@Override
	public void setApplicationContext(ApplicationContext appContent)
			throws BeansException {
		resources = appContent;
		
	}

	@Override
	public String getMessage(String key, Locale locale) {
		return resources.getMessage(key, null, key, locale);
	}
	
	public String getMessage(String key) {
		User currentUser = UserThreadLocal.getCurrentUser();
		Locale userLocale = currentUser.getLocale();
		return resources.getMessage(key, null, key, userLocale);
	}
	
	public List<String> getMessageForRow(List<String> row, Locale locale) {
		List<String> messageRow = new ArrayList<String>();
		String localeMessage = null;
		for(String cell : row) {
			localeMessage = getMessage(cell, locale);
			messageRow.add(localeMessage);
		}
		return messageRow;
	}
	
	public List<List<String>> getMessageForRows(List<List<String>> rows, Locale locale) {
		List<List<String>> messageRows = new ArrayList<List<String>>();
		for(List<String> messageRow : rows) {
			messageRows.add(getMessageForRow(messageRow, locale));
		}
		return messageRows;
	}
	
	public List<String[]> getLocalTableHeader(List<String> header) {
		User currentUser = UserThreadLocal.getCurrentUser();
		Locale userLocale = currentUser.getLocale();
		List<String[]> localHeader = new ArrayList<String[]>(header.size());
		for(String column : header) {
			String[] columnValue = new String[]{column, getMessage(column, userLocale)};
			localHeader.add(columnValue);
		}
		return localHeader;
	}

}

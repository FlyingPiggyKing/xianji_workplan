package com.liming.workplan.service.impl;

import java.util.Locale;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;

import com.liming.workplan.service.LanguageService;

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

}

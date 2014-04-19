package com.liming.workplan.service;

import java.util.List;
import java.util.Locale;

public interface LanguageService {
	public String getMessage(String key, Locale locale);
	public String getMessage(String key);
	public List<String> getMessageForRow(List<String> row, Locale locale);
	public List<List<String>> getMessageForRows(List<List<String>> rows, Locale locale);
	public List<String[]> getLocalTableHeader(List<String> header);
}

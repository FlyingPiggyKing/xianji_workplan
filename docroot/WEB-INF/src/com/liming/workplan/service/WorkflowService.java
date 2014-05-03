package com.liming.workplan.service;

import java.util.List;
import java.util.Map;

import com.liming.workplan.model.pojo.WorkflowNode;

public interface WorkflowService {
	public List<String> getRoleOrderList();
	public List<Object[]> getWorkflowsByRoleAndType(String nodeType, int pageNumber, int pageSize, String sortColumn, String sortOrder);
	public List<Map<String, String>> convertNodeToData(String nodeType, List<Object[]> workplanPojosRows);
	public List<Map<String, Object>> getWorkflowHeader(String nodeType);
	public void approveWorkflow(String nodeType, String ids[], List<Object[]> loadedEntities);
	public void rejectWorkflow(String nodeType, String ids[], List<Object[]> loadedEntities);
	public Long countWorkflow(String nodeType);
}

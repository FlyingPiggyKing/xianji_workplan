package com.liming.workplan.dao;

import java.util.List;

import com.liming.workplan.model.pojo.WorkPlanNode;
import com.liming.workplan.model.pojo.WorkflowNode;

public interface WorkflowDao {
	public List<Object[]> getWorkplanWSByRole(long roleId, String nodeType, int pageNumber, int pageSize, String sortColumn, String sortOrder, boolean isSortByWFCol);
	public void updateWorkflows(List<WorkflowNode> nodes);
	public void persist(WorkflowNode transientInstance);
	public Long countWorkplanWSByRole(long roleId, String nodeType);
	public void updateWorkplanNodes(List<WorkPlanNode> nodes);
}

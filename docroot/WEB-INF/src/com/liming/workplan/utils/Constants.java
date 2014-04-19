package com.liming.workplan.utils;

public interface Constants {
	public static final String WorkPlanManagementPortlet_RESOURCE_CMD = "resource_cmd";
	public static final String WorkPlanManagementPortlet_RESOURCE_CMD_ADD = "add";
	public static final String WorkPlanManagementPortlet_RESOURCE_CMD_DELETE = "delete";
	public static final String WorkPlanManagementPortlet_RESOURCE_CMD_PUBLISH_COUNT = "pcount";
	public static final String WorkPlanManagementPortlet_RESOURCE_CMD_UNPUBLISHED_COUNT = "upcount";
	public static final String WorkPlanManagementPortlet_RESOURCE_CMD_LOAD_PUBLISHED_NODES = "getPublishedNodes";
	public static final String WorkPlanManagementPortlet_RESOURCE_CMD_STATISTICS = "statistics";
	public static final String WorkPlanManagementPortlet_RESOURCE_CMD_EXPORT = "export";
	public static final String WorkPlanManagementPortlet_RESOURCE_CMD_LOAD_UNPUBLISHED_NODES = "getUnPublishedNodes";
	public static final String WorkPlanManagementPortlet_PARAM_IDS_KEY = "ids";
	
	public static final String WorkflowManagementPortlet_RESOURCE_CMD = "resource_cmd";
	public static final String WorkflowManagementPortlet_RESOURCE_CMD_APPROVE = "approve";
	public static final String WorkflowManagementPortlet_RESOURCE_CMD_REJECT = "reject";
	public static final String WorkflowManagementPortlet_RESOURCE_CMD_LOAD = "load";
	public static final String WorkflowManagementPortlet_RESOURCE_CMD_COUNT = "count";
	public static final String WorkflowManagementPortlet_PARAM_IDS_KEY = "ids";
	public static final String WorkflowManagementPortlet_PARAM_NODETYPE_KEY = "dataType";
	
	public static final String PORTLET_REQUEST_PARAM_PAGE_NUMBER = "pageNumber";
	public static final String PORTLET_REQUEST_PARAM_PAGE_SIZE = "pageSize";
	
	public static final String HQL_SELECT_COUNT = "select count(*) ";

	
	public static final String WorkPlanNode_STATUS_UNPUBLISH = "unpublish";
	public static final String WorkPlanNode_STATUS_PUBLISH = "publish";
	public static final String WorkPlanNode_STATUS_REJECTED = "rejected";
	public static final String WorkplanNode_STATUS = "status";
	
	public static final String VALUE_SEP = ",";
	public static final String ResearchProjectServiceImpl_NODE_TYPE = "ResearchProject";
	
	public static final String WorkflowNode_NODE_STATUS_ACTIVE = "active";
	public static final String WorkflowNode_NODE_STATUS_COMPLETED = "completed";
	
	public static final String BK = " ";
}

package com.liming.workplan.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.model.UserGroupRole;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.RoleServiceUtil;
import com.liming.workplan.dao.WorkflowDao;
import com.liming.workplan.model.pojo.ResearchProject;
import com.liming.workplan.model.pojo.WorkflowNode;
import com.liming.workplan.service.LanguageService;
import com.liming.workplan.service.ResearchProjectService;
import com.liming.workplan.service.WorkflowService;
import com.liming.workplan.utils.Constants;
import com.liming.workplan.utils.CurrentUserRoleThreadLocal;
import com.liming.workplan.utils.UserThreadLocal;
import com.liming.workplan.utils.WorkplanDataThreadLocal;

public class WorkflowServiceImpl implements WorkflowService {
	private List<String> roleOrderList;
	private WorkflowDao workflowDao;
	private ResearchProjectService researchProjectService;
	private final String NODE_TYPE_RESEARCH_PROJECT = "ResearchProject";
	private LanguageService languageService;
	private enum WorkflowColumn {
		APPROVER("approver"),
		APPROVEDDATE("approvedDate");
		
		private final String value;
        private WorkflowColumn(String value) {
            this.value = value;
        }
        public String value() {
        	return value;
        }
	}

	@Override
	public void submitWorkflow(WorkflowNode newNode) {
		// TODO Auto-generated method stub
		
	}
	
	public List<Object[]> getWorkflowsByRoleAndType(String nodeType, int pageNumber, int pageSize, String sortColumn, String sortOrder) {
		long roleId = getCurrentUserRole();
		List<Object[]> workplanRows  = new ArrayList<Object[]>();
		if(roleId == -1) {
			return workplanRows;
		}
//		List<String> workflowCells = new ArrayList(WorkflowColumn.values().length);
//		for(WorkflowColumn cell : WorkflowColumn.values()) {
//			workflowCells.add(cell.value());
//		}
//		List<String> workplanHeader = null;
//		List<Object[]> workplanRows = null;
		
//		if(NODE_TYPE_RESEARCH_PROJECT.equals(nodeType)) {
		boolean isSortByWorkflowColumn = false;
		if(WorkflowColumn.APPROVEDDATE.value().equals(sortColumn) || WorkflowColumn.APPROVER.value().equals(sortColumn)) {
			isSortByWorkflowColumn = true;
		}
			workplanRows = workflowDao.getWorkplanWSByRole(roleId, nodeType, pageNumber, pageSize, sortColumn, sortOrder, isSortByWorkflowColumn);
			
			List<Map<String, String>> displayData = convertNodeToData(nodeType, workplanRows);
			WorkplanDataThreadLocal.setDisplayData(nodeType, displayData);
//		}
		
		return workplanRows;
	}

	private long getCurrentUserRole() {
		long roleId = -1;//12904;//RoleServiceUtil.getUserRoles(currentUser.getUserId())
		List<UserGroupRole> roles = CurrentUserRoleThreadLocal.getCurrentGroupRole();
		for(int roleIndex = roleOrderList.size() - 1; roleIndex >= 0; roleIndex--) {
			for(UserGroupRole userGroupRole : roles) {
				try {
					if(userGroupRole.getRole().getName().equals(roleOrderList.get(roleIndex))) {
						roleId = userGroupRole.getRole().getRoleId();
						break;
					}
				} catch (PortalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SystemException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(roleId != -1) {
					break;
				}
			}
		}
		return roleId;
	}
	
	public void approveWorkflow(String nodeType, String ids[], List<Object[]> loadedEntities) {
		User currentUser = UserThreadLocal.getCurrentUser();
		List<ResearchProject> changedNodes = new ArrayList<ResearchProject>();
		for(String id : ids) {
			for(Object[] entity : loadedEntities) {
				
				if(NODE_TYPE_RESEARCH_PROJECT.equals(nodeType)) {
					ResearchProject researchProject = (ResearchProject )entity[0];
					if(researchProject.getNodeId() == Integer.valueOf(id)) {
						long approvingRoleId = researchProject.getWorkflowNode().getApprovingRoleId();
						String approvingRoleName = null;
						try {
							
							//locate the current workflow status
							Role role = RoleLocalServiceUtil.getRole(approvingRoleId);
							approvingRoleName = role.getName();
							
							int currentRoleIndex = -1;
							for(int roleIndex = 0; roleIndex < roleOrderList.size(); roleIndex++) {
								if(roleOrderList.get(roleIndex).equals(approvingRoleName)) {
									currentRoleIndex = roleIndex;
									break;
								}
							}
							//change workflow status if necessory
							if(currentRoleIndex != -1) {
								WorkflowNode workflowNode = researchProject.getWorkflowNode();
								if(currentRoleIndex < roleOrderList.size() - 1) {
									Role nextApprovingRole = RoleLocalServiceUtil.getRole(currentUser.getCompanyId(), roleOrderList.get(currentRoleIndex + 1));
									workflowNode.setApprovingRoleId(nextApprovingRole.getRoleId());
									
								} else {
									workflowNode.setStatus(Constants.WorkflowNode_NODE_STATUS_COMPLETED);
									researchProject.setStatus(Constants.WorkPlanNode_STATUS_PUBLISH);
								}
								workflowNode.setApprover(currentUser.getFullName());
								workflowNode.setApprovedDate(new Date());
							}
							changedNodes.add(researchProject);
						} catch (PortalException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SystemException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
								
						break;
					}
				}
			}
		}
		if(changedNodes.size() > 0) {
			researchProjectService.updateNodes(changedNodes);
		}
	
	}
	
	public void rejectWorkflow(String nodeType, String ids[], List<Object[]> loadedEntities) {
		User currentUser = UserThreadLocal.getCurrentUser();
		List<ResearchProject> changedNodes = new ArrayList<ResearchProject>();
		for(String id : ids) {
			for(Object[] entity : loadedEntities) {
				if(NODE_TYPE_RESEARCH_PROJECT.equals(nodeType)) {
					ResearchProject researchProject = (ResearchProject )entity[0];
					if(researchProject.getNodeId() == Integer.valueOf(id)) {
						WorkflowNode workflowNode = researchProject.getWorkflowNode();
						workflowNode.setStatus(Constants.WorkflowNode_NODE_STATUS_COMPLETED);
						researchProject.setStatus(Constants.WorkPlanNode_STATUS_REJECTED);
						workflowNode.setApprover(currentUser.getFullName());
						workflowNode.setApprovedDate(new Date());
						changedNodes.add(researchProject);
						break;
					}
				}
			}
		}
		if(changedNodes.size() > 0) {
			researchProjectService.updateNodes(changedNodes);
		}
	}
	
	public Long countWorkflow(String nodeType) {
		long roleId = getCurrentUserRole();
		return workflowDao.countWorkplanWSByRole(roleId, nodeType);
	}
	
	public List<Map<String, String>> convertNodeToData(String nodeType, List<Object[]> workplanPojosRows) {
		List<Map<String, String>> result = new ArrayList<Map<String, String>>(workplanPojosRows.size());
		int cellCount = WorkflowColumn.values().length;
		if(NODE_TYPE_RESEARCH_PROJECT.equals(nodeType)) {
			cellCount += researchProjectService.getPublishedTableHeader().size();
			for(int rowIndex = 0 ; rowIndex < workplanPojosRows.size(); rowIndex++) {
				Object[] entites = (Object[])workplanPojosRows.get(rowIndex);
				Map<String, String> valueMap = new HashMap<String, String>(cellCount);
				researchProjectService.fillDisplayTable((ResearchProject)entites[0], valueMap);
				fillWorkflowCell((WorkflowNode)entites[1], valueMap);
				result.add(valueMap);
			}
		}
		
		return result;
	}
	
	private void fillWorkflowCell(WorkflowNode node, Map<String, String> valueMap) {
		
		valueMap.put(WorkflowColumn.APPROVER.value(), node.getApprover());
		Date approveDate = node.getApprovedDate();
		if(approveDate != null) {
			valueMap.put(WorkflowColumn.APPROVEDDATE.value(), approveDate.toGMTString());
		} else {
			valueMap.put(WorkflowColumn.APPROVEDDATE.value(), null);
		}
		
		
	}

	@Override
	public List<String> getRoleOrderList() {
		// TODO Auto-generated method stub
		return roleOrderList;
	}

	public void setRoleOrderList(List<String> roleOrderList) {
		this.roleOrderList = roleOrderList;
	}

	public void setWorkflowDao(WorkflowDao workflowDao) {
		this.workflowDao = workflowDao;
	}

	public ResearchProjectService getResearchProjectService() {
		return researchProjectService;
	}

	public void setResearchProjectService(
			ResearchProjectService researchProjectService) {
		this.researchProjectService = researchProjectService;
	}

	@Override
	public List<String[]> getWorkflowHeader(String nodeType) {
		List<String[]> workplanHeader = null;
		if(NODE_TYPE_RESEARCH_PROJECT.equals(nodeType)) {
			workplanHeader = researchProjectService.getPublishedTableHeader();
		}
		List<String[]> workflowHeader = new ArrayList<String[]>(workplanHeader.size() + WorkflowColumn.values().length);
		workflowHeader.addAll(workplanHeader);
		User currentUser = UserThreadLocal.getCurrentUser();
		Locale userLocale = currentUser.getLocale();
		for(WorkflowColumn workflowNodeCell : WorkflowColumn.values()) {
			workflowHeader.add(new String[]{workflowNodeCell.value(), languageService.getMessage(workflowNodeCell.value(), userLocale)});
		}
		return workflowHeader;
	}

	public LanguageService getLanguageService() {
		return languageService;
	}

	public void setLanguageService(LanguageService languageService) {
		this.languageService = languageService;
	}

}

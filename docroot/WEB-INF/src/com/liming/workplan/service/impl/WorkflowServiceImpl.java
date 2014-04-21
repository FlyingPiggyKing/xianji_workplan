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
import com.liming.workplan.dao.WorkflowDao;
import com.liming.workplan.model.pojo.ResearchAchievement;
import com.liming.workplan.model.pojo.ResearchProject;
import com.liming.workplan.model.pojo.WorkPlanNode;
import com.liming.workplan.model.pojo.WorkflowNode;
import com.liming.workplan.service.LanguageService;
import com.liming.workplan.service.ResearchAchievementService;
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
	private ResearchAchievementService researchAchievementService;
	private final String NODE_TYPE_RESEARCH_PROJECT = "ResearchProject";
	private final String NODE_TYPE_RESEARCH_ACHIEVEMENT = "ResearchAchievement";
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


	public List<Object[]> getWorkflowsByRoleAndType(String nodeType, int pageNumber, int pageSize, String sortColumn, String sortOrder) {
		long roleId = getCurrentUserRole();
		List<Object[]> workplanRows  = new ArrayList<Object[]>();
		if(roleId == -1) {
			return workplanRows;
		}

		boolean isSortByWorkflowColumn = false;
		if(WorkflowColumn.APPROVEDDATE.value().equals(sortColumn) || WorkflowColumn.APPROVER.value().equals(sortColumn)) {
			isSortByWorkflowColumn = true;
		}
		workplanRows = workflowDao.getWorkplanWSByRole(roleId, nodeType, pageNumber, pageSize, sortColumn, sortOrder, isSortByWorkflowColumn);
		
		List<Map<String, String>> displayData = convertNodeToData(nodeType, workplanRows);
		WorkplanDataThreadLocal.setDisplayData(nodeType, displayData);
		
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
		List<WorkPlanNode> changedNodes = new ArrayList<WorkPlanNode>();
		for(String id : ids) {
			for(Object[] entity : loadedEntities) {
				WorkPlanNode node = (WorkPlanNode)entity[0];
				if(node.getNodeId() == Integer.valueOf(id)) {
					WorkflowNode workflowNode = node.getWorkflowNode();
					long approvingRoleId = workflowNode.getApprovingRoleId();
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
							if(currentRoleIndex < roleOrderList.size() - 1) {
								Role nextApprovingRole = RoleLocalServiceUtil.getRole(currentUser.getCompanyId(), roleOrderList.get(currentRoleIndex + 1));
								workflowNode.setApprovingRoleId(nextApprovingRole.getRoleId());
								
							} else {
								workflowNode.setStatus(Constants.WorkflowNode_NODE_STATUS_COMPLETED);
								node.setStatus(Constants.WorkPlanNode_STATUS_PUBLISH);
							}
							workflowNode.setApprover(currentUser.getFullName());
							workflowNode.setApprovedDate(new Date());
							changedNodes.add(node);
						} else {
							//current user do not have permission to chage the workflow
						}
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
		if(changedNodes.size() > 0) {
			workflowDao.updateWorkplanNodes(changedNodes);
		}
	
	}
	
	public void rejectWorkflow(String nodeType, String ids[], List<Object[]> loadedEntities) {
		User currentUser = UserThreadLocal.getCurrentUser();
		List<WorkPlanNode> changedNodes = new ArrayList<WorkPlanNode>();
		for(String id : ids) {
			for(Object[] entity : loadedEntities) {

				WorkPlanNode node = (WorkPlanNode)entity[0];
				if(node.getNodeId() == Integer.valueOf(id)) {
					WorkflowNode workflowNode = node.getWorkflowNode();
					workflowNode.setStatus(Constants.WorkflowNode_NODE_STATUS_COMPLETED);
					node.setStatus(Constants.WorkPlanNode_STATUS_REJECTED);
					workflowNode.setApprover(currentUser.getFullName());
					workflowNode.setApprovedDate(new Date());
					changedNodes.add(node);
					break;
				}
			}
		}
		if(changedNodes.size() > 0) {
			workflowDao.updateWorkplanNodes(changedNodes);
		}
	}
	
	public Long countWorkflow(String nodeType) {
		long roleId = getCurrentUserRole();
		return workflowDao.countWorkplanWSByRole(roleId, nodeType);
	}
	
	public List<Map<String, String>> convertNodeToData(String nodeType, List<Object[]> workplanPojosRows) {
		List<Map<String, String>> result = new ArrayList<Map<String, String>>(workplanPojosRows.size());
		
		for(int rowIndex = 0 ; rowIndex < workplanPojosRows.size(); rowIndex++) {
			Object[] entites = (Object[])workplanPojosRows.get(rowIndex);
			Map<String, String> valueMap = new HashMap<String, String>();
			fillWorkplanByType(nodeType, entites, valueMap);
			
			fillWorkflowCell((WorkflowNode)entites[1], valueMap);
			result.add(valueMap);
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
		workplanHeader = getWorkplanHeaderByType(nodeType, workplanHeader);
		List<String[]> workflowHeader = new ArrayList<String[]>(workplanHeader.size() + WorkflowColumn.values().length);
		workflowHeader.addAll(workplanHeader);
		User currentUser = UserThreadLocal.getCurrentUser();
		Locale userLocale = currentUser.getLocale();
		for(WorkflowColumn workflowNodeCell : WorkflowColumn.values()) {
			workflowHeader.add(new String[]{workflowNodeCell.value(), languageService.getMessage(workflowNodeCell.value(), userLocale)});
		}
		return workflowHeader;
	}
	
	private void fillWorkplanByType(String nodeType, Object[] entites,
			Map<String, String> valueMap) {
		if(NODE_TYPE_RESEARCH_PROJECT.equals(nodeType)) {
			researchProjectService.fillDisplayTable((ResearchProject)entites[0], valueMap);
		} else if(NODE_TYPE_RESEARCH_ACHIEVEMENT.equals(nodeType)) {
			researchAchievementService.fillDisplayTable((ResearchAchievement)entites[0], valueMap);
		}
	}
	
	private List<String[]> getWorkplanHeaderByType(String nodeType,
			List<String[]> workplanHeader) {
		if(NODE_TYPE_RESEARCH_PROJECT.equals(nodeType)) {
			workplanHeader = researchProjectService.getPublishedTableHeader();
		} else if(NODE_TYPE_RESEARCH_ACHIEVEMENT.equals(nodeType)) {
			workplanHeader = researchAchievementService.getPublishedTableHeader();
		}
		return workplanHeader;
	}
	
	public LanguageService getLanguageService() {
		return languageService;
	}

	public void setLanguageService(LanguageService languageService) {
		this.languageService = languageService;
	}

	public ResearchAchievementService getResearchAchievementService() {
		return researchAchievementService;
	}

	public void setResearchAchievementService(
			ResearchAchievementService researchAchievementService) {
		this.researchAchievementService = researchAchievementService;
	}

}

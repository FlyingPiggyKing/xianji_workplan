package com.liming.workplan.dao.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.liming.workplan.dao.WorkflowDao;
import com.liming.workplan.model.pojo.WorkflowNode;
import com.liming.workplan.utils.Constants;

public class WorkflowDaoImpl implements WorkflowDao {
	
	private static final Log log = LogFactory.getLog(WorkflowDaoImpl.class);
	private static final String PARAM_ROLE_ID = "approvingRoleId";
	private static final String PARAM_NODE_TYPE = "nodeType";
	private static final String PARAM_NODE_STATUS = "wf_status";
	
	

	
	private SessionFactory sessionFactory;

//	@Override
//	public List<Object[]> getWorkplanWSByRole(long roleId, String nodeType, List<String> columns, List<String> workflowColumns, int pageNumber, int pageSize) {
//		Session session = sessionFactory.getCurrentSession();
//		StringBuilder hqlBuilder = new StringBuilder();
//		hqlBuilder.append("select ");
//		for(int colIndex = 0; colIndex < columns.size(); colIndex++) {
//			hqlBuilder.append("wp." + columns.get(colIndex));
//			hqlBuilder.append(", ");
//		}
//		for(int colIndex = 0; colIndex < workflowColumns.size(); colIndex++) {
//			hqlBuilder.append("wf." + workflowColumns.get(colIndex));
//			if(colIndex != workflowColumns.size() -1) {
//				hqlBuilder.append(", ");
//			}
//		}
//		hqlBuilder.append(" from ");
//		hqlBuilder.append(nodeType + " wp inner join wp.workflowNode wf where wf.approvingRoleId = :"
//			+ PARAM_ROLE_ID + " and wf.nodeType = :" + PARAM_NODE_TYPE);
//		if(log.isDebugEnabled()) {
//			log.debug(hqlBuilder.toString());
//		}
//		
//		Query query = session.createQuery(hqlBuilder.toString());
//		query.setParameter(PARAM_ROLE_ID, roleId);
//		query.setParameter(PARAM_NODE_TYPE, nodeType);
//		query.setFirstResult(pageSize * (pageNumber - 1));
//		query.setMaxResults(pageSize);
//		List<Object[]> result = (List<Object[]>)query.list();
//		return result;
//	}
	
	@Override
	public List<Object[]> getWorkplanWSByRole(long roleId, String nodeType, int pageNumber, int pageSize, String sortColumn, String sortOrder, boolean isSortByWFCol) {
		Session session = sessionFactory.getCurrentSession();
		StringBuilder hqlBuilder = new StringBuilder();
		hqlBuilder.append("from ");
		hqlBuilder.append(nodeType + " wp inner join wp.workflowNode wf ");
		hqlBuilder.append("where wp.status = :" + Constants.WorkplanNode_STATUS + " and wf.approvingRoleId = :"
			+ PARAM_ROLE_ID + " and wf.nodeType = :" + PARAM_NODE_TYPE + " and wf.status = :" + PARAM_NODE_STATUS);
		appendSortClause(sortColumn, sortOrder, hqlBuilder, isSortByWFCol);
		if(log.isDebugEnabled()) {
			log.debug(hqlBuilder.toString());
		}
		
		Query query = session.createQuery(hqlBuilder.toString());
		query.setParameter(PARAM_ROLE_ID, roleId);
		query.setParameter(PARAM_NODE_TYPE, nodeType);
		query.setParameter(Constants.WorkplanNode_STATUS, Constants.WorkPlanNode_STATUS_UNPUBLISH);
		query.setParameter(PARAM_NODE_STATUS, Constants.WorkflowNode_NODE_STATUS_ACTIVE);
		query.setFirstResult(pageSize * (pageNumber - 1));
		query.setMaxResults(pageSize);
		List<Object[]> result = (List<Object[]>)query.list();
		return result;
	}
	
	private void appendSortClause(String sortColumn, String order,
			StringBuilder hqlBuilder, boolean isSortByWFCol) {
		if(sortColumn != null) {
			if(isSortByWFCol) {
				hqlBuilder.append(" order by wf.");
			} else {
				hqlBuilder.append(" order by wp.");
			}
			
			hqlBuilder.append(sortColumn);
			if(order != null && "desc".equals(order)) {
				hqlBuilder.append(" desc");
			}
		}
	}
	
	public Long countWorkplanWSByRole(long roleId, String nodeType) {
		Session session = sessionFactory.getCurrentSession();
		StringBuilder hqlBuilder = new StringBuilder();
		hqlBuilder.append(Constants.HQL_SELECT_COUNT);
		hqlBuilder.append("from ");
		hqlBuilder.append(nodeType + " wp inner join wp.workflowNode wf ");
		hqlBuilder.append("where wp.status = :" + Constants.WorkplanNode_STATUS + " and wf.approvingRoleId = :"
			+ PARAM_ROLE_ID + " and wf.nodeType = :" + PARAM_NODE_TYPE + " and wf.status = :" + PARAM_NODE_STATUS);
		Query query = session.createQuery(hqlBuilder.toString());
		query.setParameter(PARAM_ROLE_ID, roleId);
		query.setParameter(PARAM_NODE_TYPE, nodeType);
		query.setParameter(Constants.WorkplanNode_STATUS, Constants.WorkPlanNode_STATUS_UNPUBLISH);
		query.setParameter(PARAM_NODE_STATUS, Constants.WorkflowNode_NODE_STATUS_ACTIVE);
		return (Long)query.uniqueResult();
	}
	
	public void updateWorkflows(List<WorkflowNode> nodes) {
//		Session session = sessionFactory.getCurrentSession();
//		for(Wor) {
//			
//		}
	}
	
	public void persist(WorkflowNode transientInstance) {
		log.debug("persisting WorkflowNode instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

}

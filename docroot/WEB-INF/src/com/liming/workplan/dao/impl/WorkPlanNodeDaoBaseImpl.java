package com.liming.workplan.dao.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.liming.workplan.model.pojo.ResearchProject;
import com.liming.workplan.utils.Constants;

public abstract class WorkPlanNodeDaoBaseImpl {
	
	private static final Log log = LogFactory.getLog(WorkPlanNodeDaoBaseImpl.class);
	private SessionFactory sessionFactory;
	
	private static final String HQL_FROM = "from ";
	private static final String HQL_COUNT = "select count(*) from ";
	private static final String PARAM_AUTHOR = "author";
	private static final String QUERY_BY_STATUS = " r where r.status = :" + Constants.WorkplanNode_STATUS;
	private static final String QUERY_BY_STATUS_AUTHOR = " r where r.status != :" + Constants.WorkplanNode_STATUS + " and r.author = :" + PARAM_AUTHOR;
	private static final String COUNT_BY_STATUS = " r where r.status = :" + Constants.WorkplanNode_STATUS;
	private static final String COUNT_BY_STATUS_AUTHOR = " r where r.status != :" + Constants.WorkplanNode_STATUS + " and r.author = :" + PARAM_AUTHOR;
	
	public void persist(Object transientInstance) {
		log.debug("persisting ResearchProject instance");
		try {
			sessionFactory.getCurrentSession().save(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}
	
	public List<Object> getPublishedResearchPorjects(String type, Map<String, Object> searchObj, int pageNumber, int pageSize, String sortColumn, String order) {
		StringBuilder hqlBuilder = new StringBuilder();
		hqlBuilder.append(HQL_FROM);
		hqlBuilder.append(type);
		hqlBuilder.append(QUERY_BY_STATUS);
		Set<String> keys = searchObj.keySet();
		for(String key : keys) {
			hqlBuilder.append(" and r.");
			hqlBuilder.append(key);
			hqlBuilder.append(" = :" + key);
		}
		appendSortClause(sortColumn, order, hqlBuilder);
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery(hqlBuilder.toString());
		query.setParameter(Constants.WorkplanNode_STATUS, Constants.WorkPlanNode_STATUS_PUBLISH);
		for(String key : keys) {
			query.setParameter(key, searchObj.get(key));
		}
		query.setFirstResult(pageSize * (pageNumber - 1));
		query.setMaxResults(pageSize);
		List<Object> result = (List<Object>)query.list();
		return result;
	}
	
	public List<Object> getUnPublishedResearchPorjects(String type, long userId, int pageNumber, int pageSize, String sortColumn, String order) {
		Session session = sessionFactory.getCurrentSession();
		StringBuilder hqlBuilder = new StringBuilder();
		hqlBuilder.append(HQL_FROM);
		hqlBuilder.append(type);
		hqlBuilder.append(QUERY_BY_STATUS_AUTHOR);
		appendSortClause(sortColumn, order, hqlBuilder);
		Query query = session.createQuery(hqlBuilder.toString());
		query.setParameter(Constants.WorkplanNode_STATUS, Constants.WorkPlanNode_STATUS_PUBLISH);
		query.setParameter(PARAM_AUTHOR, userId);
		query.setFirstResult(pageSize * (pageNumber - 1));
		query.setMaxResults(pageSize);
		List<Object> result = (List<Object>)query.list();
		return result;
	}
	
	public Long getPublishedCount(String type, Map<String, Object> searchObj) {
		StringBuilder hqlBuilder = new StringBuilder();
		hqlBuilder.append(HQL_COUNT);
		hqlBuilder.append(type);
		hqlBuilder.append(COUNT_BY_STATUS);
		Set<String> keys = searchObj.keySet();
		for(String key : keys) {
			hqlBuilder.append(" and r.");
			hqlBuilder.append(key);
			hqlBuilder.append(" = :" + key);
		}
		Session session = getSessionFactory().getCurrentSession();
		Query query = session.createQuery(hqlBuilder.toString());
		query.setParameter(Constants.WorkplanNode_STATUS, Constants.WorkPlanNode_STATUS_PUBLISH);
		for(String key : keys) {
			query.setParameter(key, searchObj.get(key));
		}
		Long count = (Long)query.uniqueResult();
		return count;
	}
	
	
	public Long getUnPublishedCount(String type, long userId) {
		StringBuilder hqlBuilder = new StringBuilder();
		hqlBuilder.append(HQL_COUNT);
		hqlBuilder.append(type);
		hqlBuilder.append(COUNT_BY_STATUS_AUTHOR);
		
		Session session = getSessionFactory().getCurrentSession();
		Query query = session.createQuery(hqlBuilder.toString());
		query.setParameter(Constants.WorkplanNode_STATUS, Constants.WorkPlanNode_STATUS_PUBLISH);
		query.setParameter(PARAM_AUTHOR, userId);
		Long count = (Long)query.uniqueResult();
		return count;
	}
	
	private void appendSortClause(String sortColumn, String order,
			StringBuilder hqlBuilder) {
		if(sortColumn != null) {
			hqlBuilder.append(" order by r.");
			hqlBuilder.append(sortColumn);
			if(order != null && "desc".equals(order)) {
				hqlBuilder.append(" desc");
			}
		}
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
}

package com.liming.workplan.dao.impl;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.liming.workplan.dao.ResearchProjectDao;
import com.liming.workplan.model.pojo.ResearchProject;
import com.liming.workplan.utils.Constants;

public class ResearchProjectDaoImpl implements ResearchProjectDao {
	private static final Log log = LogFactory.getLog(ResearchProjectDaoImpl.class);
	private static final String PARAM_AUTHOR = "author";
	private static final String PARAM_ID = "nodeId";
	private static final String QUERY_BY_STATUS = "from ResearchProject r where r.status = :" + Constants.WorkplanNode_STATUS;
	private static final String QUERY_STATISTICS = "select sum(r.projectFunding) ";
	private static final String QUERY_BY_STATUS_AUTHOR = "from ResearchProject r where r.status != :" + Constants.WorkplanNode_STATUS + " and r.author = :" + PARAM_AUTHOR;
	private static final String COUNT_BY_STATUS = "select count(*) from ResearchProject r where r.status = :" + Constants.WorkplanNode_STATUS;
	private static final String COUNT_BY_STATUS_AUTHOR = "select count(*) from ResearchProject r where r.status != :" + Constants.WorkplanNode_STATUS + " and r.author = :" + PARAM_AUTHOR;
	private static final String DELETE_BY_ID_AUTHOR = "delete from ResearchProject where nodeId = :" + PARAM_ID + " and author = :" + PARAM_AUTHOR;
	
	private SessionFactory sessionFactory;
	
	public ResearchProject create() {
		return new ResearchProject();
	}
	
	public void persist(ResearchProject transientInstance) {
		log.debug("persisting ResearchProject instance");
		try {
			sessionFactory.getCurrentSession().save(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}
	
	public List<ResearchProject> getPublishedResearchPorjects(Map<String, Object> searchObj, int pageNumber, int pageSize, String sortColumn, String order) {
		StringBuilder hqlBuilder = new StringBuilder();
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
		List<ResearchProject> result = (List<ResearchProject>)query.list();
		return result;
	}
	
	public Long getPublishedCount(Map<String, Object> searchObj) {
		StringBuilder hqlBuilder = new StringBuilder();
		hqlBuilder.append(COUNT_BY_STATUS);
		Set<String> keys = searchObj.keySet();
		for(String key : keys) {
			hqlBuilder.append(" and r.");
			hqlBuilder.append(key);
			hqlBuilder.append(" = :" + key);
		}
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery(hqlBuilder.toString());
		query.setParameter(Constants.WorkplanNode_STATUS, Constants.WorkPlanNode_STATUS_PUBLISH);
		for(String key : keys) {
			query.setParameter(key, searchObj.get(key));
		}
		Long count = (Long)query.uniqueResult();
		return count;
	}
	
	public List<ResearchProject> getUnPublishedResearchPorjects(long userId, int pageNumber, int pageSize, String sortColumn, String order) {
		Session session = sessionFactory.getCurrentSession();
		StringBuilder hqlBuilder = new StringBuilder();
		hqlBuilder.append(QUERY_BY_STATUS_AUTHOR);
		appendSortClause(sortColumn, order, hqlBuilder);
		Query query = session.createQuery(hqlBuilder.toString());
		query.setParameter(Constants.WorkplanNode_STATUS, Constants.WorkPlanNode_STATUS_PUBLISH);
		query.setParameter(PARAM_AUTHOR, userId);
		query.setFirstResult(pageSize * (pageNumber - 1));
		query.setMaxResults(pageSize);
		List<ResearchProject> result = (List<ResearchProject>)query.list();
		return result;
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
	
	public void deleteResearchProjects(String[] ids, long userId) {
		Session session = sessionFactory.getCurrentSession();
		for(String id : ids) {
			Query query = session.createQuery(DELETE_BY_ID_AUTHOR);
			query.setParameter(PARAM_ID, Integer.valueOf(id));
			query.setParameter(PARAM_AUTHOR, userId);
			query.executeUpdate();
		}
	}
	
	public void updateNodes(List<ResearchProject> nodes) {
		Session session = sessionFactory.getCurrentSession();
		for(ResearchProject node : nodes) {
			session.update(node);
		}
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public Long getUnPublishedCount(long userId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery(COUNT_BY_STATUS_AUTHOR);
		query.setParameter(Constants.WorkplanNode_STATUS, Constants.WorkPlanNode_STATUS_PUBLISH);
		query.setParameter(PARAM_AUTHOR, userId);
		Long count = (Long)query.uniqueResult();
		return count;
	}

	@Override
	public Map<String, String> getStatistics(Map<String, Object> searchObj) {
		StringBuilder hqlBuilder = new StringBuilder();
		hqlBuilder.append(QUERY_STATISTICS);
		hqlBuilder.append(QUERY_BY_STATUS);
		Set<String> keys = searchObj.keySet();
		for(String key : keys) {
			hqlBuilder.append(" and r.");
			hqlBuilder.append(key);
			hqlBuilder.append(" = :" + key);
		}
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery(hqlBuilder.toString());
		query.setParameter(Constants.WorkplanNode_STATUS, Constants.WorkPlanNode_STATUS_PUBLISH);
		for(String key : keys) {
			query.setParameter(key, searchObj.get(key));
		}
		Double statistics = (Double)query.uniqueResult();
		Map<String, String> statisticsResult = new HashMap<String, String>(1);
		statisticsResult.put("projectFunding", Double.toString(statistics));
		return statisticsResult;
	}
}
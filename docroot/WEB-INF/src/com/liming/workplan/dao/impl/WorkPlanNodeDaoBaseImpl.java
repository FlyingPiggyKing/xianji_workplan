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

public class WorkPlanNodeDaoBaseImpl {
	
	private static final Log log = LogFactory.getLog(WorkPlanNodeDaoBaseImpl.class);
	private SessionFactory sessionFactory;
	
	private static final String HQL_FROM = "from ";
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
	
}

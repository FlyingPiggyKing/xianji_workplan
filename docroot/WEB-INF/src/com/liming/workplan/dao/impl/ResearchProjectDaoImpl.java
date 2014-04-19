package com.liming.workplan.dao.impl;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;

import com.liming.workplan.dao.ExportDao;
import com.liming.workplan.dao.ResearchProjectDao;
import com.liming.workplan.model.pojo.ResearchProject;
import com.liming.workplan.utils.Constants;

public class ResearchProjectDaoImpl extends WorkPlanNodeDaoBaseImpl implements ResearchProjectDao, ExportDao {
	private static final Log log = LogFactory.getLog(ResearchProjectDaoImpl.class);
	
	
	private static final String QUERY_BY_STATUS = "from ResearchProject r where r.status = :" + Constants.WorkplanNode_STATUS;
	private static final String QUERY_STATISTICS = "select sum(r.projectFunding) ";
	

	
	public ResearchProject create() {
		return new ResearchProject();
	}
	
	public void updateNodes(List<ResearchProject> nodes) {
		Session session = getSessionFactory().getCurrentSession();
		for(ResearchProject node : nodes) {
			session.update(node);
		}
	}


	@Override
	public Map<String, Object> getStatistics(Map<String, Object> searchObj) {
		StringBuilder hqlBuilder = new StringBuilder();
		hqlBuilder.append(QUERY_STATISTICS);
		hqlBuilder.append(QUERY_BY_STATUS);
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
		Object statistics = (Double)query.uniqueResult();
		Map<String, Object> statisticsResult = new HashMap<String, Object>(1);
		statisticsResult.put("projectFunding", statistics);
		return statisticsResult;
	}
}

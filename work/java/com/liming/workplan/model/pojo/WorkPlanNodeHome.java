package com.liming.workplan.model.pojo;

// Generated 2014-4-27 11:57:44 by Hibernate Tools 4.0.0

import java.util.List;
import javax.naming.InitialContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;

/**
 * Home object for domain model class WorkPlanNode.
 * @see com.liming.workplan.model.pojo.WorkPlanNode
 * @author Hibernate Tools
 */
public class WorkPlanNodeHome {

	private static final Log log = LogFactory.getLog(WorkPlanNodeHome.class);

	private final SessionFactory sessionFactory = getSessionFactory();

	protected SessionFactory getSessionFactory() {
		try {
			return (SessionFactory) new InitialContext()
					.lookup("SessionFactory");
		} catch (Exception e) {
			log.error("Could not locate SessionFactory in JNDI", e);
			throw new IllegalStateException(
					"Could not locate SessionFactory in JNDI");
		}
	}

	public void persist(WorkPlanNode transientInstance) {
		log.debug("persisting WorkPlanNode instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(WorkPlanNode instance) {
		log.debug("attaching dirty WorkPlanNode instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(WorkPlanNode instance) {
		log.debug("attaching clean WorkPlanNode instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(WorkPlanNode persistentInstance) {
		log.debug("deleting WorkPlanNode instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public WorkPlanNode merge(WorkPlanNode detachedInstance) {
		log.debug("merging WorkPlanNode instance");
		try {
			WorkPlanNode result = (WorkPlanNode) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public WorkPlanNode findById(int id) {
		log.debug("getting WorkPlanNode instance with id: " + id);
		try {
			WorkPlanNode instance = (WorkPlanNode) sessionFactory
					.getCurrentSession().get(
							"com.liming.workplan.model.pojo.WorkPlanNode", id);
			if (instance == null) {
				log.debug("get successful, no instance found");
			} else {
				log.debug("get successful, instance found");
			}
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(WorkPlanNode instance) {
		log.debug("finding WorkPlanNode instance by example");
		try {
			List results = sessionFactory
					.getCurrentSession()
					.createCriteria(
							"com.liming.workplan.model.pojo.WorkPlanNode")
					.add(Example.create(instance)).list();
			log.debug("find by example successful, result size: "
					+ results.size());
			return results;
		} catch (RuntimeException re) {
			log.error("find by example failed", re);
			throw re;
		}
	}
}

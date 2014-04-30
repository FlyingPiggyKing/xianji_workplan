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
 * Home object for domain model class ResearchAchievement.
 * @see com.liming.workplan.model.pojo.ResearchAchievement
 * @author Hibernate Tools
 */
public class ResearchAchievementHome {

	private static final Log log = LogFactory
			.getLog(ResearchAchievementHome.class);

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

	public void persist(ResearchAchievement transientInstance) {
		log.debug("persisting ResearchAchievement instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(ResearchAchievement instance) {
		log.debug("attaching dirty ResearchAchievement instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(ResearchAchievement instance) {
		log.debug("attaching clean ResearchAchievement instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(ResearchAchievement persistentInstance) {
		log.debug("deleting ResearchAchievement instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public ResearchAchievement merge(ResearchAchievement detachedInstance) {
		log.debug("merging ResearchAchievement instance");
		try {
			ResearchAchievement result = (ResearchAchievement) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public ResearchAchievement findById(int id) {
		log.debug("getting ResearchAchievement instance with id: " + id);
		try {
			ResearchAchievement instance = (ResearchAchievement) sessionFactory
					.getCurrentSession()
					.get("com.liming.workplan.model.pojo.ResearchAchievement",
							id);
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

	public List findByExample(ResearchAchievement instance) {
		log.debug("finding ResearchAchievement instance by example");
		try {
			List results = sessionFactory
					.getCurrentSession()
					.createCriteria(
							"com.liming.workplan.model.pojo.ResearchAchievement")
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

package com.liming.workplan.service.aop;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.aspectj.lang.ProceedingJoinPoint;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class TransactionAOP {
	protected final static Logger log = Logger.getLogger(TransactionAOP.class.getName());
	private SessionFactory sessionFactory;
	
	public Object doInTransaction(ProceedingJoinPoint pjp) {
		Object retVal = null;
		Session session = null;
		long begin = System.currentTimeMillis();
		boolean needOpen = false;
		try {
			session = sessionFactory.getCurrentSession();
			
			if(!session.getTransaction().isActive()) {
				needOpen = true;
				session.beginTransaction();
			}
			
			if(log.isLoggable(Level.FINE)) {
				if(needOpen) {
					log.fine("Before Transaction-" + pjp.toLongString() + " begin transaction.");
				} else {
					log.fine("Before Transaction-" + pjp.toLongString());
				}
			}
			retVal = pjp.proceed();
			long afterExe = System.currentTimeMillis();
			if(log.isLoggable(Level.FINE)) {
				if(needOpen) {
					long time = afterExe - begin;
					log.fine("Execution Service consume: " + time);
				}
				
			}
			if(needOpen) {
				session.getTransaction().commit();
			}
			if(log.isLoggable(Level.FINE)) {
				if(needOpen) {
					long afterTransactionCommit = System.currentTimeMillis();
					long time = afterTransactionCommit - afterExe;
					log.fine("Commit transaction consume: " + time);
					log.fine("After Transaction-" + pjp.toLongString() + " close transaction.");
				} else {
					log.fine("After Transaction-" + pjp.toLongString());
				}
				
			}
		} catch (Throwable e) {
			System.out.print(e);
			if(needOpen) {
				session.getTransaction().rollback();
			}
		}
		return retVal;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
}
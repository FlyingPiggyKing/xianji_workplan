package com.liming.workplan.service;


import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BeanLocator {
	private BeanLocator() {
		
	}
	private static ApplicationContext ctx = new ClassPathXmlApplicationContext("config/spring/appContext.xml");

	public static ResearchProjectService getResearchProjectService() {
		return (ResearchProjectService)ctx.getBean(ResearchProjectService.class.getName());
	}
	
	public static ResearchAchievementService getResearchAchievementService() {
		return (ResearchAchievementService)ctx.getBean(ResearchAchievementService.class.getName());
	}
	
	public static WorkflowService getWorkflowService() {
		return (WorkflowService)ctx.getBean(WorkflowService.class.getName());
	}
}

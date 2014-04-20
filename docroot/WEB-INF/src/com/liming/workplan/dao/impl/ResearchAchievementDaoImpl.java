package com.liming.workplan.dao.impl;

import java.util.List;

import org.hibernate.Session;

import com.liming.workplan.dao.ResearchAchievementDao;
import com.liming.workplan.model.pojo.ResearchAchievement;

public class ResearchAchievementDaoImpl extends WorkPlanNodeDaoBaseImpl implements ResearchAchievementDao {

	public ResearchAchievement create() {
		return new ResearchAchievement();
	}
	
	public void updateNodes(List<ResearchAchievement> nodes) {
		Session session = getSessionFactory().getCurrentSession();
		for(ResearchAchievement node : nodes) {
			session.update(node);
		}
	}
}

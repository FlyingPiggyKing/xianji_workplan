package com.liming.workplan.model.pojo;

// Generated 2014-4-20 16:56:40 by Hibernate Tools 4.0.0

import java.util.Date;

/**
 * ResearchAchievement generated by hbm2java
 */
public class ResearchAchievement extends
		com.liming.workplan.model.pojo.WorkPlanNode implements
		java.io.Serializable {

	private String type;
	private String subType;
	private String achievementName;
	private String achievementAuthor;
	private String publishChannel;
	private String publishDetail;
	private String language;
	private String remark;

	public ResearchAchievement() {
	}

	public ResearchAchievement(Attachment attachment, WorkflowNode workflowNode) {
		super(attachment, workflowNode);
	}

	public ResearchAchievement(long author, Date createDate, String status,
			Attachment attachment, WorkflowNode workflowNode, String type,
			String subType, String achievementName, String achievementAuthor,
			String publishChannel, String publishDetail, String language,
			String remark) {
		super(author, createDate, status, attachment, workflowNode);
		this.type = type;
		this.subType = subType;
		this.achievementName = achievementName;
		this.achievementAuthor = achievementAuthor;
		this.publishChannel = publishChannel;
		this.publishDetail = publishDetail;
		this.language = language;
		this.remark = remark;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSubType() {
		return this.subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}

	public String getAchievementName() {
		return this.achievementName;
	}

	public void setAchievementName(String achievementName) {
		this.achievementName = achievementName;
	}

	public String getAchievementAuthor() {
		return this.achievementAuthor;
	}

	public void setAchievementAuthor(String achievementAuthor) {
		this.achievementAuthor = achievementAuthor;
	}

	public String getPublishChannel() {
		return this.publishChannel;
	}

	public void setPublishChannel(String publishChannel) {
		this.publishChannel = publishChannel;
	}

	public String getPublishDetail() {
		return this.publishDetail;
	}

	public void setPublishDetail(String publishDetail) {
		this.publishDetail = publishDetail;
	}

	public String getLanguage() {
		return this.language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getRemark() {
		return this.remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}

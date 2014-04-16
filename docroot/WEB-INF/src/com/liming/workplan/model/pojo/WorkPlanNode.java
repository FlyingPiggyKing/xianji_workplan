package com.liming.workplan.model.pojo;

// Generated 2014-4-16 14:57:46 by Hibernate Tools 4.0.0

import java.util.Date;

/**
 * WorkPlanNode generated by hbm2java
 */
public class WorkPlanNode implements java.io.Serializable {

	private int nodeId;
	private long author;
	private Date createDate;
	private String status;
	private Attachment attachment;
	private WorkflowNode workflowNode;

	public WorkPlanNode() {
	}

	public WorkPlanNode(Attachment attachment, WorkflowNode workflowNode) {
		this.attachment = attachment;
		this.workflowNode = workflowNode;
	}

	public WorkPlanNode(long author, Date createDate, String status,
			Attachment attachment, WorkflowNode workflowNode) {
		this.author = author;
		this.createDate = createDate;
		this.status = status;
		this.attachment = attachment;
		this.workflowNode = workflowNode;
	}

	public int getNodeId() {
		return this.nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	public long getAuthor() {
		return this.author;
	}

	public void setAuthor(long author) {
		this.author = author;
	}

	public Date getCreateDate() {
		return this.createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Attachment getAttachment() {
		return this.attachment;
	}

	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;
	}

	public WorkflowNode getWorkflowNode() {
		return this.workflowNode;
	}

	public void setWorkflowNode(WorkflowNode workflowNode) {
		this.workflowNode = workflowNode;
	}

}
package com.liming.workplan.model.pojo;

// Generated 2014-4-16 14:57:46 by Hibernate Tools 4.0.0

import java.util.HashSet;
import java.util.Set;

/**
 * Attachment generated by hbm2java
 */
public class Attachment implements java.io.Serializable {

	private String attachmentId;
	private String attachmentName;
	private String attachmentURL;
	private String typeDesc;
	private Set node = new HashSet(0);

	public Attachment() {
	}

	public Attachment(String attachmentId) {
		this.attachmentId = attachmentId;
	}

	public Attachment(String attachmentId, String attachmentName,
			String attachmentURL, String typeDesc, Set node) {
		this.attachmentId = attachmentId;
		this.attachmentName = attachmentName;
		this.attachmentURL = attachmentURL;
		this.typeDesc = typeDesc;
		this.node = node;
	}

	public String getAttachmentId() {
		return this.attachmentId;
	}

	public void setAttachmentId(String attachmentId) {
		this.attachmentId = attachmentId;
	}

	public String getAttachmentName() {
		return this.attachmentName;
	}

	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}

	public String getAttachmentURL() {
		return this.attachmentURL;
	}

	public void setAttachmentURL(String attachmentURL) {
		this.attachmentURL = attachmentURL;
	}

	public String getTypeDesc() {
		return this.typeDesc;
	}

	public void setTypeDesc(String typeDesc) {
		this.typeDesc = typeDesc;
	}

	public Set getNode() {
		return this.node;
	}

	public void setNode(Set node) {
		this.node = node;
	}

}

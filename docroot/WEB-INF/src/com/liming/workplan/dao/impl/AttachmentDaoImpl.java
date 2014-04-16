package com.liming.workplan.dao.impl;

import com.liming.workplan.dao.AttachmentDao;
import com.liming.workplan.model.pojo.Attachment;

public class AttachmentDaoImpl implements AttachmentDao {
	public Attachment create() {
		return new Attachment();
	}
}

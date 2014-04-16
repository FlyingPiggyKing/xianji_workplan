create table ATTACHMENT (attachmentId varchar(255) not null, attachmentName varchar(255), attachmentURL varchar(255), typeDesc varchar(255), primary key (attachmentId)) engine=InnoDB;
create table RESEARCH_PROJECT (Research_Porject_ID integer not null, type varchar(255), projectType varchar(255), projectName varchar(255), supportUnit varchar(255), projectLevel varchar(255), charger varchar(255), assistant varchar(255), projectFunding float, delegatedDepartment varchar(255), primary key (Research_Porject_ID)) engine=InnoDB;
create table WORK_FLOW_NODE (workflowId integer not null, HIBERNATE_OBJ_VERSION integer not null, workplanNodeId integer, status varchar(255), approver varchar(255), approvedDate date, approvingRoleId bigint, comment varchar(255), nodeType varchar(255), primary key (workflowId)) engine=InnoDB;
create table WORK_PLAN_NODE (nodeId integer not null, author bigint, createDate date, status varchar(255), attachmentId varchar(255) not null, workflowNodeId integer not null unique, primary key (nodeId)) engine=InnoDB;
alter table RESEARCH_PROJECT add index FKE9D564553C1773FB (Research_Porject_ID), add constraint FKE9D564553C1773FB foreign key (Research_Porject_ID) references WORK_PLAN_NODE (nodeId);
alter table WORK_PLAN_NODE add index FK3F2145AAF6E917BD (workflowNodeId), add constraint FK3F2145AAF6E917BD foreign key (workflowNodeId) references WORK_FLOW_NODE (workflowId);
alter table WORK_PLAN_NODE add index FK3F2145AA3D8C98E1 (attachmentId), add constraint FK3F2145AA3D8C98E1 foreign key (attachmentId) references ATTACHMENT (attachmentId);
create table workplan_management_sequence ( next_value integer );
insert into workplan_management_sequence values ( 0 );

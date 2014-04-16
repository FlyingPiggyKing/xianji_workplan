<%
/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */
%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>

<portlet:defineObjects />


<div id="<portlet:namespace/>workflow"></div>

<portlet:resourceURL var="resouceURL_submit">
</portlet:resourceURL>


<aui:script>
	YUI().use(
	  'workflowTableWidget', 'aui-button',
	  function(Y) {

		new Y.WorkflowNodeTable({resourceURL:'<%= resouceURL_submit %>'}).render('#<portlet:namespace/>workflow');

	  }
	);
</aui:script>
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
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<portlet:defineObjects />

<div id="<portlet:namespace/>container">
    <ul>
        <li><a href="#<portlet:namespace/>view"><liferay-ui:message key="display-record" /></a></li>
        <li><a href="#<portlet:namespace/>edit"><liferay-ui:message key="my-record" /></a></li>
    </ul>
    <div>
        <div id="<portlet:namespace/>view">
        	<div id="<portlet:namespace/>published">
        	</div>
        </div>
        <div id="<portlet:namespace/>edit">
        	<div id="<portlet:namespace/>addrecording">
        	</div>
        	<div id="<portlet:namespace/>unpublished">
        	</div>
        </div>
    </div>
</div>

<portlet:resourceURL var="resouceURL_submit"></portlet:resourceURL>
<portlet:resourceURL var="resouceURL_getUnPublished">
</portlet:resourceURL>
<portlet:resourceURL var="resouceURL_getPublished">
</portlet:resourceURL>

<aui:script>
	YUI().use(
	  'recordAdding', 'workplanTableWidget', 'myWorkplanTableWidget', 'aui-tabview',
	  function(Y) {
	  	new Y.TabView({
	        srcNode: '#<portlet:namespace/>container'
	    }).render();
	
		new Y.WorkplanNodeTable({resourceURL:'<%= resouceURL_getPublished %>',
			rowConfJson:'<%= renderRequest.getAttribute("SEARCH_CONFIG_JSON") %>'}).render('#<portlet:namespace/>published');
	    new Y.RecordAdding({resourceURL:'<%= resouceURL_submit %>', 
	    	rowConfJson:'<%= renderRequest.getAttribute("ROW_CONFIG_JSON") %>'}).render('#<portlet:namespace/>addrecording');
	    var myWorkplanNodeTable = new Y.MyWorkplanNodeTable({resourceURL:'<%= resouceURL_getUnPublished %>'}).render('#<portlet:namespace/>unpublished');
	    Y.on(Y.RecordAdding.EVENT_SUBMIT_COMPLETE, Y.bind(myWorkplanNodeTable.refreshFirstPage, myWorkplanNodeTable));
	  }
	);
</aui:script>
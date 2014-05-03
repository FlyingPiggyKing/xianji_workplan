YUI.add("workflowTableWidget", function(Y) {

	function WorkflowNodeTable(config) {
		WorkflowNodeTable.superclass.constructor.apply(this, arguments);
	}
	
	WorkflowNodeTable.NAME = "workflowNode";
	WorkflowNodeTable.CHECKBOX_UNCHECKED = '<input type="checkbox" class="rowSelect" />';
	WorkflowNodeTable.CHECKBOX_CHECKED = '<input type="checkbox" class="rowSelect" checked />';
	WorkflowNodeTable.BUTTON_APPROVE = '<button id="approveButton">通过</button>';
	WorkflowNodeTable.BUTTON_REJECT = '<button id="rejectButton">拒绝</button>';
	WorkflowNodeTable.CHECK_COLUMN = 'checkbox';
	WorkflowNodeTable.DIALOG_CONTAINER = '<div class="dialogContainer" />';
	WorkflowNodeTable.SELECT_TYPE_TEMPLATE = '<select>' +
		'<option name=\'ResearchProject\'>科研项目</option>' + 
		'<option name=\'ResearchAchievement\'>科研成果</option>' + 
		'<option name=\'test\'>组织学术会议</option>' + 
		'<option name=\'test\'>参加学术会议和培训进修</option>' + 
		'<option name=\'test\'>开展学术交流</option>' + 
		'<option name=\'test\'>学术传播</option>' + 
		'<option name=\'test\'>担任专家</option>' + 
		'</select>';
	
	WorkflowNodeTable.ATTRS = {
		approveButton : {value:null},
		rejectButton : {value:null},
		nodeType : {value:'ResearchProject'}
	};
	
	Y.WorkflowNodeTable = Y.extend(WorkflowNodeTable, Y.BaseTableWidget, {
		
		initializer: function(cfg) {
	    	this.domHelper = new Y.WorkplanNodeDomHelper({});
	    },
	    
		setTableColumns : function(header) {
			for(var headerIndex = 0; headerIndex < header.length; headerIndex++) {
				//format url header.
				//every url should be one line.
				if(header[headerIndex].key == "typeDesc") {
					header[headerIndex] = {
						key: 'typeDesc',
						allowHTML: true,
						label:header[headerIndex].label,
						remoteSortable:header[headerIndex].remoteSortable,
						nodeFormatter: this.domHelper.formatFileDesc	
					}
					
				} else if(header[headerIndex].key == "attachmentName") {
					header[headerIndex] = {
						key: 'attachmentName',
						allowHTML: true,
						label:header[headerIndex].label,
						remoteSortable:header[headerIndex].remoteSortable,
						nodeFormatter: this.domHelper.formatFileLink	
					}
				}
			}
			var checkColumn = {
					key: WorkflowNodeTable.CHECK_COLUMN,
					allowHTML: true,
					formatter: this._formatter,
					label: " "
			}
			header.splice(0, 0, checkColumn);
			header = this._setBaseTableColumnsTemplate(header);
	    	this.get('table').set('columns', header);
	    },
	    
	    _formatter : function(o) {
	    	if(o.value == false) {
	    		return WorkflowNodeTable.CHECKBOX_UNCHECKED;
	    	} else {
	    		return WorkflowNodeTable.CHECKBOX_CHECKED;
	    	}
	    	
	    },
   
	    setData : function(data) {
	    	for(var rowIndex = 0; rowIndex < data.length; rowIndex++) {
	    		data[rowIndex][WorkflowNodeTable.CHECK_COLUMN] = false;
	    	}
	    	this.get('table').data.reset(data);
	    },
	    
	    _generateLoadData: function(options) {
	    	var params = this.constructor.superclass._generateLoadData.call(this, options);
	    	params.dataType = this.get('nodeType');
	    	params.resource_cmd = 'load';
			return params;
		},
	    
	    _getTotalPageNumberParams: function() {
	    	var params = this.constructor.superclass._getTotalPageNumberParams.call(this);
	    	params.dataType = this.get('nodeType');
	    	return params;
	    },
	    
	    _renderPreUIAdditional : function() {
	    	var contentBox =  this.get('contentBox');
	    	this.typeSelector = Y.Node.create(WorkflowNodeTable.SELECT_TYPE_TEMPLATE);
			contentBox.appendChild(this.typeSelector);
	    },
	    
	    _renderUIAdditional : function() {
	    	var contentBox = this.get('contentBox');
	    	
	    	var approveButton = this._appendButton(WorkflowNodeTable.BUTTON_APPROVE);
			this.set('approveButton', approveButton);
			
			var rejectButton = this._appendButton(WorkflowNodeTable.BUTTON_REJECT);
			this.set('rejectButton', rejectButton);
			
			contentBox.append(Y.Node.create(WorkflowNodeTable.DIALOG_CONTAINER));
	    },
	    
	    _appendButton: function(buttonHtml) {
	    	var contentBox = this.get('contentBox');
	    	var addButton = Y.Node.create(buttonHtml);
	    	contentBox.appendChild(addButton);
	    	var approveButton = new Y.Button(
		      {
		          srcNode: '#' + addButton.get('id')
		      }
		    );
			approveButton.render();
			return approveButton;
	    },
	    
	    _bindUIAdditional : function() {
	    	this.get('table').delegate('click', Y.bind(this._selectRow, this), '.yui3-datatable-data .rowSelect');
	    	this.get('approveButton').on('click', Y.bind(this._onApproveButtonClick, this));
	    	this.get('rejectButton').on('click', Y.bind(this._onRejectButtonClick, this));
	    	this.typeSelector.on('change', Y.bind(this._afterTypeChange, this));
	    },
	    
	    
	    _selectRow: function(e) {
	       var table = this.get('table');
	       var target = e.currentTarget;
	       var checked = target.get('checked');
	       if(checked) {
	    	   table.getRecord(e.currentTarget.get("id")).set(WorkflowNodeTable.CHECK_COLUMN, true);
	       } else {
	    	   table.getRecord(e.currentTarget.get("id")).set(WorkflowNodeTable.CHECK_COLUMN, false);
	       }
	       
	    },
	    
	    _afterTypeChange : function() {
	    	var selIndex = this.typeSelector.get("selectedIndex");
	    	var typeOpts = this.typeSelector.get("options");
	    	var selectedType = typeOpts.item(selIndex).getAttribute("name");
	    	if("test" != selectedType) {
	    		this.set('nodeType', selectedType);
		    	this.refreshFirstPage();
	    	}
	    },
	    
	    _onApproveButtonClick: function(e){
	    	var selectedIds = this._getSelectedRows();
	    	this._submitButtonAjaxCommand('approve', selectedIds, Y.bind(this._onSubmitSuccessed, this));
		},
		
		_onRejectButtonClick: function(e){
			var selectedIds = this._getSelectedRows();
	    	this._submitButtonAjaxCommand('reject', selectedIds, Y.bind(this._onSubmitSuccessed, this));
		},
		
		_getSelectedRows: function() {
			var data =  this.get('data');
	    	var selectedIds = [];
	    	for(var index = 0; index < data.size(); index++) {
	    		var row = data.item(index);
	    		if(row.get(WorkflowNodeTable.CHECK_COLUMN) == true) {
	    			selectedIds.push(row.get('nodeId'));
	    		}
	    	}
			return selectedIds;
		},
		
		_submitButtonAjaxCommand: function(cmd, ids, callback) {
			var postParams = {'ids': ids, 'resource_cmd':cmd, 'dataType': this.get('nodeType')};
        	var submitCfg = {
					method: 'POST',
					data: postParams,
					on: {
						complete: callback
					}
			};
			Y.io(this.get('resourceURL'), submitCfg);
		},
		
		_onSubmitSuccessed : function(id, res) {
			if(this.callbackPanel == null) {
				this.callbackPanel = new Y.Panel({
					srcNode : this.get('contentBox').one('.dialogContainer'),
					bodyContent: '操作成功',
					visible  : false,
			        width   : 400,
			        centered: true,

			        // Make changes to the buttons through the `buttons` attribute,
			        // which takes an Array of Objects.
			        buttons  : {
			            footer: [
			                {
			                    name     : 'proceed',
			                    label    : '确认',
			                    action   : 'onOK'
			                }
			            ]
			        }
			    });
				
				this.callbackPanel.onOK = Y.bind(function (e) {
			        e.preventDefault();
			        this.callbackPanel.hide();
			        this.refreshFirstPage();
			    }, this);
				this.callbackPanel.render();
			}
			
			this.callbackPanel.show();
			
		},
		
		refreshFirstPage : function() {
	    	this.get('data').load({'pageNumber':1, 'header':true});
	        this._getTotalPageNumber();
	    }
	});
	
}, '0.0.1', {requires:["event", "domHelper", "widget", "panel", "io", "model-list", "baseTableWidget", "json-parse"]});
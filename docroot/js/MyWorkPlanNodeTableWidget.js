YUI.add("myWorkplanTableWidget", function(Y) {

	function MyWorkplanNodeTable(config) {
		MyWorkplanNodeTable.superclass.constructor.apply(this, arguments);
	}
	
	MyWorkplanNodeTable.NAME = "myWorkplanNode";
	MyWorkplanNodeTable.CHECKBOX_UNCHECKED = '<input type="checkbox" class="rowSelect" />';
	MyWorkplanNodeTable.CHECKBOX_CHECKED = '<input type="checkbox" class="rowSelect" checked />';
	MyWorkplanNodeTable.BUTTON_DELETE = '<button id="deleteButton">删除</button>';
	MyWorkplanNodeTable.DIALOG_CONTAINER = '<div class="dialogContainer" />';
	MyWorkplanNodeTable.FILE_ITEM_WRAPPER = '<div class="fileLink" />';
	
	Y.MyWorkplanNodeTable = Y.extend(MyWorkplanNodeTable, Y.BaseTableWidget, {
	    
		setTableColumns : function(header) {
			var checkColumn = {
					key: 'checkbox',
					allowHTML: true,	
					nodeFormatter: this._nodeFormatter,
					label:' '
			}
			for(var headerIndex = 0; headerIndex < header.length; headerIndex++) {
				//format url header.
				//every url should be one line.
				if(header[headerIndex].key == "typeDesc") {
					header[headerIndex] = {
						key: 'typeDesc',
						allowHTML: true,
						label:header[headerIndex].label,
						nodeFormatter: this._FileDescFormatter	
					}
					
				} else if(header[headerIndex].key == "attachmentName") {
					header[headerIndex] = {
						key: 'attachmentName',
						allowHTML: true,
						label:header[headerIndex].label,
						nodeFormatter: this._FileLinkFormatter	
					}
				}
			}
			header.splice(0, 0, checkColumn);
			header = this._setBaseTableColumnsTemplate(header);
	    	this.get('table').set('columns', header);
	    },
	    
	    _nodeFormatter : function(o) {
	    	if(o.data.status == "rejected") {
//	    		return WorkflowNodeTable.CHECKBOX_UNCHECKED;
	    		if(o.data.checkbox == false) {
	    			o.cell.append(MyWorkplanNodeTable.CHECKBOX_UNCHECKED);
	    		} else {
	    			o.cell.append(MyWorkplanNodeTable.CHECKBOX_CHECKED);
	    		}
	    		
	    	} else {
	    		o.cell.set('text', '');
	    	}
	    	return false;
	    },
	    
	    _FileDescFormatter : function(o) {
	    	var values = o.data.typeDesc;
	    	values = values.split("~-~");
	    	var index = 1;
	    	Y.each(values, function(value){
	    		var wrapper = Y.Node.create(MyWorkplanNodeTable.FILE_ITEM_WRAPPER);
	    		wrapper.setHTML(index++ + "." + value);
	    		o.cell.append(wrapper);
	    	});
	    	return false;
	    },
	    
	    _FileLinkFormatter : function(o) {
	    	var values = o.data.attachmentName;
	    	values = values.split("~-~");
	    	var urls = o.data.attachmentURL;
	    	urls = urls.split("~-~");
	    	
	    	for(var index = 0; index < values.length; index++) {
	    		var wrapper = Y.Node.create(MyWorkplanNodeTable.FILE_ITEM_WRAPPER);
	    		var link = Y.Node.create('<a>' + (index + 1) + '.' + values[index] + '</a>');
	    		link.setAttribute('href', urls[index]);
	    		wrapper.setHTML(link);
	    		o.cell.append(wrapper);
	    	}
	    		
	    	return false;
	    },
	    
	    setData : function(data) {
	    	for(var rowIndex = 0; rowIndex < data.length; rowIndex++) {
	    		data[rowIndex].checkbox = false;
	    	}
	    	this.get('table').data.reset(data);
	    },
	    
	    refreshFirstPage : function() {
	    	this.get('data').load({'pageNumber':1});
	        this._getTotalPageNumber();
	    },
	    
	    _renderPreUIAdditional : function() {
	    	this.get('contentBox').append(Y.Node.create(MyWorkplanNodeTable.DIALOG_CONTAINER));
	    },
	    
	    _renderUIAdditional : function() {
	    	var contentBox = this.get('contentBox');
	    	
	    	var deleteButton = this._appendButton(MyWorkplanNodeTable.BUTTON_DELETE);
			this.deleteButton = deleteButton;
	    },
	    
	    _generateLoadData: function(options) {
	    	var params = this.constructor.superclass._generateLoadData.call(this, options);
	    	params.resource_cmd = 'getUnPublishedNodes';
			return params;
		},
		
	    _getTotalPageNumberParams: function() {	
	    	return params = {'resource_cmd' : 'upcount'};
	    },
	    
	    _appendButton: function(buttonHtml) {
	    	var contentBox = this.get('contentBox');
	    	var addButton = Y.Node.create(buttonHtml);
	    	contentBox.appendChild(addButton);
	    	var approveButton = new Y.Button(
		      {
		          srcNode: '#' + addButton.get('id'),
		          label: Liferay.Language.get('button-delete')
		      }
		    );
			approveButton.render();
			return approveButton;
	    },
	    
	    _bindUIAdditional : function() {
	    	this.get('table').delegate('click', Y.bind(this._selectRow, this), '.yui3-datatable-data .rowSelect');
	    	this.deleteButton.on('click', Y.bind(this._onDeleteButtonClick, this));
	    },
	    
	    
	    _selectRow: function(e) {
	       var table = this.get('table');
	       var target = e.currentTarget;
	       var checked = target.get('checked');
	       if(checked) {
	    	   table.getRecord(e.currentTarget.get("id")).set('checkbox', true);
	       } else {
	    	   table.getRecord(e.currentTarget.get("id")).set('checkbox', false);
	       }
	       
	    },
	    
	    _onDeleteButtonClick: function(e){
	    	var selectedIds = this._getSelectedRows();
	    	this._submitButtonAjaxCommand('delete', selectedIds, Y.bind(this._onDeleteSuccessed, this));
		},
		
		_onDeleteSuccessed : function(id, res) {
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
		
		_getSelectedRows: function() {
			var data =  this.get('data');
	    	var selectedIds = [];
	    	for(var index = 0; index < data.size(); index++) {
	    		var row = data.item(index);
	    		if(row.get('checkbox') == true) {
	    			selectedIds.push(row.get('nodeId'));
	    		}
	    	}
			return selectedIds;
		},
		
		_submitButtonAjaxCommand: function(cmd, ids, callback) {
			var postParams = {'ids': ids, 'resource_cmd':cmd};
        	var submitCfg = {
					method: 'POST',
					data: postParams,
					on: {
						complete: callback
					}
			};
			Y.io(this.get('resourceURL'), submitCfg);
		}
		
		
	});
	
}, '0.0.1', {requires:["event", "widget", "panel", "io", "model-list", "baseTableWidget", "json-parse"]});
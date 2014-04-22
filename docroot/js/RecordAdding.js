/**
 * 
 */
YUI.add("recordAdding", function(Y) {
	function RecordAdding(config) {
		RecordAdding.superclass.constructor.apply(this, arguments);
	}
	
	RecordAdding.NAME = "recordAdding";
	
	RecordAdding.ATTRS = {
		resourceURL: {value:""}
	};
	
	RecordAdding.ROW_CONTAINER = '<div id=\'rowContainer\' class=\'yui3-g\'></div>';
	RecordAdding.FORM_WRAPER = '<form enctype="multipart/form-data" method="POST"></form>';
	RecordAdding.ADDING_BUTTON = '<button id=\'addingButton\' class="btn btn-primary">添加记录</button>';
	RecordAdding.SUBMIT_BUTTON = '<button id=\'submitButton\' class="btn btn-primary">提交</button>';
	RecordAdding.DIALOG_CONTAINER = '<div class="dialogContainer" />';
	RecordAdding.EVENT_SUBMIT_COMPLETE = RecordAdding.NAME + ":submitComplete";
	
	Y.RecordAdding = Y.extend(RecordAdding, Y.Widget, {
		initializer : function(cfg) {
			this.generator = new Y.WorkplanNodeGeneratorHelper({'rowConfJson' : cfg.rowConfJson, 'hasDefault':true});
			this.rowCount = 0;
	    },
	    
		renderUI : function() {
			var contentBox = this.get('contentBox');
			var addButton = Y.Node.create(RecordAdding.ADDING_BUTTON);
			contentBox.appendChild(addButton);
			var submitButton = Y.Node.create(RecordAdding.SUBMIT_BUTTON);
			contentBox.appendChild(submitButton);
			var rowContainer = Y.Node.create(RecordAdding.ROW_CONTAINER);
			contentBox.appendChild(rowContainer);
			contentBox.appendChild(Y.Node.create(RecordAdding.DIALOG_CONTAINER));
        },
        
        bindUI : function() {
        	var contentBox = this.get('contentBox');
        	var addingButton = contentBox.one('#addingButton');
        	addingButton.on('click', Y.bind(this._onAddClick, this));
        	var submitButton = contentBox.one('#submitButton');
        	submitButton.on('click', Y.bind(this._onSubmitClick, this));
        },
        
        _onAddClick: function() {
        	var contentBox = this.get('contentBox');
        	var rowCon = contentBox.one('#rowContainer');
        	var rowWP = this.generator.buildRow(this.rowCount);
        	var formCon = Y.Node.create(RecordAdding.FORM_WRAPER);
        	formCon.append(rowWP);
        	rowCon.appendChild(formCon);
        	
        	this.rowCount += 1;
        },
        
        _onSubmitClick: function() {
        	var contentBox = this.get('contentBox');
        	var rowCon = contentBox.one('#rowContainer');
        	var rows = rowCon.all('form');
        	
        	var values = [];
        	for(var index = 0; index < rows.size(); index++) {
        		var row = rows.item(index);
//        		var rowValue = this.generator.getRowValue(row);
        		values.push(row);
        	}
        	this._submitValues(values);
//        	rowCon.empty();
        	this.rowCount = 0;
        },
        
        _submitValues: function(values) {
//        	var postParams = {'values': values, 'resource_cmd':'add'};
        	var submitCfg = {
					method: 'POST',
					form: {
			            id: values[0],
			            upload: true
			        },
					on: {
						complete: Y.bind(this._onSubmitSuccessed, this)
					}
			};
//        	rowWP.appendChild('<input type="hidden" name="resource_cmd" value="add" />');
			Y.io(this.get('resourceURL') + "&resource_cmd=add", submitCfg);
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
			        Y.fire(RecordAdding.EVENT_SUBMIT_COMPLETE, {'status' : 'success'});
			    }, this);
				this.callbackPanel.render();
			}
			
			this.callbackPanel.show();
			
		},
	});
	
}, '0.0.1', {requires:["event", "nodeGenerator", "widget", "io", "aui-button", "json-parse"]});
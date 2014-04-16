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
        	rowCon.appendChild(rowWP);
        	
        	this.rowCount += 1;
        },
        
        _onSubmitClick: function() {
        	var contentBox = this.get('contentBox');
        	var rowCon = contentBox.one('#rowContainer');
        	var rows = rowCon.all('.rowWrapper');
        	
        	var values = [];
        	for(var index = 0; index < rows.size(); index++) {
        		var row = rows.item(index);
        		var rowValue = this.generator.getRowValue(row);
        		values.push(rowValue);
        	}
        	this._submitValues(values);
        	rowCon.empty();
        	this.rowCount = 0;
        },
        
        _submitValues: function(values) {
        	var postParams = {'values': values, 'resource_cmd':'add'};
        	var submitCfg = {
					method: 'POST',
					data: postParams,
					on: {
						complete: Y.bind(this._onSubmitSuccessed, this)
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
			        Y.fire(RecordAdding.EVENT_SUBMIT_COMPLETE, {'status' : 'success'});
			    }, this);
				this.callbackPanel.render();
			}
			
			this.callbackPanel.show();
			
		},
	});
	
}, '0.0.1', {requires:["event", "nodeGenerator", "widget", "io", "aui-button", "json-parse"]});
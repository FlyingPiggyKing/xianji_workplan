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
	RecordAdding.SELECT_INPUT = '<input type="file" />';
	RecordAdding.SUBMIT_BUTTON = '<button id=\'submitButton\' class="btn btn-primary">提交</button>';
	RecordAdding.ADD_FILE_BUTTON = '<div class="yui3-u-1"><button class="btn btn-primary addFile">添加文件</button></div>';
	RecordAdding.FILE_INPUT_WRAPER = '<div class="uploadFileInput yui3-u-1-4" />';
	RecordAdding.DIALOG_CONTAINER = '<div class="dialogContainer" />';
	RecordAdding.SUBMIT_RESPONSE_CONTAINER = '<div class="responseContainer" />';
	RecordAdding.EVENT_SUBMIT_COMPLETE = RecordAdding.NAME + ":submitComplete";
	
	Y.RecordAdding = Y.extend(RecordAdding, Y.Widget, {
		initializer : function(cfg) {
			this.generator = new Y.WorkplanNodeGeneratorHelper({'rowConfJson' : cfg.rowConfJson, 'hasDefault':true});
			this.rowCount = 0;
//			this.formNumber = 1;
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
        	
        	this.get("contentBox").delegate('click', Y.bind(this._onAddFileClick, this), '.addFile');
        },
        
        _onAddClick: function() {
        	var contentBox = this.get('contentBox');
        	var rowCon = contentBox.one('#rowContainer');
        	var rowWP = this.generator.buildRow(this.rowCount);
        	var fileTypeSelector = rowWP.one('select[name="typeDesc"]').ancestor('div');
        	rowWP.removeChild(fileTypeSelector);
        	if(this.fileTypeSelector == null) {
        		this.fileTypeSelector = fileTypeSelector;
        	}
        	var formCon = Y.Node.create(RecordAdding.FORM_WRAPER);
        	formCon.append(rowWP);
        	var addFileButton = Y.Node.create(RecordAdding.ADD_FILE_BUTTON);
        	formCon.append(addFileButton);
//        	formCon.setAttribute('id', this.formNumber);
//        	this.formNumber += 1;
        	
        	rowCon.appendChild(formCon);
        	
//        	this.rowCount += 1;
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
        
        _onAddFileClick : function(e) {
        	e.preventDefault();
        	var target = e.target;
        	var parentForm = target.ancestor('form');
        	
        	var uploadWraper = Y.Node.create(RecordAdding.FILE_INPUT_WRAPER);
        	var selector = this.fileTypeSelector.cloneNode(true);
        	var time = new Date().valueOf();
        	selector.one('select').setAttribute('name', 'attachment' + time);
        	uploadWraper.append(selector);
        	var fileInput = Y.Node.create(RecordAdding.SELECT_INPUT);
        	fileInput.setAttribute('name', 'attachmentFile' + time);
        	uploadWraper.append(fileInput);
        	parentForm.append(uploadWraper);
        },
        
        _submitValues: function(values) {
//        	var postParams = {'values': values, 'resource_cmd':'add'};
        	Y.each(values, Y.bind(function(form, key){
        		var submitCfg = {
    					method: 'POST',
    					form: {
    			            id: form,
    			            upload: true
    			        },
    					on: {
    						complete: Y.bind(this._onSubmitSuccessed, this)
    					}
    			};
//            	rowWP.appendChild('<input type="hidden" name="resource_cmd" value="add" />');
//    			Y.io(this.get('resourceURL') + "&resource_cmd=add&groupPermissions=VIEW", submitCfg);
    			Y.io.queue(this.get('resourceURL') + "&resource_cmd=add&groupPermissions=VIEW", submitCfg);
        	}, this));
        	Y.io.queue.start();
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
			        this.callbackPanel.get('srcNode').empty();
			    }, this);
				this.callbackPanel.render();
			}
			var contentDiv = this.callbackPanel.get('srcNode');
			var responseCon = Y.Node.create(RecordAdding.SUBMIT_RESPONSE_CONTAINER);
			responseCon.setHTML(id + " submit successfully.");
			contentDiv.one('.yui3-widget-bd').append(responseCon);
			this.callbackPanel.show();
			
		},
	});
	
}, '0.0.1', {requires:["event", "nodeGenerator", "widget", "io", "aui-button", "json-parse"]});
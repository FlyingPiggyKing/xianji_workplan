/**
 * 
 */
YUI.add("recordAdding", function(Y) {
	function RecordAdding(config) {
		RecordAdding.superclass.constructor.apply(this, arguments);
	}
	
	RecordAdding.NAME = "recordAdding";
	
	RecordAdding.ATTRS = {
		resourceURL: {value:""},
		formRecords: {value:[]}
	};
	
	RecordAdding.ROW_CONTAINER = '<div id=\'rowContainer\' class=\'yui3-g\'></div>';
	RecordAdding.FORM_WRAPER = '<div class="yui3-g yui3-u-23-24"><form enctype="multipart/form-data" method="POST"></form>';
	RecordAdding.FORM_INDEX = '<div class="yui3-u-1-24"></div>';
	RecordAdding.ADDING_BUTTON = '<button id=\'addingButton\' class="btn btn-primary">添加记录</button>';
	RecordAdding.SELECT_INPUT = '<input type="file" />';
	RecordAdding.SUBMIT_BUTTON = '<button id=\'submitButton\' class="btn btn-primary">提交</button>';
	RecordAdding.ADD_FILE_BUTTON = '<div class="yui3-u-1"><button class="btn btn-primary addFile">添加文件</button></div>';
	RecordAdding.FILE_INPUT_WRAPER = '<div class="uploadFileInput yui3-u-1-4" />';
	RecordAdding.DIALOG_CONTAINER = '<div class="dialogContainer" />';
	RecordAdding.SUBMIT_RESPONSE_CONTAINER = '<div class="responseContainer" />';
	RecordAdding.EVENT_SUBMIT_COMPLETE = RecordAdding.NAME + ":submitComplete";
	RecordAdding.RECORD_ODD = '<div class=\'yui3-g yui3-u-1\' style=\'background-color:#EDF5FF\'></div>';
	RecordAdding.RECORD_EVEN = '<div class=\'yui3-g yui3-u-1\'></div>';
	
	Y.RecordAdding = Y.extend(RecordAdding, Y.Widget, {
		initializer : function(cfg) {
			this.generator = new Y.WorkplanNodeDomHelper({'rowConfJson' : cfg.rowConfJson, 'hasDefault':true});
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
        	
        	//the content of submit dialog will be updated once the submit record changes
        	this.after('formRecordsChange', this._afterformRecordsChange);
        },
        
        _onAddClick: function() {
        	var contentBox = this.get('contentBox');
        	var rowCon = contentBox.one('#rowContainer');
        	//remove the file upload input from normal configuration,
        	//"add attachment" button will be responsible for generating the file upload widget 
        	var rowWP = this.generator.buildRow();
        	var fileTypeSelector = rowWP.one('select[name="typeDesc"]').ancestor('div');
        	rowWP.removeChild(fileTypeSelector);
        	if(this.fileTypeSelector == null) {
        		this.fileTypeSelector = fileTypeSelector;
        	}
        	//form record is used to indicate how many form is being submitted,
        	//then user can see the progress when submit the records.
        	var formRecords = this.get('formRecords');
        	var recordIndex = formRecords.length + 1;
        	var recordName = "记录" + recordIndex;
        	formRecords.push(recordName + "提交中。。。");
        	this.set('formRecords', formRecords);
        	//begin to add record to the page
        	//append a record wrapper first, the color should be depend on the row number
        	var recordWrapper = null;
    		if(++this.rowCount % 2 == 1) {
    			recordWrapper = Y.Node.create(RecordAdding.RECORD_EVEN);
	    	} else {
	    		recordWrapper = Y.Node.create(RecordAdding.RECORD_ODD);
	    	}
        	
        	//record name should like '记录1'
        	var formIndex = Y.Node.create(RecordAdding.FORM_INDEX);
        	formIndex.setHTML(recordName);
        	recordWrapper.append(formIndex);
        	//record from, following the index
        	var formCon = Y.Node.create(RecordAdding.FORM_WRAPER);
        	formCon.one('form').append(rowWP);
        	var addFileButton = Y.Node.create(RecordAdding.ADD_FILE_BUTTON);
        	formCon.one('form').append(addFileButton);	
        	recordWrapper.appendChild(formCon);
        	//add record to the page
        	rowCon.append(recordWrapper);
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
        	rowCon.empty();
        	this.rowCount = 0;
        },
        
        _afterformRecordsChange : function() {
        	if(this.callbackPanel != null) {
        		var formRecords = this.get('formRecords');
        		var dialogContent = '';
        		var contentDiv = this.callbackPanel.get('srcNode').one('.yui3-widget-bd');
        		contentDiv.empty();
        		Y.each(formRecords, function(record){
        			var responseCon = Y.Node.create(RecordAdding.SUBMIT_RESPONSE_CONTAINER);
        			responseCon.setHTML(record);
        			contentDiv.append(responseCon);
        		});
        	}
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
    						complete: Y.bind(this._onSubmitComplete, this)
    					}
    			};
//            	rowWP.appendChild('<input type="hidden" name="resource_cmd" value="add" />');
//    			Y.io(this.get('resourceURL') + "&resource_cmd=add&groupPermissions=VIEW", submitCfg);
    			Y.io.queue(this.get('resourceURL') + "&resource_cmd=add&groupPermissions=VIEW", submitCfg);
        	}, this));
        	Y.io.queue.start();
        },
        
        _onSubmitComplete : function(id, res) {
        	if(this.callbackPanel == null) {
				this._initDialog();
			}
        	var json = Y.JSON.parse(res.responseText);
        	var result = json.result;
//        	if(error != null) {
//        		this._onSubmitFailure(error);
//        	} else {
//        		this._onSubmitSuccessed();
//        	}
        	var formRecords = this.get('formRecords');
			for(var formIndex = 0; formIndex < formRecords.length; formIndex++) {
				var curStr = formRecords[formIndex];
				var submitIndex = curStr.indexOf('提交中。。。');
				if(submitIndex != -1) {
					formRecords[formIndex] = curStr.substr(0, submitIndex) + result;
					break;
				}
			}
			this.set('formRecords', formRecords);
			this.callbackPanel.show();
        },
        
//        _onSubmitSuccessed : function(result) {
//			var formRecords = this.get('formRecords');
//			for(var formIndex = 0; formIndex < formRecords.length; formIndex++) {
//				var curStr = formRecords[formIndex];
//				var submitIndex = curStr.indexOf('提交中。。。');
//				if(submitIndex != -1) {
//					formRecords[formIndex] = curStr.substr(0, submitIndex) + '提交成功';
//					break;
//				}
//			}
//			this.set('formRecords', formRecords);
//			this.callbackPanel.show();
//		},
//		
//		_onSubmitFailure : function(error) {
//			this.set('formRecords', [error]);
//			this.callbackPanel.show();
//		},
		
		_initDialog : function() {
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
		        this.set('formRecords', []);
		    }, this);
			this.callbackPanel.render();
		}
	});
	
}, '0.0.1', {requires:["event", "domHelper", "widget", "io", "aui-button", "json-parse"]});
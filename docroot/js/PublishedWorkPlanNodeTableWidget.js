YUI.add("workplanTableWidget", function(Y) {

	function WorkplanNodeTable(config) {
		WorkplanNodeTable.superclass.constructor.apply(this, arguments);
	}
	
	WorkplanNodeTable.NAME = "workplanNode";
	
	WorkplanNodeTable.ROW_CONTAINER = '<div id=\'rowContainer\' class=\'yui3-g\'></div>';
	WorkplanNodeTable.BUTTON_SEARCH = '<button id="search">查询</button>';
	WorkplanNodeTable.BUTTON_STATIS = '<button id="statistics">统计</button>';
	WorkplanNodeTable.BUTTON_DOWNLAOD = '<button id="download">下载</button>';
	WorkplanNodeTable.STATISTICS_CONTAINER = '<div class="statistics" />';
	
	Y.WorkplanNodeTable = Y.extend(WorkplanNodeTable, Y.BaseTableWidget, {
	    initializer: function(cfg) {
	    	this.domHelper = new Y.WorkplanNodeDomHelper({'rowConfJson' : cfg.rowConfJson, 'hasDefault': false});
	    	this.needStatistics = cfg.needStatistics;
	    },
	    
	    _renderPreUIAdditional : function() {
	    	var contentBox =  this.get('contentBox');
	    	var rowContainer = Y.Node.create(WorkplanNodeTable.ROW_CONTAINER);
			contentBox.appendChild(rowContainer);
	    	var rowWP = this.domHelper.buildRow();
	    	rowContainer.appendChild(rowWP);
	    	//add search button
	    	this.searchButton = this._appendButton(WorkplanNodeTable.BUTTON_SEARCH);
	    	
	    	
	    },
	    
	    _renderUIAdditional : function() {
	    	var contentBox =  this.get('contentBox');
	    	this.statisticsButton = this._appendButton(WorkplanNodeTable.BUTTON_STATIS);
	    	contentBox.append(Y.Node.create(WorkplanNodeTable.STATISTICS_CONTAINER));
	    	this.downloadButton = this._appendButton(WorkplanNodeTable.BUTTON_DOWNLAOD);
	    },
	    
	    _bindUIAdditional : function() {
	    	this.searchButton.on('click', Y.bind(this._onSearchButtonClick, this));
	    	this.statisticsButton.on('click', Y.bind(this._onStatisticsClick, this));
	    	this.downloadButton.on('click', Y.bind(this._onDownloadClick, this));
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
	    
	    _onSearchButtonClick: function() {
	    	var contentBox = this.get('contentBox');
	    	var searchContainer = contentBox.one('#rowContainer');
	    	var row = searchContainer.one('.rowWrapper');
	    	this.rowValue = this.domHelper.getRowValue(row);
	    	
	    	this.get('data').load({'pageNumber':1});
	    	this._getTotalPageNumber();
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
						nodeFormatter: this.domHelper.formatFileDesc	
					}
					
				} else if(header[headerIndex].key == "attachmentName") {
					header[headerIndex] = {
						key: 'attachmentName',
						allowHTML: true,
						label:header[headerIndex].label,
						nodeFormatter: this.domHelper.formatFileLink	
					}
				}
			}
			header = this._setBaseTableColumnsTemplate(header);
	    	this.get('table').set('columns', header);
	    },
	    
//	    _nodeFormatter : function(o) {
//	    	var content = Y.Node.create('<a href="' + o.data.attachmentURL + '">' + o.data.attachmentName + '</a>');
//	    	o.cell.append(content);
//	    	return false;
//	    },
	    
	    _generateLoadData: function(options) {
	    	var params = this.constructor.superclass._generateLoadData.call(this,options);
	    	params.resource_cmd = 'getPublishedNodes';
	    	if(this.rowValue != null) {
	    		params.searchParams = this.rowValue;
	    	}
			return params;
		},
	    
	    _getTotalPageNumberParams : function() {
	    	return params = {'resource_cmd' : 'pcount', 'searchParams' : this.rowValue};
	    },
	    
	    _onStatisticsClick : function() {
	    	this._submitButtonAjaxCommand('statistics', Y.bind(this._updateStatistics, this));
	    },
	    
	    _updateStatistics : function(id, res) {
	    	if(this.statisticsTable == null) {
	    		this.statisticsTable = new Y.DataTable({

		    	});
	    		var contentBox =  this.get('contentBox');
	    		this.statisticsTable.render(contentBox.one('.statistics'));
	    	}
	    	var json = Y.JSON.parse(res.responseText);
	    	
	    	var header = json.header;
	    	header.splice(0, 0, {key:'count', label:'数量'});
	    	this.statisticsTable.set('columns', header);
	    	
	    	var data = json.data;
	    	data.count = this.pg.get('totalItems');
	    	this.statisticsTable.data.reset([data]);
	    	
	    },
	    
	    _submitButtonAjaxCommand : function(cmd, callback) {
			var postParams = {'resource_cmd':cmd, 'searchParams' : this.rowValue};
        	var submitCfg = {
					method: 'POST',
					data: postParams,
					on: {
						complete: callback
					}
			};
			Y.io(this.get('resourceURL'), submitCfg);
		},
		
		_onDownloadClick : function() {
//			this._submitButtonAjaxCommand('export');
			var searchParam = '';
			if(this.rowValue != null) {
				Y.each(this.rowValue, function(row){
					searchParam += '&searchParam=';
					searchParam += row;
				});
			}
			window.location.href = this.get('resourceURL') + searchParam + "&resource_cmd=export";
		}
	});
	
}, '0.0.1', {requires:["event", "domHelper", "widget", "io", "model-list", "baseTableWidget", "json-parse"]});
YUI.add("baseTableWidget", function(Y) {
	
	//build a custom workplan node model list
	var WorkplanNodeModelList = Y.Base.create('user', Y.ModelList, [], {
		initializer : function(cfg) {
			this.resourceURL = cfg.resourceURL;
			this.widget = cfg.widget;
	    },
	    
		sync: function (action, options, callback) {
		    if (action === 'read') {
		    	var params = this.widget._generateLoadData(options);
		    	this.widget._submitAjaxCommand(params, Y.bind(function(id, res){
					var json = Y.JSON.parse(res.responseText);
					var header = json.header;
					
					if(header != null) {
						this.widget.setTableColumns(header);
					}
					var data = json.data;
					this.widget.setData(data);
		    	}, this));
		    } else {
		      callback('Unsupported sync action: ' + action);
		    }
		  }
	});
	
//	WorkplanNodeModelList.ATTRS = {
//		resourceURL: {value:""}
//	};
	
	function BaseTableWidget(config) {
		BaseTableWidget.superclass.constructor.apply(this, arguments);
	}
	
	BaseTableWidget.NAME = "recordAdding";
	BaseTableWidget.CONTROL_TEMPLATE = '<div class="controls">' + 
    	'<ul>' +
        	'<li class="control-first"><a class="control" data-type="first">First</a></li>' +
        	'<li class="control-prev"><a class="control" data-type="prev">Prev</a></li>' +
        	'<li class="control-next"><a class="control" data-type="next">Next</a></li>' +
        	'<li class="control-last"><a class="control" data-type="last">Last</a></li>' +
        '</ul>' +
        '<div class="currentPage"></div>' +
    '</div>';
	BaseTableWidget.CURRENT_PAGE_TEMPLATE = 'Page {page} of {totalPages}';
	BaseTableWidget.TABLE_CONTAINER = '<div class="tableContainer" />';
	
	BaseTableWidget.ATTRS = {
		data: {value: ''},
		resourceURL: {value:""},
		table:{value:null},
		sortColumn:{value:{columnName:null, order:null}}
	};
	
	Y.BaseTableWidget = Y.extend(BaseTableWidget, Y.Widget, {
		initializer : function(cfg) {
			this.set('data', new WorkplanNodeModelList({'resourceURL':cfg.resourceURL, 'widget':this}));
	    },
	    
	    renderUI : function() {
	    	var contentBox = this.get('contentBox');
	    	//apply by sub class
	    	this._renderPreUIAdditional();
	    	
	    	var dataList = this.get('data');
	    	contentBox.append(Y.Node.create(BaseTableWidget.TABLE_CONTAINER));
	    	var table = new Y.DataTable({
	    		data:dataList,
	    		columns: ["type"]
	    	});

	    	table.render(contentBox.one('.tableContainer'));
	    	this.set('table', table);
	    	
	    	dataList.load({'pageNumber':1, 'header':true});
	    	
	    	var controlPanel = Y.Node.create(BaseTableWidget.CONTROL_TEMPLATE);
	    	contentBox.append(controlPanel);
	    	
	    	this._getTotalPageNumber();
	    	
	    	this._renderUIAdditional();
	    },
	    
	    bindUI : function() {
	    	var contentBox = this.get("contentBox");
	    	contentBox.delegate('click', Y.bind(function (e) {
	    	    e.preventDefault();

	    	    var control = e.currentTarget,
	    	        type = control.getData('type');

	    	    if (control.hasClass('disabled')) {
	    	        return;
	    	    }
	    	    var pg = this.pg;
	    	    switch (type) {
	    	        case 'first': pg.set('page', 1); break;
	    	        case 'prev': pg.prevPage(); break;
	    	        case 'next': pg.nextPage(); break;
	    	        case 'last': pg.set('page', pg.get('totalPages')); break;
	    	    }

	    	}, this), '.control');
	    	
	    	//for sorting
	    	contentBox.delegate('click', Y.bind(this._sort, this), '.yui3-datatable-header');
	    	this.after('sortColumnChange', this._onSortColumnChanged);
	    	
			this._bindUIAdditional();
		},
		
		
		_updatePagatorUI: function() {
			var pg = this.pg;
		    var hasNext = pg.hasNextPage(),
		        hasPrev = pg.hasPrevPage();
		    var contentBox = this.get('contentBox');
		    contentBox.one('.control-first a').toggleClass('disabled', !hasPrev);
		    contentBox.one('.control-prev a').toggleClass('disabled', !hasPrev);
		    contentBox.one('.control-next a').toggleClass('disabled', !hasNext);
		    contentBox.one('.control-last a').toggleClass('disabled', !hasNext);
		    contentBox.one('.currentPage').set('text',
		       Y.Lang.sub(BaseTableWidget.CURRENT_PAGE_TEMPLATE, pg.getAttrs())
		    );
		},
		
		_generateLoadData: function(options) {
			var params = {'resource_cmd' : 'count', 'pageNumber' : options.pageNumber, 'pageSize':10};
			if(options.header) {
				params.header = true;
			}
			if(this.get('sortColumn').columnName != null) {
				params.sort = this.get('sortColumn').columnName;
				params.order = this.get('sortColumn').order;
			} 
			return params;
		},
		
		_getTotalPageNumberParams: function() {
	    	return params = {'resource_cmd' : 'count'};
	    },
		
		_onSortColumnChanged: function(event) {
			if(this.pg.get('page') == 1) {
				var nodeType = this.get('nodeType');
		    	this.get('table').data.load({'pageNumber':1});
			} else {
				this.pg.set('page', 1);
			}
		},
		
		_sort: function(e) {
			 //change header ui
			 if(e.target.getAttribute('class') != 'datatable-sort-cell-container') {
				 return false;
			 }
			 var sort = e.target.get('id');
			 
			 var contentBox = this.get('contentBox');
			 var headers = contentBox.all('.yui3-datatable-header');
			 var asc = 'asc';
			 headers.each(function(header) {
				 var arrow = header.one('span');
				 if(arrow != null) {
					 if(header.one('div').get('id') == sort) {
						 if(arrow.hasClass('sortArrow')) {
							 arrow.replaceClass('sortArrow', 'sortArrowAsc');
						 } else if(arrow.hasClass('sortArrowAsc')) {
							 arrow.replaceClass('sortArrowAsc', 'sortArrowDesc');
							 asc = 'desc';
						 } else if(arrow.hasClass('sortArrowDesc')) {
							 arrow.replaceClass('sortArrowDesc', 'sortArrowAsc');
						 }
					 } else {
						 if(arrow.hasClass('sortArrowAsc')) {
							 arrow.replaceClass('sortArrowAsc', 'sortArrow');
						 } else if(arrow.hasClass('sortArrowDesc')) {
							 arrow.replaceClass('sortArrowDesc', 'sortArrow');
						 } 
					 }
				 }
				 
			 });
			 
			 var sortColumn = {columnName:sort, order:asc};
			 this.set('sortColumn', sortColumn);
		 },
	    
	    setTableColumns : function(header) {
	    },
	    
	    _setBaseTableColumnsTemplate : function(headers) {
	    	var templatedHeader = [];
	    	for(var colIndex = 0; colIndex < headers.length; colIndex++) {
	    		var header = headers[colIndex];
	    		if(header.constructor == String) {
	    			header = {key:header};
	    		}
	    		if(header.key != "checkbox" && header.remoteSortable == true) {
	    			header.headerTemplate = '<th id="{id}" class="{className}"><div id=\'{key}\' class=\'datatable-sort-cell-container\'>{label}<span class=\'sortArrow\'></span></div></th>';
	    		} 
	    		
	    		templatedHeader.push(header);
	    	}
	    	return templatedHeader;
	    },
	    
	    setData : function(data) {
	    	this.get('table').data.reset(data);
	    },
	    
	    _bindUIAdditional : function() {
	    },
	    
	    _renderPreUIAdditional : function() {
	    },
	    
	    _renderUIAdditional : function() {
	    },
	    
	    _getTotalPageNumber: function() {
	    	var params = this._getTotalPageNumberParams();
	    	this._submitAjaxCommand(params, Y.bind(function(id, res){
	    		var json = Y.JSON.parse(res.responseText);
	    		
	    		if(this.pg != null) {
	    			Y.Event.purgeElement(this.pg);
					this.pg = null;
	    		}
				 
	    		var pg = new Y.Paginator({
		            itemsPerPage: 10
		        });
		    	this.pg = pg;
		    	
				this.pg.set('totalItems', json.count);
				
				//retrieve data after page changes
		    	this.pg.after('pageChange', Y.bind(function (e) {
		    	    var nodeType = this.get('nodeType');
			    	this.get('table').data.load({'pageNumber':e.newVal});
		    	    this._updatePagatorUI();
		    	}, this));
		    	
				this._updatePagatorUI();
	    	}, this))
	    },
	    
	    _submitAjaxCommand: function(params, callback) {
        	var submitCfg = {
					method: 'POST',
					data: params,
					on: {
						complete: function(id, res) {
							callback(id, res);
						}
					}
			};
			Y.io(this.get('resourceURL'), submitCfg);
		}
	});
	
}, '0.0.1', {requires:["console", "event", "paginator", "widget", "io", "model-list", "datatable", "json-parse"]});
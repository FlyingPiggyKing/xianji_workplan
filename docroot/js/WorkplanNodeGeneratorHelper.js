/**
 * 
 */
YUI.add("nodeGenerator", function(Y) {
	function WorkplanNodeGeneratorHelper(config) {
		WorkplanNodeGeneratorHelper.superclass.constructor.apply(this, arguments);
	}
	
	WorkplanNodeGeneratorHelper.NAME = "workplanNodeGeneratorHelper";
	WorkplanNodeGeneratorHelper.CONNECTOR = "~-~";
	
//	WorkplanNodeGeneratorHelper.ATTRS = {
//		rowConfJson: {value:""}
//	};
	
	WorkplanNodeGeneratorHelper.ROW_WRAPPER_ODD = '<div class=\'rowWrapper yui3-g yui3-u\' style=\'background-color:#EDF5FF\'></div>';
	WorkplanNodeGeneratorHelper.ROW_WRAPPER_EVEN = '<div class=\'rowWrapper yui3-g yui3-u\'></div>';
	WorkplanNodeGeneratorHelper.CELL_WRAPPER = '<div class=\'cellWrapper yui3-u-1-4\'></div>';
	
	Y.WorkplanNodeGeneratorHelper = Y.extend(WorkplanNodeGeneratorHelper, Y.Base, {
		
		initializer : function(cfg) {
			this.rowConfJson = cfg.rowConfJson;
			this.hasDefault = cfg.hasDefault;
	    },
	    
        buildRow: function(rowCount) {
        	var rowConfJson = Y.JSON.parse(this.rowConfJson).rowConfigJson;

        	var rowWP = Y.Node.create(WorkplanNodeGeneratorHelper.ROW_WRAPPER);
        	if(rowCount == null) {
        		rowCount = 0;
        	}
    		if(rowCount % 2 == 1) {
    			rowWP = Y.Node.create(WorkplanNodeGeneratorHelper.ROW_WRAPPER_ODD);
        	} else {
        		rowWP = Y.Node.create(WorkplanNodeGeneratorHelper.ROW_WRAPPER_EVEN);
        	}
        	for(var colIndex = 0; colIndex < rowConfJson.length; colIndex++) {
        		var col = rowConfJson[colIndex];
        		var cellObject = null;
        		if(col.type == "select") {
        			var options = '';
        			if(!this.hasDefault) {
        				options += '<option></option>';
        			}
        			
    				for(var valueIndex = 0; valueIndex < col.value.length; valueIndex++) {
    					options = options + '<option>' + col.value[valueIndex] + '</option>';
    				}

    				cellObject = Y.Node.create('<select name=\"' + col.field + '\">' + options + '</select>');
        		} else if(col.type == "date") {
        			
        		} else {
        			cellObject = Y.Node.create('<input name=\"' + col.field + '\" type=\"text\" />');
        		}
        		
        		var cellLabelWP = Y.Node.create(WorkplanNodeGeneratorHelper.CELL_WRAPPER);
        		cellLabelWP.append(Y.Node.create('<label>' + col.name +'</label>'));
        		rowWP.appendChild(cellLabelWP);
        		var cellWP = Y.Node.create(WorkplanNodeGeneratorHelper.CELL_WRAPPER);
            	cellWP.appendChild(cellObject);
            	rowWP.appendChild(cellWP);
        	}
        	return rowWP;
//        	rowCon.appendChild(rowWP);
        },
        
        getRowValue: function(row) {
        	var rowConfJson = Y.JSON.parse(this.rowConfJson).rowConfigJson;
  
    		var row = row.all('.cellWrapper');
    		var rowValue = [];
    		for(var colIndex = 0; colIndex < rowConfJson.length; colIndex++) {
    			var col = rowConfJson[colIndex];
    			var cellValue = null;
    			if(col.type == "select") {
    				var select = row.item((colIndex + 1) * 2 - 1).one('select');
    				var list = select.get("options");
    				var name = select.get('name');
    				for (var listI = 0; listI < list.size(); listI++) {
						if(list.item(listI).get('selected')) {
							if(list.item(listI).get('value') != '') {
								cellValue = name + WorkplanNodeGeneratorHelper.CONNECTOR 
								+ list.item(listI).get('value') + WorkplanNodeGeneratorHelper.CONNECTOR
								+ "select";
							} else {
								cellValue = null
							}
							
							break;
						}
					}
    				
    			} else if(col.type == "date") {
        			
        		} else if(col.type == "double") {
        			var input = row.item((colIndex + 1) * 2 - 1).one('input[type="text"]');
        			var value = input.get('value');
        			if("" != value) {
        				cellValue = input.get('name') 
        				+ WorkplanNodeGeneratorHelper.CONNECTOR 
        				+ input.get('value') + WorkplanNodeGeneratorHelper.CONNECTOR
						+ "double";
        			} else {
        				cellValue = null;
        			}
        		} else {
        			var input = row.item((colIndex + 1) * 2 - 1).one('input[type="text"]');
        			var value = input.get('value');
        			if("" != value) {
        				cellValue = input.get('name') 
        				+ WorkplanNodeGeneratorHelper.CONNECTOR 
        				+ input.get('value') + WorkplanNodeGeneratorHelper.CONNECTOR
						+ "text";
        			} else {
        				cellValue = null;
        			}
        		}
    			rowValue.push(cellValue);
    		}
    		return rowValue;
        }
	});
	
}, '0.0.1', {requires:["base"]});
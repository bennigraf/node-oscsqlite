var sqlite3 = require('sqlite3').verbose();
// var db = new sqlite3.Database(':memory:');
var db = new sqlite3.Database('osctest.db');

var osc = require('node-osc');
var client = new osc.Client('127.0.0.1', 57120);


var oscServer = new osc.Server(13333, '0.0.0.0');
var transactionIds = 1000; // generate ids for transactions

oscServer.on("message", function (msg, rinfo) {
	console.log("Message:");
	console.log(msg);
	
	if(msg[0] == '/db/status') {
		console.log("status: all good!");
		client.send('/db/transaction', 'success');
		client.send('/db/status', 'all good!');
	}
	
	if(msg[0] == '/db/query') {
		var tid = msg[1];
		console.log("query: "+msg[2] + " (tid: "+tid+")");
		db.run(msg[2], function(err, result) {
			if(err) {
				client.send('/db/transaction', tid, 'error', err.code);
				console.log(err);
			} else {
				client.send('/db/transaction', tid, 'end', 'success');
				console.log(result);
			}
		});
	}
	
	if(msg[0] == '/db/select') {
		var tid = msg[1];
		var tablename = msg[2];
		var fields = msg[3];
		var where = msg[4];
		var cond = "";
		if(where) {
			cond = " WHERE "+where;
		};
		var order = msg[5];
		var orderStr = "";
		if(order) {
			orderStr = " ORDER BY "+order;
		}
		db.all("SELECT "+fields+" FROM "+tablename+cond+orderStr, function(err, rows){
			if(err) {
				client.send('/db/transaction', tid, 'error', err);
			} else {
				client.send('/db/transaction', tid, 'start');
				for (var i=0; i < rows.length; i++) {
					var msg = new osc.Message('/db/result', tid);
					// TODO: js-foreach??
					for (var field in rows[i]) {
						msg.append(field);
						msg.append(rows[i][field]);
						// console.log("field: "+field);
					};
					client.send(msg);
				};
				client.send('/db/transaction', tid, 'end', 'data');
			}
		});
	}
});


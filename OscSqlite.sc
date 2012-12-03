/* DRAFT
 * .new(NetAddr); // NetAddr where node-server lives
 * .select(table, fields, where, order) // select, return results as Event or List or whatever
 * .insert(table, data) // insert data (contents -> array) to fields (array)
 * .update(table, data, condition) // see above
 * .delete(table, conditions) // delete where ``conditions``
 * 
 * LATER
 * .new(table or db);
 */

OscSqlite {
	
	var netAddr;	// holds NetAddr
	var oscFuncs;	// holds OSCFuncs (duh)
	var <transactions;	// holds custom transactio-object to store transaction-ID, Callback, ...
						// basically a transaction-stack!! () with UID as key
		
	classvar someClassVar;
	
	*initClass {
		
	}
	
	*new { |na|
		^super.new.init(na);
	}
	init { |na|
		if(na.class == NetAddr, {
			netAddr = na;
		}, {
			netAddr = NetAddr("127.0.0.1", 13333); // default...
		});
		
		oscFuncs = List();
		this.setupOscResponders();
		
		transactions = ();
	}
		
	select { |callback, table, fields, where = nil, order = nil|
		// The way to go is to create a transaction-object and store it on the transaction stack.
		// Then we send the query-message to the sqlite-osc-server, which responds with a couple 
		// of messages. The responders figure out what to do with them by looking at the 
		// transaction stack... :-)
		var tid = UniqueID.next;
		var myTable = table;
		var myFields = fields;
		var myWhere = where;
		var myOrder = order;
		// create transaction-stack-object
		var transobj = ();
		transobj.tid = tid;
		transobj.query = ["/db/select", tid, myTable, myFields, myWhere, myOrder];
		transobj.status = "running"; // running, failed, completed
		transobj.callback = callback;
		transobj.data = List();
		// put it on the stack
		transactions[tid] = transobj;
		// send the message
		netAddr.sendMsg(*transobj.query); // * unpacks array and uses elements as arguments
	}
	
	// * .insert(table, data) // insert data (contents -> array) to fields (array)
	// We send the sql query as string here to keep logic away from 'server', seems easier for now
	// (allthough it's not consistent compared with .select())
	insert { |table, data|
		var myTable = table;
		var myData = data;
		// check if data is a Dict/Event or an array of Dicts/Events
		if((myData.class == Dictionary) || (myData.class == Event), {
			myData = [myData];
		});
		// go through each row, check for fields and values
		myData.do({|d, i|
			var tid = UniqueID.next;
			var transobj = ();
			var fields = List();
			var storeData = List();
			var sql = "INSERT INTO " + myTable + "(";
			d.keysValuesDo({ |field, someData|
				fields.add(field);
				storeData.add(someData);
			});
			sql = sql + fields.join(",") + ") VALUES (\"" ++ storeData.join("\",\"") ++ "\")";
			sql.postln;
			transobj.tid = tid;
			transobj.query = ["/db/query", tid, sql];
			transobj.status = "running";
			transactions[tid] = transobj;
			netAddr.sendMsg(*transobj.query);
		});
	}
	
	// * .update(table, data, condition) // see above
	update { |table, data, condition|
		var myTable = table;
		var myData = data;
		// check if data is a Dict/Event or an array of Dicts/Events
		if((myData.class == Dictionary) || (myData.class == Event), {
			myData = [myData];
		});
		"updating...".postln;
		// go through each row, check for fields and values
		myData.do({|d, i|
			var tid = UniqueID.next;
			var transobj = ();
			var updates = List();
			var sql = "UPDATE " + myTable + " SET ";
			d.keysValuesDo({ |field, someData|
				updates.add("\""++field++"\" = '"++someData++"'");
			});
			sql = sql + updates.join(", ") + "WHERE" + condition;
			sql.postln;
			transobj.tid = tid;
			transobj.query = ["/db/query", tid, sql];
			transobj.status = "running";
			transactions[tid] = transobj;
			netAddr.sendMsg(*transobj.query);
		});
	}
	
	// * .delete(table, conditions) // delete where ``conditions``
	delete { |table, conditions|
		var myTable = table;
		var myCond = conditions;
		// go through each row, check for fields and values
		var tid = UniqueID.next;
		var transobj = ();
		var sql = "DELETE FROM " + myTable + " WHERE " + myCond;
		sql.postln;
		transobj.tid = tid;
		transobj.query = ["/db/query", tid, sql];
		transobj.status = "running";
		transactions[tid] = transobj;
		netAddr.sendMsg(*transobj.query);
	}
	
	
	setupOscResponders {
		oscFuncs.add(OSCFunc({ |msg, time|
			[time, msg].postln;
		}, '/db/status'));
		
		oscFuncs.add(OSCFunc({ |msg, time|
			var tid = msg[1];
			var transaction = transactions[tid];
			[time, msg].postln;
			if(msg[2] == "error", { // transaction failed
				transaction.status = "failed";
				("Transaction failed! (TID: "++tid++")").postln;
				transactions.removeAt(tid);
			});
			if(msg[2] == 'end', { // transaction completed
				if(msg[3] == 'data', { // got some data -> callback
					var data = List();
					transaction.status = "completed";
					data = transaction.data.collect({ |d|
						Dictionary.newFrom(d);
					});
					transaction[\callback].value(data, transaction);
				});
				if(msg[3] == 'success', { // query or whatever has been successful apparently
					transaction.status = "completed";
					"Successful query!".postln;
				});
				transactions.removeAt(tid);
			});
		}, '/db/transaction'));

		oscFuncs.add(OSCFunc({ |msg, time|
			var tid = msg[1];
			var transaction = transactions[tid];
			[time, msg].postln;
			if(transaction.status == "running", {
				transaction.data.add(msg[2..]);
			});
		}, '/db/result'));
	}
}
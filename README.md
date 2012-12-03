node-oscsqlite
==============

Provide sqlite database access over osc.

Might be useful as a way to access a database from SuperCollider. Note that all communication is asynchronous. Will support only basic CRUD operations. The useage might be:

(tid is always a unique transaction id to group returned values and manage stuff because of its asynchronous nature)

* ``/db/status`` -- return database status (connected/disconnected)
* ``/db/query tid sql_string`` -- execute query (for inserts, ...)
* ``/db/select tid tablename fields where order`` -- select rows from table, answer with an osc-message for each row to the same address

For a later implementation:
* ``/database/select`` -- select database to use
* ``/database/connect`` -- "connect" to database (open file)
* ``/database/disconnect`` -- close connection to database (yet don't end node-server!)


Useage with the (included) OscSqlite-SuperCollider-Class:
=========================================================

(Run the node-osc-server in the terminal by ``node oscsqlite.js`` after defining the path to a database file in line 3.)

```
o = OscSqlite()

d = (field1: "This is", field2: "a little test");
o.insert("mytable", d);

o.select({ |data|
	"callback:".postln;
	data.postln;
}, "mytable", "*", "id <= 3", "id DESC");

o.update("mytable", (field2: "a big test"), "id = 1");

o.delete("mytable", "id = 1");
```


Dependencies
=============

Uses ``node-osc`` and ``node-sqlite3``, install with npm:

```bash
npm install node-osc
npm install sqlite3
```


Contact & Copyright
===================

All stuff is (c) 2012 by Benjamin Graf, but feel free to do whatever you want with it.
E-Mail: d@bennigraf.de
Web: http://www.bennigraf.de
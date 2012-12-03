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


Useage with OscSqlite-SuperCollider-Class:
===========================================

```
o = OscSqlite()

d = (field1: "This is", field2: "a little test");
o.insert("mytable", d);

o.select({ |data|
	"callback:".postln;
	data.postln;
}, "mytable", "*", "id <= 3", "id DESC");
```
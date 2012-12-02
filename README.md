node-oscsqlite
==============

Provide sqlite database access over osc.

Might be useful as a way to access a database from SuperCollider. Note that all communication is asynchronous. The scheme might be:

* ``/database/status`` -- return database status (connected/disconnected)
* ``/database/query sql_string`` -- execute query
* ``/database/select tablename fields where [query_id]`` -- select rows from table, answer with an osc-message for each row to the same address using the optional query_id as identifier

For a later implementation:
* ``/database/select`` -- select database to use
* ``/database/connect`` -- "connect" to database (open file)
* ``/database/disconnect`` -- close connection to database (yet don't end node-server!)
# Data Access with JDBC

Spring Framework JDBC抽象所提供的价值，或许可以通过下表中概述的操作序列得到最好的体现。该表展示了Spring负责处理哪些操作，以及哪些操作需要由你来负责。

分工如下表

|Action	|Spring	|You |
| --- | --- | --- |
|Define connection parameters.| | X|
|Open the connection.|X | |
|Specify the SQL statement.| |X|
|Declare parameters and provide parameter values| |X|
| Prepare and run the statement.|X | |
| Set up the loop to iterate through the results (if any). |X | |
| Do the work for each iteration.| |X |
| Process any exception.|X | |
| Handle transactions.|X | |
|Close the connection, the statement, and the resultset.|X ||
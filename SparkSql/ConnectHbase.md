there are 2 ways to connect to hbase tables

- Directly connect to Hbase :
Directly connect hbase and create a DataFrame from RDD and execute SQL on top of that. Im not going to re-invent the wheel please see How to read from hbase using spark as the answer from @iMKanchwala in the above link has already described it. only thing is convert that in to dataframe (using toDF) and follow the sql approach.

- Register table as hive external table with hbase storage handler and you can use hive on spark from hivecontext. It is also easy way.
Ex : 
CREATE TABLE users(
userid int, name string, email string, notes string)
STORED BY 
'org.apache.hadoop.hive.hbase.HBaseStorageHandler' 
WITH SERDEPROPERTIES ( 
"hbase.columns.mapping" = 
”small:name,small:email,large:notes”);
How to do that please see as an example

I would prefer approach 1.

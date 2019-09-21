# Run application locally on 8 cores
spark-submit --class TwitterHandler --master local \
 /Users/sunshuhan/Documents/mum/course/BDT/BDT-spark/SparkSqlProject/target/spark-project-1.0-SNAPSHOT.jar \
 selectAll

  # Run on a Spark standalone cluster in client deploy mode
spark-submit \
  --class TwitterHandler \
  --master spark://localhost:7077 \
  --executor-memory 20G \
  --total-executor-cores 100 \
./spark-project-1.0-SNAPSHOT.jar \
  selectAll /user/cloudera/spark-sql-output/selectAll


# /Users/sunshuhan/Documents/mum/course/BDT/BDT-spark/SparkSqlProject/target/spark-project-1.0-SNAPSHOT.jar \
# createTable
# loadData
# selectAll
# countByTime "Thu Sep 19 02:36:10 +0000 2019" "Thu Sep 19 19:47:28 +0000 2019"
# countByTime2 "Thu Sep 19 02:36:10 +0000 2019" "Thu Sep 19 20:47:28 +0000 2019"
# averageWord
# maxTwittes
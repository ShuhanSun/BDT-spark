import bean.Twitter;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.hive.HiveContext;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SparkSqlTest {

    private static final String TWITTER_JSON_PATH_100 = "src/main/resources/twitter100.txt";

    /**
     * @param args
     */
    public static void main(String[] args) {


        SparkConf conf = new SparkConf().setAppName("twitter Application");
        conf.setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
//        SQLContext sqlContext = new org.apache.spark.sql.SQLContext(sc);

//        DataFrame df = sqlContext.read().json(TWITTER_JSON_PATH_100);
//        df.show();
//        df.printSchema();

        // Select everybody, but increment the age by 1
        //        df.select(df.col("name"), df.col("age").plus(1)).show();

        // Select people older than 21
        //        df.filter(df.col("age").gt(21)).show();

        // Count people by age
        //        df.groupBy("age").count().show();

        // Register this DataFrame as a table.
//        df.registerTempTable("twitter");
//        DataFrame all = sqlContext.sql("SELECT * FROM twitter");
//        all.count();

        // sc is an existing JavaSparkContext.
        //TODO: 启动hive的元数据服务  $HIVE_HOME/conf/hive-site.xml,
        //TODO: ${HIVE_HOME}/bin/hive --service metastore  1>/dev/null  2>&1  &
        //TODO: 并将hive配置文件复制到spark conf下； $HIVE_HOME/lib/mysql-connector-java-5.1.12.jar copy或者软链到$SPARK_HOME/lib/
        //Spark SQL自己也可创建元数据库，并不一定要依赖hive创建元数据库，所以不需要一定启动hive，
        HiveContext hsqlContext = new org.apache.spark.sql.hive.HiveContext(sc);
        hsqlContext.setConf("hive.metastore.warehouse.dir", "/Users/sunshuhan/Documents/mum/course/BDT/BDT-spark/SparkSqlProject/spark-warehouse");

        //TODO: 提出去，每次都插入
//        hsqlContext.sql("CREATE TABLE IF NOT EXISTS twitter " +
//                "(id_str String, " +
//                "created_at String, " +
//                "user_id String, " +
//                "user_name String, " +
//                "user_location String, " +
//                "text String) " +
////                "PARTITIONED BY (id_str STRING) " +
//                "ROW FORMAT DELIMITED FIELDS TERMINATED BY ';' LINES TERMINATED BY '\\n'");
//        //CREATE EXTERNAL TABLE  ... LOCATION '/user/hive/cs523';
//
//        hsqlContext.sql("LOAD DATA LOCAL INPATH '" + TWITTER_JSON_PATH_100 + "' INTO TABLE twitter");

        Row[] results = hsqlContext.sql("SELECT id_str, user_id, user_name, user_location, created_at, text FROM twitter ").collect();
        System.out.println(results.length);

        for (Row row :
                results) {
            System.out.println(row.toString());
        }

        // 1. How many twitter in different time slot?
        //Wed Mar 18 13:46:38 +0000 2009
//        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
//        String dateStart = "Thu Apr 06 15:24:15 +0000 2017";
//        String dateEnd = "Thu Apr 08 15:24:15 +0000 2017";
//        Row[] results1 = hsqlContext.sql("SELECT id_str, created_at, text FROM twitter  WHERE created_at >= '" + dateStart + "' AND created_at <= '" + dateEnd + "'").collect();

//        System.out.println(d.count());

        //2. How many average word count of twitter in different location?


        //3. What’s the count of max twitters of one user and what’s his/her name?
    }
}

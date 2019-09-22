import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.*;
import org.apache.spark.sql.hive.HiveContext;

import static org.apache.spark.sql.functions.count;

/**
 * Twitter Handler
 * <p>
 * 启动hive的元数据服务  $HIVE_HOME/conf/hive-site.xml,
 * ${HIVE_HOME}/bin/hive --service metastore  1>/dev/null  2>&1  &
 * 并将hive配置文件复制到spark conf下； $HIVE_HOME/lib/mysql-connector-java-5.1.12.jar copy或者软链到$SPARK_HOME/lib/
 * Spark SQL自己也可创建元数据库，并不一定要依赖hive创建元数据库，所以不需要一定启动hive，
 */
public class TwitterHandler {

    private static final String TWITTER_TXT_PATH_100 = "src/main/resources/twitter100.txt";
    private static final String TWITTER_JSON_PATH_100 = "src/main/resources/twitter100.json";

    private static SparkConf conf;
    private static JavaSparkContext sc;
    private static HiveContext hsqlContext;
    private static SQLContext sqlContext;

    /**
     * defult is runing on local model
     */
    public TwitterHandler() {
        conf = new SparkConf().setAppName("Twitter Application");
        conf.setMaster("local");
        sc = new JavaSparkContext(conf);
        sqlContext = new org.apache.spark.sql.SQLContext(sc);
        hsqlContext = new org.apache.spark.sql.hive.HiveContext(sc);
        hsqlContext.setConf("hive.metastore.warehouse.dir", "/Users/sunshuhan/Documents/mum/course/BDT/BDT-spark/SparkSqlProject/spark-warehouse");
    }

    public TwitterHandler(String master, String metastorePath) {
        conf = new SparkConf().setAppName("Twitter Application");
        conf.setMaster(master);
        sc = new JavaSparkContext(conf);
        sqlContext = new org.apache.spark.sql.SQLContext(sc);
        hsqlContext = new org.apache.spark.sql.hive.HiveContext(sc);
        hsqlContext.setConf("hive.metastore.warehouse.dir", metastorePath);
    }

    /**
     * test
     *
     * @param args
     */
    public static void main(String[] args) {
        TwitterHandler twitterHandler = new TwitterHandler();

        switch (args[0]) {
            case "createTable":
                twitterHandler.createTable();
                return;
            case "createExternalTable":
                twitterHandler.createExternalTable(args[1]);
                return;
            case "loadDataLocal":
                twitterHandler.loadDataLocal(args[1]);
                return;
            case "loadData":
                twitterHandler.loadData(args[1]);
            case "selectAll":
                selectAll(twitterHandler);
                return;
            case "twitterTime":
                // 1. How many twitter in different time slot?
                twitterTime(twitterHandler);
                return;
            case "averageWord":
                //2. How many average word count of twitter in different location?
                averageWord();
                return;
            case "maxTwittes":
                //3. What’s the count of max twitters of one user and what’s his/her name?
                maxTwittes();
                return;
        }
    }

    private static void maxTwittes() {
        DataFrame df3 = hsqlContext.sql("SELECT user_id, COUNT(*) cnt FROM twitter GROUP BY user_id ORDER BY cnt DESC");
        df3.show();
        df3.write().saveAsTable("maxTwittes");
    }

    private static void averageWord() {
        DataFrame averageWord = hsqlContext.sql("SELECT user_location, avg(t.len) FROM (SELECT user_location, length(text) AS len FROM twitter) AS t GROUP BY user_location");
        averageWord.show();
        averageWord.write().saveAsTable("averageWord");
    }

    private static void twitterTime(TwitterHandler twitterHandler) {
//        DataFrame twitterTime = sqlContext.table("twitter");
        DataFrame twitterTime = twitterHandler.selectAll();
        twitterTime.groupBy(new Column("created_at").substr(0, 11).as("time"))
                .agg(count("id_str")).write().saveAsTable("twitterTime");
    }

    private static void selectAll(TwitterHandler twitterHandler) {
        DataFrame dataFrame = twitterHandler.selectAll();
        dataFrame.show();
        System.out.println(dataFrame.count());
    }

    public DataFrame selectAll() {
        return hsqlContext.sql("SELECT * FROM twitter ");
    }

    public void createTable() {
        hsqlContext.sql("CREATE TABLE IF NOT EXISTS twitter " +
                "(id_str String, " +
                "created_at String, " +
                "favorite_count INT, " +
                "user_id String, " +
                "user_name String, " +
                "user_location String, " +
                "text String) " +
//                "PARTITIONED BY (id_str STRING) " +
                "ROW FORMAT DELIMITED FIELDS TERMINATED BY ';' LINES TERMINATED BY '\\n'");
    }

    /**
     * table may be store on hdfs
     * Eg:/user/hive/cs523
     *
     * @param location
     */
    public void createExternalTable(String location) {
        hsqlContext.sql("CREATE EXTERNAL TABLE IF NOT EXISTS twitter LOCATION " + location +
                "(id_str String, " +
                "created_at String, " +
                "favorite_count INT, " +
                "user_id String, " +
                "user_name String, " +
                "user_location String, " +
                "text String) " +
//                "PARTITIONED BY (id_str STRING) " +
                "ROW FORMAT DELIMITED FIELDS TERMINATED BY ';' LINES TERMINATED BY '\\n'");
    }

    public void loadData(String path) {
        hsqlContext.sql("LOAD DATA INPATH '" + path + "' INTO TABLE twitter");
    }

    public void loadDataLocal(String path) {
        hsqlContext.sql("LOAD DATA LOCAL INPATH '" + path + "' INTO TABLE twitter");
    }
}

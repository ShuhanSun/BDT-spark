import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.hive.HiveContext;

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

    /**
     * defult is runing on local model
     */
    public TwitterHandler() {
        conf = new SparkConf().setAppName("Twitter Application");
        conf.setMaster("local");
        sc = new JavaSparkContext(conf);
        hsqlContext = new org.apache.spark.sql.hive.HiveContext(sc);
        hsqlContext.setConf("hive.metastore.warehouse.dir", "/Users/sunshuhan/Documents/mum/course/BDT/BDT-spark/SparkSqlProject/spark-warehouse");
    }

    public TwitterHandler(String master, String metastorePath) {
        conf = new SparkConf().setAppName("Twitter Application");
        conf.setMaster(master);
        sc = new JavaSparkContext(conf);
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
                selectAll(args[1], twitterHandler);
                return;
            case "countByTime":
                countByTime(args);
                return;
            case "countByTime2":
                // 1. How many twitter in different time slot?
                countByTime2(args, twitterHandler);
                return;
            case "averageWord":
                //2. How many average word count of twitter in different location?
                averageWord(args[1]);
                return;
            case "maxTwittes":
                //3. What’s the count of max twitters of one user and what’s his/her name?
                maxTwittes(args[1]);
                return;
        }
    }

    private static void maxTwittes(String arg) {
        DataFrame df3 = hsqlContext.sql("SELECT user_name, COUNT(*) cnt FROM twitter GROUP BY user_name ORDER BY cnt");
        df3.show();
        df3.write().mode(SaveMode.Overwrite).format("json").save(arg);
    }

    private static void averageWord(String arg) {
        DataFrame averageWord = hsqlContext.sql("SELECT user_location, avg(t.len) FROM (SELECT user_location, length(text) AS len FROM twitter) AS t GROUP BY user_location");
        averageWord.show();
        averageWord.write().mode(SaveMode.Overwrite).format("json").save(arg);
    }

    private static void countByTime2(String[] args, TwitterHandler twitterHandler) {
        DataFrame dataFrame2 = twitterHandler.selectAll();
        DataFrame dfBetween = dataFrame2.filter(new Column("created_at").between(Utils.dateString2long(args[1]), Utils.dateString2long(args[2])));
        dfBetween.show();
        dfBetween.write().mode(SaveMode.Overwrite).format("json").save(args[3]);
    }

    private static void countByTime(String[] args) {
        long timeStart = Utils.dateString2long(args[1]);
        long timeEnd = Utils.dateString2long(args[2]);
        DataFrame countByTime = hsqlContext.sql("SELECT count(*) FROM twitter  WHERE created_at >= '" + timeStart + "' AND created_at <= '" + timeEnd + "'");
        countByTime.write().mode(SaveMode.Overwrite).format("json").save(args[3]);
    }

    private static void selectAll(String arg, TwitterHandler twitterHandler) {
        DataFrame dataFrame = twitterHandler.selectAll();
        dataFrame.show();
        dataFrame.write().mode(SaveMode.Overwrite).format("json").save(arg);
        System.out.println(dataFrame.count());
    }

    public DataFrame selectAll() {
        return hsqlContext.sql("SELECT * FROM twitter ");
//        return hsqlContext.sql("SELECT id_str, created_at, favorite_count, user_id, user_name, user_location, text FROM twitter ").collect();
    }

    public void createTable() {
        hsqlContext.sql("CREATE TABLE IF NOT EXISTS twitter " +
                "(id_str String, " +
                "created_at BIGINT, " +
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
                "created_at BIGINT, " +
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

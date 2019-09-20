import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.hive.HiveContext;

/**
 * Twitter Handler
 *
 * 启动hive的元数据服务  $HIVE_HOME/conf/hive-site.xml,
 * ${HIVE_HOME}/bin/hive --service metastore  1>/dev/null  2>&1  &
 * 并将hive配置文件复制到spark conf下； $HIVE_HOME/lib/mysql-connector-java-5.1.12.jar copy或者软链到$SPARK_HOME/lib/
 * Spark SQL自己也可创建元数据库，并不一定要依赖hive创建元数据库，所以不需要一定启动hive，
 */
public class TwitterHandler {

    private static final String TWITTER_JSON_PATH_100 = "src/main/resources/twitter100.txt";

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
        twitterHandler.createTable();
        twitterHandler.loadData(TWITTER_JSON_PATH_100);
        Row[] results = twitterHandler.selectAll();

        System.out.println(results.length);
        for (Row row : results) {
            System.out.println(row.toString());
        }
        // 1. How many twitter in different time slot?
//        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
//        String dateStart = "Thu Apr 06 15:24:15 +0000 2017";
//        String dateEnd = "Thu Apr 08 15:24:15 +0000 2017";
//        Row[] results1 = hsqlContext.sql("SELECT id_str, created_at, text FROM twitter  WHERE created_at >= '" + dateStart + "' AND created_at <= '" + dateEnd + "'").collect();

        //2. How many average word count of twitter in different location?


        //3. What’s the count of max twitters of one user and what’s his/her name?
    }

    public Row[] selectAll() {
        return hsqlContext.sql("SELECT id_str, user_id, user_name, user_location, created_at, text FROM twitter ").collect();
    }

    public void createTable() {
        hsqlContext.sql("CREATE TABLE IF NOT EXISTS twitter " +
                "(id_str String, " +
                "created_at String, " +
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
                "user_id String, " +
                "user_name String, " +
                "user_location String, " +
                "text String) " +
//                "PARTITIONED BY (id_str STRING) " +
                "ROW FORMAT DELIMITED FIELDS TERMINATED BY ';' LINES TERMINATED BY '\\n'");
    }

    public void loadData(String path) {
        hsqlContext.sql("LOAD DATA LOCAL INPATH '" + path + "' INTO TABLE twitter");
    }
}

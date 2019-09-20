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

    private static final String TWITTER_JSON_PATH = "src/main/resources/twitter.json";
    private static final String TWITTER_JSON_PATH_100 = "src/main/resources/twitter100.json";

    /**
     * @param args
     */
    public static void main(String[] args) {


        SparkConf conf = new SparkConf().setAppName("twitter Application");
        conf.setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        SQLContext sqlContext = new org.apache.spark.sql.SQLContext(sc);

        DataFrame df = sqlContext.read().json(TWITTER_JSON_PATH_100);
        df.show();
        df.printSchema();

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
        HiveContext hsqlContext = new org.apache.spark.sql.hive.HiveContext(sc);

        //hive table: created_at, id_str, text, user_id, user_name, user_location
        hsqlContext.sql("CREATE TABLE IF NOT EXISTS twitter (id_str String, created_at DATE, text String, user String)");
        hsqlContext.sql("LOAD DATA LOCAL INPATH '" + TWITTER_JSON_PATH_100 + "' INTO TABLE twitter");

        Row[] results = hsqlContext.sql("FROM twitter SELECT id_str, created_at, text").collect();
        for (Row row :
                results) {
            System.out.println(row.toString());
        }

        // 1. How many twitter in different time slot?
        //Wed Mar 18 13:46:38 +0000 2009
//        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
//        String dateStart = "Thu Apr 06 15:24:15 +0000 2017";
//        String dateEnd = "Thu Apr 08 15:24:15 +0000 2017";
//        DataFrame d = sqlContext.sql("SELECT * FROM twitter WHERE created_at >= '" + dateStart + "' AND created_at >= '" + dateEnd+"'");
//        System.out.println(d.count());

        //2. How many average word count of twitter in different location?
        //3. What’s the count of max twitters of one user and what’s his/her name?
    }
}

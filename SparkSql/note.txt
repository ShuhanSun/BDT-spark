import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
// col("...") is preferable to df.col("...")
import static org.apache.spark.sql.functions.col;

import java.util.Arrays;
import java.util.Collections;
import java.io.Serializable;

import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;

/**
* Spark
* 是MapReduce的替代方案，而且兼容HDFS、Hive，可融入Hadoop的生态系统，以弥补MapReduce的不足。
* 运行速度提高100倍。易用。兼容。
* 核心：SparkCore：将分布式数据抽象为弹性分布式数据集（RDD），实现了应用任务调度、RPC、序列化和压缩，并为运行在其上的上层组件提供API。
* 封装了对分布式文件的处理流程，提供了类似普通文件处理的编程接口。
* Driver->Cluster Manager ->> WorkerNode[Executor[Task,Task..]]
* RDD DAG
*
* SparkSQL 组件：
* SparkSQL作为Spark生态的一员使得Spark不再受限于Hive，只是兼容Hive；
* Spark为结构化数据处理引入了一个称为Spark SQL的编程模块。
* 它提供了一个称为DataFrame（数据框）的编程抽象，DF的底层仍然是RDD，并且可以充当分布式SQL查询引擎。
* 为结构化和半结构化数据提供支持。
* 
* DataFrame就相当于数据库的一张表。它是个只读的表，不能在运算过程再往里加元素。
* DataFrame是一种以RDD为基础的分布式数据集。DataFrame带有schema元信息，即数据集的每一列都带有名称和类型
* == DataSets
* Spark -> RDD
* SparkSql -> DataFrame
*
* 使用内存列存储（In-Memory Columnar Storage）:在空间占用量和读取吞吐率上都占有很大优势。
*
* Hive与Spark库捆绑为HiveContext，它继承自SQLContext。 使用HiveContext，
 
 * 参考：
 * 1. 提交jar包：
 * # Package a JAR containing your application
 * $ mvn package
 * ...
 * [INFO] Building jar: {..}/{..}/target/simple-project-1.0.jar
 *
 * # Use spark-submit to run your application
 * $ YOUR_SPARK_HOME/bin/spark-submit \
 *   --class "SimpleApp" \
 *   --master local[4] \
 *   target/simple-project-1.0.jar
 *
 *
 * 2. 从hive(->hbase)中读取：
 * Configuration of Hive is done by placing your hive-site.xml, core-site.xml (for security configuration), and hdfs-site.xml (for HDFS configuration) file in conf/.

*/
class SparkSqlTest{


	public static void main(String[] args){
		SparkSession spark = SparkSession
		  .builder()
		  .appName("Java Spark SQL basic example")
		  .config("spark.some.config.option", "some-value")
		  .getOrCreate();
		//With a SparkSession, applications can create DataFrames from an existing RDD, from a Hive table, or from Spark data sources.
		Dataset<Row> df = spark.read().json("examples/src/main/resources/people.json");

		// Displays the content of the DataFrame to stdout
		df.show();
		// Print the schema in a tree format
		df.printSchema();
		// Select only the "name" column
		df.select("name").show();
		// Select everybody, but increment the age by 1
		df.select(col("name"), col("age").plus(1)).show();
		// Select people older than 21
		df.filter(col("age").gt(21)).show();
		// Count people by age
		df.groupBy("age").count().show();

		//Running SQL Queries Programmatically
		// Register the DataFrame as a SQL temporary view
		df.createOrReplaceTempView("people");
		Dataset<Row> sqlDF = spark.sql("SELECT * FROM people");
		sqlDF.show();

		//Global Temporary View:Temporary views in Spark SQL are session-scoped and will disappear if the session that creates it terminates.
		// Register the DataFrame as a global temporary view
		df.createGlobalTempView("people");
		// Global temporary view is tied to a system preserved database `global_temp`
		spark.sql("SELECT * FROM global_temp.people").show();
		// Global temporary view is cross-session
		spark.newSession().sql("SELECT * FROM global_temp.people").show();




		//Creating Datasets
		// Create an instance of a Bean class
		Person person = new Person();
		person.setName("Andy");
		person.setAge(32);

		// Encoders are created for Java beans
		Encoder<Person> personEncoder = Encoders.bean(Person.class);
		Dataset<Person> javaBeanDS = spark.createDataset(
		  Collections.singletonList(person),
		  personEncoder
		);
		javaBeanDS.show();

		// Encoders for most common types are provided in class Encoders
		Encoder<Integer> integerEncoder = Encoders.INT();
		Dataset<Integer> primitiveDS = spark.createDataset(Arrays.asList(1, 2, 3), integerEncoder);
		Dataset<Integer> transformedDS = primitiveDS.map(
		    (MapFunction<Integer, Integer>) value -> value + 1,
		    integerEncoder);
		transformedDS.collect(); // Returns [2, 3, 4]

		// DataFrames can be converted to a Dataset by providing a class. Mapping based on name
		String path = "examples/src/main/resources/people.json";
		Dataset<Person> peopleDS = spark.read().json(path).as(personEncoder);
		peopleDS.show();





		//Interoperating with RDDs
		//Inferring the Schema Using Reflection
		// Create an RDD of Person objects from a text file
		JavaRDD<Person> peopleRDD = spark.read()
		  .textFile("examples/src/main/resources/people.txt")
		  .javaRDD()
		  .map(line -> {
		    String[] parts = line.split(",");
		    Person person = new Person();
		    person.setName(parts[0]);
		    person.setAge(Integer.parseInt(parts[1].trim()));
		    return person;
		  });

		// Apply a schema to an RDD of JavaBeans to get a DataFrame
		Dataset<Row> peopleDF = spark.createDataFrame(peopleRDD, Person.class);
		// Register the DataFrame as a temporary view
		peopleDF.createOrReplaceTempView("people");

		// SQL statements can be run by using the sql methods provided by spark
		Dataset<Row> teenagersDF = spark.sql("SELECT name FROM people WHERE age BETWEEN 13 AND 19");

		// The columns of a row in the result can be accessed by field index
		Encoder<String> stringEncoder = Encoders.STRING();
		Dataset<String> teenagerNamesByIndexDF = teenagersDF.map(
		    (MapFunction<Row, String>) row -> "Name: " + row.getString(0),
		    stringEncoder);
		teenagerNamesByIndexDF.show();

		// or by field name
		Dataset<String> teenagerNamesByFieldDF = teenagersDF.map(
		    (MapFunction<Row, String>) row -> "Name: " + row.<String>getAs("name"),
		    stringEncoder);
		teenagerNamesByFieldDF.show();



		//Data Sources
		Dataset<Row> usersDF = spark.read().load("examples/src/main/resources/users.parquet");
		usersDF.select("name", "favorite_color").write().save("namesAndFavColors.parquet");
		//Manually Specifying Options:(json, parquet, jdbc, orc, libsvm, csv, text).
		Dataset<Row> peopleDF =
		  spark.read().format("json").load("examples/src/main/resources/people.json");
		peopleDF.select("name", "age").write().format("parquet").save("namesAndAges.parquet");
		//Run SQL on files directly
		Dataset<Row> sqlDF =
  			spark.sql("SELECT * FROM parquet.`examples/src/main/resources/users.parquet`");
  		//Save Modes
  		//Saving to Persistent Tables
  		//Bucketing, Sorting and Partitioning
  		peopleDF.write().bucketBy(42, "name").sortBy("age").saveAsTable("people_bucketed");
  		peopleDF
		  .write()
		  .partitionBy("favorite_color")
		  .bucketBy(42, "name")
		  .saveAsTable("people_partitioned_bucketed");


		  //JSON Datasets

		// A JSON dataset is pointed to by path.
		// The path can be either a single text file or a directory storing text files
		Dataset<Row> people = spark.read().json("examples/src/main/resources/people.json");

		// The inferred schema can be visualized using the printSchema() method
		people.printSchema();
		// root
		//  |-- age: long (nullable = true)
		//  |-- name: string (nullable = true)

		// Creates a temporary view using the DataFrame
		people.createOrReplaceTempView("people");

		// SQL statements can be run by using the sql methods provided by spark
		Dataset<Row> namesDF = spark.sql("SELECT name FROM people WHERE age BETWEEN 13 AND 19");
		namesDF.show();
		// +------+
		// |  name|
		// +------+
		// |Justin|
		// +------+

		// Alternatively, a DataFrame can be created for a JSON dataset represented by
		// a Dataset<String> storing one JSON object per string.
		List<String> jsonData = Arrays.asList(
		        "{\"name\":\"Yin\",\"address\":{\"city\":\"Columbus\",\"state\":\"Ohio\"}}");
		Dataset<String> anotherPeopleDataset = spark.createDataset(jsonData, Encoders.STRING());
		Dataset<Row> anotherPeople = spark.read().json(anotherPeopleDataset);
		anotherPeople.show();
		// +---------------+----+


		//Hive Tables




	}
	

	public static class Person implements Serializable {
	  private String name;
	  private int age;

	  public String getName() {
	    return name;
	  }

	  public void setName(String name) {
	    this.name = name;
	  }

	  public int getAge() {
	    return age;
	  }

	  public void setAge(int age) {
	    this.age = age;
	  }
	}


	public static class MyAverage extends UserDefinedAggregateFunction {

	  private StructType inputSchema;
	  private StructType bufferSchema;

	  public MyAverage() {
	    List<StructField> inputFields = new ArrayList<>();
	    inputFields.add(DataTypes.createStructField("inputColumn", DataTypes.LongType, true));
	    inputSchema = DataTypes.createStructType(inputFields);

	    List<StructField> bufferFields = new ArrayList<>();
	    bufferFields.add(DataTypes.createStructField("sum", DataTypes.LongType, true));
	    bufferFields.add(DataTypes.createStructField("count", DataTypes.LongType, true));
	    bufferSchema = DataTypes.createStructType(bufferFields);
	  }
	  // Data types of input arguments of this aggregate function
	  public StructType inputSchema() {
	    return inputSchema;
	  }
	  // Data types of values in the aggregation buffer
	  public StructType bufferSchema() {
	    return bufferSchema;
	  }
	  // The data type of the returned value
	  public DataType dataType() {
	    return DataTypes.DoubleType;
	  }
	  // Whether this function always returns the same output on the identical input
	  public boolean deterministic() {
	    return true;
	  }
	  // Initializes the given aggregation buffer. The buffer itself is a `Row` that in addition to
	  // standard methods like retrieving a value at an index (e.g., get(), getBoolean()), provides
	  // the opportunity to update its values. Note that arrays and maps inside the buffer are still
	  // immutable.
	  public void initialize(MutableAggregationBuffer buffer) {
	    buffer.update(0, 0L);
	    buffer.update(1, 0L);
	  }
	  // Updates the given aggregation buffer `buffer` with new input data from `input`
	  public void update(MutableAggregationBuffer buffer, Row input) {
	    if (!input.isNullAt(0)) {
	      long updatedSum = buffer.getLong(0) + input.getLong(0);
	      long updatedCount = buffer.getLong(1) + 1;
	      buffer.update(0, updatedSum);
	      buffer.update(1, updatedCount);
	    }
	  }
	  // Merges two aggregation buffers and stores the updated buffer values back to `buffer1`
	  public void merge(MutableAggregationBuffer buffer1, Row buffer2) {
	    long mergedSum = buffer1.getLong(0) + buffer2.getLong(0);
	    long mergedCount = buffer1.getLong(1) + buffer2.getLong(1);
	    buffer1.update(0, mergedSum);
	    buffer1.update(1, mergedCount);
	  }
	  // Calculates the final result
	  public Double evaluate(Row buffer) {
	    return ((double) buffer.getLong(0)) / buffer.getLong(1);
	  }
	}

	public static void average(){

		// Register the function to access it
		spark.udf().register("myAverage", new MyAverage());

		Dataset<Row> df = spark.read().json("examples/src/main/resources/employees.json");
		df.createOrReplaceTempView("employees");
		df.show();
		// +-------+------+
		// |   name|salary|
		// +-------+------+
		// |Michael|  3000|
		// |   Andy|  4500|
		// | Justin|  3500|
		// |  Berta|  4000|
		// +-------+------+

		Dataset<Row> result = spark.sql("SELECT myAverage(salary) as average_salary FROM employees");
		result.show();
	}
}

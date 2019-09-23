package org.apache.spark.spark_streaming_kafka_0_10_2;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.sql.SQLException;
import java.util.*;

import org.apache.spark.SparkConf;
import org.apache.spark.TaskContext;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import scala.Tuple2;

import org.apache.spark.streaming.kafka.KafkaUtils;

import kafka.serializer.StringDecoder;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.spark.api.java.function.FlatMapFunction;
//import org.apache.spark.sql.*;
//import org.apache.spark.sql.streaming.StreamingQuery;

public class KafkaToStreaming {

	private static StringBuffer cache = new StringBuffer();
	private static SparkSQLUtility twitterHandler;

	static class KafkaStreamSeqOutputFormat extends
			SequenceFileOutputFormat<Text, BytesWritable> {

	}

	public static void main(String[] args) {
		// try {
		// KafkaToStreaming.createAppendHDFS("/user/cloudera/output/part-00000.txt","hello world.");
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// KafkaToStreaming.append();
		KafkaToStreaming.test();
	}

	public static void test() {

		SparkConf conf = new SparkConf().setAppName("kafka-sandbox")
		// .setMaster("local[*]")
		// "spark://127.0.0.1:33020")
		// .setJars(
		// new String[] {
		// "/home/cloudera/bdtproject/BDT-spark/TwitterDS/spark-streaming-kafka-0-10_2.11/target/spark-streaming-kafka-0-10_2.11-0.0.1-SNAPSHOT.jar"
		// })
		;
		JavaSparkContext sc = new JavaSparkContext(conf);
		JavaStreamingContext ssc = new JavaStreamingContext(sc, new Duration(
				15000));

		// create Hive
		twitterHandler = new SparkSQLUtility(sc);

		Set<String> topics = Collections.singleton("TutorialTopic");
		Map<String, String> kafkaParams = new HashMap<>();
		kafkaParams.put("metadata.broker.list", "localhost:9092");

		JavaPairInputDStream<String, String> directKafkaStream = KafkaUtils
				.createDirectStream(ssc, String.class, String.class,
						StringDecoder.class, StringDecoder.class, kafkaParams,
						topics);

		directKafkaStream.foreachRDD(rdd -> {
			System.out.println("--- New RDD with " + rdd.partitions().size()
					+ " partitions and " + rdd.count() + " records");
			// rdd.saveAsHadoopFile("a",
			// Text.class,BytesWritable.class,KafkaStreamSeqOutputFormat.class);
			// rdd.coalesce(1, true);
			// rdd.repartition(1);

				StringBuffer thiscache = new StringBuffer();
				rdd.foreach(record -> {
					String one = record._2;
					// Func(true,one,0);
					// System.out.println(one);

				});
				for (Tuple2<String, String> r : rdd.collect()) {
					thiscache.append(Utils.twitterJson2String(r._2) + "\r\n");
				}
				String filename = "/user/cloudera/output/part-"
						+ String.valueOf(System.currentTimeMillis()) + ".txt";
				KafkaToStreaming.createAppendHDFS(filename,
						thiscache.toString());
				twitterHandler.loadData(filename);
				System.out.println("--- save to file: " + filename
						+ " and load to table.--");
			});

		// directKafkaStream.transform(transformFunc)
		// directKafkaStream.saveAsHadoopFiles("twitter", "seq",
		// Text.class,BytesWritable.class, KafkaStreamSeqOutputFormat.class);

		// JavaDStream<String> valueDStream = directKafkaStream
		// .map(new Function<Tuple2<String, String>, String>() {
		// /**
		// *
		// */
		// private static final long serialVersionUID = 5905913587447035735L;
		//
		// public String call(Tuple2<String, String> v1)
		// throws Exception {
		// // Func(true,v1._2);
		//
		// return v1._2();
		// }
		// });
		// valueDStream.count().print();
		// String suffix = "seq";
		//
		// valueDStream
		// .dstream()
		// .repartition(1)
		// .saveAsTextFiles("hdfs://localhost/user/cloudera/output/twitter",
		// suffix);

		// (new Thread(merge)).start();
		ssc.start();
		ssc.awaitTermination();
	}

	public static synchronized String Func(boolean isappend,
			String appendstring, int size) {
		if (isappend) {
			KafkaToStreaming.cache.append(Utils
					.twitterJson2String(appendstring) + "\r\n");
		} else {

			String tmp = cache.toString();
			if (tmp.length() < size) {
				return "";
			}
			cache = new StringBuffer();
			return tmp;
		}
		return "";
	}

	static Runnable merge = new Runnable() {
		String filename = "/user/cloudera/output/part-"
				+ String.valueOf(System.currentTimeMillis()) + ".txt";

		@Override
		public void run() {

			while (true) {

				try {
					String content = Func(false, "", 1);

					// if (content.length() > 1024 * 1024){
					// filename =
					// "/user/cloudera/output/part-"+String.valueOf(System.currentTimeMillis())+".txt";
					// }
					System.out.println("---------------------"
							+ content.length() + "------" + cache.length()
							+ "---------------------");
					if (content.length() > 1) {

						filename = "/user/cloudera/output/part-"
								+ String.valueOf(System.currentTimeMillis())
								+ ".txt";
						KafkaToStreaming.createAppendHDFS(filename, content);
						twitterHandler.loadData(filename);
						// HiveJDBC.loadData("twitter",filename);
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	};

	/*
	 * public static void sparkStreaming(){
	 * 
	 * SparkSession spark = SparkSession .builder()
	 * .appName("JavaStructuredNetworkWordCount") .config("spark.master",
	 * "local") .getOrCreate();
	 * 
	 * 
	 * spark.readStream() .format("kafka") .option("kafka.bootstrap.servers",
	 * "localhost:9092") .option("subscribe", "TutorialTopic") .load()
	 * .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
	 * .writeStream().format("json") // can be "orc", "json", "csv", etc.
	 * .option("path", "/user/cloudera/output") .start(); }
	 */

	// public static void append() {
	// String hdfs_path = "hdfs://localhost/user/cloudera/output/part-00000";
	// Configuration conf = new Configuration();
	// conf.setBoolean("dfs.support.append", true);
	//
	// String inpath = "/home/cloudera/bdtproject/twitters.txt";
	// FileSystem fs = null;
	// try {
	// fs = FileSystem.get(URI.create(hdfs_path), conf);
	//
	// InputStream in = new BufferedInputStream(
	// new FileInputStream(inpath));
	// OutputStream out = fs.append(new Path(hdfs_path));
	// out.write("appending into file. \n".getBytes());
	// out.write("appending into file. \n".getBytes());
	// out.write("appending into file. \n".getBytes());
	//
	// out.flush();
	// Thread.sleep(300);
	// fs.close();
	// // IOUtils.copyBytes(in, out, 4096, true);
	// } catch (IOException | InterruptedException e) {
	// e.printStackTrace();
	// }
	// }

	public static void createAppendHDFS(String filePath, String content)
			throws IOException {
		Configuration hadoopConfig = new Configuration();
		hadoopConfig.set("fs.defaultFS", "hdfs://localhost/");
		hadoopConfig.setBoolean("dfs.support.append", true);

		FileSystem fileSystem = FileSystem.get(hadoopConfig);
		// String filePath = "/user/cloudera/output/part-00000.txt"; //
		// hdfs://localhost/user/cloudera/output
		Path hdfsPath = new Path(filePath);
		// fShell.setrepr((short) 1, filePath);
		FSDataOutputStream fileOutputStream = null;
		try {
			if (fileSystem.exists(hdfsPath)) {
				fileOutputStream = fileSystem.append(hdfsPath);
				// fileOutputStream.writeBytes("appending into file. \n");
				fileOutputStream.writeBytes(content + "\n");
			} else {
				fileOutputStream = fileSystem.create(hdfsPath);
				// fileOutputStream.writeBytes("creating and writing into file\n");
				fileOutputStream.writeBytes(content + "\n");
			}
		} finally {
			if (fileOutputStream != null) {
				fileOutputStream.close();
			}
			if (fileSystem != null) {
				fileSystem.close();
			}
		}
	}
}

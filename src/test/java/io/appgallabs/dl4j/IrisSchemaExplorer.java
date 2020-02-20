package io.appgallabs.dl4j;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.transform.analysis.DataAnalysis;
import org.datavec.api.transform.analysis.columns.DoubleAnalysis;
import org.datavec.api.transform.schema.Schema;
import org.datavec.api.transform.ui.HtmlAnalysis;
import org.datavec.api.writable.Writable;
import org.datavec.spark.transform.AnalyzeSpark;
import org.datavec.spark.transform.misc.StringToWritablesFunction;
import org.deeplearning4j.examples.download.DownloaderUtility;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class IrisSchemaExplorer {

    @Test
    public void testExplorer() throws Exception {
        //Build a Schema based on CSV comma-separated values and add the labels
        Schema schema = new Schema.Builder()
                .addColumnsDouble("Sepal length", "Sepal width", "Petal length", "Petal width")
                .addColumnInteger("Species")
                .build();

        SparkConf conf = new SparkConf();
        conf.setMaster("local[*]");
        conf.setAppName("DataVec Example");

        JavaSparkContext sc = new JavaSparkContext(conf);

        String directory = DownloaderUtility.IRISDATA.Download();
        JavaRDD<String> stringData = sc.textFile(directory);

        //We first need to parse this comma-delimited (CSV) format; we can do this using CSVRecordReader:
        RecordReader rr = new CSVRecordReader();
        JavaRDD<List<Writable>> parsedInputData = stringData.map(new StringToWritablesFunction(rr));

        //Produce an Analysis of the DataSet
        int maxHistogramBuckets = 10;
        DataAnalysis dataAnalysis = AnalyzeSpark.analyze(schema, parsedInputData, maxHistogramBuckets);

        System.out.println(dataAnalysis);

        //We can get statistics on a per-column basis:
        DoubleAnalysis da = (DoubleAnalysis)dataAnalysis.getColumnAnalysis("Sepal length");
        double minValue = da.getMin();
        double maxValue = da.getMax();
        double mean = da.getMean();

        System.out.println(da);

        //HTML Output of dataAnalysis
        HtmlAnalysis.createHtmlAnalysisFile(dataAnalysis, new File("DataVecIrisAnalysis.html"));

        //HTML Output of Sepal length Analysis, but same output as dataAnalysis. which is interesting
        //considering da is just a subset of dataAnalysis.
        //Food for thought, but moving to the next component
        HtmlAnalysis.createHtmlAnalysisFile(dataAnalysis, new File("DataVecIrisAnalysis2.html"));
    }
}

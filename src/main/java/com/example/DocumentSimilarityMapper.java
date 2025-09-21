package com.example;
import java.io.IOException;
import java.util.HashSet;
import java.util.StringTokenizer;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

public class DocumentSimilarityMapper extends Mapper<Object, Text, Text, Text> {
    
    public void map(Object Key, Text value, Context context) throws IOException, InterruptedException{
        
        String fileName = ((FileSplit) context.getInputSplit()).getPath().getName();
        HashSet<String> wordSet = new HashSet<>();
        StringTokenizer tokenizer = new StringTokenizer(value.toString());

        while (tokenizer.hasMoreTokens()) {
            wordSet.add(tokenizer .nextToken().toLowerCase());
        }
        context.write(new Text(fileName), new Text(String.join(",", wordSet)));

    }
}
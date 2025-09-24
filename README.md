# **Document Similarity Using Hadoop MapReduce**

## **Overview**

This project calculates the **Jaccard Similarity** between multiple documents stored in a Hadoop distributed file system (HDFS). The implementation is based on Hadoop MapReduce, with a Mapper that processes each document and a Reducer that calculates the similarity between all pairs of documents.

## **Approach and Implementation**

### Mapper

The **Mapper** in this implementation processes the input documents and creates a set of words for each document. Each document is identified by its name (e.g., `doc1.txt`, `doc2.txt`, etc.). The words in the document are split by spaces and stored in a set to eliminate duplicates. The output key-value pairs from the Mapper are:

- **Key**: Document name (e.g., `doc1.txt`)
- **Value**: Comma-separated list of words in that document

The Mapper reads the input file, processes each document's words, and emits a key-value pair for each document.

### Reducer

The **Reducer** collects the word sets for all documents. It calculates the Jaccard similarity for each pair of documents by computing the intersection and union of their word sets:

- **Intersection**: Common words between two documents.
- **Union**: All unique words between the two documents.

The Jaccard Similarity is calculated as:
```
Jaccard Similarity = |A ∩ B| / |A ∪ B|
```
Where:
- `|A ∩ B|` is the number of words common to both documents
- `|A ∪ B|` is the total number of unique words in both documents

## Example Calculation

Consider two documents:
 
**doc1.txt words**: `{hadoop, is, a, distributed, system}`
**doc2.txt words**: `{hadoop, is, used, for, big, data, processing}`

- Common words: `{hadoop, is}`
- Total unique words: `{hadoop, is, a, distributed, system, used, for, big, data, processing}`

Jaccard Similarity calculation:
```
|A ∩ B| = 2 (common words)
|A ∪ B| = 10 (total unique words)

Jaccard Similarity = 2/10 = 0.2 or 20%
```
Jaccard Similarity is commonly used in:
- Document similarity detection
- Plagiarism checking
- Recommendation systems
- Clustering algorithms

### **Expected Output**  

The output should show the Jaccard Similarity between document pairs in the following format:  
```
(doc1, doc2) -> 60%  
(doc2, doc3) -> 50%  
```
---

## Setup and Execution

### 1. **Start the Hadoop Cluster**

Run the following command to start the Hadoop cluster:

```bash
docker compose up -d
```

### 2. **Build the Code**

Build the code using Maven:

```bash
mvn install
```

### 3. **Move JAR File to Shared Folder**

Move the generated JAR file to a shared folder for easy access:

```bash
mv target/*.jar jar_file/
```

### 4. **Copy JAR to Docker Container**

Copy the JAR file to the Hadoop ResourceManager container:

```bash
docker cp /workspaces/Assignment-2-MapReduce-DocSimilarity/target/DocumentSimilarity-0.0.1-SNAPSHOT.jar resourcemanager:/opt/hadoop-3.2.1/share/hadoop/mapreduce/
```

### 5. **Move Dataset to Docker Container**

Copy the dataset to the Hadoop ResourceManager container:

```bash
docker cp input_files/ resourcemanager:/opt/hadoop-3.2.1/share/hadoop/mapreduce/
```

### 6. **Connect to Docker Container**

Access the Hadoop ResourceManager container:

```bash
docker exec -it resourcemanager /bin/bash
```

Navigate to the Hadoop directory:

```bash
cd /opt/hadoop-3.2.1/share/hadoop/mapreduce/
```

### 7. **Set Up HDFS**

Create a folder in HDFS for the input dataset:

```bash
hadoop fs -mkdir -p /input/dataset
```

Copy the input dataset to the HDFS folder:

```bash
hadoop fs -put ./input_files /input/dataset
```

### 8. **Execute the MapReduce Job**

Run your MapReduce job using the following command:

```bash
hadoop jar DocumentSimilarity-0.0.1-SNAPSHOT.jar com.example.controller.DocumentSimilarityDriver /input/dataset/input_files /output
```

```bash
hadoop jar DocumentSimilarity-0.0.1-SNAPSHOT.jar com.example.controller.DocumentSimilarityDriver /input/dataset/input_files /output/SingleNode
```

### 9. **View the Output**

To view the output of your MapReduce job, use:

```bash
hadoop fs -cat /output/*
```

### 10. **Copy Output from HDFS to Local OS**

To copy the output from HDFS to your local machine:

1. Use the following command to copy from HDFS:
    ```bash
    hdfs dfs -get /output /opt/hadoop-3.2.1/share/hadoop/mapreduce/
    ```

2. use Docker to copy from the container to your local machine:
   ```bash
   exit 
   ```
    ```bash
    docker cp resourcemanager:/opt/hadoop-3.2.1/share/hadoop/mapreduce/output/ output/
    ``` 

### **Sample Input**  

The following are the sample inputs and the output after jaccard similarity is performed: 

#### **Example Documents**  

##### **doc1.txt**  
```
hadoop is a distributed system
```

##### **doc2.txt**  
```
hadoop is used for big data processing
```

##### **doc3.txt**  
```
big data is important for data analysis and processing
```
##### **doc4.txt**  
```
hadoop and big data is used for analysis and processing
```
### **Sample Output**  
(doc3.txt, doc2.txt)    -> 50%
(doc4.txt, doc3.txt)    -> 70%
(doc4.txt, doc2.txt)    -> 78%
---

### **Challenges Faced**
```
Limited Debugging Options
	•	Since the code runs in a distributed Hadoop environment, debugging requires checking logs rather than simple print statements, making error detection harder

Path Configuration Issues
	•	If the input and output paths are not correctly specified when running the job, it may fail or overwrite existing data.

```# Assignment-2-MapReduce-DocSimilarity

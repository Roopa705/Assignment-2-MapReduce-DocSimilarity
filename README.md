# Assignment 2: Document Similarity using MapReduce

**Name:** Roopa Naisa

**Student ID:** 801431316

## Approach and Implementation

### Mapper Design

1. Parse the line to separate the `DocumentID` (e.g., `Document1`) from the document body.
2. Tokenize the document body:
   - Convert to lowercase.
   - Replace non-alphanumeric characters with space.
   - Split on whitespace.
   - (Optional) Generate k-shingles if configured.
3. Build a `Set<String>` of unique tokens for this document.
4. For every token `t` in the set, emit intermediate pairs to help group documents by token. Two common strategies:
   - Emit `(t, DocumentID)` so the reducer receives, for each token, the list of documents that contain it — reducer can then enumerate all document pairs that share that token and increment an intersection counter.
   - Or emit `(DocumentID, tokenSet)` if you prefer pairwise join later.  

This repository uses the token-key approach: the mapper emits `(token, documentID)`.

---


### Reducer Design

**Input key-value:** `(Text token, Iterable<Text> documentIDs)`

**Reducer steps:**
1. Receive each token and the list of document IDs that contain the token.
2. For that token, generate all unordered pairs `(docA, docB)` from the list (skip pairs where `docA == docB`).
3. For each pair, increment a counter in an in-memory map `pairIntersectionCount[(docA, docB)] += 1`. This map (distributed across reducers keyed deterministically) accumulates the size of the intersection `|A ∩ B|`.
4. Separately, the program must know or compute each document's token set size `|A|` and `|B|`. This can be achieved by:
   - Emitting a special record from the mapper `(\_DOCSIZE\_, DocumentID=|tokens|)` and handling it in the reducer; or
   - Running a preparatory MapReduce job that computes and writes document sizes to HDFS that the final job reads.
5. After reducers finish counting intersections, compute the Jaccard similarity for each pair:
   ```
   similarity = intersection / (sizeA + sizeB - intersection)
   ```
6. Emit final similarity line for each pair with intersection above zero (or above a configured threshold).

**Final output format:**
```
Document1,Document2    Similarity: 0.56
Document1,Document3    Similarity: 0.42
...
```

---

### Overall Data Flow

1. **Input**: Plain text files in `input_files/` folder where each file contains document lines. The driver job packs them into the Hadoop input split.
2. **Map**: Tokenize each document, emit `(token, documentID)` and optionally `(DOCSIZE, documentID|size)`.
3. **Shuffle & Sort**: Group by `token`. All documents that share the same token arrive at the same reducer.
4. **Reduce**: For each token, emit all document pairs and increment pairwise intersection counters. At the end, reducers compute Jaccard similarity using stored document sizes and write final similarity scores to HDFS `output/` folder.
5. **Output**: A text file containing similarity scores for document pairs.

---

## Setup and Execution

### ` Note: The below commands are the ones used for the Hands-on. You need to edit these commands appropriately towards your Assignment to avoid errors. `

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

### 3. **Copy JAR to Docker Container**

Copy the JAR file to the Hadoop ResourceManager container:

```bash
docker cp /workspaces/Assignment-2-MapReduce-DocSimilarity/target/DocumentSimilarity-0.0.1-SNAPSHOT.jar resourcemanager:/opt/hadoop-3.2.1/share/hadoop/mapreduce/
```

### 4. **Move Dataset to Docker Container**

Copy the dataset to the Hadoop ResourceManager container:

```bash
docker cp input_files/ resourcemanager:/opt/hadoop-3.2.1/share/hadoop/mapreduce/
```

### 5. **Connect to Docker Container**

Access the Hadoop ResourceManager container:

```bash
docker exec -it resourcemanager /bin/bash
```

Navigate to the Hadoop directory:

```bash
cd /opt/hadoop-3.2.1/share/hadoop/mapreduce/
```

### 6. **Set Up HDFS**

Create a folder in HDFS for the input dataset:

```bash
hadoop fs -mkdir -p /input/dataset
```

Copy the input dataset to the HDFS folder:

```bash
hadoop fs -put ./input_files /input/dataset
```

### 7. **Execute the MapReduce Job**

Run your MapReduce job using the following command:

```bash
hadoop jar DocumentSimilarity-0.0.1-SNAPSHOT.jar com.example.controller.DocumentSimilarityDriver /input/dataset/input_files /output
```

### 8. **View the Output**

To view the output of your MapReduce job, use:

```bash
hadoop fs -cat /output/*
```

### 9. **Copy Output from HDFS to Local OS**

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

---

## Challenges and Solutions

### Java class execption:
- **Problem**: While running mapreduce job to get output, The code was throughing class execption error.
- **Solution**: In the original Git template, In the src folder java folder was missing. Once, Java folder was added the code produced output with no issues.

### Running with single Node:
- **Problem**: While running mapreduce job to get output, The code was throughing error.
- **Solution**: Changing RAM memory to allcate change in nodemanager helped in producing output.
---
## Sample Input

**Input from `small_dataset.txt`**
```
Document1 This is a sample document containing words
Document2 Another document that also has words
Document3 Sample text with different words
```
## Sample Output

**Output from `small_dataset.txt`**
```
"Document1, Document2 Similarity: 0.56"
"Document1, Document3 Similarity: 0.42"
"Document2, Document3 Similarity: 0.50"
```
## Obtained Output: 

```
(document2.txt, document1.txt)	-> 0.16
(document3.txt, document1.txt)	-> 0.19
(document3.txt, document2.txt)	-> 0.13
```
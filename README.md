# 🎓 TU Delft: Data Science Portfolio
### Big Data Processing (BDP) & Data Mining (DM)

![TU Delft](https://img.shields.io/badge/University-TU%20Delft-00A6D6?style=for-the-badge&logo=delft-university-of-technology&logoColor=white)
![Language](https://img.shields.io/badge/Language-Scala%20%7C%20Python%20%7C%20SQL-orange?style=for-the-badge)
![Tools](https://img.shields.io/badge/Tools-Spark%20%7C%20Flink%20%7C%20Unix-red?style=for-the-badge)

Welcome to my central archive for the **Data Science & Technology** track at TU Delft. This repository contains assignments, lab work, and project implementations focusing on large-scale data systems and pattern recognition.

---

## 🏗 Repository Structure

The project is organized into two main pillars based on the curriculum:

### 🚀 Big Data Processing (BDP)
Core focus on distributed computing and stream processing.
* **[flink](./BDP/flink)**: Stream processing implementations using the Apache Flink DataStream API.
* **[fp](./BDP/fp)**: Functional Programming foundations in Scala.
* **[spark](./BDP/spark)**: Batch processing and RDD manipulation.
* **[unix](./BDP/unix)**: High-performance data plumbing using shell utilities.
* **[scala_raw](./BDP/scala_raw)**: Pure Scala implementations and logic.
* **[zips](./BDP/zips)**: Compressed datasets and archive resources.

### 🔍 Data Mining (DM)
Focused on algorithmic pattern discovery.
* **[DM](./DM)**: Algorithms for frequent itemset mining, clustering, and link analysis (PageRank).

---

## 🛠 Tech Stack

| Category | Tools/Frameworks |
| :--- | :--- |
| **Engines** | Apache Spark, Apache Flink |
| **Languages** | Scala, Python (PySpark), Bash |
| **Storage** | HDFS, Parquet, Avro |
| **Concepts** | MapReduce, Stream Processing, Lambda Architecture |

---

## 📈 Key Learnings
> "In God we trust, all others bring data." — *W. Edwards Deming*

* **Scalability:** Writing code that scales from a single machine to a massive cluster.
* **Latency vs Throughput:** Navigating the trade-offs in real-time streaming vs. heavy batch processing.
* **Algorithm Design:** Implementing MinHash, LSH, and CURE to handle massive datasets.

---

## 📥 Getting Started

To run the BDP assignments, you'll generally need **SBT** (Scala Build Tool) installed.

```bash
# Clone the repo
git clone [https://github.com/your-username/your-repo-name.git](https://github.com/your-username/your-repo-name.git)

# Example: Run a Spark job
cd BDP/spark
sbt run

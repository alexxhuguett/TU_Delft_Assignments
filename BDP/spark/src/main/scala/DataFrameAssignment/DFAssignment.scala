package DataFrameAssignment

import java.sql.Timestamp

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import utils.File

/**
  * Note read the comments carefully, as they describe the expected result and may contain hints in how
  * to tackle the exercises. Note that the data that is given in the examples in the comments does
  * reflect the format of the data, but not the result the graders expect (unless stated otherwise).
  */
object DFAssignment {


  /**
   *                                     Description
   *
   * To get a better overview of the data, we want to see only a few columns out of the data. We want to know
   * the committer name, the timestamp of the commit and the length of the message in that commit
   *
   *                                      Output
   *
   *
   * | committer      | timestamp            | message_length |
   * |----------------|----------------------|----------------|
   * | Harbar-Inbound | 2019-03-10T15:24:16Z | 1              |
   * | ...            | ...                  | ...            |
   *
   *                                       Hints
   *
   * Try to work out the individual stages of the exercises, which makes it easier to track bugs,
   * and figure out how Spark Dataframes and their operations work.
   *
   * You can also use the `printSchema()` function and `show()` function to take a look at the structure
   * and contents of the Dataframes.
   *
   * For mapping values of a single column, look into user defined functions (udf)
   *
   * @param commits Commit Dataframe, created from the data_raw.json file.
   * @return DataFrame of commits including the commit timestamp
   *         and the length of the message in that commit.
   */
  def assignment_12(commits: DataFrame): DataFrame = {
    val x = commits.select(
      col("commit.committer.name").as("committer"),
      col("commit.committer.date").as("timestamp"),
      length(col("commit.message")).as("message_length")
    )
    x.show()
    x
  }

  /**
   *                                    Description
   *
   * In this exercise we want to know all the commit SHAs from a list of commit committers.
   * We want to order them by the committer names alphabetically.
   *
   *                                      Output
   *
   * | committer      | sha                                      |
   * |----------------|------------------------------------------|
   * | Harbar-Inbound | 1d8e15a834a2157fe7af04421c42a893e8a1f23a |
   * | ...            | ...                                      |
   *
   * @param commits Commit Dataframe, created from the data_raw.json file.
   * @param committers Sequence of String representing the authors from which we want to know their respective commit
   *                SHAs.
   * @return DataFrame of commits from the requested authors including the commit SHA.
   */
  def assignment_13(commits: DataFrame, committers: Seq[String]): DataFrame = {
    commits
      .filter(col("commit.committer.name").isin(committers: _*))
      .select(
        col("commit.committer.name").as("committer"),
        col("sha").as("sha")
      )
      .orderBy(col("committer").asc)
  }

  /**
   *                                   Description
   *
   * We want to generate yearly dashboards for all users, per each project they contribute to.
   * In order to achieve that, we need the data to be partitioned by years.
   *
   *
   *                                      Output
   *
   * | repository | committer        | year | count   |
   * |------------|------------------|------|---------|
   * | Maven      | magnifer         | 2019 | 21      |
   * | .....      | ..               | .... | ..      |
   *
   * @param commits Commit Dataframe, created from the data_raw.json file.
   * @return Dataframe containing 4 columns, Repository name, committer name, year
   *         and the number of commits for a given year.
   */
  def assignment_14(commits: DataFrame): DataFrame = {
    val x = commits
      .select(
        split(col("url"), "/").getItem(5).as("repository"),
        col("commit.committer.name").as("committer"),
        year(col("commit.committer.date")).as("year")
      )
      .groupBy("repository", "committer", "year")
      .count()
    x.show()
    x
  }

  /**
   *                                        Description
   *
   * A developer is interested in what day of the week some commits are pushed. Extend the DataFrame
   * by determining for each row the day of the week the commit was made on.
   *
   *                                          Output
   *
   * | day    |
   * |--------|
   * | Mon    |
   * | Fri    |
   * | ...    |
   *
   *                                           Hints
   *
   * Look into SQL functions in for Spark SQL.
   *
   * @param commits Commit Dataframe, created from the data_raw.json file.
   * @return the inputted DataFrame appended with a day column.
   */
  def assignment_15(commits: DataFrame): DataFrame = {
    commits
      .withColumn("day", date_format(to_date(col("commit.committer.date")), "EEE"))
  }

  /**
   *                                            Description
   *
   * We want to know how often some committers commit, and more specifically, what are their time intervals
   * between their commits. To achieve that, for each commit, we want to add two columns:
   * the column with the previous commits of that user and the next commit of that user. The dates provided should be
   * independent from repository - if a user works on a few repositories at the same time, the previous date or the
   * next date can be from a different repository.
   *
   *                                              Output
   *
   *
   * | $oid                     	| prev_date   	           | date                     | next_date 	             |
   * |--------------------------	|--------------------------|--------------------------|--------------------------|
   * | 5ce6929e6480fd0d91d3106a 	| 2019-01-03T09:11:26.000Z | 2019-01-27T07:09:13.000Z | 2019-03-04T15:21:52.000Z |
   * | 5ce693156480fd0d5edbd708 	| 2019-01-27T07:09:13.000Z | 2019-03-04T15:21:52.000Z | 2019-03-06T13:55:25.000Z |
   * | 5ce691b06480fd0fe0972350 	| 2019-03-04T15:21:52.000Z | 2019-03-06T13:55:25.000Z | 2019-04-14T14:17:23.000Z |
   * | ...                      	| ...    	                 | ...                      | ...       	             |
   *
   *                                               Hints
   *
   * Look into Spark sql's Window to have more expressive power in custom aggregations
   *
   *
   * @param commits Commit DataFrame, see commit.json and data_raw.json for the structure of the file, or run
   *                `println(commits.schema)`.
   * @param committerName Name of the author for which the result must be generated.
   * @return DataFrame with a columns `$oid` , `prev_date`, `date` and `next_date`
   */
  def assignment_16(commits: DataFrame, committerName: String): DataFrame = {
    val x = commits
      .select(
        col("_id.$oid").as("oid"),
        col("commit.committer.date"),
        col("commit.committer.name").as("committer")
      )
      .filter(col("committer") === committerName)
    val w = Window.partitionBy("committer").orderBy(col("date"))
    val y = x.select(
      col("oid"),
      lag(col("date"), 1).over(w).as("prev_date"),
      col("date"),
      lead(col("date"), 1).over(w).as("next_date")
    )
    y.show()
    y
  }


  /**
   *
   *                                           Description
   *
   * After looking at the results of assignment 5, you realise that the timestamps are somewhat hard to read
   * and analyze easily. Therefore, you now want to change the format of the list.
   * Instead of the timestamps of previous, current and next commit, output:
   *      - Timestamp of the current commit  (date)
   *      - Difference in days between current commit and the previous commit (days_diff)
   *      - Difference in minutes between the current commit (minutes_diff [rounded down])
   *      - Current commit (Oid)
   *
   * For both fields, i.e. the difference in days and difference in minutes, if the value is null
   * replace it with 0. When there is no previous commit, the value should be 0.
   *
   *
   *                                             Output
   *
   *
   * | $oid                        | date                     | days_diff 	| minutes_diff |
   * |--------------------------	|--------------------------	|-----------	|--------------|
   * | 5ce6929e6480fd0d91d3106a 	| 2019-01-27T07:09:13.000Z 	| 0         	| 3            |
   * | 5ce693156480fd0d5edbd708 	| 2019-03-04T15:21:52.000Z 	| 36        	| 158          |
   * | 5ce691b06480fd0fe0972350 	| 2019-03-06T13:55:25.000Z 	| 2         	| 22           |
   * | ...                      	| ...                      	| ...       	| ...          |
   *
   *                                              Hints
   *
   * Look into Spark sql functions. Days difference is easier to calculate than minutes difference.
   *
   * @param commits Commit DataFrame, see commit.json and data_raw.json for the structure of the file, or run
   *                `println(commits.schema)`.
   * @param committerName Name of the author for which the result must be generated.
   * @return DataFrame with columns as described above.
   */
  def assignment_17(commits: DataFrame, committerName: String): DataFrame = {
    val filteredCommits = commits
      .filter(col("commit.committer.name") === lit(committerName))
      .select(
        col("_id.$oid").as("$oid"),
        col("commit.committer.date").as("date")
      )

    val windowSpec = Window.partitionBy(lit(committerName)).orderBy(col("date").asc)

    val previousDate = lag(col("date"), 1).over(windowSpec)

    filteredCommits
      .withColumn("days_diff",
        coalesce(datediff(col("date"), previousDate), lit(0)))
      .withColumn("minutes_diff",
        coalesce(
          floor((col("date").cast("timestamp").cast("long") -
            previousDate.cast("timestamp").cast("long")) / lit(60)),
          lit(0)))
      .select(
        col("$oid"),
        col("date"),
        col("days_diff"),
        col("minutes_diff")
      )
  }


  /**
   *                                        Description
   *
   * To get a bit of insight in the spark SQL, and its aggregation functions, you will have to
   * implement a function that returns a DataFrame containing columns:
   *        - repository
   *        - month
   *        - commits_per_month(how many commits were done to the given repository in the given month)
   *
   *                                          Output
   *
   *
   * | repository   | month | commits_per_month |
   * |--------------|-------|-------------------|
   * | OffloadBuddy | 1     | 32                |
   * | ...          | ...   | ...               |
   *
   * @param commits Commit DataFrame, see commit.json and data_raw.json for the structure of the file, or run
   *                `println(commits.schema)`.
   * @return DataFrame containing a `repository` column, a `month` column and a `commits_per_month`
   *         representing a count of the total number of commits that that were ever made during that month.
   */
  def assignment_18(commits: DataFrame): DataFrame = {
    val repoExtracted = commits.withColumn("repository", split(col("url"), "/").getItem(5))
    val monthExtracted = repoExtracted.withColumn("month", month(col("commit.committer.date")))
    val grouped = monthExtracted.groupBy(col("repository"), col("month"))
    val counted = grouped.count()
    val renamed = counted.withColumnRenamed("count", "commits_per_month")

    renamed
  }

  /**
   *                                        Description
   *
   * In a repository, the general order of commits can be deduced from  timestamps. However, that does not say
   * anything about branches, as work can be done in multiple branches simultaneously. To trace the actual order
   * of commits, using commits SHAs and Parent SHAs is necessary. We are interested in commits were a parent commit
   * has a different committer than the child commit.
   *
   * Output a list of committer names, and the number of times this happened.
   *
   *                                          Output
   *
   *
   * | parent_name | times_parent |
   * |-------------|--------------|
   * | Emeric      | 2            |
   * | ...         | ...          |
   *
   * @param commits Commit DataFrame, see commit.json and data_raw.json for the structure of the file, or run
   *                `println(commits.schema)`.
   * @return DataFrame containing the parent name and the count for the parent.
   */
  def assignment_19(commits: DataFrame): DataFrame = {
    commits
      .select(col("sha"), col("commit.committer.name").as("committer_name"), explode(col("parents.sha")).as("parent_sha"))
      .join(
        commits.select(col("sha").as("parent_sha"), col("commit.committer.name").as("parent_name")),
        Seq("parent_sha"),
        "inner"
      )
      .filter(col("committer_name") =!= col("parent_name"))
      .groupBy("parent_name")
      .agg(count("*").as("times_parent"))
      .orderBy(col("times_parent").desc)
  }



}

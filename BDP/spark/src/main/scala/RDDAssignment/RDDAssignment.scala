package RDDAssignment

import java.math.BigInteger
import java.security.MessageDigest
import java.sql.Timestamp
import java.util.UUID
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import utils.{Commit, File, Stats}

/**
 * Hint regarding the exercises: it is sometimes specified that the assignment asks about the committer or the
 * commit author. Those are two different entities, as per the Commit.scala file. Inspect it thoroughly and make
 * sure to always refer to the proper entity!
 */
object RDDAssignment {


  /**
   *                                     Description
   *
   * Reductions are often used in data processing in order to gather more useful data out of raw data. In this case
   * we want to know how many commits a given RDD contains.
   *
   *
   *                                        Hints
   *
   * You should be able to complete this assignment with using only one function. If in doubt, read the Spark RDD
   * documentation in detail: https://spark.apache.org/docs/2.4.3/api/scala/index.html#org.apache.spark.rdd.RDD
   *
   * @param commits RDD containing commit data.
   * @return Long indicating the number of commits in the given RDD.
   */
  def assignment_1(commits: RDD[Commit]): Long = {
    return commits.count()
  }

  /**
   *                                     Description
   *
   * We want to know what is the most popular email domain.
   * We require a RDD containing tuples of the used
   *        - email domain
   *        - combined with the number of occurrences.
   *
   *                                       Hints
   *
   * You should use the email of the author
   *
   * @param commits RDD containing commit data.
   * @return RDD containing tuples indicating the email domain (extension) and number of occurrences.
   */
  def assignment_2(commits: RDD[Commit]): RDD[(String, Long)] = {
    commits
      .map(commit => commit.commit.author.email)
      .filter(email => email.contains("@"))
      .distinct()
      .map(email => email.split("@")(1))
      .groupBy(identity)
      .mapValues(_.size)
  }

  /**
   *                                        Description
   *
   * Return a Tuple with :
   *      - filename
   *      - number of changes of the most frequently changed file
   * If there is no filename, use 'unknown'.
   *
   *                                           Hints
   *
   * Files in a directory must have unique names but can have the same name in different directories.
   * During refactoring, files can be moved between directories, resulting in the same file
   * having a different absolute path from a point in time. However, a directory can have more than
   * one file with the same name (but in different directories), so just taking the file name might be too lenient.
   * To simplify things, you may assume that an absolute path is sufficient to identify a file. To further simplify this,
   * use absolute filepath as filename.
   * @param commits RDD containing commit data.
   * @return A tuple containing the filename and number of changes.
   */
  def assignment_3(commits: RDD[Commit]): (String, Long) = {
    commits
      .flatMap(commit => commit.files)
      .map(file => (file.filename.getOrElse("unknown"), file.changes))
      .reduceByKey(_ + _)
      .reduce((a, b) => if (a._2 >= b._2) a else b)
  }

  /**
   *                                        Description
   *
   * Some users on Github might be interested in their ranking in number of comments.
   * Return a RDD containing tuples of:
   *        - the rank (zero indexed) of a commit author
   *        - commit author's name
   *        - the sum of comments counts made by the commit author.
   *
   * As in general with performance rankings, a higher performance means a better
   * ranking (0 = best). In case of a tie, the lexicographical ordering of the usernames should be used to break the
   * tie. For the lexicographical ordering, the size of the letters should not matter.
   *
   * @param commits RDD containing commit data.
   * @return RDD containing the rank number, commit author names and number of comments of author in order.
   */
  def assignment_4(commits: RDD[Commit]): RDD[(Long, String, Long)] = {
    commits
      .map(c => (c.commit.author.name, c.commit.comment_count.toLong))
      .reduceByKey(_ + _)
      .sortBy { case (name, comments) => (-comments, name.toLowerCase, name) }
      .zipWithIndex()
      .map { case ((name, comments), rank) => (rank, name, comments) }
  }

  /**
   *                                        Description
   *
   * We want to know how stable and how widely used some programming languages are.
   * There are many ways to achieve that,but for the purpose of this exercise,
   * the measure we choose is how many additions, deletions and changes occur in each file extension.
   * We will provide a list of file extensions.
   *
   * We want an RDD of tuples containing :
   *        - file extension name
   *        - Stats object. (Stats object is only used for commits and single files only have additions,
   * deletions and changes value)
   *
   * we want you to compose the Stats object for each file with those values.
   *
   *                                           Hints
   *
   * The value of "changes" is the sum of additions and deletions, so it is an equivalent of the
   * "total" value in stats.
   *
   * @param commits RDD containing commit data.
   * @param fileExtensions List of String containing file extensions
   * @return RDD containing file extension and an aggregation of the committers' Stats.
   */
  def assignment_5(commits: RDD[Commit], fileExtensions: List[String]): RDD[(String, Stats)] = {
    commits
      .flatMap(commit => commit.files)
      .filter(file =>
        file.filename.isDefined &&
          fileExtensions.exists(ext => file.filename.get.endsWith(s".$ext"))
      )
      .map(file => {
        val extension = file.filename.get.split("\\.").last
        (extension, Stats(file.changes, file.additions, file.deletions))
      })
      .reduceByKey((s1, s2) =>
        Stats(s1.total + s2.total, s1.additions + s2.additions, s1.deletions + s2.deletions)
      )
  }

  /**
   *                                        Description
   *
   * There are different types of people, those who own repositories, and those who make commits.
   * There are also people who do both.
   *
   * We require as output an RDD containing :
   *      - names of commit authors and repository owners that have both committed to repositories
   *      and own repositories in the given RDD.
   *
   * Note that the repository owner is contained within Github urls.
   *
   * @param commits RDD containing commit data.
   * @return RDD of Strings representing the author names that have both committed to and own repositories.
   */
  def assignment_6(commits: RDD[Commit]): RDD[String] = {
    val commitAuthors = commits
      .map(commit => commit.commit.author.name)
      .distinct()

    val repoOwners = commits
      .map(commit => commit.url.split("/")(4))
      .distinct()

    commitAuthors.intersection(repoOwners)
  }


  /**                                       IMPORTANT NOTE!!!!!!
   *
   * * From here on, expensive functions on RDDs like groupBy are *NOT* allowed.
   * In real life wide dependency functions are performance killers, but luckily there are better performing
   * alternatives! Automatic graders will check computation history of returned RDDs.
   */


  /**
   *                                            Description
   *
   *
   * Sometimes developers make mistakes, sometimes they make many. One way of observing mistakes in commits is by
   * looking at so-called revert commits. We define a 'revert streak' as the number of times `Revert` occurs
   * in a commit.
   *
   * Note that for a commit to be eligible for a 'commit streak', its message must start with `Revert`.
   * As an example: `Revert "Revert ...` would be a revert streak of 2, whilst `Oops, Revert Revert little mistake`
   * is not a 'revert streak'.
   *
   * Return a RDD containing tuples of
   *      - repository name (can be derived from the url)
   *      - average streak length computed over all commits.
   *
   * Note: Commit messages such as 'Reverted...' or 'Revert removal of "..."' also count for the revert streak.
   *
   * @param commits RDD containing commit data.
   * @return RDD of Tuple type containing a repository name and a double representing the average streak length.
   */
  def assignment_7(commits: RDD[Commit]): RDD[(String, Double)] = {
    val eligibleStart = "^(Revert|Reverted)\\b".r
    val revertToken   = "Revert".r

    commits
      .map { c =>
        val url     = Option(c.url).getOrElse("")
        val parts   = url.split("/")
        val repo    = if (parts.length >= 6) parts(5) else ""

        val msg         = Option(c.commit).flatMap(cc => Option(cc.message)).getOrElse("").trim
        val isEligible  = eligibleStart.findFirstIn(msg).isDefined
        val streak      = if (isEligible) revertToken.findAllMatchIn(msg).length else 0

        (repo, (streak, 1))
      }
      .filter { case (repo, _) => repo.nonEmpty }
      .reduceByKey { case ((s1, n1), (s2, n2)) => (s1 + s2, n1 + n2) }
      .mapValues { case (sum, n) => sum.toDouble / n }
  }

  /**
   *
   *                                      Description
   *
   * We want to know the number of commits that are made by unique committers (represented by the field committer
   * in CommitData) in a given RDD. Besides the number of commits, we also want to know how many different
   * repositories the committers committed to. The repository name can be found in url.
   *
   * @param commits RDD containing commit data.
   * @return RDD of tuple containing committer name, list of repositories and
   * total number of commits committed across all repositories.
   */
  def assignment_8(commits: RDD[Commit]): RDD[(String, Iterable[String], Long)] = {
    val repos = commits
      .map(commit => (commit.commit.committer.name, List(commit.url.split("/")(5))))
      .distinct()
      .reduceByKey((repo1, repo2) => repo1 ++ repo2)
    val counts = commits
      .map(commit => (commit.commit.committer.name, 1))
      .reduceByKey(_+_)
    repos
      .join(counts)
      .map { case (name, (repos, tot)) => (name, repos, tot)}
  }


  /**
   *                                       Description
   *
   * Return RDD of tuples containing
   *  - repository names
   *  - list of all commit authors of that repository (commit.author.name), with date of first commit.
   *
   *
   *                                          Hint
   * Use commit.author.date
   *
   * @param commits RDD containing commit data.
   * @return RDD containing the repository names, list of tuples of Timestamps and commit author names
   */
  def assignment_9(commits: RDD[Commit]): RDD[(String, Iterable[(Timestamp, String)])] = {
    commits
      .map(commit => {
        val repoName = commit.url.split("/")(5)
        val authorName = commit.commit.author.name
        val commitDate = commit.commit.author.date
        ((repoName, authorName), commitDate)
      })
      .reduceByKey((date1, date2) => {
        if (date1.before(date2)) date1 else date2
      })
      .map(entry => {
        val repoName = entry._1._1
        val authorName = entry._1._2
        val firstCommitDate = entry._2
        (repoName, (firstCommitDate, authorName))
      })
      .aggregateByKey(List.empty[(Timestamp, String)])(
        (accumulator, value) => value :: accumulator,
        (accumulator1, accumulator2) => accumulator1 ::: accumulator2
      )
      .map(result => (result._1, result._2))
  }


  /**
   *                                             Description
   *
   * We want to know the committers that worked on a certain file to make an overview of every file in a repository.
   *
   * Create a tuple containing
   *  - file name
   *  - set of tuples with name of committers
   *  - Stat object representing the changes made to the file by each committer.
   *
   * @param commits RDD containing commit data.
   * @param repository String name of repository
   * @return RDD containing tuples representing a file name and a list of tuples of committer names and Stats object.
   */
  def assignment_10(commits: RDD[Commit], repository: String): RDD[(String, List[(String, Stats)])] = {
    commits
      .flatMap { commit =>
        commit.files.map { file =>
          (commit.commit.committer.name, commit.url.split("/")(5), file)
        }
      }
      .filter { case (_, repo, _) => repo.equals(repository) }
      .filter { case (_, _, file) => file.filename.isDefined }
      .map { case (committerName, _, file) =>
        (
          (file.filename.get, committerName),
          (file.additions, file.deletions)
        )
      }
      .reduceByKey { case (accumulator, current) =>
        (accumulator._1 + current._1, accumulator._2 + current._2)
      }
      .map { case ((fileName, committerName), (additions, deletions)) =>
        (fileName, (committerName, additions, deletions))
      }
      .aggregateByKey(List[(String, Stats)]())(
        seqOp = { case (list, (committer, adds, dels)) =>
          list :+ (committer, Stats(adds + dels, adds, dels))
        },
        combOp = { case (list1, list2) =>
          list1 ++ list2
        }
      )
  }


  /**
    *
    * Hashing function that computes the md5 hash from a String, which in terms returns a Long to act as a hashing
    * function for repository name and username.
    *
    * @param s String to be hashed, consecutively mapped to a Long.
    * @return Long representing the MSB from the inputted String.
    */
  def md5HashString(s: String): Long = {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(s.getBytes)
    val bigInt = new BigInteger(1, digest)
    val hashedString = bigInt.toString(16)
    UUID.nameUUIDFromBytes(hashedString.getBytes()).getMostSignificantBits
  }

  /**
   * Create a bi-directional graph from committer to repositories. Use md5HashString function above to create unique
   * identifiers for creating a graph.
   *
   * As the real usage Sparks GraphX library is out of the scope of this course, we will not go further into this, but
   * this can be used for algorithms like PageRank, Hubs and Authorities, clique finding, etc.
   *
   * We expect a node for each repository and each committer (based on committer name), an edge from each
   * committer to repositories the committer has committed to.
   *
   * Look at documentation of Graph and Edge before starting this exercise.
   * Your vertices should contain information about the type of node, a 'developer' or a 'repository' node.
   * Edges should only exist between repositories and committers.
   *
   * @param commits RDD containing commit data.
   * @return Graph representation of the commits as described above.
   */
  def assignment_11(commits: RDD[Commit]): Graph[(String, String), String] = {
    val urlParts = commits.map(c => c.url.split("/"))

    val developerNodes = commits
      .map(c => (c.commit.committer.name, "developer"))
      .distinct
      .map { case (name, nodeType) => (md5HashString(name), (name, nodeType)) }

    val repositoryNodes = urlParts
      .map(parts => (parts(5), "repository"))
      .distinct
      .map { case (repoName, nodeType) => (md5HashString(repoName), (repoName, nodeType)) }

    val allVertices = developerNodes.union(repositoryNodes)

    val committerRepoConnections = commits.map { c =>
      val parts = c.url.split("/")
      val committerName = c.commit.committer.name
      val repoFullName = parts.reverse(3) + "/" + parts(5)
      (committerName, repoFullName)
    }.distinct

    val graphEdges = committerRepoConnections.map { case (committer, repo) =>
      Edge(md5HashString(committer), md5HashString(repo), "")
    }

    Graph(allVertices, graphEdges)
  }
}


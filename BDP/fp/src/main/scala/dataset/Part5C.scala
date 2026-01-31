package dataset

import java.text.SimpleDateFormat
import java.util.SimpleTimeZone
import dataset.util.Commit.Commit
import org.json4s.native.Serialization
import org.json4s.{Formats, NoTypeHints}

import scala.io.Source
import scala.math.Ordering.Implicits._

/**
 * PART 5C - Mining Software Repositories
 *
 * Use your knowledge of functional programming to complete the following function.
 * You are recommended to use library functions when possible.
 *
 * The data is provided as a list of `Commit`s. This case class can be found in util/Commit.scala.
 * When asked for dates, use the `commit.commit.committer.date` field.
 *
 * This part is worth 7 points.
 */
object Part5C {
  implicit val formats: AnyRef with Formats = Serialization.formats(NoTypeHints)
  val source: List[Commit] = Source.fromResource("1000_commits.json").getLines().map(Serialization.read[Commit]).toList

  /** Q31 (7p)
   *
   * A day has different parts:
   *   Morning 5:00 am to 11:59 am
   *   Afternoon 12:00 pm to 4:59 pm
   *   Evening 5:00 pm to 8:59 pm
   *   Night 9:00 pm to 4:59 am
   *
   * Which part of the day was the most productive in terms of commits ?
   * Return a tuple with the part of the day and the number of commits
   *
   * Be careful, as the dates are represented in the ZonedDateTime format,
   * which contains information about timezone. You must consider the
   * times in the UTC timezone.
   *
   * Hint: for the time, use `SimpleDateFormat` and `SimpleTimeZone`.
   */
  def mostProductivePart(input: List[Commit]): (String, Int) = {
    val sdf = new SimpleDateFormat("HH")
    sdf.setTimeZone(new SimpleTimeZone(0, "UTC"))

    def partOfDay(date: java.util.Date): String = {
      val hour = sdf.format(date).toInt
      hour match {
        case h if h >= 5  && h < 12 => "morning"
        case h if h >= 12 && h < 17 => "afternoon"
        case h if h >= 17 && h < 21 => "evening"
        case _                      => "night"
      }
    }

    input
      .map(c => partOfDay(c.commit.committer.date))
      .groupBy(identity)
      .mapValues(_.size)
      .maxBy(_._2)
  }
  // END OF PART 5C & END OF THE LAB ^_^
  // Hope you enjoyed it and good luck with the next assignment!
}

package it.maior.jira.docx

import java.util

import it.maior.docx.{DocxFileCreator, ParagraphStyle}
import org.docx4j.wml.P

import scala.collection.GenTraversableOnce
import scala.collection.JavaConverters._

object BacklogItemDocxExporter {
  private val TITLE_TAG_EMBEDDED_IN_DESCRIPTION_REGEX = "^h(\\d*?)\\..*$"
  private val TITLE_TAG_EMBEDDED_IN_DESCRIPTION_PATTERN =
    TITLE_TAG_EMBEDDED_IN_DESCRIPTION_REGEX.r

  private val FILENAME_EMBEDDED_IN_DESCRIPTION_REGEX = "!(.*?)\\|.*?!"
  private val FILENAME_EMBEDDED_IN_DESCRIPTION_PATTERN =
    FILENAME_EMBEDDED_IN_DESCRIPTION_REGEX.r
}

class BacklogItemDocxExporter(val issue: BacklogItem,
                              val docxFileCreator: DocxFileCreator) {
  type Description = String

  def export() = {
    writeIssueInATable()
  }

  private def writeIssueInATable() = {

    val header = issue.getId + " - " + issue.getTitle

    val titleRow: List[util.List[P]] = createTitleParagraphs(
      docxFileCreator.createStyledParagraphOfText(
        ParagraphStyle.HEADING3.getStyle,
        header
      )
    ).map(createCell).getOrElse(List.empty)


    val status: List[util.List[P]] = createParagraphs(
      docxFileCreator.createStyledParagraphOfText(
        ParagraphStyle.NORMAL.getStyle,
        issue.getStatus
      )
    ).map(createCell).getOrElse(List.empty)




    val labelString = issue.getLabels.toArray().mkString(" , ")
    val labels: List[util.List[P]] = createParagraphs(
      docxFileCreator.createStyledParagraphOfText(
        ParagraphStyle.NORMAL.getStyle,
        labelString
      )
    ).map(createCell).getOrElse(List.empty)




    val descriptionRows: List[util.List[P]] = createDescriptionParagraphs(
      issue.getDescription
    ).map(createCell).getOrElse(List.empty)

    docxFileCreator.addTableWith(
      List(titleRow.asJava, status.asJava, labels.asJava, descriptionRows.asJava).asJava
    )
  }

  private def createParagraphs(paragraph: P) = Option(List(paragraph))

  private def createDescriptionParagraphs(
                                           description: Description
                                         ): Option[List[P]] = {
    def extractListOfParagraphs(descriptionLines: Array[String]): List[P] = {
      descriptionLines.flatMap(this.createParagraphsFromUnformattedText).toList
    }

    Option(description)
      .map(desc => desc.split("\\n"))
      .map(extractListOfParagraphs)
  }

  private def getCleanDescription(description: Description) = {
    val removeDuplicatedNewLineBeforeBulletList = (text: Description) =>
      text.replace("\n\n*", "\n*")
    val removeFormattingTagsWithUnderscore = (text: String) =>
      text.replaceAll("(?<!\\\\)[_]", "")
    val removeFormattingTagsWithDash = (text: String) =>
      text.replaceAll("(?<![\\\\\\w])[-]", "")

    def removeEscapeCharacter = (text: String) => text.replace("\\", "")

    val performCleanup = removeDuplicatedNewLineBeforeBulletList andThen
      removeFormattingTagsWithUnderscore andThen
      removeFormattingTagsWithDash andThen
      removeEscapeCharacter

    performCleanup(description)
  }

  private def createParagraphsFromUnformattedText(p: Description) = {
    if (p.startsWith("*") || p.startsWith("#"))
      List(createBulletPoint(getCleanDescription(p)))
    else if (p.startsWith("!")) createImage(p)
    else if (isTagged(p)) createTaggedParagraph(getCleanDescription(p))
    else List(createSimpleParagraph(getCleanDescription(p)))
  }

  private def createTaggedParagraph(paragraph: String): List[P] = {
    def removeMetadataPrefixToDescribeHeading(text: String) =
      text.substring(3).replace('\u00A0', ' ').trim

    def generateHeadingId(headingLevel: String) =
      ParagraphStyle.getHeadingPrefix + adjustHeading(headingLevel)

    paragraph match {
      case BacklogItemDocxExporter.TITLE_TAG_EMBEDDED_IN_DESCRIPTION_PATTERN(
          headingLevel
          ) =>
        List(
          docxFileCreator.createStyledParagraphOfText(
            generateHeadingId(headingLevel),
            removeMetadataPrefixToDescribeHeading(paragraph)
          )
        )
      case _ => List.empty
    }
  }

  private def adjustHeading(headingLevel: String) =
    Math.max(6, Integer.valueOf(headingLevel) + 3)

  def isTagged(paragraph: String) = {
    paragraph match {
      case BacklogItemDocxExporter.TITLE_TAG_EMBEDDED_IN_DESCRIPTION_PATTERN(
          _
          ) =>
        true
      case _ => false
    }
  }

  private def createSimpleParagraph(paragraph: String) =
    docxFileCreator.createParagraphOfText(paragraph)

  private def createImage(filenameParagraph: String): GenTraversableOnce[P] = {
    try {

      BacklogItemDocxExporter.FILENAME_EMBEDDED_IN_DESCRIPTION_PATTERN
        .findAllIn(filenameParagraph)
        .map {
          case BacklogItemDocxExporter.FILENAME_EMBEDDED_IN_DESCRIPTION_PATTERN(
              filename
              ) =>
            val imageBytes = issue.getAttachments.get(filename)
            docxFileCreator.createImage(imageBytes, filename)
        }
    } catch {
      case e: Exception => {
        println(s"Error while creating image for story: ${this.issue.getId}")
        List.empty
      }
    }
  }

  private def createBulletPoint(text: String) = {
    def removeMetadataPrefixToDescribeBulletList(text: String, level: Int) =
      text.substring(level).trim

    val level = text.chars.takeWhile((c: Int) => c == '*' || c == '#').count.toInt

    val actualTextOfBulletListItem =
      removeMetadataPrefixToDescribeBulletList(text, level)
    docxFileCreator.createBulletParagraphOfText(
      1,
      level,
      actualTextOfBulletListItem
    )
  }

  private def createTitleParagraphs(paragraph: P) = Option(List(paragraph))

  private def createCell(paragraphs: List[P]) = List(paragraphs.asJava)
}

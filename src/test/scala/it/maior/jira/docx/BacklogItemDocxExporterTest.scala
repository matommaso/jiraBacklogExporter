package it.maior.jira.docx

import it.maior.docx.{DocxFileCreator, ParagraphStyle}
import it.maior.jira.JiraIssue
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.mockito.integrations.scalatest.MockitoFixture
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

import scala.collection.JavaConverters._

class BacklogItemDocxExporterTest extends FlatSpec with MockitoSugar with ArgumentMatchersSugar  {



  trait Fixture {
    val mockDocxCreator = mock[DocxFileCreator]

    val mockJiraIssue = mock[JiraIssue]

    val itemExporter = new BacklogItemDocxExporter(mockJiraIssue, mockDocxCreator)
  }

  it should "identify text without tags" in new Fixture {
    // When
    val result = itemExporter.isTagged("Test")

    // Then
    result shouldBe false
  }

  it should "remove dash at the start of a line" in new Fixture {
    // Given
    when(mockJiraIssue.getDescription).thenAnswer("-test123")

    // When
    val result = itemExporter.export()

    // Then
    verify(mockDocxCreator).createParagraphOfText("test123")
  }

  it should "not remove dash within a line" in new Fixture {
    // Given
    when(mockJiraIssue.getDescription).thenAnswer("test-123")

    // When
    val result = itemExporter.export()

    // Then
    verify(mockDocxCreator).createParagraphOfText("test-123")
  }

  it should "add Jira ID to header" in new Fixture {
    // Given
    when(mockJiraIssue.getTitle).thenAnswer("Title")
    when(mockJiraIssue.getId).thenAnswer("ID123")
    when(mockJiraIssue.getDescription).thenAnswer("Description")

    // When
    val result = itemExporter.export()

    // Then
    verify(mockDocxCreator).createStyledParagraphOfText(ParagraphStyle.HEADING3.getStyle, "ID123 - Title")
  }
}

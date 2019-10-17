package it.maior.jira.docx

import java.util.Map

trait BacklogItem {
  def getTitle: String
  def getId: String
  def getDescription: String
  def getAttachments: Map[String, Array[Byte]]
  def getAcceptanceCriteria: String
}

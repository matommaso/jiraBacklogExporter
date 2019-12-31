package it.maior.jira.docx

import java.util
import java.util.{Map, Set}

trait BacklogItem {
  def getTitle: String
  def getId: String
  def getDescription: String
  def getAttachments: Map[String, Array[Byte]]
  def getAcceptanceCriteria: String
  def getLabels: Set[String]
  def getStatus: String
}

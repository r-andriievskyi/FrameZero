package com.frame.zero.core.upload

import com.frame.zero.dto.task.CreateTaskRequest
import com.frame.zero.dto.task.TaskPriority
import kotlin.test.Test
import kotlin.test.assertTrue

class TaskMultipartBodyTest {
  @Test
  fun `builds a well-formed multipart body with the file part`() {
    val boundary = "BOUNDARY123"
    val body = buildTaskMultipartBody(
      request = CreateTaskRequest(
        productionId = "p1",
        title = "Storyboard",
        priority = TaskPriority.HIGH,
        participantUserIds = listOf("u1", "u2")
      ),
      fileName = "doc.pdf",
      contentType = "application/pdf",
      fileBytes = "PDFDATA".encodeToByteArray(),
      boundary = boundary
    )
    val text = body.decodeToString()

    assertTrue(text.contains("--$boundary\r\n"), "uses the boundary")
    assertTrue(text.contains("name=\"productionId\"\r\n\r\np1\r\n"))
    assertTrue(text.contains("name=\"title\"\r\n\r\nStoryboard\r\n"))
    assertTrue(text.contains("name=\"priority\"\r\n\r\nHIGH\r\n"))
    assertTrue(text.contains("name=\"participantUserIds\"\r\n\r\nu1\r\n"))
    assertTrue(text.contains("name=\"participantUserIds\"\r\n\r\nu2\r\n"))
    assertTrue(text.contains("name=\"file\"; filename=\"doc.pdf\""))
    assertTrue(text.contains("Content-Type: application/pdf"))
    assertTrue(text.contains("PDFDATA"))
    assertTrue(text.endsWith("--$boundary--\r\n"), "terminates with the closing boundary")
  }

  @Test
  fun `omits optional fields when absent`() {
    val body = buildTaskMultipartBody(
      request = CreateTaskRequest(productionId = "p1", title = "T"),
      fileName = "f.bin",
      contentType = "application/octet-stream",
      fileBytes = byteArrayOf(1),
      boundary = "B"
    )
    val text = body.decodeToString()

    assertTrue(!text.contains("name=\"description\""))
    assertTrue(!text.contains("name=\"dueDate\""))
    assertTrue(!text.contains("name=\"assigneeUserId\""))
    assertTrue(!text.contains("name=\"participantUserIds\""))
  }
}

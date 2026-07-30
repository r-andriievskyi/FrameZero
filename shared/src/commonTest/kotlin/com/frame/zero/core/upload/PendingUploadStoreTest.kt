package com.frame.zero.core.upload

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PendingUploadStoreTest {
  private fun upload(id: String) =
    PendingTaskUpload(
      uploadId = id,
      productionId = "p1",
      title = "T",
      fileName = "f.bin",
      contentType = "application/octet-stream",
      localPath = "/tmp/$id",
      idempotencyKey = "key-$id"
    )

  @Test
  fun `add then recordFailure then remove are reflected`() =
    runTest {
      val store = PendingUploadStore(FakePendingUploadDao())

      store.add(upload("a"))
      assertEquals(PendingUploadStatus.Uploading, store.get("a")?.status)

      // Permanent goes terminal on the first attempt, matching a 4xx that will never succeed.
      store.recordFailure("a", UploadFailureReason.Permanent)
      assertEquals(PendingUploadStatus.Failed, store.uploads.first().single().status)

      store.remove("a")
      assertNull(store.get("a"))
    }

  @Test
  fun `recordFailure with Transient stays Uploading until the attempt budget is spent`() =
    runTest {
      val store = PendingUploadStore(FakePendingUploadDao())
      store.add(upload("a"))

      repeat(MAX_UPLOAD_ATTEMPTS - 1) {
        val updated = store.recordFailure("a", UploadFailureReason.Transient)
        assertEquals(PendingUploadStatus.Uploading, updated?.status)
      }
      val last = store.recordFailure("a", UploadFailureReason.Transient)

      assertEquals(PendingUploadStatus.Failed, last?.status)
      assertEquals(MAX_UPLOAD_ATTEMPTS, last?.attemptCount)
    }

  @Test
  fun `markUploading resets the attempt budget for an explicit retry`() =
    runTest {
      val store = PendingUploadStore(FakePendingUploadDao())
      store.add(upload("a"))
      store.recordFailure("a", UploadFailureReason.Permanent)

      store.markUploading("a")

      val restarted = store.get("a")
      assertEquals(PendingUploadStatus.Uploading, restarted?.status)
      assertEquals(0, restarted?.attemptCount)
      assertNull(restarted?.failureReason)
    }

  @Test
  fun `payload round-trips all fields`() =
    runTest {
      val store = PendingUploadStore(FakePendingUploadDao())
      val original = upload("b").copy(
        description = "notes",
        assigneeUserId = "u9",
        participantUserIds = listOf("u1", "u2")
      )

      store.add(original)

      assertEquals(original, store.get("b"))
    }

  @Test
  fun `payload persisted before participants existed still deserializes`() =
    runTest {
      val dao = FakePendingUploadDao()
      // A queued payload written by an app version that predates participantUserIds.
      dao.upsert(
        com.frame.zero.database.PendingUploadEntity(
          uploadId = "old",
          status = PendingUploadStatus.Uploading.name,
          payload = """
            {"uploadId":"old","productionId":"p1","title":"T","fileName":"f.bin",
             "contentType":"application/octet-stream","localPath":"/tmp/old","idempotencyKey":"key-old"}
          """.trimIndent()
        )
      )
      val store = PendingUploadStore(dao)

      val restored = store.get("old")

      assertEquals(upload("old"), restored)
      assertEquals(emptyList(), restored?.participantUserIds)
    }
}

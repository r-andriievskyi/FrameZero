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
  fun `add then markFailed then remove are reflected`() =
    runTest {
      val store = PendingUploadStore(FakePendingUploadDao())

      store.add(upload("a"))
      assertEquals(PendingUploadStatus.Uploading, store.get("a")?.status)

      store.markFailed("a")
      assertEquals(PendingUploadStatus.Failed, store.uploads.first().single().status)

      store.remove("a")
      assertNull(store.get("a"))
    }

  @Test
  fun `payload round-trips all fields`() =
    runTest {
      val store = PendingUploadStore(FakePendingUploadDao())
      val original = upload("b").copy(description = "notes", assigneeUserId = "u9")

      store.add(original)

      assertEquals(original, store.get("b"))
    }
}

package com.frame.zero.core.upload

import android.app.Application
import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Verifies the Android background-upload scheduler against a real WorkManager test harness:
 * each operation keeps the persisted [PendingUploadStore] record and the unique work request in
 * sync, so an enqueued upload survives process death and a cancel tears both down.
 */
@RunWith(RobolectricTestRunner::class)
// targetSdk (37) ships no Robolectric runtime jar; pin to the highest Robolectric 4.16 supports.
@Config(application = Application::class, sdk = [36])
class WorkManagerTaskUploadSchedulerTest {
  private val context: Context get() = RuntimeEnvironment.getApplication()
  private val workManager get() = WorkManager.getInstance(context)

  @Before
  fun setUp() = WorkManagerTestInitHelper.initializeTestWorkManager(context)

  @Test
  fun `enqueue persists the record and schedules unique work`() =
    runTest {
      val store = store()
      val scheduler = WorkManagerTaskUploadScheduler(context, store)

      scheduler.enqueue(upload("u1"))

      assertEquals("u1", store.get("u1")?.uploadId)
      val infos = workManager.getWorkInfosForUniqueWork("task-upload-u1").get()
      assertEquals(1, infos.size)
      assertEquals(WorkInfo.State.ENQUEUED, infos.single().state)
    }

  @Test
  fun `cancel removes the record and cancels the work`() =
    runTest {
      val store = store()
      val scheduler = WorkManagerTaskUploadScheduler(context, store)
      scheduler.enqueue(upload("u2"))

      scheduler.cancel("u2")

      assertNull(store.get("u2"))
      val state = workManager.getWorkInfosForUniqueWork("task-upload-u2").get().singleOrNull()?.state
      assertTrue(state == null || state == WorkInfo.State.CANCELLED, "unexpected state: $state")
    }

  @Test
  fun `retry re-marks the record uploading and re-schedules`() =
    runTest {
      val store = store()
      val scheduler = WorkManagerTaskUploadScheduler(context, store)
      store.add(upload("u3"))
      store.markFailed("u3")

      scheduler.retry("u3")

      assertEquals(PendingUploadStatus.Uploading, store.get("u3")?.status)
      assertNotNull(workManager.getWorkInfosForUniqueWork("task-upload-u3").get().singleOrNull())
    }

  private fun store() = PendingUploadStore(FakePendingUploadDao())

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
}

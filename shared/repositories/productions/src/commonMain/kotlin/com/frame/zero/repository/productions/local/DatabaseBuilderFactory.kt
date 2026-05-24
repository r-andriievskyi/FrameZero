package com.frame.zero.repository.productions.local

import androidx.room.RoomDatabase

interface DatabaseBuilderFactory {
  fun create(): RoomDatabase.Builder<FrameZeroDatabase>
}

package com.frame.zero.database

import androidx.room.RoomDatabase

interface DatabaseBuilderFactory {
  fun create(): RoomDatabase.Builder<FrameZeroDatabase>
}

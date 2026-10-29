package com.frame.zero.feature.production

import kotlin.test.Test
import kotlin.test.assertEquals

class CrewMemberEntryTest {
  @Test
  fun initialsUseFirstAndLastName() {
    assertEquals("DO", CrewMemberEntry(name = "dara okonkwo", role = "Director").initials)
    assertEquals("MF", CrewMemberEntry(name = "Mia  van Fontaine", role = "Colorist").initials)
  }

  @Test
  fun initialsOfASingleOrBlankName() {
    assertEquals("C", CrewMemberEntry(name = " Cher ", role = "Other").initials)
    assertEquals("?", CrewMemberEntry(name = "  ", role = "Other").initials)
  }
}

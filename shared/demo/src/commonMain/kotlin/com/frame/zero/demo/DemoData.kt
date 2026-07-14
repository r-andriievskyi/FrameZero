package com.frame.zero.demo

import com.frame.zero.domain.User
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionDetail
import com.frame.zero.domain.production.ProductionMember
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.domain.production.ProductionPipelinePhase
import com.frame.zero.domain.production.ViewerCrew
import com.frame.zero.domain.task.TaskAssignee
import com.frame.zero.domain.task.TaskDetail
import com.frame.zero.domain.task.TaskPriority
import com.frame.zero.domain.task.TaskStatus
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/**
 * Everything is generated relative to "today" so the app
 * always looks current, then held in memory by [DemoDataStore]. No wire types, no network.
 */
internal object DemoData {
  const val USER_ID = "demo-user"

  val defaultUser = User(
    id = USER_ID,
    email = "demo@framezero.app",
    firstName = "Alex",
    lastName = "Reyes"
  )

  private val palette = listOf(
    "#E4572E",
    "#17BEBB",
    "#FFC914",
    "#2E86AB",
    "#8367C7",
    "#54C6EB",
    "#F45B69",
    "#3A7D44",
    "#D7263D",
    "#1B998B"
  )

  fun color(seed: Int) = palette[(seed % palette.size + palette.size) % palette.size]

  fun initials(name: String): String =
    name.split(' ')
      .filter { it.isNotBlank() }
      .take(2)
      .joinToString("") { it.first().uppercase() }

  fun pipeline(current: ProductionPhase): List<ProductionPipelinePhase> {
    val stages = listOf(
      ProductionPhase.DEVELOPMENT to "Development",
      ProductionPhase.PRE_PRODUCTION to "Pre-production",
      ProductionPhase.PRODUCTION to "Production",
      ProductionPhase.POST_PRODUCTION to "Post",
      ProductionPhase.RELEASE to "Release"
    )
    return stages.map { (phase, label) ->
      ProductionPipelinePhase(
        phase = phase,
        label = label,
        isCompleted = phase.ordinal < current.ordinal,
        isCurrent = phase == current
      )
    }
  }

  /**
   * A production plus its people, built so the viewer (the demo user) is a crew member on the
   * active shows. [memberSeed] keeps avatar colors and ids stable across productions.
   */
  private class Crew(
    val members: List<ProductionMember>,
    val viewerCrew: ViewerCrew?
  )

  private fun crew(
    productionId: String,
    now: Instant,
    people: List<Triple<String, String, String?>>, // name, role, userId
    viewerRole: String?
  ): Crew {
    val members = people.mapIndexed { index, (name, role, userId) ->
      ProductionMember(
        id = "$productionId-m$index",
        userId = userId,
        name = name,
        role = role,
        initials = initials(name),
        avatarColorHex = color(name.hashCode()),
        addedAt = now - (people.size - index).days,
        reportsToMemberId = if (index == 0) null else "$productionId-m0"
      )
    }
    val viewer = viewerRole?.let { role ->
      ProductionMember(
        id = "$productionId-viewer",
        userId = USER_ID,
        name = "${defaultUser.firstName} ${defaultUser.lastName}",
        role = role,
        initials = initials("${defaultUser.firstName} ${defaultUser.lastName}"),
        avatarColorHex = color(USER_ID.hashCode()),
        addedAt = now - people.size.days,
        reportsToMemberId = members.firstOrNull()?.id
      )
    }
    val viewerCrew = viewer?.let {
      ViewerCrew(
        viewer = it,
        manager = members.firstOrNull(),
        peers = members.drop(1).take(3),
        reports = emptyList()
      )
    }
    return Crew(members = members + listOfNotNull(viewer), viewerCrew = viewerCrew)
  }

  fun seedProductions(
    now: Instant,
    today: LocalDate
  ): List<ProductionDetail> {
    fun daysLeft(wrap: LocalDate) = today.daysUntil(wrap).coerceAtLeast(0)

    val midnight = run {
      val id = "prod-midnight"
      val start = today.minusDays(40)
      val wrap = today.plusDays(18)
      val c = crew(
        id,
        now,
        people = listOf(
          Triple("Priya Anand", "Director", "u-priya"),
          Triple("Marcus Webb", "Producer", "u-marcus"),
          Triple("Lena Ortiz", "1st AD", "u-lena"),
          Triple("Tomas Reid", "Director of Photography", "u-tomas"),
          Triple("Iris Chen", "Gaffer", "u-iris"),
          Triple("Sam Duval", "Editor", "u-sam")
        ),
        viewerRole = "Line Producer"
      )
      ProductionDetail(
        id = id,
        title = "Midnight Harvest",
        genre = Genre.THRILLER,
        logline = "A grieving farmer discovers the fog that feeds his crops also hides the dead.",
        phase = ProductionPhase.PRODUCTION,
        progressPercent = 62,
        daysLeft = daysLeft(wrap),
        startDate = start,
        wrapDate = wrap,
        budgetCents = 4_200_000_00,
        membersCount = c.members.size,
        keyCrew = c.members,
        pipeline = pipeline(ProductionPhase.PRODUCTION).toImmutableList(),
        createdAt = now - 45.days,
        updatedAt = now - 1.days,
        viewerCrew = c.viewerCrew
      )
    }

    val neon = run {
      val id = "prod-neon"
      val wrap = today.plusDays(5)
      val c = crew(
        id,
        now,
        people = listOf(
          Triple("Dara Okonkwo", "Director", "u-dara"),
          Triple("Vince Halloran", "Producer", "u-vince"),
          Triple("Mia Fontaine", "Colorist", "u-mia"),
          Triple("Noah Bright", "Sound Designer", "u-noah")
        ),
        viewerRole = "Post Supervisor"
      )
      ProductionDetail(
        id = id,
        title = "Neon Tide",
        genre = Genre.OTHER,
        logline = "A synthwave music video following a courier through a rain-soaked megacity.",
        phase = ProductionPhase.POST_PRODUCTION,
        progressPercent = 80,
        daysLeft = daysLeft(wrap),
        startDate = today.minusDays(90),
        wrapDate = wrap,
        budgetCents = 350_000_00,
        membersCount = c.members.size,
        keyCrew = c.members,
        pipeline = pipeline(ProductionPhase.POST_PRODUCTION).toImmutableList(),
        createdAt = now - 95.days,
        updatedAt = now - 2.days,
        viewerCrew = c.viewerCrew
      )
    }

    val longWait = run {
      val id = "prod-longwait"
      val start = today.plusDays(10)
      val wrap = today.plusDays(70)
      val c = crew(
        id,
        now,
        people = listOf(
          Triple("Hana Kim", "Director", "u-hana"),
          Triple("Owen Blake", "Producer", "u-owen"),
          Triple("Zoe Marsh", "Casting Director", "u-zoe")
        ),
        viewerRole = "Production Manager"
      )
      ProductionDetail(
        id = id,
        title = "The Long Wait",
        genre = Genre.DRAMA,
        logline = "Two estranged siblings are stranded overnight in a shuttered train station.",
        phase = ProductionPhase.PRE_PRODUCTION,
        progressPercent = 15,
        daysLeft = daysLeft(wrap),
        startDate = start,
        wrapDate = wrap,
        budgetCents = 120_000_00,
        membersCount = c.members.size,
        keyCrew = c.members,
        pipeline = pipeline(ProductionPhase.PRE_PRODUCTION).toImmutableList(),
        createdAt = now - 12.days,
        updatedAt = now - 3.days,
        viewerCrew = c.viewerCrew
      )
    }

    val lanterns = run {
      val id = "prod-lanterns"
      val wrap = today.minusDays(30)
      val c = crew(
        id,
        now,
        people = listOf(
          Triple("Ravi Sethi", "Director", "u-ravi"),
          Triple("Elin Vasquez", "Producer", "u-elin")
        ),
        viewerRole = null
      )
      ProductionDetail(
        id = id,
        title = "Paper Lanterns",
        genre = Genre.DOCUMENTARY,
        logline = "A festival town keeps a vanishing craft alive, one paper lantern at a time.",
        phase = ProductionPhase.ARCHIVED,
        progressPercent = 100,
        daysLeft = 0,
        startDate = today.minusDays(200),
        wrapDate = wrap,
        budgetCents = 80_000_00,
        membersCount = c.members.size,
        keyCrew = c.members,
        pipeline = pipeline(ProductionPhase.RELEASE).toImmutableList(),
        createdAt = now - 210.days,
        updatedAt = now - 30.days,
        viewerCrew = c.viewerCrew
      )
    }

    return listOf(midnight, neon, longWait, lanterns)
  }

  fun seedTasks(
    now: Instant,
    today: LocalDate,
    productions: List<ProductionDetail>
  ): List<TaskDetail> {
    val byId = productions.associateBy { it.id }

    fun assignee(
      userId: String,
      name: String
    ) = TaskAssignee(userId = userId, name = name, avatarColorHex = color(name.hashCode()))

    fun task(
      num: Int,
      productionId: String,
      title: String,
      dueOffsetDays: Int?,
      status: TaskStatus,
      priority: TaskPriority,
      assigneeUserId: String?,
      assigneeName: String?
    ): TaskDetail {
      val prod = byId.getValue(productionId)
      return TaskDetail(
        id = "demo-task-$num",
        productionId = productionId,
        productionTitle = prod.title,
        title = title,
        description = null,
        dueDate = dueOffsetDays?.let { today.plusDays(it) },
        status = status,
        priority = priority,
        assigneeUserId = assigneeUserId,
        assignee = if (assigneeUserId != null && assigneeName != null) {
          assignee(assigneeUserId, assigneeName)
        } else {
          null
        },
        createdAt = now - (num % 20).days
      )
    }

    val me = DemoData.USER_ID
    val myName = "${defaultUser.firstName} ${defaultUser.lastName}"
    return listOf(
      task(1, "prod-midnight", "Lock the barn location permit", -1, TaskStatus.OPEN, TaskPriority.HIGH, me, myName),
      task(
        2,
        "prod-midnight",
        "Confirm fog machine rental",
        0,
        TaskStatus.OPEN,
        TaskPriority.HIGH,
        "u-lena",
        "Lena Ortiz"
      ),
      task(3, "prod-midnight", "Sign stunt coordinator", 2, TaskStatus.OPEN, TaskPriority.MEDIUM, me, myName),
      task(
        4,
        "prod-midnight",
        "Approve day 14 call sheet",
        3,
        TaskStatus.OPEN,
        TaskPriority.MEDIUM,
        "u-marcus",
        "Marcus Webb"
      ),
      task(5, "prod-midnight", "Wrap night-shoot risk assessment", 5, TaskStatus.OPEN, TaskPriority.LOW, me, myName),
      task(
        6,
        "prod-midnight",
        "Return camera package",
        -3,
        TaskStatus.DONE,
        TaskPriority.MEDIUM,
        "u-tomas",
        "Tomas Reid"
      ),
      task(7, "prod-neon", "Final color pass on chorus", 1, TaskStatus.OPEN, TaskPriority.HIGH, me, myName),
      task(8, "prod-neon", "Deliver 5.1 stereo mix", 4, TaskStatus.OPEN, TaskPriority.HIGH, "u-noah", "Noah Bright"),
      task(9, "prod-neon", "Upload masters to label portal", 6, TaskStatus.OPEN, TaskPriority.MEDIUM, me, myName),
      task(10, "prod-neon", "Sign off VFX shot 042", -2, TaskStatus.DONE, TaskPriority.LOW, "u-mia", "Mia Fontaine"),
      task(11, "prod-longwait", "Finish location scout deck", 7, TaskStatus.OPEN, TaskPriority.MEDIUM, me, myName),
      task(
        12,
        "prod-longwait",
        "Schedule chemistry read",
        9,
        TaskStatus.OPEN,
        TaskPriority.MEDIUM,
        "u-zoe",
        "Zoe Marsh"
      ),
      task(13, "prod-longwait", "Draft insurance paperwork", 12, TaskStatus.OPEN, TaskPriority.LOW, me, myName)
    )
  }

  // --- date helpers ---

  private fun LocalDate.plusDays(days: Int): LocalDate = plus(days, DateTimeUnit.DAY)

  private fun LocalDate.minusDays(days: Int): LocalDate = minus(days, DateTimeUnit.DAY)
}

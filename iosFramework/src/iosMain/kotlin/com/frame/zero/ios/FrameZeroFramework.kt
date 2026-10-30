package com.frame.zero.ios

import com.frame.zero.di.IosAppGraph
import com.frame.zero.di.IosGraphHolder

/**
 * Swift's handle on the object graph (`FrameZeroFrameworkKt.iosAppGraph()`). Always the same
 * instance — see [IosGraphHolder] for why there is exactly one graph per process.
 */
fun iosAppGraph(): IosAppGraph = IosGraphHolder.graph

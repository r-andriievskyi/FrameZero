package com.frame.zero.repository.device_token

import com.frame.zero.dto.device.DevicePlatform

/** The platform this build runs on, sent to the server with the device token. */
expect fun devicePlatform(): DevicePlatform

package com.solvyx.ui.components.berto

/**
 * Static Berto poses/angles, driven by a single Enum property in a Rive ViewModel (unlike
 * [BertoRiveAnimation]'s mood+streak hero, this only ever needs one input).
 *
 * [riveValue] is the exact string sent to Rive via `setEnum("pose", riveValue)` — if the Rive
 * file ends up using different casing for its enum values, fix it here, not at the call sites.
 */
enum class BertoPose(val riveValue: String) {
    LEFT("Left"),
    LEFT_SIMPLE("LeftSimple"),
    RIGHT("Right"),
    CENTER_IDLE("CenterIdle"),
    CENTER_IDLE_TO_RIGHT("CenterIdleToRight"),
    CENTER_IDLE_HELLO("CenterIdleHello"),
}

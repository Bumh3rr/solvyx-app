package com.solvyx.ui.screens.profilesetup

/**
 * The 3 steps of the profile setup wizard, which runs once after creating an email account
 * (fresh registration or anonymous→email conversion). Not to be confused with `Routes.ONBOARDING`
 * (the introductory carousel shown before `AuthChoice`) — these are distinct concepts.
 */
enum class ProfileSetupStep { SUBSTANCES, ASSIST, RED_APOYO }

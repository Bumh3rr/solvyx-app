package com.solvyx.ui.screens.profile.legal

import androidx.compose.runtime.Composable

@Composable
fun PrivacyScreen(onBack: () -> Unit) = LegalDocumentScreen(document = PrivacyDocument, onBack = onBack)

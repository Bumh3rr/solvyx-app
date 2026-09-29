package com.solvyx.ui.screens.profile.legal

import androidx.compose.runtime.Composable

@Composable
fun TermsScreen(onBack: () -> Unit) = LegalDocumentScreen(document = TermsDocument, onBack = onBack)

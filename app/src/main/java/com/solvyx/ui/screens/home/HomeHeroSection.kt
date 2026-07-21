package com.solvyx.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.berto.BertoRiveAnimation
import com.solvyx.ui.theme.TealLight

@Composable
fun HomeHeroSection(
    estadoAnimo: String,
    rachaActual: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 10.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_header_hero),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .align(Alignment.TopCenter),
            contentScale = ContentScale.FillWidth,
            colorFilter = ColorFilter.tint(TealLight)
        )
        BertoRiveAnimation(
            estadoAnimo = estadoAnimo,
            rachaActual = rachaActual,
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.CenterEnd)
        )
    }
}

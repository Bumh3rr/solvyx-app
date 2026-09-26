package com.solvyx.ui.screens.directory.hub

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxSegmentedControl
import com.solvyx.ui.components.common.SolvyxTextField
import com.solvyx.ui.screens.directory.DirectoryFilter
import com.solvyx.ui.screens.directory.model.DirectoryCategory
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight

private const val AllLabel = "Todos"

/** Segment 0 is "Todos"; the rest follow [DirectoryCategory] declaration order. */
private val CategoryOptions = listOf(AllLabel) + DirectoryCategory.entries.map { it.pluralLabel }

/** Search + category + "solo sin costo": everything that narrows the list, in one block. */
@Composable
fun DirectoryFilters(
    filter: DirectoryFilter,
    onQueryChange: (String) -> Unit,
    onCategoryChange: (DirectoryCategory?) -> Unit,
    onToggleOnlyFree: () -> Unit,
    resultCount: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SolvyxTextField(
            value = filter.query,
            onValueChange = onQueryChange,
            placeholder = "Busca: adicciones, ansiedad, colonia...",
            leadingIconRes = R.drawable.ic_search,
            imeAction = ImeAction.Search
        )
        SolvyxSegmentedControl(
            options = CategoryOptions,
            selectedIndex = filter.category?.let { it.ordinal + 1 } ?: 0,
            onSelect = { index -> onCategoryChange(DirectoryCategory.entries.getOrNull(index - 1)) }
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            OnlyFreeToggle(checked = filter.onlyFree, onToggle = onToggleOnlyFree)
            Text(
                text = if (resultCount == 1) "1 resultado" else "$resultCount resultados",
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun OnlyFreeToggle(checked: Boolean, onToggle: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val background by animateColorAsState(if (checked) primary else Color.Transparent, label = "freeBg")
    val content = if (checked) MaterialTheme.colorScheme.onPrimary else TealDark
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .border(1.dp, if (checked) primary else TealLight, RoundedCornerShape(50))
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (checked) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = "Solo sin costo",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = content
        )
    }
}

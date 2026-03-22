package perozzi.gib.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.ui.graphics.vector.ImageVector

sealed class GibDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    data object Splash : GibDestination("splash", "Splash", Icons.Outlined.EditNote)
    data object Daily : GibDestination("daily", "Daily Log", Icons.Outlined.EditNote)
    data object History : GibDestination("history", "History", Icons.Outlined.CalendarMonth)
    data object Trends : GibDestination("trends", "Trends", Icons.Outlined.BarChart)
    data object Me : GibDestination("me", "Me", Icons.Outlined.PersonOutline)
}

val bottomDestinations = listOf(
    GibDestination.Daily,
    GibDestination.History,
    GibDestination.Trends,
    GibDestination.Me,
)

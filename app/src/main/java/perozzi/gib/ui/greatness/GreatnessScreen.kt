package perozzi.gib.ui.greatness

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GreatnessScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text("Greatness is boring.", style = MaterialTheme.typography.headlineMedium)
        }
        item {
            Text(
                "Results come from ordinary repeated actions.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        item {
            Text(
                "GIB tracks calories, exercise",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

package perozzi.gib.ui.greatness

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
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
            Spacer(Modifier.size(8.dp))
        }
        item {
            Text(
                "Results come from ordinary repeated actions.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        item {
            Text(
                "In the GIB app, you set a weight goal, you set a deadline, you log calories, you log activity level, and you log weight. That is it. The formula for weight goals is not complex; it's respecting the formula that's difficult. In that sense, this app aims to bring that formula more directly to you to help you better inform your weight journey.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

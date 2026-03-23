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
import androidx.compose.ui.text.style.TextAlign
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
            Text(
                text = "Greatness is boring.",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
        }
        item {
            Spacer(Modifier.size(12.dp))
        }
        item {
            Text(
                "Weight tracking is straightforward. If your calorie input is larger than your calorie output, you'll gain weight, and vice versa.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
        item {
            Spacer(Modifier.size(12.dp))
        }
        item {
            Text(
                "Understanding the formula for our weight goals is not complex. It's living by that formula and keeping track of it throughout our life that usually leads to us neglecting those goals, as life-changing as they would be.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
        item {
            Spacer(Modifier.size(12.dp))
        }
        item {
            Text(
                "We cannot force you to live by the formula. What we can do is contextualize that formula into your life and better inform your weight journey, so that, come time for you to be ready to live by it, you are properly equipped for success.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
        item {
            Spacer(Modifier.size(12.dp))
        }
        item {
            Text(
                "In the GIB app, you set a weight goal, and you set a deadline for when you want to meet that weight goal. You then log daily calories, log daily activity level, and log weight. We let you know about your progress and whether you're on pace based on what you've inputted. That's about it though. We also provide some basic visual representations of your journey.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
        item {
            Spacer(Modifier.size(12.dp))
        }
        item {
            Text(
                "We can't promise you the journey won't be boring. But greatness is boring. Results come from ordinary, repeated actions. And you must continue to strive for greatness in spite of the ordinary.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

# GIB MVP Plan

## Concrete implementation plan

1. Keep Android UI in Compose and isolate domain logic in plain Kotlin packages.
2. Store day records locally with Room and keep settings in DataStore.
3. Make `Daily Log` the fastest screen in the app, including `Copy yesterday`.
4. Treat `History` as a compact mobile table with expandable spreadsheet detail.
5. Use lightweight in-app charts for `Trends` to avoid heavy libraries.
6. Keep auth and sync out of V1, but preserve repository seams so they can be added later.

## Proposed project structure

```text
app/
  src/main/java/perozzi/gib/
    data/local/
    domain/model/
    domain/repository/
    domain/usecase/
    ui/components/
    ui/daily/
    ui/history/
    ui/trends/
    ui/me/
    ui/navigation/
    ui/theme/
```

## Shared domain/data models

- `DayEntry`: one day, one record.
- `MealParts`: four fixed calorie buckets as `List<Int>`.
- `ExerciseLevel`: `None`, `Light`, `Moderate`, `Hard`.
- `UserSettings`: base calorie target, goal, and exercise adjustments.

## Persistence approach

- Room stores day records in `day_entries`.
- DataStore stores user settings.
- Repository interfaces sit above persistence so a future shared KMP module does not depend on Room or Android.

## UI architecture

- Bottom navigation with four tabs: Daily Log, History, Trends, Me.
- ViewModels are Android-specific.
- Calculation logic stays in `domain/usecase/BehaviorCalculator.kt`.

## Navigation structure

- `daily?dateEpochDay={dateEpochDay}`
- `history`
- `trends`
- `me`

## Wireframe-level screen intent

### Daily Log

- Date controls at the top: previous, today, next.
- Two summary cards: actual calories and recommended calories.
- `Copy yesterday` shortcut.
- Four meal sections with part chips, quick-add chips, and manual add.
- Alcohol toggle, exercise chips, and weight field.

### History

- Sticky header with compact column labels.
- One expandable row per day.
- Long-press row to edit that day in Daily Log.
- Expanded state reveals meal totals and part breakdowns.

### Trends

- Actual vs recommended calorie line chart.
- Weight smoothing chart.
- Weekly alcohol bars.
- Exercise frequency pills.

### Me

- Goal selection.
- Base calorie target and exercise adjustment tuning.
- Placeholder sync note.

## Key composables

- `MetricCard`
- `SectionCard`
- `PartChip`
- `SimpleLineChart`
- `SimpleBarChart`

## Core business logic

- Meal total = `sum(parts)`
- Day total = sum of all four meal totals
- Recommended calories = `base target + goal offset + exercise adjustment`
- 7-day average = average of most recent seven logged day totals
- Weekly alcohol tally = count of `drankAlcohol == true` grouped by week
- Weight trend = rolling average of recent weigh-ins

## Sample seed data

- Last seven days preloaded.
- Includes repeated breakfast structure to demonstrate `Copy yesterday`.
- Includes weight, alcohol, and mixed exercise levels.

## Recommended V1 feature cut if needed

- Keep:
  - Daily Log
  - History
  - Copy yesterday
  - Recommended calorie target
  - Weight and alcohol trends
- Cut first if needed:
  - per-meal copy shortcut
  - long-press row menu
  - richer chart styling

## Adding auth/sync later

- Add a remote data source and sync coordinator behind existing repositories.
- Keep local-first writes.
- Add Firebase Auth later only at the app shell and repository orchestration layer.

## Mobile history/table UX note

- Do not recreate a wide spreadsheet.
- Preserve the table mental model through compact fixed metrics in each row.
- Put meal-level detail behind row expansion.
- Keep bulk controls in a sticky header so scanning stays fast.

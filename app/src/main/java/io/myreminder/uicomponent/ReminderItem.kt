package io.myreminder.uicomponent

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.myreminder.common.TransparentIndication
import io.myreminder.data.local.LocalRemindersDataProvider
import io.myreminder.data.model.Reminder
import io.myreminder.extension.convert24HourTo12Hour
import io.myreminder.extension.hourMinuteFormat
import io.myreminder.extension.toast
import io.myreminder.theme.RemindMeTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ReminderItem(
	reminder: Reminder,
	is24Hour: Boolean,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	onClick: () -> Unit,
	onCheckedChange: (Boolean) -> Unit
) {
	
	val context = LocalContext.current
	
	val time = remember(reminder) {
		val (hour, format) = if (is24Hour) hourMinuteFormat(reminder.hour) to ""
		else convert24HourTo12Hour(reminder.hour)
		
		val formattedTime = "$hour:${hourMinuteFormat(reminder.minute)}"
		
		if (format.isEmpty()) formattedTime else "$formattedTime ${format.uppercase()}"
	}

	BoxWithConstraints {
		Card(
			onClick = onClick,
			modifier = modifier
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.padding(horizontal = 8.dp)
			) {
				Row(
					verticalAlignment = Alignment.Bottom
				) {
					Text(
						text = time,
						style = MaterialTheme.typography.titleLarge
					)
					
					Spacer(modifier = Modifier.width(8.dp))
					
					Text(
						text = reminder.name,
						style = MaterialTheme.typography.bodySmall
					)
				}
				
				Spacer(modifier = Modifier.weight(1f))
				
				Switch(
					enabled = enabled,
					checked = reminder.isActive,
					onCheckedChange = onCheckedChange
				)
			}
			
			CompositionLocalProvider(
				LocalOverscrollConfiguration provides null,
				LocalIndication provides TransparentIndication,
			) {
				LazyRow(
					modifier = Modifier
						.padding(horizontal = 8.dp)
				) {
					items(reminder.messages) { message ->
						FilterChip(
							selected = true,
							onClick = {
								message.toast(context)
							},
							label = {
								Text(
									text = message,
									maxLines = 1,
									overflow = TextOverflow.Ellipsis
								)
							},
							modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .widthIn(max = this@BoxWithConstraints.maxWidth / 3)
						)
					}
				}
			}
		}
	}
}

@Preview(widthDp = 400)
@Composable
fun ReminderItem24HourPreview() {
	RemindMeTheme {
		ReminderItem(
			reminder = LocalRemindersDataProvider.allReminders[0],
			is24Hour = true,
			onClick = {},
			onCheckedChange = {}
		)
	}
}

@Preview(widthDp = 400)
@Composable
fun ReminderItem12HourPreview() {
	RemindMeTheme {
		ReminderItem(
			reminder = LocalRemindersDataProvider.allReminders[1],
			is24Hour = false,
			onClick = {},
			onCheckedChange = {}
		)
	}
}

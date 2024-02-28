package io.myreminder.ui.setting

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.myreminder.R
import io.myreminder.data.RemindMeRoute
import io.myreminder.uicomponent.BasicPreference
import io.myreminder.uicomponent.RemindMeTopAppBar
import io.myreminder.uicomponent.SwitchPreference
import io.myreminder.utils.RemindMeContentType

@Composable
fun SettingScreen(
	viewModel: SettingViewModel,
	contentType: RemindMeContentType,
	onBackPressed: () -> Unit = {}
) {
	
	val context = LocalContext.current
	
	LazyColumn(
		modifier = Modifier
			.fillMaxSize()
			.systemBarsPadding()
	) {
		item {
			RemindMeTopAppBar(
				route = RemindMeRoute.SETTING,
				contentType = contentType,
				onNavigationIconClicked = onBackPressed
			)
		}
		
		item {
			SwitchPreference(
				isChecked = viewModel.is24Hour,
				onCheckedChange = viewModel::setUse24Hour,
				title = {
					Text(stringResource(id = R.string.setting_use_24_hour_format))
				},
				summary = {
					Text(stringResource(id = R.string.setting_use_24_hour_format_summary))
				},
				icon = {
					Icon(
						painter = painterResource(id = R.drawable.ic_clock),
						contentDescription = null
					)
				}
			)
			
			Spacer(modifier = Modifier.height(8.dp))
			
			SwitchPreference(
				isChecked = viewModel.autoSave,
				onCheckedChange = viewModel::setUseAutoSave,
				title = {
					Text(stringResource(id = R.string.setting_use_auto_save))
				},
				summary = {
					Text(stringResource(id = R.string.setting_use_auto_save_summary))
				},
				icon = {
					Icon(
						painter = painterResource(id = R.drawable.ic_receive_square),
						contentDescription = null
					)
				}
			)

//			Spacer(modifier = Modifier.height(8.dp))
//
//			BasicPreference(
//				title = {
//					Text(stringResource(id = R.string.source_code))
//				},
//				icon = {
//					Icon(
//						painter = painterResource(id = R.drawable.ic_github_mark),
//						contentDescription = null
//					)
//				},
//				onClick = {
//					context.startActivity(
//						Intent(Intent.ACTION_VIEW).apply {
//							flags = Intent.FLAG_ACTIVITY_NEW_TASK
//							data = Uri.parse("https://github.com/kafri8889/Remind-Me")
//						}
//					)
//				}
//			)
		}
	}
}

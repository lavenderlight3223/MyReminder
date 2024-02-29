package io.myreminder.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.window.layout.DisplayFeature
import io.myreminderapp.R
import io.myreminder.data.RemindMeTopLevelDestination
import io.myreminder.data.RemindMeTopLevelDestinations
import io.myreminder.data.ReminderMessageType
import io.myreminder.data.model.Reminder
import io.myreminder.extension.openSettings
import io.myreminder.extension.toast
import io.myreminder.ui.remind_me.RemindMeUiState
import io.myreminder.ui.reminder_detail.ReminderDetailScreen
import io.myreminder.ui.reminder_detail.ReminderDetailViewModel
import io.myreminder.uicomponent.PaddingRow
import io.myreminder.uicomponent.RemindMeTopAppBar
import io.myreminder.uicomponent.ReminderItem
import io.myreminder.uicomponent.ReminderMessageItem
import io.myreminder.utils.RemindMeContentType
import com.google.accompanist.adaptive.HorizontalTwoPaneStrategy
import com.google.accompanist.adaptive.TwoPane
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import io.myreminder.data.RemindMeRoute

@Composable
fun MainScreen(
	contentType: RemindMeContentType,
	displayFeatures: List<DisplayFeature>,
	remindMeUiState: RemindMeUiState,
	navigateToTopLevelDestination: (RemindMeTopLevelDestination) -> Unit,
	closeReminderScreen: () -> Unit,
	navigateToReminder: (Int, RemindMeContentType) -> Unit,
	updateReminder: (Reminder) -> Unit,
	onDeleteReminder: () -> Unit
) {
	
	/**
	 * When moving from LIST_AND_REMINDER page to LIST page clear the selection and user should see LIST screen.
	 */
	LaunchedEffect(key1 = contentType) {
		if (contentType == RemindMeContentType.SINGLE_PANE && !remindMeUiState.isDetailOnlyOpen) {
			closeReminderScreen()
		}
	}
	
	val reminderLazyListState = rememberLazyListState()
	
	if (contentType == RemindMeContentType.DUAL_PANE) {
		TwoPane(
			displayFeatures = displayFeatures,
			strategy = HorizontalTwoPaneStrategy(
				splitFraction = 0.5f,
				gapWidth = 16.dp
			),
			first = {
				RemindMeReminderList(
					is24Hour = remindMeUiState.userPreferences.is24Hour,
					reminders = remindMeUiState.reminders,
					contentType = contentType,
					reminderLazyListState = reminderLazyListState,
					navigateToTopLevelDestination = navigateToTopLevelDestination,
					navigateToReminder = navigateToReminder,
					updateReminder = updateReminder
				)
			},
			second = {
				RemindMeReminderDetail(
					reminder = remindMeUiState.selectedReminder,
					contentType = contentType,
					onBackPressed = closeReminderScreen,
					onDeleteReminder = onDeleteReminder
				)
			}
		)
	} else {
		Box(modifier = Modifier.fillMaxSize()) {
			RemindMeSinglePaneContent(
				remindMeUiState = remindMeUiState,
				reminderLazyListState = reminderLazyListState,
				navigateToTopLevelDestination = navigateToTopLevelDestination,
				closeDetailScreen = closeReminderScreen,
				navigateToReminder = navigateToReminder,
				updateReminder = updateReminder,
				onDeleteReminder = onDeleteReminder
			)
		}
	}
}

@Composable
fun RemindMeSinglePaneContent(
	remindMeUiState: RemindMeUiState,
	reminderLazyListState: LazyListState,
	modifier: Modifier = Modifier,
	navigateToTopLevelDestination: (RemindMeTopLevelDestination) -> Unit,
	closeDetailScreen: () -> Unit,
	navigateToReminder: (Int, RemindMeContentType) -> Unit,
	updateReminder: (Reminder) -> Unit,
	onDeleteReminder: () -> Unit
) {
	
	if (remindMeUiState.selectedReminder != null && remindMeUiState.isDetailOnlyOpen) {
		BackHandler {
			closeDetailScreen()
		}
		
		RemindMeReminderDetail(
			reminder = remindMeUiState.selectedReminder,
			contentType = RemindMeContentType.SINGLE_PANE,
			onDeleteReminder = onDeleteReminder,
			onBackPressed = closeDetailScreen
		)
	} else {
		RemindMeReminderList(
			is24Hour = remindMeUiState.userPreferences.is24Hour,
			reminders = remindMeUiState.reminders,
			contentType = RemindMeContentType.SINGLE_PANE,
			reminderLazyListState = reminderLazyListState,
			navigateToTopLevelDestination = navigateToTopLevelDestination,
			navigateToReminder = navigateToReminder,
			updateReminder = updateReminder,
			modifier = modifier
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun RemindMeReminderList(
	is24Hour: Boolean,
	reminders: List<Reminder>,
	contentType: RemindMeContentType,
	reminderLazyListState: LazyListState,
	modifier: Modifier = Modifier,
	navigateToTopLevelDestination: (RemindMeTopLevelDestination) -> Unit,
	navigateToReminder: (Int, RemindMeContentType) -> Unit,
	updateReminder: (Reminder) -> Unit
) {

	val context = LocalContext.current
	val lifecycleOwner = LocalLifecycleOwner.current
	
	var permissionRequested by remember { mutableStateOf(false) }
	
	var postNotificationGranted by remember {
		mutableStateOf(
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
			} else true
		)
	}
	
	val postNotificationPermission = rememberPermissionState(
		permission = Manifest.permission.POST_NOTIFICATIONS,
		onPermissionResult = { granted ->
			postNotificationGranted = granted
			
			if (!granted) {
				permissionRequested = true
			}
		}
	)
	
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
		DisposableEffect(lifecycleOwner) {
			// Create an observer that triggers our remembered callbacks
			// for sending analytics events
			val observer = LifecycleEventObserver { _, event ->
				if (event == Lifecycle.Event.ON_START) {
					postNotificationGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
				}
			}
			
			// Add the observer to the lifecycle
			lifecycleOwner.lifecycle.addObserver(observer)
			
			// When the effect leaves the Composition, remove the observer
			onDispose {
				lifecycleOwner.lifecycle.removeObserver(observer)
			}
		}
	}
	
	Box(modifier = modifier) {
		if (contentType == RemindMeContentType.SINGLE_PANE) {
			FloatingActionButton(
				onClick = {
					navigateToTopLevelDestination(RemindMeTopLevelDestinations.newReminder)
				},
				modifier = Modifier
					.padding(32.dp)
					.align(Alignment.BottomEnd)
					.zIndex(2f)
			) {
				Icon(
					imageVector = Icons.Rounded.Add,
					contentDescription = null
				)
			}
		}
		
		if (reminders.isNotEmpty()) {
			LazyColumn(
				state = reminderLazyListState,
				modifier = Modifier
					.fillMaxSize()
			) {
				item {
					RemindMeTopAppBar(
						route = RemindMeRoute.REMINDER_LIST,
						contentType = contentType,
						onNavigationIconClicked = {},
						onSettingClicked = {
							navigateToTopLevelDestination(RemindMeTopLevelDestinations.setting)
						}
					)
				}
				
				if (!postNotificationGranted) {
					item {
						FilledTonalButton(
							onClick = {
								when {
									!permissionRequested && !postNotificationPermission.status.isGranted -> {
										postNotificationPermission.launchPermissionRequest()
									}
//									postNotificationPermission.status.shouldShowRationale -> {}
									else -> {
										context.openSettings()
									}
								}
							},
							modifier = Modifier
								.padding(horizontal = 8.dp)
								.fillMaxWidth()
						) {
							Text(
								text = stringResource(id = R.string.grant_post_notification_permission)
							)
						}
					}
				}
				
				items(
					items = reminders,
					key = { item: Reminder -> item.id }
				) { reminder ->
					ReminderItem(
						reminder = reminder,
						is24Hour = is24Hour,
						enabled = postNotificationGranted,
						onClick = {
							navigateToReminder(reminder.id, contentType) },
						onCheckedChange = { isActive ->
							updateReminder(
								reminder.copy(
									isActive = isActive
								)
							)
						},
						modifier = Modifier
							.padding(8.dp)
							.fillMaxWidth()
					)
				}
			}
		} else {
			Column {
				RemindMeTopAppBar(
					route = RemindMeRoute.REMINDER_LIST,
					contentType = contentType,
					onNavigationIconClicked = {},
					onSettingClicked = {
						navigateToTopLevelDestination(RemindMeTopLevelDestinations.setting)
					}
				)
				
				Box(
					contentAlignment = Alignment.Center,
					modifier = Modifier
						.fillMaxSize()
				) {
					Text(
						text = stringResource(id = R.string.no_reminder),
						style = MaterialTheme.typography.bodyMedium
					)
				}
			}
		}
	}
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun RemindMeReminderDetail(
	reminder: Reminder?,
	contentType: RemindMeContentType,
	modifier: Modifier = Modifier,
	onBackPressed: () -> Unit = {},
	onDeleteReminder: () -> Unit = {}
) {
	
	val context = LocalContext.current
	val focusManager = LocalFocusManager.current
	
	val isKeyboardShowed = WindowInsets.isImeVisible
	
	val viewModel = hiltViewModel<ReminderDetailViewModel>()
	
	val lazyListState = rememberLazyListState()
	
	LaunchedEffect(reminder) {
//		if (reminder == null) {
//			"No reminder selected".toast(context)
//			onBackPressed()
//		} else {
//			viewModel.updateWithReminder(reminder)
//		}
		
		if (reminder != null) {
			viewModel.updateWithReminder(reminder)
		}
	}
	
	LaunchedEffect(isKeyboardShowed) {
		if (!isKeyboardShowed) {
			focusManager.clearFocus()
			
			viewModel.messages.apply {
				removeIf { (text, type) ->
					type == ReminderMessageType.Add || text.isBlank()
				}
			}
		} else {
			lazyListState.animateScrollToItem(lazyListState.layoutInfo.totalItemsCount - 1)
		}
	}
	
	BackHandler(enabled = reminder != null) {
		onBackPressed()
	}
	
	if (reminder == null) {
		Box(
			contentAlignment = Alignment.Center,
			modifier = modifier
                .fillMaxSize()
                .systemBarsPadding()
		) {
			Text(
				text = stringResource(id = R.string.no_reminder_selected),
				style = MaterialTheme.typography.titleMedium
			)
		}
	} else {
		LazyColumn(
			state = lazyListState,
			modifier = modifier
                .fillMaxSize()
                .systemBarsPadding()
		) {
			item {
				RemindMeTopAppBar(
					route = RemindMeRoute.REMINDER_DETAIL,
					contentType = contentType,
					showSaveButton = !viewModel.autoSave,
					onNavigationIconClicked = onBackPressed,
					onTrashClicked = onDeleteReminder,
					onSaveClicked = {
						val saved = viewModel.saveReminder()
						
						if (saved.isSuccess) context.getString(R.string.saved).toast(context)
						else saved.exceptionOrNull()?.let { it.message.toast(context) }
					}
				)
			}
			
			item {
				ReminderDetailScreen(
					viewModel = viewModel
				)
			}
			
			item {
				PaddingRow(
					horizontal = 8.dp,
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(
						text = stringResource(id = R.string.random_message),
						style = MaterialTheme.typography.titleMedium
					)
					
					Spacer(modifier = Modifier.weight(1f))
					
					Switch(
						checked = viewModel.isReminderMessageRandom,
						onCheckedChange = viewModel::updateIsReminderReminderMessageRandom
					)
				}
			}
			
			item {
				Spacer(modifier = Modifier.height(8.dp))
			}
			
			item {
				Text(
					text = stringResource(id = R.string.message),
					style = MaterialTheme.typography.titleMedium,
					modifier = Modifier
						.padding(horizontal = 8.dp)
				)
			}
			
			itemsIndexed(viewModel.messages) { i, (text, type) ->
				val reminderMessageModifier = Modifier
                    .padding(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    )
                    .fillMaxWidth()
                    .animateItemPlacement()
				
				if (type == ReminderMessageType.Fixed) {
					ReminderMessageItem(
						message = text,
						onDelete = {
							viewModel.messages.apply {
								removeAt(i)
							}
						},
						modifier = reminderMessageModifier
					)
				} else {
					ReminderMessageItem(
						onSave = { message ->
							viewModel.messages.apply {
								removeIf { (text, type) ->
									type == ReminderMessageType.Add || text.isBlank()
								}
								
								add(message to ReminderMessageType.Fixed)
							}
							
							viewModel.saveReminder()
						},
						modifier = reminderMessageModifier
							.imePadding()
					)
				}
			}
			
			if (viewModel.messages.isEmpty()) {
				item { 
					Box(
						modifier = Modifier
                            .padding(8.dp)
                            .border(
                                width = 1.dp,
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.outline
                            )
					) {
						Text(
							text = stringResource(id = R.string.no_message),
							style = MaterialTheme.typography.bodyMedium,
							textAlign = TextAlign.Center,
							modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth()
						)
					}
				}
			}
			
			item {
				FilledTonalButton(
					shape = MaterialTheme.shapes.medium,
					onClick = {
						viewModel.messages.apply {
							add("" to ReminderMessageType.Add)
						}
					},
					modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
				) {
					Icon(
						imageVector = Icons.Rounded.Add,
						contentDescription = null
					)
				}
			}
			
			item {
				Spacer(modifier = Modifier.height(24.dp))
			}
		}
	}
}

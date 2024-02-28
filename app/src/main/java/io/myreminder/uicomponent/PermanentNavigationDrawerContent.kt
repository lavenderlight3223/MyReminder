package io.myreminder.uicomponent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import io.myreminder.R
import io.myreminder.data.LayoutType
import io.myreminder.data.RemindMeTopLevelDestination
import io.myreminder.data.TOP_LEVEL_DESTINATIONS
import io.myreminder.utils.RemindMeNavigationContentPosition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermanentNavigationDrawerContent(
	selectedDestination: String,
	navigationContentPosition: RemindMeNavigationContentPosition,
	navigateToTopLevelDestination: (RemindMeTopLevelDestination) -> Unit,
	onFABClicked: () -> Unit = {},
) {
	PermanentDrawerSheet(
		modifier = Modifier
			.sizeIn(minWidth = 200.dp, maxWidth = 300.dp)
	) {
		// TODO remove custom nav drawer content positioning when NavDrawer component supports it. ticket : b/232495216
		Layout(
			modifier = Modifier
                .background(MaterialTheme.colorScheme.inverseOnSurface)
                .padding(16.dp),
			content = {
				Column(
					modifier = Modifier.layoutId(LayoutType.HEADER),
					horizontalAlignment = Alignment.Start,
					verticalArrangement = Arrangement.spacedBy(4.dp)
				) {
					Text(
						modifier = Modifier
							.padding(16.dp),
						text = stringResource(id = R.string.app_name).uppercase(),
						style = MaterialTheme.typography.titleMedium,
						color = MaterialTheme.colorScheme.primary
					)
					ExtendedFloatingActionButton(
						onClick = onFABClicked,
						modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 40.dp),
						containerColor = MaterialTheme.colorScheme.tertiaryContainer,
						contentColor = MaterialTheme.colorScheme.onTertiaryContainer
					) {
						Icon(
							imageVector = Icons.Default.Edit,
							contentDescription = null,
							modifier = Modifier.size(18.dp)
						)
						Text(
							text = stringResource(id = R.string.new_reminder),
							textAlign = TextAlign.Center,
							modifier = Modifier
								.weight(1f)
						)
					}
				}
				
				Column(
					modifier = Modifier
                        .layoutId(LayoutType.CONTENT)
                        .verticalScroll(rememberScrollState()),
					horizontalAlignment = Alignment.CenterHorizontally,
				) {
					TOP_LEVEL_DESTINATIONS.forEach { replyDestination ->
						NavigationDrawerItem(
							selected = selectedDestination == replyDestination.route,
							label = {
								Text(
									text = stringResource(id = replyDestination.iconTextId),
									modifier = Modifier.padding(horizontal = 16.dp)
								)
							},
							icon = {
								Icon(
									painter = painterResource(id = replyDestination.selectedIcon),
									contentDescription = stringResource(
										id = replyDestination.iconTextId
									)
								)
							},
							colors = NavigationDrawerItemDefaults.colors(
								unselectedContainerColor = Color.Transparent
							),
							onClick = { navigateToTopLevelDestination(replyDestination) }
						)
					}
				}
			},
			measurePolicy = { measurables, constraints ->
				lateinit var headerMeasurable: Measurable
				lateinit var contentMeasurable: Measurable
				measurables.forEach {
					when (it.layoutId) {
						LayoutType.HEADER -> headerMeasurable = it
						LayoutType.CONTENT -> contentMeasurable = it
						else -> error("Unknown layoutId encountered!")
					}
				}
				
				val headerPlaceable = headerMeasurable.measure(constraints)
				val contentPlaceable = contentMeasurable.measure(
					constraints.offset(vertical = -headerPlaceable.height)
				)
				layout(constraints.maxWidth, constraints.maxHeight) {
					// Place the header, this goes at the top
					headerPlaceable.placeRelative(0, 0)
					
					// Determine how much space is not taken up by the content
					val nonContentVerticalSpace = constraints.maxHeight - contentPlaceable.height
					
					val contentPlaceableY = when (navigationContentPosition) {
						// Figure out the place we want to place the content, with respect to the
						// parent (ignoring the header for now)
						RemindMeNavigationContentPosition.TOP -> 0
						RemindMeNavigationContentPosition.CENTER -> nonContentVerticalSpace / 2
					}
						// And finally, make sure we don't overlap with the header.
						.coerceAtLeast(headerPlaceable.height)
					
					contentPlaceable.placeRelative(0, contentPlaceableY)
				}
			}
		)
	}
}

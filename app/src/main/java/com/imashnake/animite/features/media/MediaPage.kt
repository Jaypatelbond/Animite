package com.imashnake.animite.features.media

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.TextView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.imashnake.animite.R
import com.imashnake.animite.api.anilist.sanitize.media.Media
import com.imashnake.animite.core.extensions.bannerParallax
import com.imashnake.animite.core.extensions.landscapeCutoutPadding
import com.imashnake.animite.core.ui.LocalPaddings
import com.imashnake.animite.core.ui.NestedScrollableContent
import com.imashnake.animite.core.ui.TranslucentStatusBarLayout
import com.imashnake.animite.dev.internal.Constants
import com.imashnake.animite.features.ui.MediaSmall
import com.imashnake.animite.features.ui.MediaSmallRow
import com.ramcosta.composedestinations.annotation.Destination

@Destination(navArgsDelegate = MediaPageArgs::class)
@Composable
fun MediaPage(
    viewModel: MediaPageViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val bannerHeight = dimensionResource(R.dimen.banner_height)

    val media = viewModel.uiState

    MaterialTheme(colorScheme = rememberColorSchemeFor(color = media.color)) {
        // TODO: [Add shimmer](https://google.github.io/accompanist/placeholder/).
        TranslucentStatusBarLayout(
            scrollState = scrollState,
            distanceUntilAnimated = bannerHeight,
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                MediaBanner(
                    imageUrl = media.bannerImage,
                    tintColor = Color(media.color ?: 0).copy(alpha = 0.25f),
                    modifier = Modifier
                        .height(bannerHeight)
                        .fillMaxWidth()
                        .bannerParallax(scrollState)
                )

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(top = bannerHeight)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(bottom = LocalPaddings.current.large)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(LocalPaddings.current.large)
                ) {
                    MediaDetails(
                        title = media.title.orEmpty(),
                        description = media.description.orEmpty(),
                        // TODO Can we do something about this Modifier chain?
                        modifier = Modifier
                            .padding(
                                start = LocalPaddings.current.large
                                        + dimensionResource(R.dimen.media_card_width)
                                        + LocalPaddings.current.large,
                                top = LocalPaddings.current.medium,
                                end = LocalPaddings.current.large
                            )
                            .landscapeCutoutPadding()
                            .height(
                                WindowInsets.statusBars
                                    .asPaddingValues()
                                    .calculateTopPadding()
                                        + dimensionResource(R.dimen.media_card_top_padding)
                                        + dimensionResource(R.dimen.media_card_height)
                                        - dimensionResource(R.dimen.banner_height)
                                        - LocalPaddings.current.medium
                            )
                            .fillMaxSize()
                    )

                    if (!media.ranks.isNullOrEmpty()) {
                        MediaRankings(
                            rankings = media.ranks,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = LocalPaddings.current.large)
                                .landscapeCutoutPadding()
                        )
                    }

                    if (!media.genres.isNullOrEmpty()) {
                        MediaGenres(
                            genres = media.genres,
                            contentPadding = PaddingValues(
                                start = LocalPaddings.current.large + if (
                                    LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
                                ) {
                                    WindowInsets.displayCutout.asPaddingValues()
                                        .calculateLeftPadding(LayoutDirection.Ltr)
                                } else 0.dp,
                                end = LocalPaddings.current.large
                            ),
                            color = Color(media.color ?: (0xFF152232).toInt()),
                        )
                    }

                    if (!media.characters.isNullOrEmpty()) {
                        MediaCharacters(
                            characters = media.characters,
                            contentPadding = PaddingValues(horizontal = LocalPaddings.current.large)
                        )
                    }

                    if (media.trailer != null) {
                        MediaTrailer(
                            trailer = media.trailer,
                            modifier = Modifier
                                .padding(horizontal = LocalPaddings.current.large)
                                .landscapeCutoutPadding()
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(
                            top = dimensionResource(R.dimen.media_card_top_padding),
                            start = LocalPaddings.current.large,
                            end = LocalPaddings.current.large
                        )
                        .landscapeCutoutPadding()
                ) {
                    MediaSmall(
                        image = media.coverImage,
                        label = null,
                        onClick = {},
                        modifier = Modifier.width(dimensionResource(R.dimen.media_card_width))
                    )
                }
            }
        }
    }
}

@Composable
fun MediaBanner(
    imageUrl: String?,
    tintColor: Color,
    modifier: Modifier = Modifier
) {
    if (!imageUrl.isNullOrEmpty()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(Constants.CROSSFADE_DURATION)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier,
            alignment = Alignment.Center,
            colorFilter = ColorFilter.tint(
                color = tintColor,
                blendMode = BlendMode.SrcAtop
            )
        )
    } else {
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier,
            alignment = Alignment.TopCenter,
            colorFilter = ColorFilter.tint(
                color = tintColor,
                blendMode = BlendMode.SrcAtop
            )
        )
    }
}

@Composable
fun MediaDetails(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onBackground.copy(
        alpha = ContentAlpha.medium
    ).toArgb()

    val html = remember(description) {
        HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    Column(modifier) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )

        NestedScrollableContent { contentModifier ->
            // TODO: Get rid of this once Compose supports HTML/Markdown
            //  https://issuetracker.google.com/issues/139326648
            AndroidView(
                factory = {
                    TextView(it).apply {
                        movementMethod = LinkMovementMethod.getInstance()
                        setTextColor(textColor)
                        textSize = 14f
                        // This is needed since `FontFamily` can't be used with `AndroidView`.
                        typeface = ResourcesCompat.getFont(it, com.imashnake.animite.core.R.font.manrope_medium)
                    }
                },
                update = { it.text = html },
                modifier = contentModifier
            )
        }
    }
}

@Composable
fun MediaRankings(
    rankings: List<Media.Ranking>,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = modifier
    ) {
        rankings.forEach { ranking ->
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = ranking.type.name,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.labelSmall
                )

                Text(
                    text = when (ranking.type) {
                        Media.Ranking.Type.SCORE -> "${ranking.rank}%"
                        Media.Ranking.Type.RATED, Media.Ranking.Type.POPULAR -> "#${ranking.rank}"
                    },
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.displaySmall
                )
            }
        }
    }
}

@Composable
fun MediaGenres(
    genres: List<String>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    color: Color = MaterialTheme.colorScheme.primaryContainer
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(
            LocalPaddings.current.medium
        ),
        contentPadding = contentPadding,
        modifier = modifier
    ) {
        items(genres) { genre ->
            SuggestionChip(
                label = {
                    Text(
                        text = genre.lowercase(),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(
                            vertical = LocalPaddings.current.small
                        )
                    )
                },
                onClick = { },
                shape = CircleShape,
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = color.copy(alpha = 0.25f)
                ),
                border = BorderStroke(width = 0.dp, color = Color.Transparent)
            )
        }
    }
}

@Composable
fun MediaCharacters(
    characters: List<Media.Character>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    Column(modifier) {
        Text(
            text = stringResource(R.string.characters),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(contentPadding)
                .landscapeCutoutPadding()
        )

        Spacer(Modifier.size(LocalPaddings.current.medium))

        MediaSmallRow(
            mediaList = characters
        ) { character ->
            MediaSmall(
                image = character.image,
                label = character.name,
                onClick = { Log.d("CharacterId", "${character.id}") },
                modifier = Modifier.width(dimensionResource(R.dimen.character_card_width))
            )
        }
    }
}

@Composable
fun MediaTrailer(
    trailer: Media.Trailer,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            text = stringResource(R.string.trailer),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.size(LocalPaddings.current.medium))

        val context = LocalContext.current
        Box(
            modifier = Modifier
                .wrapContentSize()
                .clip(RoundedCornerShape(dimensionResource(R.dimen.trailer_corner_radius)))
                .background(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                .clickable {
                    val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(trailer.url))
                    context.startActivity(appIntent)
                }
        ) {
            var bestThumbnail by remember { mutableStateOf(trailer.thumbnail.maxResDefault) }

            val model = remember(bestThumbnail) {
                ImageRequest.Builder(context)
                    .data(bestThumbnail)
                    .apply {
                        listener(
                            onError = { _, _ ->
                                bestThumbnail = if (bestThumbnail?.contains("maxresdefault") == true) {
                                    trailer.thumbnail.sdDefault
                                } else trailer.thumbnail.defaultThumbnail
                                Log.d("bestThumbnail", bestThumbnail.toString())
                            }
                        )
                    }
                    .crossfade(Constants.CROSSFADE_DURATION)
                    .build()
            }

            AsyncImage(
                model = model,
                contentDescription = stringResource(R.string.trailer),
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.778f) // 16 : 9
                    .clip(
                        RoundedCornerShape(dimensionResource(R.dimen.trailer_corner_radius))
                    ),
                alignment = Alignment.Center
            )

            Image(
                painter = painterResource(R.drawable.youtube),
                contentDescription = null,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

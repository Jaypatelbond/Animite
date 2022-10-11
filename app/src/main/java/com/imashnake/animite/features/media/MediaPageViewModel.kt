package com.imashnake.animite.features.media

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imashnake.animite.data.repos.MediaRepository
import com.imashnake.animite.type.MediaRankType
import com.imashnake.animite.type.MediaType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class MediaPageViewModel @Inject constructor(
    private val mediaRepository: MediaRepository
) : ViewModel() {
    var uiState by mutableStateOf(MediaUiState())
        private set

    private var fetchJob: Job? = null
    fun populateMediaPage(id: Int?, mediaType: MediaType) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                val media = mediaRepository.fetchMedia(id, mediaType)

                val stats = mutableListOf<Stat>()
                stats.add(Stat(StatLabel.SCORE, media?.averageScore))
                media?.rankings?.forEach {
                    if (it?.allTime == true) {
                        if (it.type == MediaRankType.RATED) stats.add(Stat(StatLabel.RATING, it.rank))
                        if (it.type == MediaRankType.POPULAR) stats.add(Stat(StatLabel.POPULARITY, it.rank))
                    }
                }

                uiState = with(uiState) {
                    copy(
                        bannerImage = media?.bannerImage,
                        coverImage = media?.coverImage?.extraLarge,
                        color = media?.coverImage?.color,
                        title = media?.title?.romaji ?:
                                media?.title?.english ?:
                                media?.title?.native,
                        description = media?.description,
                        stats = stats,
                        genres = media?.genres,
                        characters = media?.characters?.nodes?.map {
                            Pair(it?.image?.large, it?.name?.full)
                        },
                        trailer = Pair(
                            first = when (media?.trailer?.site) {
                                "youtube" -> {
                                    "https://www.youtube.com/watch?v=${media.trailer.id}"
                                }
                                "dailymotion" -> {
                                    "https://www.dailymotion.com/video/${media.trailer.id}"
                                }
                                else -> {
                                    null
                                }
                            },
                            second = when (media?.trailer?.site) {
                                // TODO: Does a high resolution image always exist?
                                "youtube" -> {
                                    "https://img.youtube.com/vi/${media.trailer.id}/maxresdefault.jpg"
                                }
                                // TODO: Change the icon and handle this properly.
                                "dailymotion" -> {
                                    media.trailer.thumbnail
                                }
                                else -> {
                                    null
                                }
                            }
                        )
                    )
                }
            } catch(ioe: IOException) {
                TODO()
            }
        }
    }
}

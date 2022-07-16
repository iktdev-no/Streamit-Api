package no.iktdev.streamit.api.helper

import no.iktdev.streamit.api.classes.Episode
import no.iktdev.streamit.api.classes.Season
import no.iktdev.streamit.api.classes.Serie
import no.iktdev.streamit.api.classes.SerieFlat

class serieHelper
{
    class map {
        fun mapFromFlat(item: SerieFlat): Serie
        {
            val mappedList: Map<String, List<SerieFlat>> = this.listOfList(listOf(item))
            return mapToSerie(mappedList).first()
        }

        fun mapFromFlatList(items: List<SerieFlat>): List<Serie>
        {
            val mappedList: Map<String, List<SerieFlat>> = this.listOfList(items)
            return mapToSerie(mappedList)
        }

        private fun mapToSerie(items: Map<String, List<SerieFlat>>): List<Serie>
        {
            val serieList: MutableList<Serie> = mutableListOf()
            items.forEach()
            {
                serieList.add(mergeSerie(it.value))
            }
            return serieList
        }

        fun mergeSerie(list: List<SerieFlat>): Serie
        {
            val serie: Serie = Serie.fromFlat(list.first()) // This should assign the shared catalog values, and apply the first season + episode
            val seasonMap: MutableMap<Int, MutableList<Episode>> = mutableMapOf()
            list.forEach()
            {
                if (seasonMap.containsKey(it.season))
                {
                    seasonMap[it.season]?.add(Episode.fromFlat(it))
                }
                else
                {
                    seasonMap[it.season] = mutableListOf(Episode.fromFlat(it))
                }
            }
            serie.seasons = listOfMap(seasonMap)
            serie.seasons = serie.seasons.sortedBy { it.season }
            return serie
        }

        private fun listOfMap(items: MutableMap<Int, MutableList<Episode>>): List<Season>
        {
            return items.toList().map {
                Season(it.first, it.second)
            }
        }


        private fun listOfList(items: List<SerieFlat>): Map<String, List<SerieFlat>>
        {
            val mappedCollection: MutableMap<String, MutableList<SerieFlat>> = mutableMapOf()
            items.forEach {
                if (mappedCollection.containsKey(it.collection))
                {
                    mappedCollection[it.collection]?.add(it)
                }
                else
                {
                    mappedCollection[it.collection] = mutableListOf(it)
                }
            }
            return mappedCollection
        }
    }

    class flatten {

        fun list(serie: Serie): List<SerieFlat>
        {
            val out: MutableList<SerieFlat> = mutableListOf()
            serie.seasons.forEach {
                season -> season.episodes.forEach {
                    episode -> out.add(
                        SerieFlat(
                            id = -1,
                            title = serie.title,
                            cover = serie.cover,
                            type = serie.type,
                            collection = serie.collection ?: serie.title,
                            season = season.season,
                            episode = episode.episode,
                            episodeTitle = episode.title,
                            video = episode.video,
                            genres = serie.genres,
                            recent = serie.recent
                        )
                    )
                }
            }
            return out
        }
    }

}
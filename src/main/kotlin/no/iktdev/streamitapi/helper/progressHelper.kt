package no.iktdev.streamitapi.helper

import no.iktdev.streamitapi.classes.*

class progressHelper
{
    class map
    {
        fun fromMixedProgressTable(items: List<ProgressTable>): List<BaseProgress>
        {
            val mixed: MutableList<BaseProgress> = mutableListOf()

            /**
             * Filters on type and appends Progress Movie to mixed list
             */
            items.filter { it.type.toLowerCase() == "movie" }.forEach() {
                val movieProgress = ProgressMovie.fromProgressTable(it)
                mixed.add(movieProgress)
            }

            val ofSeries = items.filter { it.type.toLowerCase() == "serie" }
            mapCollection(ofSeries).map {
                mixed.add(mergeSerieTables(it.value))
            }

            return mixed
        }

        fun mapCollection(items: List<ProgressTable>): Map<String, List<ProgressTable>>
        {
            val serieMap: MutableMap<String, MutableList<ProgressTable>> = mutableMapOf()
            items.forEach()
            {
                if (it.collection == null)
                    return@forEach
                if (serieMap.containsKey(it.collection))
                {
                    serieMap[it.collection]?.add(it)
                }
                else
                {
                    serieMap[it.collection] = mutableListOf(it)
                }
            }
            return serieMap
        }

        fun mergeSerieTables(items: List<ProgressTable>): ProgressSerie
        {
            var serie: ProgressSerie = ProgressSerie.fromProgressTable(items.first())
            var seasonMap: MutableMap<Int, MutableList<ProgressEpisode>> = mutableMapOf()
            items.forEach()
            {
                if (it.season == null)
                    return@forEach
                if (seasonMap.containsKey(it.season))
                {
                    ProgressEpisode.fromFlat(it)?.let { it1 -> seasonMap[it.season]?.add(it1) }
                }
                else
                {
                    seasonMap[it.season] = ProgressEpisode.fromFlat(it)?.let { it1 -> mutableListOf(it1) }!!
                }
            }
            serie.seasons = mapToSeasons(seasonMap)
            return serie
        }

        private fun mapToSeasons(items: MutableMap<Int, MutableList<ProgressEpisode>>): List<ProgressSeason>
        {
            return items.toList().map {
                ProgressSeason(it.first, it.second)
            }
        }
    }

    class flatten
    {
        /**
         * ProgressTable flattened from ProgressSerie will always have -1 as value.
         * This is to prevent it from updating any row with id 0, in case it is present
         */
        fun list(serie: ProgressSerie): List<ProgressTable>
        {
            val out: MutableList<ProgressTable> = mutableListOf()
            serie.seasons.forEach() { ses ->
                ses.episodes.forEach() { epi ->
                    out.add(
                        ProgressTable(
                            id = -1,
                            guid = serie.guid,
                            type = serie.type,
                            title = serie.title,
                            collection = serie.collection,
                            video = epi.video,
                            season = ses.season,
                            episode = epi.episode,
                            progress = epi.progress,
                            duration = epi.duration,
                            played = epi.played
                    ))
                }
            }
            return out
        }
    }

}
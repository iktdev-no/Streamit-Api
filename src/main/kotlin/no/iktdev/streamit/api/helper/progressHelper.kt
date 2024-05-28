package no.iktdev.streamit.api.helper

import no.iktdev.streamit.api.classes.*

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
            items.filter { it.type.lowercase() == "movie" }.forEach {
                val movieProgress = ProgressMovie.fromProgressTable(it)
                mixed.add(movieProgress)
            }

            val ofSeries = items.filter { it.type.lowercase() == "serie" }
            mapCollection(ofSeries).map {
                mixed.add(mergeSerieTables(it.value))
            }

            return mixed
        }

        fun fromSerieProgressTable(items: List<ProgressTable>): List<ProgressSerie> {
            val mapped: MutableList<ProgressSerie> = mutableListOf()

            val ofSeries = items.filter { it.type.lowercase() == "serie" }
            mapCollection(ofSeries).map {
                mapped.add(mergeSerieTables(it.value))
            }

            return mapped
        }


        private fun mapCollection(items: List<ProgressTable>): Map<String, List<ProgressTable>>
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
            val serie: ProgressSerie = ProgressSerie.fromProgressTable(items.first()).apply {
                this.episodes = items.mapNotNull { ProgressEpisode.fromFlat(it) }
            }

            return serie
        }

    }


}
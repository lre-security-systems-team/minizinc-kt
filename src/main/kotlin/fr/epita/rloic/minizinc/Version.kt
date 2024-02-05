package fr.epita.rloic.fr.epita.rloic.minizinc

import kotlinx.serialization.Serializable

@Serializable
data class Version(val major: Int, val minor: Int, val patch: Int) : Comparable<Version> {
    companion object {
        private val comparator = Comparator.comparingInt(Version::major)
            .thenComparing(Version::minor)
            .thenComparing(Version::patch)
    }

    override fun compareTo(other: Version): Int {
        return comparator.compare(this, other)
    }

    override fun toString() = "$major.$minor.$patch"
}
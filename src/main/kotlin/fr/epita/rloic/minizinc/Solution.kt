package fr.epita.rloic.fr.epita.rloic.minizinc

import fr.epita.rloic.fr.epita.rloic.minizinc.dzn.DznData

sealed class Solution {
    data class Single(val data: DznData) : Solution()
    data class MultipleSolutions(val solutions: MutableList<Single> = mutableListOf()) : Solution() {
        operator fun plusAssign(other: Solution) {
            when (other) {
                is Single -> solutions += other
                is MultipleSolutions -> solutions += other.solutions
            }
        }
    }
}
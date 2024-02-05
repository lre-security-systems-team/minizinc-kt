package fr.epita.rloic

import fr.epita.rloic.fr.epita.rloic.minizinc.*
import fr.epita.rloic.fr.epita.rloic.minizinc.dzn.DznValue
import kotlin.io.path.Path


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {

    val model = Model(Path("test.mzn"))
    model["x"] = DznValue.Num(10)
    val solver = Solver.lookup("sat")

    val instance = Instance(solver, model)
    val solution = instance.solve().solution

    if (solution != null && solution is Solution.Single) {
        println(solution.data["x"])
    }

}



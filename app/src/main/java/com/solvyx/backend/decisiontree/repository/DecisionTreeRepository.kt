package com.solvyx.backend.decisiontree.repository

import com.solvyx.backend.decisiontree.model.DecisionTree
import com.solvyx.backend.decisiontree.trees.alcoholCravingTree
import com.solvyx.backend.decisiontree.trees.alcoholInfoTree
import com.solvyx.backend.decisiontree.trees.cigarroCravingTree
import com.solvyx.backend.decisiontree.trees.cigarroInfoTree
import com.solvyx.backend.decisiontree.trees.cristalCravingTree
import com.solvyx.backend.decisiontree.trees.cristalInfoTree
import com.solvyx.backend.decisiontree.trees.vapeCravingTree
import com.solvyx.backend.decisiontree.trees.vapeInfoTree
import javax.inject.Inject

class DecisionTreeRepository @Inject constructor() {

    // Registro central de árboles
    private val trees = mapOf(

        "alcohol_craving" to alcoholCravingTree,

        "cristal_craving" to cristalCravingTree,

        "vape_craving" to vapeCravingTree,

        "cigarro_craving" to cigarroCravingTree,


        "alcohol_info" to alcoholInfoTree,

        "cristal_info" to cristalInfoTree,

        "vape_info" to vapeInfoTree,

        "cigarro_info" to cigarroInfoTree
    )

    // Obtener árbol
    fun obtenerArbol(
        treeId: String
    ): DecisionTree {

        return trees[treeId]

            ?: error(
                "Árbol no encontrado: $treeId"
            )
    }
}
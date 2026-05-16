package com.solvyx.backend.decisiontree.repository

import com.solvyx.backend.decisiontree.model.DecisionTree
import com.solvyx.backend.decisiontree.trees.alcoholCravingTree
import com.solvyx.backend.decisiontree.trees.alcoholInfoTree
import com.solvyx.backend.decisiontree.trees.cristalCravingTree
import com.solvyx.backend.decisiontree.trees.cristalInfoTree
import javax.inject.Inject

class DecisionTreeRepository @Inject constructor() {

    // Registro central de árboles
    private val trees = mapOf(

        "alcohol_craving" to alcoholCravingTree,

        "cristal_craving" to cristalCravingTree,

        "alcohol_info" to alcoholInfoTree,

        "cristal_info" to cristalInfoTree
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
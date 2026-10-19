package com.churchbooklibrary.core.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.churchbooklibrary.R

@Composable
fun getLocalizedCategoryName(category: String): String {
    return when (category) {
        "Biographies" -> stringResource(R.string.category_biographies)
        "Devotionals" -> stringResource(R.string.category_devotionals)
        "Kids" -> stringResource(R.string.category_kids)
        "Commentaries" -> stringResource(R.string.category_commentaries)
        "Theology" -> stringResource(R.string.category_theology)
        "Practical Christianity" -> stringResource(R.string.category_practical_christianity)
        "Bibles" -> stringResource(R.string.category_bibles)
        "Bible Study Material" -> stringResource(R.string.category_bible_study_material)
        "Christian Living" -> stringResource(R.string.category_christian_living)
        "Church History" -> stringResource(R.string.category_church_history)
        "Apologetics" -> stringResource(R.string.category_apologetics)
        "Other" -> stringResource(R.string.category_other)
        else -> category
    }
}

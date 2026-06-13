package com.faiz.bumiloka.model

data class Faq(
    val question: String,
    val answer: String,
    var isExpanded: Boolean = false
)

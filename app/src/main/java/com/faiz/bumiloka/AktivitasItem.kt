package com.faiz.bumiloka

sealed class AktivitasItem {
    data class Header(val title: String) : AktivitasItem()
    data class Item(val data: AktivitasModel) : AktivitasItem()
}

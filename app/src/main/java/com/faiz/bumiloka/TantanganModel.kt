data class TantanganModel(

    val id: Int,
    val nama: String,

    var progress: Int = 0,
    var totalTask: Int = 2,

    var materiSelesai: Boolean = false,
    var kuisSelesai: Boolean = false,

    var status: String = "belum",

    var tanggalMulai: Long = 0L,
    var tanggalBerakhir: Long = 0L
)
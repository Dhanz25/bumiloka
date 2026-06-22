package com.faiz.bumiloka.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.faiz.bumiloka.data.repository.AdminRepository
import com.faiz.bumiloka.model.Edukasi
import com.faiz.bumiloka.model.Kuis
import com.faiz.bumiloka.model.SoalKuis

class AdminViewModel : ViewModel() {
    private val repository = AdminRepository()

    private val _edukasiList = MutableLiveData<List<Edukasi>>()
    val edukasiList: LiveData<List<Edukasi>> = _edukasiList

    private val _kuisList = MutableLiveData<List<Kuis>>()
    val kuisList: LiveData<List<Kuis>> = _kuisList

    private val _soalList = MutableLiveData<List<SoalKuis>>()
    val soalList: LiveData<List<SoalKuis>> = _soalList

    private val _statistik = MutableLiveData<Map<String, Long>>()
    val statistik: LiveData<Map<String, Long>> = _statistik

    fun fetchEdukasi() {
        repository.getEdukasi { _edukasiList.postValue(it) }
    }

    fun saveEdukasi(edukasi: Edukasi, onComplete: (Boolean) -> Unit) {
        repository.saveEdukasi(edukasi, onComplete)
    }

    fun deleteEdukasi(id: String, onComplete: (Boolean) -> Unit) {
        repository.deleteEdukasi(id, onComplete)
    }

    fun fetchKuis() {
        repository.getKuis { _kuisList.postValue(it) }
    }

    fun saveKuis(kuis: Kuis, onComplete: (Boolean) -> Unit) {
        repository.saveKuis(kuis, onComplete)
    }

    fun deleteKuis(id: String, onComplete: (Boolean) -> Unit) {
        repository.deleteKuis(id, onComplete)
    }

    fun fetchSoal(kuisId: String) {
        repository.getSoal(kuisId) { _soalList.postValue(it) }
    }

    fun saveSoal(kuisId: String, soal: SoalKuis, onComplete: (Boolean) -> Unit) {
        repository.saveSoal(kuisId, soal, onComplete)
    }

    fun deleteSoal(kuisId: String, soalId: String, onComplete: (Boolean) -> Unit) {
        repository.deleteSoal(kuisId, soalId, onComplete)
    }

    fun fetchStatistik() {
        repository.getStatistik { _statistik.postValue(it) }
    }
}

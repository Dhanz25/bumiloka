package com.faiz.bumiloka.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.faiz.bumiloka.data.repository.AdminRepository
import com.faiz.bumiloka.model.*

class AdminViewModel : ViewModel() {
    private val repository = AdminRepository()

    private val _edukasiList = MutableLiveData<List<Edukasi>>()
    val edukasiList: LiveData<List<Edukasi>> = _edukasiList

    private val _kuisList = MutableLiveData<List<Kuis>>()
    val kuisList: LiveData<List<Kuis>> = _kuisList

    private val _tantanganList = MutableLiveData<List<Tantangan>>()
    val tantanganList: LiveData<List<Tantangan>> = _tantanganList

    private val _bonusTantanganList = MutableLiveData<List<BonusChallengeModel>>()
    val bonusTantanganList: LiveData<List<BonusChallengeModel>> = _bonusTantanganList

    private val _badgeList = MutableLiveData<List<Badge>>()
    val badgeList: LiveData<List<Badge>> = _badgeList

    private val _soalList = MutableLiveData<List<SoalKuis>>()
    val soalList: LiveData<List<SoalKuis>> = _soalList

    private val _statistik = MutableLiveData<Map<String, Long>>()
    val statistik: LiveData<Map<String, Long>> = _statistik

    // --- EDUKASI ---
    fun fetchEdukasi() { repository.getEdukasi { _edukasiList.postValue(it) } }
    fun saveEdukasi(edukasi: Edukasi, onComplete: (Boolean) -> Unit) { repository.saveEdukasi(edukasi, onComplete) }
    fun deleteEdukasi(id: String, onComplete: (Boolean) -> Unit) { repository.deleteEdukasi(id, onComplete) }

    // --- KUIS ---
    fun fetchKuis() { repository.getKuis { _kuisList.postValue(it) } }
    fun saveKuis(kuis: Kuis, onComplete: (Boolean) -> Unit) { repository.saveKuis(kuis, onComplete) }
    fun deleteKuis(id: String, onComplete: (Boolean) -> Unit) { repository.deleteKuis(id, onComplete) }
    fun saveKuisLengkap(kuis: Kuis, soalList: List<SoalKuis>, onComplete: (Boolean) -> Unit) { repository.saveKuisLengkap(kuis, soalList, onComplete) }

    // --- TANTANGAN UTAMA ---
    fun fetchTantangan() { repository.getTantangan { _tantanganList.postValue(it) } }
    fun saveTantangan(tantangan: Tantangan, onComplete: (Boolean) -> Unit) { repository.saveTantangan(tantangan, onComplete) }
    fun deleteTantangan(id: String, onComplete: (Boolean) -> Unit) { repository.deleteTantangan(id, onComplete) }

    // --- TANTANGAN BONUS ---
    fun fetchBonusTantangan() { repository.getBonusTantangan { _bonusTantanganList.postValue(it) } }
    fun saveBonusTantangan(bonus: BonusChallengeModel, onResult: (Boolean, String?) -> Unit) { repository.saveBonusTantangan(bonus, onResult) }
    fun deleteBonusTantangan(id: String, onComplete: (Boolean) -> Unit) { repository.deleteBonusTantangan(id, onComplete) }

    // --- BADGES ---
    fun fetchBadges() { repository.getBadges { _badgeList.postValue(it) } }
    fun saveBadge(badge: Badge, onComplete: (Boolean) -> Unit) { repository.saveBadge(badge, onComplete) }
    fun deleteBadge(id: String, onComplete: (Boolean) -> Unit) { repository.deleteBadge(id, onComplete) }

    // --- SOAL ---
    fun fetchSoal(kuisId: String) { repository.getSoal(kuisId) { _soalList.postValue(it) } }
    fun saveSoal(kuisId: String, soal: SoalKuis, onComplete: (Boolean) -> Unit) { repository.saveSoal(kuisId, soal, onComplete) }
    fun deleteSoal(kuisId: String, soalId: String, onComplete: (Boolean) -> Unit) { repository.deleteSoal(kuisId, soalId, onComplete) }

    // --- STATISTIK ---
    fun fetchStatistik() { repository.getStatistik { _statistik.postValue(it) } }
}

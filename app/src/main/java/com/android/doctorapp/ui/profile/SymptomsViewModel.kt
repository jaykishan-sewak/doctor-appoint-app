package com.android.doctorapp.ui.profile

import android.content.Context
import android.view.View
import android.widget.RadioGroup
import androidx.lifecycle.MutableLiveData
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.AuthRepository
import com.android.doctorapp.repository.local.Session
import com.android.doctorapp.util.SingleLiveEvent
import javax.inject.Inject

class SymptomsViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val authRepository: AuthRepository,
    private val context: Context,
    private val session: Session
) : BaseViewModel() {

    val selectYesOrNo: MutableLiveData<Boolean> =
        MutableLiveData(false)
    val isCalender: MutableLiveData<View> = SingleLiveEvent()


    fun consultOrNOt(group: RadioGroup, checkedId: Int) {
       selectYesOrNo.value = R.id.radioButtonNo != checkedId
    }

    fun calenderClick(textLastVisitDate: View) {
        isCalender.value = textLastVisitDate
    }

}
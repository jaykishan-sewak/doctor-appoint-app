package com.android.doctorapp.ui.doctordashboard.doctorfragment

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentViewClinicBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.ui.bottomsheet.ClinicImgBottomSheetDialog
import com.android.doctorapp.ui.doctordashboard.adapter.ClinicImgAdapter
import com.android.doctorapp.ui.profile.ProfileViewModel
import com.android.doctorapp.util.extension.alert
import com.android.doctorapp.util.extension.negativeButton
import com.android.doctorapp.util.extension.positiveButton
import javax.inject.Inject


class ViewClinicFragment : BaseFragment<FragmentViewClinicBinding>(R.layout.fragment_view_clinic) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<ProfileViewModel> { viewModelFactory }
    private lateinit var bindingView: FragmentViewClinicBinding
    lateinit var clinicBottomSheetFragment: ClinicImgBottomSheetDialog
    private lateinit var clinicImgAdapter: ClinicImgAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        // Inflate the layout for this fragment
        bindingView = binding {
            viewModel = this@ViewClinicFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        binding.rvClinicImages.layoutManager =
            GridLayoutManager(requireActivity(), 2)

        binding.addClinicImg.setOnClickListener {
            clinicBottomSheetFragment =
                ClinicImgBottomSheetDialog(object : ClinicImgBottomSheetDialog.DialogListener {
                    override fun getImageUri(uri: Uri) {
                        viewModel.uploadImage(uri)
                    }
                })
            clinicBottomSheetFragment.show(
                requireActivity().supportFragmentManager,
                "BSDialogFragment"
            )
        }
        setUpWithViewModel(viewModel)
        registerObservers()
        return bindingView.root
    }

    private fun registerObservers() {

        if (viewModel.userProfileDataResponse.value == null)
            viewModel.getUserProfileData()

        updateHolidayRecyclerview(arrayListOf())
//        viewModel.userProfileDataResponse.observe(viewLifecycleOwner) {
//            if (it != null) {
//                viewModel.clinicImgList.value = it.clinicImg!!
//                viewModel.clinicImgArrayList = it.clinicImg!!
//            }
//        }
        viewModel.clinicImgList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                Log.d(ContentValues.TAG, "registerObservers: $it")
                clinicImgAdapter.updateClinicImgList(it)
            }
        }
    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withTitle(R.string.view_clinic)
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .withNavigationIcon(AppCompatResources.getDrawable(requireActivity(), R.drawable.ic_back_white))
            .withNavigationListener{
                findNavController().popBackStack()
            }
            .build()
    }

    private fun updateHolidayRecyclerview(newClinicImgList: ArrayList<String>) {
        clinicImgAdapter = ClinicImgAdapter(newClinicImgList,
            object : ClinicImgAdapter.OnItemClickListener {
                override fun onItemClick(imageUrl: Uri, position: Int) {
                }

                override fun onItemDelete(imageUrl: Uri, position: Int) {
                    requireActivity().alert {
                        setTitle(R.string.delete)
                        setMessage("Are you sure you want to delete Image?")
                        positiveButton { dialog ->
                            viewModel.deleteImage(position)
                            dialog.dismiss()
                        }
                        negativeButton(resources.getString(R.string.cancel)) { dialog ->
                            dialog.dismiss()
                        }

                    }
                }
            })
        binding.rvClinicImages.adapter = clinicImgAdapter
    }

}
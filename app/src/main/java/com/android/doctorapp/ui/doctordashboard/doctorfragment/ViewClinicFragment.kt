package com.android.doctorapp.ui.doctordashboard.doctorfragment

import android.net.Uri
import android.os.Bundle
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
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.GET_CLINIC_IMAGE_LIST_KEY
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.OPEN_IMAGE_POSITION_KEY
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.VIEW_CLINIC_IMAGE_URL
import com.android.doctorapp.util.extension.alert
import com.android.doctorapp.util.extension.negativeButton
import com.android.doctorapp.util.extension.positiveButton
import javax.inject.Inject


class ViewClinicFragment : BaseFragment<FragmentViewClinicBinding>(R.layout.fragment_view_clinic) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<ProfileViewModel> { viewModelFactory }
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
        val arguments: Bundle? = arguments
        if (arguments != null) {
            viewModel.isUserViewClinicImg.value = true
            viewModel.clinicImgList.value =
                requireArguments().getStringArrayList(GET_CLINIC_IMAGE_LIST_KEY)
        } else {
            viewModel.isUserViewClinicImg.value = false
        }
        // Inflate the layout for this fragment
        val bindingView = binding {
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
        viewModel.isUserViewClinicImg.observe(viewLifecycleOwner) {
            if (it) {
                binding.addClinicImg.visibility = if (it) View.GONE else View.VISIBLE
            } else {
                if (viewModel.userProfileDataResponse.value == null)
                    viewModel.getUserProfileData()
            }
        }

        updateHolidayRecyclerview(arrayListOf())
        viewModel.clinicImgList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                viewModel.noClinicImgFound.value = false
                clinicImgAdapter.updateClinicImgList(
                    it,
                    viewModel.isUserViewClinicImg.value == true
                )
            } else
                viewModel.noClinicImgFound.value = true
        }
    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withTitle(R.string.view_clinic)
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .withNavigationIcon(
                AppCompatResources.getDrawable(
                    requireActivity(),
                    R.drawable.ic_back_white
                )
            )
            .withNavigationListener {
                findNavController().popBackStack()
            }
            .build()
    }

    private fun updateHolidayRecyclerview(newClinicImgList: ArrayList<String>) {
        clinicImgAdapter = ClinicImgAdapter(newClinicImgList,
            object : ClinicImgAdapter.OnItemClickListener {
                override fun onItemClick(imageUrl: Uri, position: Int) {
                    val bundle = Bundle()
                    bundle.putString(VIEW_CLINIC_IMAGE_URL, imageUrl.toString())
                    val imageList: MutableList<String>? = viewModel.clinicImgList.value
                    bundle.putStringArrayList(GET_CLINIC_IMAGE_LIST_KEY, ArrayList(imageList!!))
                    bundle.putInt(OPEN_IMAGE_POSITION_KEY, position)

                    findNavController().navigate(
                        R.id.action_viewClinic_to_previewClinic,
                        bundle
                    )

                }

                override fun onItemDelete(imageUrl: Uri, position: Int) {
                    requireActivity().alert {
                        setTitle(R.string.delete)
                        setMessage(requireActivity().getString(R.string.are_you_sure_you_want_to_Delete_image))
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
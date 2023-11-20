package com.android.doctorapp.ui.doctordashboard.doctorfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentPreviewClinicImgBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.ui.doctordashboard.adapter.PreviewImgShowAdapter
import com.android.doctorapp.ui.profile.ProfileViewModel
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.GET_CLINIC_IMAGE_LIST_KEY
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.OPEN_IMAGE_POSITION_KEY
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.VIEW_CLINIC_IMAGE_URL
import com.android.doctorapp.util.zoomImage.ZoomImageView
import javax.inject.Inject


class PreviewClinicImgFragment :
    BaseFragment<FragmentPreviewClinicImgBinding>(R.layout.fragment_preview_clinic_img),
    ZoomImageView.OnScaleChangeListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<ProfileViewModel> { viewModelFactory }
    private lateinit var bindingView: FragmentPreviewClinicImgBinding
    private var imageList: List<String> = arrayListOf()
    private var position: Int = -1
    private lateinit var imagePath: String
    private lateinit var imageShowAdapter: PreviewImgShowAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        imageShowAdapter = PreviewImgShowAdapter(requireContext(), this)
        if (arguments != null) {
            position = requireArguments().getInt(OPEN_IMAGE_POSITION_KEY)
            imagePath = requireArguments().getString(VIEW_CLINIC_IMAGE_URL)!!
            imageList = requireArguments().getStringArrayList(GET_CLINIC_IMAGE_LIST_KEY)!!

            imageShowAdapter.addAll(imageList)
            binding.viewPager.adapter = imageShowAdapter
            binding.viewPager.currentItem = position
        }


        // Inflate the layout for this fragment
        bindingView = binding {
            viewModel = this@PreviewClinicImgFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        setUpWithViewModel(viewModel)
        return bindingView.root
    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder().withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withTitle(R.string.preview)
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .withNavigationIcon(
                AppCompatResources.getDrawable(
                    requireActivity(), R.drawable.ic_back_white
                )
            ).withNavigationListener {
                findNavController().popBackStack()
            }.build()
    }


    override fun onScaleChange(zoomValue: Int) {
        if (zoomValue == 0) {
            binding.viewPager.setIsDragValue(false)
        } else {
            binding.viewPager.setIsDragValue(true)
        }
    }

}
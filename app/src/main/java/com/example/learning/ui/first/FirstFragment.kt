package com.example.learning.ui.first

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.learning.R
import com.example.learning.data.local.LocalDataSource
import com.example.learning.data.local.PosterDatabase
import com.example.learning.data.remote.PosterApi
import com.example.learning.data.remote.PosterApiStatus
import com.example.learning.data.remote.RemoteDataSource
import com.example.learning.databinding.FragmentFirstBinding
import com.example.learning.presentation.first.FirstViewModel
import com.example.learning.repository.PosterRepositoryImp
import com.example.learning.ui.first.adapter.PosterAdapter
import com.example.learning.ui.first.adapter.PosterListener
import com.example.learning.utils.show
import com.example.learning.utils.toPosterEntityList

class FirstFragment : Fragment() {

    private lateinit var binding: FragmentFirstBinding
    private lateinit var viewModel: FirstViewModel
    private lateinit var viewModelFac: FirstViewModel.FirstViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i("Fragment", "FirstFragment Created")

        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_first, container, false)

        //Esto nos permite eliminar el observer y hace que la vista se actualice automáticamente
        binding.lifecycleOwner = viewLifecycleOwner


        //Inyección de dependencias
        val application = requireNotNull(this.activity).application
        val database = PosterDatabase.getInstance(application).posterDatabaseDao

        val repositoryImp = PosterRepositoryImp(
            RemoteDataSource(PosterApi.retrofitService),
            LocalDataSource(database)
        )

        viewModelFac = FirstViewModel.FirstViewModelFactory(application, repositoryImp)
        viewModel = ViewModelProvider(this, viewModelFac)[FirstViewModel::class.java]
        binding.firstViewModel = viewModel


        //Asignación del adapter y carga de los post desde base de datos
        val adapter = PosterAdapter(PosterListener {
            Toast.makeText(this.requireContext(), "Has pulsado la Card $it ", Toast.LENGTH_SHORT)
                .show()
        })

        binding.postList.adapter = adapter
        viewModel.posters.observe(viewLifecycleOwner) {
            it?.let {
                adapter.submitList(it.toPosterEntityList()) //Esto indica a listAdapter que una nueva versión de la lista
            }
        }

        viewModel.status.observe(viewLifecycleOwner) {
            when (it!!) {
                PosterApiStatus.LOADING -> {
                    binding.progresBar.show(binding.contentPosters, binding.errorImage,true)
                }
                PosterApiStatus.ERROR -> binding.errorImage.show(
                    binding.contentPosters,
                    binding.progresBar,
                    true
                )
                PosterApiStatus.DONE -> binding.contentPosters.show(
                    binding.progresBar,
                    binding.errorImage,
                    true
                )
            }
        }

        viewModel.navigatTo.observe(viewLifecycleOwner) {
            if (it) {
                navigateTo()
            }
        }

        return binding.root
    }

    private fun navigateTo() {
        findNavController().navigate(
            FirstFragmentDirections.actionFirstFragmentToSecondFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("Fragment", "FirstFragment destroyed")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel
        Log.i("Fragment", "FirstFragment destroyed")
    }
}
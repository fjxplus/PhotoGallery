package com.fanjiaxing.android.photogallery

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fanjiaxing.android.photogallery.databinding.FragmentPhotoGalleryBinding
import com.fanjiaxing.android.photogallery.model.GalleryItem
import com.fanjiaxing.android.photogallery.network.ThumbnailDownloader
import com.fanjiaxing.android.photogallery.ui.PhotoGalleryFragmentViewModel

private const val TAG = "ThumbnailDownloader"

class PhotoGalleryFragment : Fragment() {

    private var _binding: FragmentPhotoGalleryBinding? = null

    private val binding get() = _binding!!

    private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoHolder>

    private val viewModel by lazy {
        ViewModelProvider(this).get(PhotoGalleryFragmentViewModel::class.java)
    }

    companion object {
        fun newInstance(): PhotoGalleryFragment = PhotoGalleryFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val responseHandler = Handler(Looper.getMainLooper())
        thumbnailDownloader = ThumbnailDownloader(responseHandler) { photoHolder, bitmap ->
            val drawable = BitmapDrawable(resources, bitmap)
            photoHolder.bindDrawable(drawable)
        }
        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoGalleryBinding.inflate(inflater, container, false)
        binding.photoRecyclerView.layoutManager = GridLayoutManager(context, 3)
        viewLifecycleOwner.lifecycle.addObserver(thumbnailDownloader.viewLifecycleObserver)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.galleryItemLiveData.observe(viewLifecycleOwner, { galleryItems ->
            binding.photoRecyclerView.adapter = PhotoAdapter(galleryItems)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewLifecycleOwner.lifecycle.removeObserver(thumbnailDownloader.viewLifecycleObserver)
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(thumbnailDownloader.fragmentLifecycleObserver)
    }

    private class PhotoHolder(itemImage: ImageView) : RecyclerView.ViewHolder(itemImage) {

        val bindDrawable: (Drawable) -> Unit = itemImage::setImageDrawable
    }

    private inner class PhotoAdapter(private val galleryItems: List<GalleryItem>) :
        RecyclerView.Adapter<PhotoHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
            val itemImage =
                layoutInflater.inflate(R.layout.list_item_gallery, parent, false) as ImageView
            return PhotoHolder(itemImage)
        }

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            val galleryItem = galleryItems[position]
            val placeHolder: Drawable =
                ContextCompat.getDrawable(requireContext(), R.drawable.bill_up_close)
                    ?: ColorDrawable()
            holder.bindDrawable(placeHolder)
            thumbnailDownloader.queueThumbnail(holder, galleryItem.url)
        }

        override fun getItemCount(): Int {
            return galleryItems.size
        }

    }
}
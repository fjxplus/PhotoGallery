package com.fanjiaxing.android.photogallery

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.fanjiaxing.android.photogallery.databinding.FragmentPhotoGalleryBinding
import com.fanjiaxing.android.photogallery.model.GalleryItem
import com.fanjiaxing.android.photogallery.network.PollWorker
import com.fanjiaxing.android.photogallery.network.ThumbnailDownloader
import com.fanjiaxing.android.photogallery.ui.PhotoGalleryFragmentViewModel
import java.util.concurrent.TimeUnit

private const val TAG = "ThumbnailDownloader"
private const val POLL_WORK = "POLL_WORK"

class PhotoGalleryFragment : VisibleFragment() {

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
        setHasOptionsMenu(true)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_pohot_gallery, menu)

        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as androidx.appcompat.widget.SearchView

        searchView.apply {
            setOnQueryTextListener(object :
                androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    Log.d(TAG, "QueryTextSubmit: $query")
                    query?.apply {
                        viewModel.fetchPhotos(query)
                    }
                    val manager = context.getSystemService(Context.INPUT_METHOD_SERVICE)
                            as InputMethodManager
                    manager.hideSoftInputFromWindow(
                        searchView.windowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS
                    )

                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    Log.d(TAG, "QueryTextChanged: $$newText")
                    return false
                }
            })

            setOnSearchClickListener {
                searchView.setQuery(viewModel.searchTerm, false)
            }
        }

        val toggleItem = menu.findItem(R.id.menu_item_toggle_polling)
        val isPolling = QueryPreference.isPolling(requireContext())
        val toggleTitle = if (isPolling) {
            R.string.stop_polling
        } else {
            R.string.start_polling
        }
        toggleItem.setTitle(toggleTitle)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_clear -> {
                viewModel.fetchPhotos("")
                true
            }
            R.id.menu_item_toggle_polling -> {
                val isPolling = QueryPreference.isPolling(requireContext())
                if (isPolling) {
                    WorkManager.getInstance().cancelUniqueWork(POLL_WORK)
                    QueryPreference.setPolling(requireContext(), false)
                } else {
                    val constraints =
                        Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build()
                    val periodRequest = PeriodicWorkRequest
                        .Builder(PollWorker::class.java, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build()
                    WorkManager.getInstance().enqueueUniquePeriodicWork(
                        POLL_WORK,
                        ExistingPeriodicWorkPolicy.KEEP,
                        periodRequest
                    )
                    QueryPreference.setPolling(requireContext(), true)
                }
                activity?.invalidateOptionsMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.galleryItemLiveData.observe(viewLifecycleOwner) { galleryItems ->
            binding.photoRecyclerView.adapter = PhotoAdapter(galleryItems)
        }
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

    private inner class PhotoHolder(itemImage: ImageView) : RecyclerView.ViewHolder(itemImage),
        View.OnClickListener {

        val bindDrawable: (Drawable) -> Unit = itemImage::setImageDrawable

        private lateinit var galleryItem: GalleryItem

        init {
            itemView.setOnClickListener(this)
        }

        fun bindGalleryItem(item: GalleryItem) {
            galleryItem = item
        }

        override fun onClick(v: View?) {
            val intent = PhotoPageActivity.newIntent(requireContext(), galleryItem.photoPageUri)
            startActivity(intent)
        }

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
            holder.bindGalleryItem(galleryItem)
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
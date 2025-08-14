package com.ahmed.pdfview.viewpdf

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.Window
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.ahmed.pdfview.viewpdf.databinding.ActivityPdfViewerBinding
import com.rajat.pdfviewer.PdfRendererView
import com.rajat.pdfviewer.util.CacheStrategy
import com.rajat.pdfviewer.util.EdgeToEdgeHelper
import com.rajat.pdfviewer.util.FileUtils
import com.rajat.pdfviewer.util.ThemeValidator
import com.rajat.pdfviewer.util.ToolbarStyle
import com.rajat.pdfviewer.util.ViewerStyle
import kotlinx.coroutines.launch
import java.io.File
import java.util.Timer
import java.util.TimerTask

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfViewerBinding

    private lateinit var cacheStrategy: CacheStrategy
    private lateinit var filePath: String
    private lateinit var fileName: String
    private var currentPage = 0

    companion object {
        const val FILE_TITLE = "pdf_file_title"
        const val ENABLE_ZOOM = "enable_zoom"
        var isZoomEnabled = true
        const val CACHE_STRATEGY = "cache_strategy"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        //setTheme(R.style.Theme_PdfView_SelectedTheme)
        ThemeValidator.validatePdfViewerTheme(this)
        super.onCreate(savedInstanceState)

        // Inflate layout once (previously done twice)
        binding = ActivityPdfViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply edge-to-edge window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            applyEdgeToEdge(window)
        }

        // Apply theme attributes (background & progress bar styles)
        applyThemeAttributes()

        // Retrieve intent extras
        extractIntentExtras()

        binding.nextBtn.setOnClickListener {
            navigateToPage(currentPage + 1)
        }

        binding.backBtn.setOnClickListener {
            navigateToPage(currentPage - 1)
        }

        binding.pageNum.addTextChangedListener(textWatcher)
        init()
    }

    private fun extractIntentExtras() {
        isZoomEnabled = intent.getBooleanExtra(ENABLE_ZOOM, true)
        val strategyOrdinal = intent.getIntExtra(CACHE_STRATEGY, CacheStrategy.MAXIMIZE_PERFORMANCE.ordinal)
        cacheStrategy = CacheStrategy.entries.getOrElse(strategyOrdinal) {
            CacheStrategy.MAXIMIZE_PERFORMANCE
        }
        filePath = intent.getStringExtra("filePath")  ?: "default_value"
        fileName = intent.getStringExtra("fileName")  ?: "default_value"
        Log.d("PdfViewer", "File path : $filePath, File name : $fileName")
    }

    private fun init() {
        binding.pdfView.statusListener = object : PdfRendererView.StatusCallBack {
            override fun onPageChanged(currentPage: Int, totalPage: Int) {
                runOnUiThread {
                    this@PdfViewerActivity.currentPage = currentPage - 1
                    binding.pageNum.setText("$currentPage")
                    binding.pageNum.setSelection(binding.pageNum.length())
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                when (state) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        // Ensure final position is correct
                        binding.pdfView.post {
                            val firstComplete = binding.pdfView.getLayoutManager()
                                ?.findFirstCompletelyVisibleItemPosition() ?: 0
                            if (firstComplete != RecyclerView.NO_POSITION) {
                                currentPage = firstComplete
                                binding.pageNum.setText("${firstComplete + 1}")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun navigateToPage(page: Int) {
        if (page in 0 until binding.pdfView.totalPageCount) {
            // Use the reset zoom function
            binding.pdfView.resetZoomAndNavigateToPage(page, smoothScroll = true)

            // Update UI immediately
            currentPage = page
            binding.pageNum.setText("${page + 1}")
            binding.pageNum.setSelection(binding.pageNum.length())
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            //initPdfViewerWithPath(filesDir.absolutePath + "/sample.pdf")
            initPdfViewerWithPath("$filePath/$fileName")
            binding.pdfView.post {
                binding.pageNum.setText("1")
                currentPage = 0
            }
        }

    }

    private val textWatcher = object : TextWatcher {
        private var timer = Timer()
        private val DELAY: Long = 1000 // 1 second delay

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            timer.cancel()
        }

        override fun afterTextChanged(s: Editable?) {
            val input = s?.toString() ?: return
            timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        if (input.isNotEmpty()) {
                            try {
                                val pageNum = input.toInt()
                                when {
                                    pageNum < 1 -> {
                                        binding.pageNum.setText("1")
                                        binding.pdfView.resetZoomAndNavigateToPage(0)
                                    }
                                    pageNum > binding.pdfView.totalPageCount -> {
                                        binding.pageNum.setText("${binding.pdfView.totalPageCount}")
                                        binding.pdfView.resetZoomAndNavigateToPage(binding.pdfView.totalPageCount - 1)
                                    }
                                    else -> {
                                        binding.pdfView.resetZoomAndNavigateToPage(pageNum - 1)
                                    }
                                }
                                binding.pageNum.setSelection(binding.pageNum.length())
                            } catch (e: NumberFormatException) {
                                binding.pageNum.setText("${currentPage + 1}")
                            }
                        }
                    }
                }
            }, DELAY)
        }
    }

    private fun applyEdgeToEdge(window: Window) {
        val isDarkMode = EdgeToEdgeHelper.isDarkModeEnabled(resources.configuration.uiMode)
        val toolbarColor = ToolbarStyle.Companion.from(this, intent).toolbarColor

        // Must be called from ComponentActivity
        enableEdgeToEdge(
            statusBarStyle = if (isDarkMode) {
                SystemBarStyle.Companion.dark(toolbarColor)
            } else {
                SystemBarStyle.Companion.light(toolbarColor, toolbarColor)
            }
        )

        // apply insets via helper
        EdgeToEdgeHelper.applyInsets(window, binding.root, isDarkMode)
    }

    private fun applyThemeAttributes() {
        ViewerStyle.Companion.from(this).applyTo(binding)
    }


    private suspend fun initPdfViewerWithPath(filePath: String?) {
        if (TextUtils.isEmpty(filePath)) {
            onPdfError("")
            return
        }
        try {
            val file = if (filePath!!.startsWith("content://")) {
                FileUtils.uriToFile(applicationContext, filePath.toUri())
            } else {
                File(filePath)
            }
            binding.pdfView.setZoomEnabled(isZoomEnabled)
            binding.pdfView.initWithFile(file, cacheStrategy)
        } catch (e: Exception) {
            onPdfError(e.toString())
        }
    }



    private fun onPdfError(e: String) {
        Log.e("Pdf render error", e)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.pdfView.closePdfRender()
    }



}
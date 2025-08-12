package com.rajat.pdfviewer

import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ahmed.pdfview.viewpdf.R
import com.rajat.pdfviewer.PdfRendererView.StatusCallBack

internal class PdfPageScrollListener(
    private val pageNoTextView: TextView,
    private val totalPageCount: () -> Int,
    private val updatePage: (Int) -> Unit,
    private val schedulePrefetch: (Int) -> Unit,
    private val statusListener: StatusCallBack?
) : RecyclerView.OnScrollListener() {

    private var lastDisplayedPage = RecyclerView.NO_POSITION
    private var isUserScrolling = false
    private val hideRunnable = Runnable {
        if (pageNoTextView.isVisible) pageNoTextView.visibility = TextView.GONE
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val firstVisible = layoutManager.findFirstVisibleItemPosition()
        val firstComplete = layoutManager.findFirstCompletelyVisibleItemPosition()

        if (firstVisible != RecyclerView.NO_POSITION) {
            // Update based on most visible page
            val pageToShow = if (firstComplete != RecyclerView.NO_POSITION) {
                firstComplete
            } else {
                firstVisible
            }

            if (pageToShow != lastDisplayedPage) {
                updatePage(pageToShow)
                pageNoTextView.text = pageNoTextView.context.getString(
                    R.string.pdfView_page_no, pageToShow + 1, totalPageCount()
                )
                pageNoTextView.visibility = TextView.VISIBLE
                pageNoTextView.removeCallbacks(hideRunnable)
                pageNoTextView.postDelayed(hideRunnable, 3000)
                lastDisplayedPage = pageToShow
            }
        }
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        when (newState) {
            RecyclerView.SCROLL_STATE_DRAGGING -> {
                isUserScrolling = true
            }
            RecyclerView.SCROLL_STATE_SETTLING -> {
                isUserScrolling = true
            }
            RecyclerView.SCROLL_STATE_IDLE -> {
                isUserScrolling = false
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstComplete = layoutManager.findFirstCompletelyVisibleItemPosition()
                if (firstComplete != RecyclerView.NO_POSITION) {
                    updatePage(firstComplete)
                    schedulePrefetch(firstComplete)
                }
            }
        }
        statusListener?.onPageScrollStateChanged(newState)
    }
}
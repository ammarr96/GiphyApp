package com.amarlubovac.giphyapp


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    var list: MutableList<GiffImage> = mutableListOf()
    var isFullScreen = false
    var limit = 21
    var offset = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        floatingActionButton2.setOnClickListener {
            var intent = Intent(applicationContext, UploadDataActivity::class.java)
            startActivity(intent)
        }

        searchEditText.setOnEditorActionListener(object: TextView.OnEditorActionListener {
            override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
                progressBar.visibility = View.VISIBLE
                showMore.visibility = View.GONE
                gridLayout?.removeAllViews()
                offset = 0
                limit = 21
                if (searchEditText.text.toString().equals("")) {
                    getTrenging()
                }
                else {
                    search(searchEditText.text.toString())
                }
                hideKeyboard()
                return true
            }

        })

        refreshLayout.setOnRefreshListener {
            gridLayout?.removeAllViews()
            showMore.visibility = View.GONE
            offset = 0
            limit = 21
            if (searchEditText.text.toString().equals("")) {
                getTrenging()
            }
            else {
                search(searchEditText.text.toString())
            }
        }

        showMore.setOnClickListener {
            limit += 9
            offset = list.size
            showMore.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            if (searchEditText.text.toString().length != 0) {
                search(searchEditText.text.toString())
            }
            else {
                getTrenging()
            }
        }

        progressBar.visibility = View.VISIBLE
        getTrenging()
    }

    fun getTrenging() {
        list.clear()
        val apiService = RetrofitFactory.makeApiService()
        val call: Call<DataModel> = apiService.getTrending(getString(R.string.api_key), limit, offset, "G")

        call.enqueue(object : Callback<DataModel> {

            override fun onResponse(call: Call<DataModel>?, response: Response<DataModel>?) {
                //Toast.makeText(applicationContext, response?.body().toString(), Toast.LENGTH_SHORT).show()
                list = response?.body()?.data!!.toMutableList()
                populateGrid()
                saveData()
                progressBar.visibility = View.GONE
                if (list.size==0) {
                    Toast.makeText(applicationContext, "Results not found", Toast.LENGTH_SHORT).show()
                }
                else  {
                    showMore.visibility = View.VISIBLE
                }
                refreshLayout.setRefreshing(false);
            }

            override fun onFailure(call: Call<DataModel>?, t: Throwable?) {
                //Toast.makeText(applicationContext, t.toString(), Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                showMore.visibility = View.GONE
                refreshLayout.setRefreshing(false);
                try {
                    readData()
                    populateGrid()
                }
                catch (e: Exception) {
                    Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    fun search(query: String) {
        val apiService = RetrofitFactory.makeApiService()
        val call: Call<DataModel> = apiService.searchGif(getString(R.string.api_key), query, limit, offset, "G")

        call.enqueue(object : Callback<DataModel> {

            override fun onResponse(call: Call<DataModel>?, response: Response<DataModel>?) {
                //Toast.makeText(applicationContext, response?.body().toString(), Toast.LENGTH_SHORT).show()
                list = response?.body()?.data!!.toMutableList()
                populateGrid()
                progressBar.visibility = View.GONE
                refreshLayout.setRefreshing(false)
                if (list.size==0) {
                    Toast.makeText(applicationContext, "Results not found", Toast.LENGTH_SHORT).show()
                }
                else  {
                    showMore.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<DataModel>?, t: Throwable?) {
                //Toast.makeText(applicationContext, t.toString(), Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                showMore.visibility = View.GONE
                refreshLayout.setRefreshing(false);
                try {
                    readData()
                    populateGrid()
                }
                catch (e: Exception) {
                    Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    private fun populateGrid() {

        gridLayout?.columnCount = 3
        gridLayout?.rowCount = list.size / 3

        for (i in offset..list.size-1) {
            addViewToGridLayout(i/3, i%3, 1, 1, list.get(i).images?.image?.url!!, list.get(i).images?.giff?.url!!)
        }

        if (offset>0) {
            scrollToBottom()
        }

    }

    fun scrollToBottom() {
        scrollView.post(Runnable {
            scrollView.scrollTo(0, gridLayout.getBottom())
        })
    }

    private fun addViewToGridLayout(row: Int, column: Int, rowSpan: Int, columnSpan: Int, image: String, giff: String) {

        var childView: View
        val imageView: ImageView

        childView = layoutInflater.inflate(R.layout.customgridcell, null) as View
        imageView = childView.findViewById(R.id.imageView) as ImageView

        //Picasso.get().load(value).into(imageView)
        Glide.with(applicationContext).asGif().diskCacheStrategy(DiskCacheStrategy.ALL).load(image).into(imageView)

        val params = GridLayout.LayoutParams()

        val displayMetrics = applicationContext?.getResources()?.displayMetrics
        val dpWidth = displayMetrics?.widthPixels

        params.width = (dpWidth!! * 1.0/3.0).toInt()
        params.height = (dpWidth * 1.0/3.0).toInt()

        params.columnSpec = GridLayout.spec(column, columnSpan)
        params.rowSpec = GridLayout.spec(row, rowSpan)

        childView.setOnClickListener() {
            //Toast.makeText(activity, Integer.toString(row) + ", " + Integer.toString(column), Toast.LENGTH_SHORT).show();
            showGifView()
            progressBar.visibility = View.VISIBLE
            Glide.with(applicationContext).asGif().load(giff)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(object: RequestListener<GifDrawable>{
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<GifDrawable>?, isFirstResource: Boolean): Boolean {
                        progressBar.visibility = View.GONE
                        Toast.makeText(applicationContext, "Gif can't be loaded", Toast.LENGTH_SHORT).show()
                        hideGifView()
                        return false
                    }

                    override fun onResourceReady(resource: GifDrawable?, model: Any?, target: Target<GifDrawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        progressBar.visibility = View.GONE
                        return false
                    }

                })
                .into(fullScreenImage)


            floatingActionButton2.isVisible = false
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)
            isFullScreen = true
        }

        gridLayout?.addView(childView, params)
    }

    fun showGifView() {
        scrollView.visibility = View.GONE
        toolbar.visibility = View.GONE
        fullScreenLayout.visibility = View.VISIBLE
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    fun hideGifView() {
        scrollView.visibility = View.VISIBLE
        toolbar.visibility = View.VISIBLE
        fullScreenLayout.visibility = View.GONE
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        isFullScreen = false
    }

    @SuppressLint("ServiceCast")
    fun hideKeyboard() {
        val view = this.currentFocus
        view?.let { v ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.let { it.hideSoftInputFromWindow(v.windowToken, 0) }
        }
    }

    fun saveData() {
        val gson = Gson()
        val json = gson.toJson(list)
        val sharedPreferences = getSharedPreferences("appPreferences", Context.MODE_MULTI_PROCESS)
        sharedPreferences.edit().putString("list", json).apply()
    }

    fun readData() {
        val sharedPreferences = applicationContext?.getSharedPreferences("appPreferences", Context.MODE_PRIVATE)
        var data = sharedPreferences?.getString("list", "")
        val gson = Gson()
        val type = object : TypeToken<List<GiffImage>>() {}.type
        list = gson.fromJson(data, type)
    }


    override fun onBackPressed() {
        if (isFullScreen) {
            hideGifView()
        }
        else {
            super.onBackPressed()
        }
    }
}

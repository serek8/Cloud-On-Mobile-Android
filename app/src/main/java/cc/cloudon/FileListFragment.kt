package cc.cloudon

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.Keep
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cc.cloudon.placeholder.FileItemContent
import kotlinx.serialization.*
import kotlinx.serialization.json.Json


//import kotlinx.serialization.json.JSON

@Keep
@Serializable
data class Point2(val x: Int, val y: Int? = null)

class FileListFragment : Fragment() {

    external fun listDirLocally(): String
    val items: ArrayList<FileItemContent> = arrayListOf()
    lateinit var adapter : FileRecyclerViewAdapter
    lateinit var selectedButton : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


//        val nlist = Json { ignoreUnknownKeys = true }.decodeFromString(Point2.serializer(),
//            """{"x": 9, "y": 0}"""
//        )


    }

    fun downloadModel() : ArrayList<FileModel>{
        val jsonFiles = listDirLocally()
        var models = Json{ignoreUnknownKeys = true}.decodeFromString<ArrayList<FileModel>>(jsonFiles)
        models = ArrayList<FileModel>(models.sortedBy { it.name.lowercase() })
        return models
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_file_list_list, container, false)
        val model = downloadModel()
        val recycleView = view.findViewById<RecyclerView>(R.id.list)
        this.adapter = FileRecyclerViewAdapter(model)
        recycleView.adapter = adapter

        val dividerItemDecoration = DividerItemDecoration(recycleView.context, LinearLayoutManager.VERTICAL)
        dividerItemDecoration.setDrawable(AppCompatResources.getDrawable(recycleView.context, R.drawable.divider)!!)
        recycleView.addItemDecoration(dividerItemDecoration)

        var swipeContainer = view.findViewById<SwipeRefreshLayout>(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener {
//            adapter.clear()
            adapter.setNewValues(downloadModel())
            swipeContainer.isRefreshing = false
            adapter.notifyDataSetChanged()
        }

        val buttonAllFiles = view.findViewById<Button>(R.id.buttonAllFiles)
        selectedButton = buttonAllFiles
        buttonAllFiles?.setOnClickListener {
            if(it != selectedButton){
                reverseButtons(it as Button)
                selectedButton = it
            }
        }
        val buttonRecentFiles = view.findViewById<Button>(R.id.buttonRecent)
        buttonRecentFiles?.setOnClickListener {
            if(it != selectedButton) {
                reverseButtons(it as Button)
                selectedButton = it
            }
        }

        return view
    }

    fun reverseButtons(clickedButton : Button){
        val buttonAllFiles = this.view?.findViewById<Button>(R.id.buttonAllFiles);
        val buttonRecentFiles = this.view?.findViewById<Button>(R.id.buttonRecent);
        var otherButton = buttonAllFiles
        if(clickedButton == buttonAllFiles){
            otherButton = buttonRecentFiles
            sortModelByName()
        }
        else{
            sortModelByDate()
        }


        val currentTypeface = buttonAllFiles?.typeface
        buttonAllFiles?.typeface = buttonRecentFiles?.typeface
        buttonRecentFiles?.typeface = currentTypeface
    }

    fun sortModelByName() {
        this.adapter.setNewValues(ArrayList<FileModel>(this.adapter.values.sortedBy { it.name.lowercase() }))
        this.adapter.notifyDataSetChanged()
    }

    fun sortModelByDate(){
        this.adapter.setNewValues(ArrayList<FileModel>(this.adapter.values.sortedBy { it.date_epoch }.reversed()))
        this.adapter.notifyDataSetChanged()
    }


}
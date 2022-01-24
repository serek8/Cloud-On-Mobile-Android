package cc.cloudon

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


//        val nlist = Json { ignoreUnknownKeys = true }.decodeFromString(Point2.serializer(),
//            """{"x": 9, "y": 0}"""
//        )


    }

    fun downloadModel() : ArrayList<FileModel>{
        val jsonFiles = listDirLocally()
        val models = Json{ignoreUnknownKeys = true}.decodeFromString<ArrayList<FileModel>>(jsonFiles)
        Log.e("sdsd", "sd");
        return models
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_file_list_list, container, false)
        val model = downloadModel()
        view.findViewById<RecyclerView>(R.id.list).adapter = FileRecyclerViewAdapter(model)
        return view
    }

}
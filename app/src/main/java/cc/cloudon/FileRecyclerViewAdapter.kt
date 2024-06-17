package cc.cloudon

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import cc.cloudon.placeholder.FileItemContent.PlaceholderItem
import cc.cloudon.databinding.FragmentFileListBinding

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class FileRecyclerViewAdapter(
    var values: ArrayList<FileModel>
) : RecyclerView.Adapter<FileRecyclerViewAdapter.ViewHolder>() {

    fun clear(){
        values.clear()
    }
    fun setNewValues(values: ArrayList<FileModel>){
        this.values = values
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentFileListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.fileNameView.text = item.name
        holder.fileSizeView.text = prettifySize(item.size)
        var resource = R.drawable.fi_file
        when(item.name.lowercase().substringAfterLast(".", "")){
            "png", "img", "jpg", "jpeg", "bmp" -> resource = R.drawable.fi_image
            "mp4" -> resource = R.drawable.fi_movie
            "mp3" -> resource = R.drawable.fi_music
        }
        holder.fileTypeIcon.setImageResource(resource)

    }

    fun prettifySize(size : Int) : String{
        val KB = 1024
        val MB = 1024*KB
        if (size == 1){
            return "$size byte"
        }
        else if (size < KB){
            return "$size bytes"
        }
        else if(size < MB){
            return ""+size/KB + " KB"
        }
        else{
            return ""+size/MB + " MB"
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentFileListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val fileNameView: TextView = binding.fileName
        val fileSizeView: TextView = binding.fileSize
        val fileTypeIcon: ImageView = binding.iconFileType

        override fun toString(): String {
            return super.toString() + " '" + fileSizeView.text + "'"
        }
    }

}
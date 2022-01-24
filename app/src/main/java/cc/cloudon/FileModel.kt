package cc.cloudon

import androidx.annotation.Keep
import kotlinx.serialization.Serializable


@Keep
@Serializable
data class FileModel (
    val name: String
)
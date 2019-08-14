package com.amarlubovac.giphyapp

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DataModel(

    @SerializedName("data")
    var data: List<GiffImage>?

) : Serializable

data class GiffImage(

    @SerializedName("slug")
    var clientName: String?,

    @SerializedName("url")
    var url: String?,

    @SerializedName("embed_url")
    var embedUrl: String?,

    @SerializedName("images")
    var images: ImagesData?

) : Serializable

data class ImagesData(

    @SerializedName("fixed_height_still")
    var image: ImageData?,

    @SerializedName("original")
    var giff: ImageData?

) : Serializable

data class ImageData(

    @SerializedName("url")
    var url: String?

) : Serializable
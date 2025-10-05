package com.example.yourroom.ui.screens.publish

object PublishRoutes {
    const val Root = "publish" // pantalla de Básicos
    const val Details = "publish/{spaceId}/details"
    const val Photos = "publish/{spaceId}/photos"




    fun details(spaceId: Long) = "publish/$spaceId/details"
    fun photos(spaceId: Long) = "publish/$spaceId/photos"
    const val Home = "home"
    fun home() = Home
}
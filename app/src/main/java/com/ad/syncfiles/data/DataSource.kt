package com.ad.syncfiles.data

object DataSource {
    //val d = List(55) { (1..99999).random() }.map { "$it \\\\92.1984.1351.13" }
    val d =
        List(5) { (1..99999).random() }.map { SMBUiState(serverUrl = "$it \\\\192.192.198.64", password = "$it pass", username = "$it username") }

}
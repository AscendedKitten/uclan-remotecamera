package libs.pedroSG94

interface ClientListener {
  fun onDisconnected(client: ServerClient)
}
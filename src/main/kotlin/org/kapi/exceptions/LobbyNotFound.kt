package org.kapi.exceptions

import org.bson.types.ObjectId

class LobbyNotFound(lobbyId: ObjectId) : Exception("Can't find lobby $lobbyId")
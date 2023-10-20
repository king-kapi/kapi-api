package org.kapi.exceptions

import org.bson.types.ObjectId

class LobbyRequestNotFound(lobbyId: ObjectId, requestId: ObjectId) :
    Exception("Can't find request $requestId in lobby $lobbyId")
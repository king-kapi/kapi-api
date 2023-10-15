package org.kapi.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import org.kapi.data.Lobby
import org.kapi.data.OnboardingParams
import org.kapi.exceptions.LobbyNotFound
import org.kapi.exceptions.UserNotFound
import org.kapi.mongo.MongoClientSingleton
import org.kapi.responses.MessageResponse
import org.kapi.service.LobbyService
import org.kapi.service.UserService

fun Application.configureLobbiesRouting() {
    val lobbyService = LobbyService(MongoClientSingleton.getKapiDatabase(environment))

    routing {
        authenticate("auth-jwt", strategy = AuthenticationStrategy.Required) {
            route("/api/lobbies") {
                get("") {
                    call.respond(lobbyService.getAllLobbies())
                }

                post("") {
                    val principal = call.principal<JWTPrincipal>()
                    val meId = ObjectId(principal!!["id"])

                    val params = call.receive<NewLobbyParams>()
                    val newLobby = Lobby(
                        name = params.name,
                        hostId = meId,
                        users = listOf(meId),
                        description = params.description,
                        numPlayers = params.numPlayers,
                        tags = params.tags
                    )

                    val createdLobby = lobbyService.createLobby(newLobby)
                    call.respond(createdLobby)
                }

                get("/{lobbyId}") {
                    val lobbyId = ObjectId(call.parameters["lobbyId"])

                    try {
                        val lobby = lobbyService.getLobby(lobbyId)
                        call.respond(lobby)
                    } catch (e: LobbyNotFound) {
                        call.response.status(HttpStatusCode.InternalServerError)
                        call.respond(MessageResponse("Can't find user $lobbyId"))
                    }
                }

                delete("/{lobbyId}") {
                    val lobbyId = ObjectId(call.parameters["lobbyId"])

                    try {
                        lobbyService.deleteLobby(lobbyId)
                        call.respond(MessageResponse("Deleted lobby $lobbyId"))
                    } catch (e: LobbyNotFound) {
                        call.response.status(HttpStatusCode.InternalServerError)
                        call.respond(MessageResponse("Can't find user $lobbyId"))
                    }
                }

                post("/{lobbyId}/request") {
                    val principal = call.principal<JWTPrincipal>()
                    val meId = ObjectId(principal!!["id"])
                    val lobbyId = ObjectId(call.parameters["lobbyId"])

                    try {
                        val request = lobbyService.sendJoinRequest(lobbyId, meId)

                        call.respond(request)
                    } catch (e: LobbyNotFound) {
                        call.response.status(HttpStatusCode.InternalServerError)
                        call.respond(MessageResponse(e.message))
                    }
                }

                post("/{lobbyId}/{requestId}/accept") {
                    val principal = call.principal<JWTPrincipal>()
                    val meId = ObjectId(principal!!["id"])
                    val lobbyId = ObjectId(call.parameters["lobbyId"])
                    val lobby = lobbyService.getLobby(lobbyId)

                    if (meId != lobby.hostId) {
                        call.response.status(HttpStatusCode.Unauthorized)
                        call.respond(MessageResponse("Not host."))
                    } else {
                        try {
                            val requestId = ObjectId(call.parameters["requestId"])
                            val updatedLobby = lobbyService.acceptJoinRequest(lobbyId, requestId)!!

                            call.respond(updatedLobby)
                        } catch (e: LobbyNotFound) {
                            call.response.status(HttpStatusCode.InternalServerError)
                            call.respond(MessageResponse(e.message))
                        }
                    }
                }

                post("/{lobbyId}/{requestId}/deny") {
                    val principal = call.principal<JWTPrincipal>()
                    val meId = ObjectId(principal!!["id"])
                    val lobbyId = ObjectId(call.parameters["lobbyId"])
                    val lobby = lobbyService.getLobby(lobbyId)

                    if (meId != lobby.hostId) {
                        call.response.status(HttpStatusCode.Unauthorized)
                        call.respond(MessageResponse("Not host $meId is not ${lobby.hostId}"))
                    } else {
                        try {
                            val requestId = ObjectId(call.parameters["requestId"])
                            val updatedLobby = lobbyService.denyJoinRequest(lobbyId, requestId)!!

                            call.respond(updatedLobby)
                        } catch (e: LobbyNotFound) {
                            call.response.status(HttpStatusCode.InternalServerError)
                            call.respond(MessageResponse(e.message))
                        }
                    }
                }

                post("/{lobbyId}/leave") {
                    val principal = call.principal<JWTPrincipal>()
                    val meId = ObjectId(principal!!["id"])

                    val lobbyId = ObjectId(call.parameters["lobbyId"])
                    val lobby = lobbyService.getLobby(lobbyId)

                    if (meId != lobby.hostId) {
                        call.response.status(HttpStatusCode.Unauthorized)
                        call.respond(MessageResponse("Not host."))
                    } else {
                        try {
                            val updatedLobby = lobbyService.kickPlayer(lobbyId, meId)!!

                            call.respond(updatedLobby)
                        } catch (e: LobbyNotFound) {
                            call.response.status(HttpStatusCode.InternalServerError)
                            call.respond(MessageResponse(e.message))
                        }
                    }
                }

                post("/{lobbyId}/kick") {
                    val principal = call.principal<JWTPrincipal>()
                    val meId = ObjectId(principal!!["id"])
                    val params = call.receive<KickParams>()
                    val kickedId = params.kickedId

                    val lobbyId = ObjectId(call.parameters["lobbyId"])
                    val lobby = lobbyService.getLobby(lobbyId)

                    if (meId != lobby.hostId) {
                        call.response.status(HttpStatusCode.Unauthorized)
                        call.respond(MessageResponse("Not host."))
                    } else {
                        try {
                            val updatedLobby = lobbyService.kickPlayer(lobbyId, kickedId)!!

                            call.respond(updatedLobby)
                        } catch (e: LobbyNotFound) {
                            call.response.status(HttpStatusCode.InternalServerError)
                            call.respond(MessageResponse(e.message))
                        }
                    }
                }
            }
        }
    }
}

@Serializable
data class NewLobbyParams(
    @Contextual
    val name: String,
    val game: String,
    val tags: List<String>,
    val numPlayers: Int,
    val description: String
)

@Serializable
data class SenderParams(
    @Contextual
    val senderId: ObjectId
)

@Serializable
data class KickParams(
    @Contextual
    val kickedId: ObjectId
)

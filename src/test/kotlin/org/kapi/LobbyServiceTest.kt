package org.kapi

import com.mongodb.kotlin.client.coroutine.MongoClient
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.kapi.data.LobbyDto
import org.kapi.service.lobby.LobbyService
import org.kapi.service.user.UserService
import kotlin.test.assertEquals

class LobbyServiceTest {
    private val client = MongoClient.create("mongodb://localhost:27017/kapi")
    private val testDatabase = client.getDatabase("test")
    private val userService = UserService(testDatabase)
    private val lobbyService = LobbyService(testDatabase)

    @Test
    fun testLobby() = runBlocking {
        lobbyService.getCollection().drop()

        assertEquals(0, lobbyService.getAllLobbies().size)

        val user1 = userService.createNewUser("test1@email.com")
        val user2 = userService.createNewUser("test2@email.com")

        val lobby1 = lobbyService.createLobby(
            LobbyDto(
                name = "Test Lobby 1",
                hostId = user1.id!!,
                numPlayers = 5,
                users = listOf(user1.id!!)
            )
        )
        val lobby2 = lobbyService.createLobby(
            LobbyDto(
                name = "Test Lobby 2",
                hostId = user2.id!!,
                numPlayers = 5,
                users = listOf(user2.id!!)
            )
        )

        assertEquals(2, lobbyService.getAllLobbies().size)

        val success = lobbyService.deleteLobby(lobby2.id!!)
        assert(success)
        assertEquals(1, lobbyService.getAllLobbies().size)
    }

    @Test
    fun testRequests() = runBlocking {
        lobbyService.getCollection().drop()

        assertEquals(0, lobbyService.getAllLobbies().size)

        val user1 = userService.createNewUser("test1@email.com")
        val user2 = userService.createNewUser("test2@email.com")

        val lobby1 = lobbyService.createLobby(
            LobbyDto(
                name = "Test Lobby 1",
                hostId = user1.id!!,
                numPlayers = 5,
                users = listOf(user1.id!!)
            )
        )

        assertEquals(0, lobbyService.getLobby(lobby1.id!!).requests.size)

        // accept
        val request1 = lobbyService.sendJoinRequest(lobby1.id!!, user2.id!!)
        assertEquals(1, lobbyService.getLobby(lobby1.id!!).requests.size)

        val updatedLobby = lobbyService.acceptJoinRequest(lobby1.id!!, request1.id)!!
        assertEquals(0, updatedLobby.requests.size)
        assertEquals(2, updatedLobby.users.size)
        assertEquals(2, lobbyService.getLobby(lobby1.id!!).users.size)

        // deny
        val lobby2 = lobbyService.createLobby(
            LobbyDto(
                name = "Test Lobby 2",
                hostId = user2.id!!,
                numPlayers = 5,
                users = listOf(user2.id!!)
            )
        )

        val request2 = lobbyService.sendJoinRequest(lobby2.id!!, user1.id!!)
        assertEquals(1, lobbyService.getLobby(lobby2.id!!).requests.size)

        val updatedLobby2 = lobbyService.denyJoinRequest(lobby2.id!!, request2.id)
        assertEquals(0, updatedLobby2!!.requests.size)
        assertEquals(1, updatedLobby2.users.size)
        assertEquals(1, lobbyService.getLobby(lobby2.id!!).users.size)
    }

    @Test
    fun testKick() = runBlocking {
        lobbyService.getCollection().drop()

        assertEquals(0, lobbyService.getAllLobbies().size)

        val user1Id = userService.createNewUser("test1@email.com").id!!
        val user2Id = userService.createNewUser("test2@email.com").id!!

        val lobby1 = lobbyService.createLobby(
            LobbyDto(
                name = "Test Lobby 1",
                hostId = user1Id,
                numPlayers = 5,
                users = listOf(user1Id, user2Id)
            )
        )

        val updatedLobby = lobbyService.kickPlayer(lobby1.id!!, user2Id)

        assertEquals(1, updatedLobby!!.users.size)
    }
}
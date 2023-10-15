package org.kapi

import com.mongodb.kotlin.client.coroutine.MongoClient
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.kapi.data.Lobby
import org.kapi.service.LobbyService
import org.kapi.service.UserService
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
            Lobby(
                name = "Test Lobby 1",
                hostId = user1.id!!,
                numPlayers = 5,
                users = listOf(user1.id!!)
            )
        )
        val lobby2 = lobbyService.createLobby(
            Lobby(
                name = "Test Lobby 2",
                hostId = user2.id!!,
                numPlayers = 5,
                users = listOf(user2.id!!)
            )
        )

        assertEquals(2, lobbyService.getAllLobbies().size)
    }

    @Test
    fun testRequests() = runBlocking {
        lobbyService.getCollection().drop()

        assertEquals(0, lobbyService.getAllLobbies().size)

        val user1 = userService.createNewUser("test1@email.com")
        val user2 = userService.createNewUser("test2@email.com")

        val lobby1 = lobbyService.createLobby(
            Lobby(
                name = "Test Lobby 1",
                hostId = user1.id!!,
                numPlayers = 5,
                users = listOf(user1.id!!)
            )
        )

        var updatedLobby = lobbyService.sendJoinRequest(lobby1.id!!, user2.id!!)

        assertEquals(1, updatedLobby!!.requests.size)
        assertEquals(user2.id, updatedLobby.requests[0].sender)
        assertEquals(1, updatedLobby.users.size)

        updatedLobby = lobbyService.acceptJoinRequest(lobby1.id!!, user2.id!!)

        assertEquals(2, updatedLobby!!.users.size)

        // deny
        val lobby2 = lobbyService.createLobby(
            Lobby(
                name = "Test Lobby 2",
                hostId = user2.id!!,
                numPlayers = 5,
                users = listOf(user2.id!!)
            )
        )

        var updatedLobby2 = lobbyService.sendJoinRequest(lobby2.id!!, user1.id!!)

        assertEquals(1, updatedLobby2!!.requests.size)
        assertEquals(user1.id, updatedLobby2.requests[0].sender)
        assertEquals(1, updatedLobby2.users.size)

        updatedLobby2 = lobbyService.denyJoinRequest(lobby2.id!!, user1.id!!)

        assertEquals(0, updatedLobby2!!.requests.size)
        assertEquals(1, updatedLobby2.users.size)
    }

    @Test
    fun testKick() = runBlocking {
        lobbyService.getCollection().drop()

        assertEquals(0, lobbyService.getAllLobbies().size)

        val user1Id = userService.createNewUser("test1@email.com").id!!
        val user2Id = userService.createNewUser("test2@email.com").id!!

        val lobby1 = lobbyService.createLobby(
            Lobby(
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
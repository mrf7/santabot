import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingString
import com.kotlindiscord.kord.extensions.commands.converters.impl.message
import com.kotlindiscord.kord.extensions.commands.converters.impl.user
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.chatCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.dm
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.respond
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Message
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.first
import java.io.File
import java.lang.RuntimeException
import java.net.URL

val TEST_SERVER = Snowflake(env("TEST_GUILD"))
val TOKEN = env("TOKEN")
suspend fun main(args: Array<String>) {
    val bot = ExtensibleBot(TOKEN) {
        chatCommands { enabled = true }
//        applicationCommands { enabled = true }
        extensions { add(::SantaExtension) }
    }
//    ChatInputCommandInvocationInteraction
    bot.start()
}

class SantaExtension : Extension() {
    override val name: String = "santa"

    @OptIn(KordExperimental::class)
    override suspend fun setup() {
        publicSlashCommand {
            name = "bingbong"
            description = "slippy slappy"
            guild(TEST_SERVER)
            action {
                respond { content = "slippy slappy" }
            }

        }
        publicSlashCommand(::SlapSlashArgs) {  // Public slash commands have public responses
            name = "slap"
            description = "Ask the bot to slap another user"

            // Use guild commands for testing, global ones take up to an hour to update
            guild(TEST_SERVER)

            action {
                val kord = this@SantaExtension.kord

                val realTarget = if (arguments.target.id == kord.selfId) {
                    member
                } else {
                    arguments.target
                }

                respond {
                    content = "*slaps ${realTarget?.mention} with their ${arguments.weapon}*"
                }
            }
        }
        publicSlashCommand(::SantaArgs) {
            name = "santa"
            description = "shuffle secret santy and send out messages"

            guild(TEST_SERVER)
            action {

            }
        }
        chatCommand {
            name = "santa"
            action {
                val kord = this@SantaExtension.kord
                val csvUrl = message.attachments.first().data.url.let { URL(it) }
                val santies =
                    csvUrl.readText()
                        .lines()
                        .drop(1)
                        .filterNot { it.isBlank() }
                        .map(::parseResponseRegex)
                        .shuffled()
                        .let {
                            it + it.first()
                        }
                val out = File("SECRET.txt").also { it.writeText("Santas\n") }
                santies.windowed(2).map { (first, second) ->
                    val member = guild?.getMembers(first.name)?.first()
                    if (member == null) {
                        message.respond("couldnt find ${first.name}")
                        throw RuntimeException()
                    }
                    member to second
                }.map { (santa, receiver) ->
                    out.appendText("${receiver.name}'s gift was bought by ${santa.nickname ?: santa.username}\n")
                    santa.dm("sup ${santa.username} you got ${receiver.name}\naddress:\n${receiver.address}\nstuff:\n${receiver.wants}")
                }
            }
        }
    }

    inner class SantaArgs : Arguments() {
        val message: Message by message(displayName = "message", description = "Message with reaccs")
    }
}

fun String.doSomeBullshit(): List<String> {
    // try ^"\n^"
    return replace("\n", "\\n")
        .replace(""" "\n" """.trim(), "\"\n\"")
        .split("\n")
}


fun parseResponseRegex(csv: String): Santy {
    val regex = """"\d+","(.+?)#(\d+)","(.+?)","(.+?)".*""".toRegex()
    return regex.matchEntire(csv)?.groupValues?.subList(1, 5)?.map { it.replace("\\n", "\n") }?.let { Santy(it) }
        ?: throw RuntimeException()
}

data class Santy(val name: String, val id: Int, val address: String, val wants: String) {
    constructor(list: List<String>) : this(list[0], list[1].toInt(), list[2], list[3])
}

class SlapSlashArgs : Arguments() {
    val target by user("target", description = "Person you want to slap")

    // Coalesced strings are not currently supported by slash commands
    val weapon by defaultingString(
        "weapon",

        defaultValue = "large, smelly trout",
        description = "What you want to slap with"
    )
}

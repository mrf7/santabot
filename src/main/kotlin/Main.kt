import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingString
import com.kotlindiscord.kord.extensions.commands.converters.impl.message
import com.kotlindiscord.kord.extensions.commands.converters.impl.user
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.chatCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.respond
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Message
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
                val csvUrl = message.attachments.first().data.url.let { URL(it) }
                val responses = csvUrl.readText().doSomeBullshit().drop(1).filterNot { it.isBlank() }.map(::parseResponseRegex)
                responses.forEach { message.respond { content = it.toString() } }
            }
        }
    }

    inner class SantaArgs : Arguments() {
        val message: Message by message(displayName = "message", description = "Message with reaccs")
    }
}

fun String.doSomeBullshit(): List<String> {
    return replace("\n", "\\n")
        .replace(""" "\n" """.trim(), "\"\n\"")
        .split("\n")
}


fun parseResponseRegex(csv: String): List<String> {
//    val regex = """"\d+","([a-zA-z]+)#(\d+)","([a-zA-Z0-9., ]+)".*""".toRegex()
    val regex = """"\d+","(.+?)#(\d+)","(.+?)","(.+?)".*""".toRegex()
    return regex.matchEntire(csv)?.groupValues?.subList(1, 5)?.map { it.replace("\\n", "\n") }
        ?: throw RuntimeException()
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

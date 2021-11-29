import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingString
import com.kotlindiscord.kord.extensions.commands.converters.impl.user
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.chatCommand
import com.kotlindiscord.kord.extensions.extensions.publicMessageCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.respond
import dev.kord.cache.api.data.description
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on

val TEST_SERVER = Snowflake(env("TEST_GUILD"))
val TOKEN = env("TOKEN")
suspend fun main(args: Array<String>) {
    val bot = ExtensibleBot(TOKEN) {
        chatCommands { enabled = true }
        applicationCommands { enabled = true }
        extensions { add(::SantaExtension) }
    }
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
        chatCommand {
            name = "chatCommand"
            description = "test chat"
            action {
                message.respond("bing")
            }
        }
    }

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

import com.kotlindiscord.kord.extensions.ExtensibleBot
import dev.kord.core.Kord
import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on

suspend fun main(args: Array<String>) {
    val bot = ExtensibleBot("OTEzOTI1NTczODc1NzQ0ODE4.YaFlhA.5lF1_gXmKO8ypYlm2K0SaLtRH-E") {
        applicationCommands {
            slashCommandCheck {

            }
        }
    }
    bot.start()
}

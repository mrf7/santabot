import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import java.lang.RuntimeException

suspend fun main(args: Array<String>) {
    val kord = Kord(TOKEN)
    kord.on<MessageCreateEvent> {
        if (message.content != "!me") return@on
        val guild = getGuild() ?: throw RuntimeException("fuck no guild").also { println(it) }
        println(guild.supplier)
        val members = guild.members.onCompletion { println("no mas $it") }?.collect {
//            kord.getUser(it.id)
            println(it.memberData)
        }
        getGuild()?.roles?.onCompletion { println("no mas role $it") }?.collect {
            println(it)
        }

    }
    kord.login()
}

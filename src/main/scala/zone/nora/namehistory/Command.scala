package zone.nora.namehistory

import java.net.URI
import java.util

import com.google.gson.JsonParser
import net.minecraft.client.Minecraft
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.util.ChatComponentText
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

import scala.collection.JavaConverters._
import scala.util.control.Breaks.{break, breakable}
import scala.util.control.NonFatal

class Command extends CommandBase {
  private val mc = Minecraft.getMinecraft

  override def getCommandName: String = "namehistory"

  override def getCommandUsage(sender: ICommandSender): String = "/namehistory [player]"

  override def getCommandAliases: util.List[String] = ("name" :: "names" :: "nh" :: Nil).asJava

  override def processCommand(sender: ICommandSender, args: Array[String]): Unit = {
    if (args.isEmpty)
      sender.addChatMessage(new ChatComponentText("\u00a7cYou must put a player"))
    else {
      val thread = new Thread(new Runnable {
        override def run(): Unit = args.foreach { it =>
          breakable {
            val response = try {
              val client = HttpClients.createDefault()
              val request = new HttpGet(new URI(s"https://api.ashcon.app/mojang/v2/user/$it"))
              request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:19.0) Gecko/20100101 Firefox/19.0")
              val response = client.execute(request)
              EntityUtils.toString(response.getEntity)
            } catch {
              case NonFatal(e) => e.printStackTrace(); error("Failed to get Mojang API data."); break
            }
            val json = new JsonParser().parse(response).getAsJsonObject
            if (json.has("error"))
              error(if (json.has("reason")) json.get("reason").getAsString else "Unexpected Error")
            else {
              breakline()
              val nameHistory = json.get("username_history").getAsJsonArray
              for (i <- 0 until nameHistory.size()) {
                val name = nameHistory.get(i).getAsJsonObject
                val b = name.has("changed_at")
                put(31
                  s"\u00a76${if (b) "" else "\u00a7l"}${name.get("username").getAsString} - ${if (b)
                    name.get("changed_at").getAsString
                      .replace("T", " @ ")
                      .replace(".000Z", "") else "Original Name"}")
              }
              breakline()
            }
          }
        }
      })
      thread.start()
    }
  }

  override def canCommandSenderUseCommand(sender: ICommandSender): Boolean = true

  override def isUsernameIndex(args: Array[String], index: Int): Boolean = true

  private def put(str: String): Unit = mc.thePlayer.addChatMessage(new ChatComponentText(str))

  private def error(message: String): Unit = put(s"\u00a7c$message")

  private def breakline(): Unit = {
    val dashes = new StringBuilder
    val dash = Math.floor((280 * mc.gameSettings.chatWidth + 40) / 320 * (1 / mc.gameSettings.chatScale) * 53).toInt
    for (_ <- 1 to dash) dashes.append("-")
    mc.thePlayer.addChatMessage(new ChatComponentText(s"\u00a76\u00a7m$dashes"))
  }
}

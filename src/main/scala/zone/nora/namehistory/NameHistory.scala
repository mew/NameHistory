package zone.nora.namehistory

import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLInitializationEvent

@Mod(modid = "NameHistory", name = "NameHistory", version = "1.0", modLanguage = "scala")
object NameHistory {
  @EventHandler
  def onInit(e: FMLInitializationEvent): Unit = ClientCommandHandler.instance.registerCommand(new Command)
}

package org.firescape.server.packethandler.client;

import org.apache.mina.common.IoSession;
import org.firescape.server.entityhandling.EntityHandler;
import org.firescape.server.entityhandling.defs.extras.FiremakingDef;
import org.firescape.server.event.DelayedEvent;
import org.firescape.server.event.ShortEvent;
import org.firescape.server.event.WalkToPointEvent;
import org.firescape.server.model.*;
import org.firescape.server.net.Packet;
import org.firescape.server.packethandler.PacketHandler;
import org.firescape.server.states.Action;
import org.firescape.server.util.DataConversions;
import org.firescape.server.util.Formulae;

public class InvUseOnGroundItem implements PacketHandler {

  /**
   * World instance
   */
  public static final World world = World.getWorld();

  public void handlePacket(Packet p, IoSession session) throws Exception {
    Player player = (Player) session.getAttachment();
    if (player.isBusy()) {
      player.resetPath();
      return;
    }
    // incExp
    player.resetAll();
    Point location = Point.location(p.readShort(), p.readShort());
    int id = p.readShort();
    ActiveTile tile = world.getTile(location);
    if (tile.hasGameObject()) {
      player.getActionSender().sendMessage("You cannot do that here, please move to a new area.");
      return;
    }
    Item item = getItem(id, tile, player);
    InvItem myItem = player.getInventory().get(p.readShort());
    if (item == null || myItem == null) {
      player.setSuspiciousPlayer(true);
      player.resetPath();
      return;
    }
    player.setStatus(Action.USING_INVITEM_ON_GITEM);
    world.getDelayedEventHandler().add(new WalkToPointEvent(player, location, 1, false) {
      public void arrived() {
        if (owner.isBusy() ||
            owner.isRanging() ||
            !tile.hasItem(item) ||
            !owner.nextTo(item) ||
            owner.getStatus() != Action.USING_INVITEM_ON_GITEM) {
          return;
        }
        switch (item.getID()) {
          case 14:
          case 632:
          case 633:
          case 634:
          case 635:
          case 636:
            FiremakingDef def = EntityHandler.getFiremakingDef(item.getID());
            if (!itemId(new int[] { 166 }) || def == null) {
              owner.getActionSender().sendMessage("Nothing interesting happens.");
              return;
            }
            if (owner.getCurStat(11) < def.getRequiredLevel()) {
              owner.getActionSender()
                   .sendMessage("You need at least " +
                                def.getRequiredLevel() +
                                " firemaking " +
                                "to light these logs.");
              return;
            }
            owner.setBusy(true);
            Bubble bubble = new Bubble(owner, 166);
            for (Player p : owner.getViewArea().getPlayersInView()) {
              p.informOfBubble(bubble);
            }
            owner.getActionSender().sendMessage("You attempt to light the logs...");
            DelayedEvent.world.getDelayedEventHandler().add(new ShortEvent(owner) {
              public void action() {
                if (Formulae.lightLogs(def, owner.getCurStat(11))) {
                  owner.getActionSender().sendMessage("They catch fire and start to burn.");
                  DelayedEvent.world.unregisterItem(item);
                  GameObject fire = new GameObject(item.getLocation(), 97, 0, 0);
                  DelayedEvent.world.registerGameObject(fire);
                  DelayedEvent.world.getDelayedEventHandler().add(new DelayedEvent(null, def.getLength()) {
                    public void run() {
                      if (tile.hasGameObject() && tile.getGameObject().equals(fire)) {
                        DelayedEvent.world.unregisterGameObject(fire);
                        DelayedEvent.world.registerItem(new Item(181, tile.getX(), tile.getY(), 1, null));
                      }
                      running = false;
                    }
                  });
                  owner.incExp(11, Formulae.firemakingExp(owner.getMaxStat(11), def.getExp()), true, true);
                  owner.getActionSender().sendStat(11);
                } else {
                  owner.getActionSender().sendMessage("You fail to light them.");
                }
                owner.setBusy(false);
              }
            });
            break;
          default:
            owner.getActionSender().sendMessage("Nothing interesting happens.");
            return;
        }
      }

      private boolean itemId(int[] ids) {
        return DataConversions.inArray(ids, myItem.getID());
      }
    });
  }

  private Item getItem(int id, ActiveTile tile, Player player) {
    for (Item i : tile.getItems()) {
      if (i.getID() == id && i.visibleTo(player)) {
        return i;
      }
    }
    return null;
  }

}
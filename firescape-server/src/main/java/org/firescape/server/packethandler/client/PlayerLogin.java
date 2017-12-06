package org.firescape.server.packethandler.client;

import org.apache.mina.common.IoSession;
import org.firescape.server.GameVars;
import org.firescape.server.model.Player;
import org.firescape.server.model.World;
import org.firescape.server.net.Packet;
import org.firescape.server.net.RSCPacket;
import org.firescape.server.packetbuilder.RSCPacketBuilder;
import org.firescape.server.packethandler.PacketHandler;
import org.firescape.server.util.DataConversions;
import org.firescape.server.io.PlayerLoader;
import static org.firescape.server.io.PlayerLoader.LoginState;

public class PlayerLogin implements PacketHandler {
  /**
   * World instance
   */
  public static final World world = World.getWorld();
  public static final PlayerLoader playerLoader = new PlayerLoader();

  public static final byte LOGIN_CODE_INVALID_LOGIN = 2;
  public static final byte LOGIN_CODE_ALREADY_LOGGED_IN = 3;
  public static final byte LOGIN_CODE_CLIENT_VERSION_MISMATCH = 4;
  public static final byte LOGIN_CODE_SESSION_KEYS_ERROR = 5;
  public static final byte LOGIN_CODE_BANNED = 6;
  // public static final byte LOGIN_CODE_BAD_USER_OR_PASSWORD = 7;
  public static final byte LOGIN_CODE_MAX_USERS_EXCEEDED = 10;
  public static final byte LOGIN_CODE_UNRECOGNIZED = 22;
  public static final byte LOGIN_CODE_BAD_USER_OR_PASSWORD = 22;

  private void sendResponsePacket(IoSession session, byte loginCode) {
    session.write(new RSCPacketBuilder()
        .setBare(true)
        .addByte(loginCode)
        .toPacket()
        );
  }

  public void handlePacket(Packet p, IoSession session) throws Exception {
    Player player = (Player) session.getAttachment();
    byte loginCode = LOGIN_CODE_UNRECOGNIZED;
    try {
      boolean reconnecting = (p.readByte() == 1);
      int clientVersion = p.readShort();
      RSCPacket loginPacket = DataConversions.decryptRSA(p.readBytes(p.readByte()));
      if (loginPacket == null) {
        System.out.println("loginPacket = null");
      }
      int[] sessionKeys = new int[4];
      for (int key = 0; key < sessionKeys.length; key++) {
        sessionKeys[key] = loginPacket.readInt();
      }
      int uid = loginPacket.readInt();
      String username = loginPacket.readString(20).trim();
      loginPacket.skip(1);
      String password = loginPacket.readString(20).trim();
      loginPacket.skip(1);

      if (username.trim().length() < 3 || password.trim().length() < 3) {
        sendResponsePacket(session, LOGIN_CODE_BAD_USER_OR_PASSWORD);
        player.destroy(true);
        return;
      }

      LoginState loginState = playerLoader.getLogin(username, password);
      if (world.countPlayers() >= org.firescape.server.GameVars.maxUsers) {
        loginCode = LOGIN_CODE_MAX_USERS_EXCEEDED;
      } else if (clientVersion != GameVars.clientVersion) {
        loginCode = LOGIN_CODE_CLIENT_VERSION_MISMATCH;
      } else if (!player.setSessionKeys(sessionKeys)) {
        loginCode = LOGIN_CODE_SESSION_KEYS_ERROR;
        player.bad_login = true;
      } else if (loginState == LoginState.BAD_PASSWORD) {
        loginCode = LOGIN_CODE_INVALID_LOGIN;
      } else if (loginState == LoginState.ALREADY_LOGGED_IN) {
        loginCode = LOGIN_CODE_ALREADY_LOGGED_IN;
      } else if (loginState == LoginState.BANNED) {
        loginCode = LOGIN_CODE_BANNED;
      } else {
        if (loginCode != LOGIN_CODE_SESSION_KEYS_ERROR) {
          player.bad_login = false;
        }
        if (loginCode != LOGIN_CODE_SESSION_KEYS_ERROR || loginCode != LOGIN_CODE_ALREADY_LOGGED_IN) {
          player.load(username, password, uid, reconnecting);
          return;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }

    sendResponsePacket(session, loginCode);
    player.destroy(true);

//     if (loginCode != 22) {
//       RSCPacketBuilder pb = new RSCPacketBuilder();
//       pb.setBare(true);
//       pb.addByte(loginCode);
//       session.write(pb.toPacket());
//       player.destroy(true);
//     }
  }

}

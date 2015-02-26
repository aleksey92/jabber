package org.codehaus.groovy.grails.jabber

import org.jivesoftware.smack.Chat
import org.jivesoftware.smack.ConnectionConfiguration
import org.jivesoftware.smack.PacketListener
import org.jivesoftware.smack.XMPPConnection
import org.jivesoftware.smack.filter.PacketTypeFilter
import org.jivesoftware.smack.packet.Message
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A simple chat bot service using Jabber.  Based on http://memo.feedlr.com/?p=11 and
 * http://blogs.bytecode.com.au/glen/2008/01/03/gravl--google-talk-notifier.html
 *
 * @author Glen Smith
 */
class ChatListener {

    private static final Logger log = LoggerFactory.getLogger(this)

    private XMPPConnection connection
    private String host
    private int port = 5222
    private String serviceName = "XMPP"
    private String userName
    private String password
    private String listenerMethod
    private targetService

    ChatListener(config, String methodName = null, service = null) {
        host = config.host
        port = config.port
        serviceName = config.serviceName
        userName = config.username
        password = config.password
        listenerMethod = methodName
        targetService = service
    }

    void connect() {

        if (connection) {
           return
        }

        connection = new XMPPConnection(new ConnectionConfiguration(host, port, serviceName))

        log.debug 'Connecting to Jabber server'
        connection.connect()
        connection.login(userName, password, userName + Long.toHexString(System.currentTimeMillis()))
        log.debug 'Connected to Jabber server: {}', connection.connected
    }

    void listen() {

        connect()

        def myListener = [processPacket: { packet ->
            log.debug 'Received message from {}, subject: {}, body: {}', packet.from, packet.subject, packet.body
            targetService."$listenerMethod"(packet)
        }] as PacketListener

        log.debug 'Adding Jabber listener...'
        connection.addPacketListener(myListener, new PacketTypeFilter(Message))
    }

    void disconnect() {
        if (connection?.connected) {
            connection.disconnect()
        }
    }

    void sendJabberMessage(String to, String body) {

        connect()

        Chat chat = connection.chatManager.createChat(to, null)
        def message = new Message(to, Message.Type.chat)
        message.body = body

        log.debug 'Sending Jabber message to {} with content {}', to, body
        chat.sendMessage(message)
    }
}

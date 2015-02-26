import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.codehaus.groovy.grails.jabber.ChatListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class JabberGrailsPlugin {
    def version = '2.0'
    def grailsVersion = "2.4 > *"
    def author = "Aleksey Nesterenko"
    def authorEmail = "aleksey.nester.itt@gmail.com"
    def title = "Jabber Plugin"
    def description = 'Provides the opportunity to send and receive Chat messages via the Jabber API'
    def documentation = "http://grails.org/plugin/jabber"

	 private Logger log = LoggerFactory.getLogger('grails.plugin.jabber.JabberGrailsPlugin')

    def doWithSpring = {

        boolean listenerDefined = false
        def config = application.config.chat

        application.serviceClasses.each { serviceClass ->
            Class clazz = serviceClass.clazz
            def exposeList = GrailsClassUtils.getStaticPropertyValue(clazz, 'expose')
            if (exposeList?.contains('jabber') && !listenerDefined) {
                log.debug 'adding Jabber listener for {} to Spring', serviceClass.shortName

                String method = GrailsClassUtils.getStaticPropertyValue(clazz, 'jabberListenerMethod') ?: "onJabberMessage"

                GrailsPluginJabberListener(ChatListener, config, method, ref(serviceClass.propertyName))
                listenerDefined = true
            }
        }

        if (!listenerDefined) {
            GrailsPluginJabberListener(ChatListener, config)
        }
    }

    def doWithApplicationContext = { ctx ->

        application.serviceClasses.each { serviceClass ->
            def exposeList = GrailsClassUtils.getStaticPropertyValue(serviceClass.clazz, 'expose')
            if (exposeList?.contains('jabber')) {
                log.debug 'Starting Jabber listener for {}', serviceClass.shortName
                ctx.GrailsPluginJabberListener.listen()
            }
        }
    }

    def doWithDynamicMethods = { ctx ->

        def listener = ctx.GrailsPluginJabberListener

        (application.serviceClasses + application.controllerClasses)*.metaClass.each { mc ->
            mc.sendJabberMessage = { to, message ->
                if (to instanceof List) {
                    to.each { listener.sendJabberMessage(it, message) }
                }
                else {
                    listener.sendJabberMessage(to, message)
                }
            }
        }
    }
}

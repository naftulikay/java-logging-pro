/**
 * Logback Configuration
 */
import net.logstash.logback.appender.LogstashSocketAppender
import net.logstash.logback.encoder.LogstashEncoder

/* Environment Variables */
def ENV = System.getenv()

def DEPLOY_ENVIRONMENT = ENV.get('DEPLOY_ENVIRONMENT') ?: 'development'
def SYSLOG_TARGET = ENV.get('SYSLOG_TARGET') ?: '127.0.0.1'
def LOG_FORMAT = ENV.get('LOG_FORMAT') ?: 'plain'

// scan for changes every thirty seconds
scan("30 seconds")

// enable JMX configuration
jmxConfigurator('logback-pro')

// Appenders
appender("stdout:json", ConsoleAppender) {
  encoder(LogstashEncoder) {
    includeMdc = true
    timeZone = "UTC"
  }
}

appender("stdout:plain", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  }
}

appender("syslog:json", LogstashSocketAppender) {
  host = SYSLOG_TARGET
  port = 514
  facility = "USER"
  includeMdc = true
  timeZone = "UTC"
}

// Loggers
logger("wtf.naftuli.logbackpro.LogbackPro", INFO)

// based on environment, set log configuration
if (DEPLOY_ENVIRONMENT =~ /prod(uction)?/) {
  // production logging configuration
  root(WARN, ["syslog:${LOG_FORMAT}"])
} else if (DEPLOY_ENVIRONMENT =~ /(stg|staging)/) {
  // staging logging configuration
  root(INFO, ["stdout:${LOG_FORMAT}", "syslog:json"])
} else {
  // development logging configuration
  logger("wtf.naftuli.logbackpro.LogbackPro", DEBUG)
  root(INFO, ["stdout:${LOG_FORMAT}"])
}
